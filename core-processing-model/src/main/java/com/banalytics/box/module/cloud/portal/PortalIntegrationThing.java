package com.banalytics.box.module.cloud.portal;

import com.banalytics.box.BanalyticsBoxInstanceState;
import com.banalytics.box.api.integration.AbstractMessage;
import com.banalytics.box.api.integration.Constants;
import com.banalytics.box.api.integration.MessageHandler;
import com.banalytics.box.api.integration.environment.*;
import com.banalytics.box.api.integration.utils.CommonUtils;
import com.banalytics.box.api.integration.webrtc.channel.environment.ThingApiCallReq;
import com.banalytics.box.module.AbstractThing;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.Singleton;
import com.banalytics.box.module.State;
import com.banalytics.box.module.standard.Messaging;
import com.banalytics.box.service.SystemOptionsService;
import com.banalytics.box.service.SystemThreadsService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.core.annotation.Order;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.ConnectionManagerSupport;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.banalytics.box.TimeUtil.currentTimeInServerTz;
import static com.banalytics.box.api.integration.environment.AccountStatusMessage.AccountState.EMPTY_BALANCE;
import static com.banalytics.box.api.integration.environment.AccountStatusMessage.AccountState.NOT_ASSIGNED;
import static com.banalytics.box.api.integration.environment.EnvironmentType.production;
import static com.banalytics.box.module.Thing.StarUpOrder.DATA_EXCHANGE;
import static com.banalytics.box.service.SystemThreadsService.SYSTEM_TIMER;

@Slf4j
@Order(DATA_EXCHANGE + 200)
public class PortalIntegrationThing extends AbstractThing<PortalIntegrationConfiguration> implements WebSocketHandler, Messaging<AbstractMessage>, Singleton {
    private static final String SSL_CONTEXT_PROPERTY = "org.apache.tomcat.websocket.SSL_CONTEXT";

    private final Statistics statistics = new Statistics();

    @Override
    public String getTitle() {
        return getSelfClassName();
    }

    private final AccountStatusMessageHandler accountStatusMessageHandler = new AccountStatusMessageHandler();
    private final DeviceLinkMessageHandler deviceLinkMessageHandler = new DeviceLinkMessageHandler();
    private final PortalShareMessageHandler portalShareMessageHandler = new PortalShareMessageHandler();
    private final ReconnectMessageHandler portalReconnectMessageHandler = new ReconnectMessageHandler();
    private final RebootMessageHandler rebootMessageHandler = new RebootMessageHandler();

    public PortalIntegrationThing(BoxEngine engine) {
        super(engine);
        this.subscribeHandler(accountStatusMessageHandler);
        this.subscribeHandler(deviceLinkMessageHandler);
        this.subscribeHandler(portalShareMessageHandler);
        this.subscribeHandler(portalReconnectMessageHandler);
        this.subscribeHandler(rebootMessageHandler);
    }

    private File deviceRegistrationFile;
    private final Properties deviceRegistrationProperties = new Properties();

    private ConnectionManagerSupport manager;

    private WebSocketSession portalWsSession;

    private final Set<MessageHandler<? extends AbstractMessage>> messageHandlers = new HashSet<>();

    public void subscribeHandler(MessageHandler<? extends AbstractMessage> messageHandler) {
        this.messageHandlers.add(messageHandler);
    }

    public void unSubscribeHandler(MessageHandler<? extends AbstractMessage> messageHandler) {
        this.messageHandlers.remove(messageHandler);
    }

    public UUID getEnvironmentUUID() {
        return configuration.getEnvironmentUUID();
    }

    private TimerTask connectionExpirationChecker;

    private final Queue<AbstractMessage> messageQueue = new ConcurrentLinkedQueue<>();

    private TimerTask sendMessageTask;

    long lastConnectionTime = System.currentTimeMillis();

    @Override
    protected void doInit() throws Exception {
        File configFolder = engine.applicationConfigFolder();

        this.deviceRegistrationFile = new File(configFolder, "device-registration.properties");
        if (!this.deviceRegistrationFile.exists()) {
            if (!this.deviceRegistrationFile.createNewFile()) {
                throw new RuntimeException("Cloud registration failed. " +
                        "Can't create file: " + this.deviceRegistrationFile.getAbsolutePath());
            }
        }
        try (FileReader fr = new FileReader(this.deviceRegistrationFile)) {
            this.deviceRegistrationProperties.load(fr);
            String uuid = deviceRegistrationProperties.getProperty(Constants.ENVIRONMENT_UUID.varName);
            if (StringUtils.isNotEmpty(uuid)) {
                this.configuration.environmentUUID = UUID.fromString(uuid);
            }
        }
    }

    private long socketSessionExpirationTime = 0;

    private int wssPort;
    private InetAddress[] clusterAddresses;
    private int currentIndex;

    private String getNextAddress() {
        this.currentIndex++;
        if (currentIndex >= clusterAddresses.length) {
            currentIndex = 0;
        }
        return clusterAddresses[this.currentIndex].getHostAddress();
    }

    @Override
    protected void doStart() throws Exception {
        URI uri = new URI(configuration.portalUrl);
        this.wssPort = uri.getPort();
        this.clusterAddresses = InetAddress.getAllByName(uri.getHost());
        this.currentIndex = (int) (Math.random() * currentIndex);
        this.currentIndex = this.currentIndex > clusterAddresses.length - 1 ? 0 : this.currentIndex;

        this.initializeManager();

        connectionExpirationChecker = new TimerTask() {
            @Override
            public void run() {
                try {
                    long now = System.currentTimeMillis();
//                    log.info("Check connection: {} > {}, {}", now, socketSessionExpirationTime, portalWsSession==null);
                    if (now > socketSessionExpirationTime || portalWsSession == null || !portalWsSession.isOpen()) {
                        log.info("\tConnection expired.");
                        connect();
                    }
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                }
            }
        };
        SYSTEM_TIMER.schedule(// each half minutes recheck is connection expired
                connectionExpirationChecker,
                1000, 10000
        );
        log.info("Connection expiration checker scheduled");
        sendMessageTask = new TimerTask() {
            boolean inside = false;

            @Override
            public void run() {
                if (inside) {
                    return;
                }
                try {
                    inside = true;
                    if (portalWsSession == null || !portalWsSession.isOpen()) {
                        log.warn("No connection with portal. Awaiting connection...");
                        return;
                    }
                    while (!messageQueue.isEmpty()) {
                        try {
                            AbstractMessage message = messageQueue.peek();
                            statistics.webSockOutMessages++;
                            CommonUtils.sendMessage(portalWsSession, message);
                            log.info("Message sent to portal: {}", message);
                            messageQueue.remove();
                        } catch (IOException e) {
                            log.error("Can't deliver message to portal: " + e.getMessage(), e);
                            break;
                        }
                    }
                } finally {
                    inside = false;
                }
            }
        };
        SYSTEM_TIMER.schedule(sendMessageTask, 1000, 1000);
        log.info("Send message task scheduled");
    }

    private void initializeManager() throws Exception {
        String currentAddr = getNextAddress();
        String connectionUri;
        if (this.wssPort == -1) {
            connectionUri = "wss://" + currentAddr + "/portal-integration?";
        } else {
            connectionUri = "wss://" + currentAddr + ":" + this.wssPort + "/portal-integration?";
        }

//        String connectionUri = configuration.portalUrl + "/portal-integration?";
        if (getEnvironmentUUID() != null) {//when new environment created
            connectionUri += Constants.ENVIRONMENT_UUID.varName + "=" + getEnvironmentUUID().toString();
        } else {
            connectionUri += Constants.ENVIRONMENT_UUID.varName + "=";
        }
        connectionUri += "&" + Constants.ENVIRONMENT_HASH.varName + "=" + SystemOptionsService.environmentHash();
        log.info("Connecting to {}", connectionUri);
        StandardWebSocketClient standardWebSocketClient = new StandardWebSocketClient();
        SSLContext sslContext = this.getSslContext();
        standardWebSocketClient.getUserProperties().put(SSL_CONTEXT_PROPERTY, sslContext);
        this.manager = new WebSocketConnectionManager(
                standardWebSocketClient,
                this,
                connectionUri
        );
    }

    private synchronized void connect() throws Exception {
        log.info("Connecting...");
        if (getState() != State.STARTING && getState() != State.RUN) {
            log.error("Not started");
            return;
        }

        applyBanalyticsWM();

        if (portalWsSession != null) {                     // if connection exists
            if (portalWsSession.isOpen()) {                // and opened
                try {
                    portalWsSession.close(CloseStatus.NORMAL);           // close current connection
                    Thread.sleep(2000);
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                }
            }
            portalWsSession = null;                        // otherwise reconnect
        }
        try {
            if (manager != null) {
                boolean running = manager.isRunning();
                if (running) {
                    manager.stop();
                    manager = null;
                    Thread.sleep(1000);
                }
                initializeManager();
            }
            log.info("Start manager...");
            manager.start();

            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (state == State.STOPPED || state == State.ERROR || state == State.INIT_ERROR) {
            log.warn("==== Stopped: {}", state);
        }
    }

    @Override
    protected void doStop() throws Exception {
        sendMessageTask.cancel();
        connectionExpirationChecker.cancel();
        Thread.sleep(1000);
        log.info("Shutdown:");
        if (this.manager != null) {
            this.manager.stop();
            log.info("\t- Socket connection manager stopped");
        }

        this.manager = null;
        this.portalWsSession = null;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Connection established: {}, attrs=[{}]", session.getId(), session.getAttributes());
        this.portalWsSession = session;
        statistics.connEstablished++;
        this.lastConnectionTime = System.currentTimeMillis();

        this.socketSessionExpirationTime = this.lastConnectionTime + configuration.connectionExpirationTime * 60 * 1000;//expire connection via 3 minutes

        String environmentUUID = deviceRegistrationProperties.getProperty(Constants.ENVIRONMENT_UUID.varName);
        if (environmentUUID != null) {// null when new environment not registered
            session.getAttributes().put(Constants.ENVIRONMENT_UUID.varName, environmentUUID);
        }
        portalWsSession.sendMessage(new PingMessage()); //send ping
        if (StringUtils.isEmpty(environmentUUID)) { //if first start request device registration
            requestDeviceRegistration();
        }
    }

    /**
     * When connection closed:
     * - log info.
     * - wait 5 seconds (time to reboot portal) and immediately try to reconnect
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("Server session closed: {}[{}] / {}", session.getId(), closeStatus, session.getAttributes());
        this.statistics.connClosed++;
        this.socketSessionExpirationTime = System.currentTimeMillis();
        this.portalWsSession = null;
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage msg) {
            try {
                statistics.webSockInMessages++;

                String payload = msg.getPayload();
                AbstractMessage portalMessage;
                try {// if message not supported with current version of API, then skip this message
                    portalMessage = AbstractMessage.from(payload);
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                    return;
                }
                for (MessageHandler<? extends AbstractMessage> mh : messageHandlers) {
                    if (!mh.isSupport(portalMessage)) {
                        continue;
                    }
                    AbstractMessage messageToPortal = mh.handleMessage(session, portalMessage);
                    if (messageToPortal != null) {
                        this.sendMessage(messageToPortal);
                    }
                }
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        } else if (message instanceof PongMessage) {
            log.info("Pong message received.");
        } else {
            log.warn("Unsupported message: {}", message);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        statistics.connErrors++;
        log.error("Transport Error", exception);
    }

    @Override
    public void destroy() {
        this.unSubscribeHandler(accountStatusMessageHandler);
        this.unSubscribeHandler(deviceLinkMessageHandler);
        this.unSubscribeHandler(portalShareMessageHandler);
        this.unSubscribeHandler(portalReconnectMessageHandler);
        this.unSubscribeHandler(rebootMessageHandler);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    @Override
    public void sendMessage(AbstractMessage message) {
        messageQueue.add(message);
    }

    void requestDeviceRegistration() {
        try {
            log.info("Requesting device registration...");
            RegisterMeRequest req = new RegisterMeRequest();
            req.setDeviceType(production);

            UUID productUuid = SystemOptionsService.generatedUuid();
            req.setProductUUID(productUuid);
            req.setEnvironmentHash(SystemOptionsService.environmentHash());
            if (SystemUtils.IS_OS_WINDOWS) {
                req.setOs(OSType.WINDOWS_x86_64);
            } else if (SystemUtils.IS_OS_LINUX) {
                req.setOs(OSType.LINUX_x86_64);
            } else if (SystemUtils.IS_OS_MAC_OSX) {
                req.setOs(OSType.MACOSX_x86_64);
            }
            log.info("Request product registration: environmentUuid='{}'", productUuid);
            this.sendMessage(req);
        } catch (Throwable e) {
            log.error("Can't send device registration message", e);
        }
    }

    public void notifyEnvironmentPasswordReset(String accountEmail, String randomPassword) {
        sendMessage(new EnvironmentPasswordResetMessage(
                accountEmail,
                randomPassword,
                getEnvironmentUUID()
        ));
    }

    public void shareEnvironmentWith(String callerAccountEmail, String randomPassword) throws Exception {
        sendMessage(new EnvironmentShareWithReq(
                callerAccountEmail.trim(),
                randomPassword,
                getEnvironmentUUID()
        ));
    }

    public void unShareEnvironmentWith(String callerAccountEmail) throws Exception {
        sendMessage(new EnvironmentUnShareWithReq(
                callerAccountEmail.trim(),
                getEnvironmentUUID()
        ));
    }

    public void publicShareEnvironment(String host, String accessToken) throws Exception {
        sendMessage(new EnvironmentPublicShareReq(
                getEnvironmentUUID(),
                host,
                accessToken
        ));
    }

    public void publicUnShareEnvironment(String accessToken) throws Exception {
        sendMessage(new EnvironmentPublicUnShareReq(
                getEnvironmentUUID(),
                accessToken
        ));
    }

    @Override
    public Set<String> apiMethodsPermissions() {
        return Set.of("readStatistics");
    }

    @Override
    public Object call(Map<String, Object> params) throws Exception {
        String method = (String) params.get(ThingApiCallReq.PARAM_METHOD);
        switch (method) {
            case "readStatistics":
                return statistics;
            default:
                throw new RuntimeException("Method not supported: " + method);
        }
    }

    private class DeviceLinkMessageHandler implements MessageHandler<AbstractDeviceRegistrationMessage> {
        @Override
        public boolean isSupport(AbstractMessage portalMessage) {
            return portalMessage instanceof ReadyToLinkResponse;
        }

        @Override
        public AbstractDeviceRegistrationMessage handleMessage(WebSocketSession session, AbstractMessage message) {
            if (message instanceof ReadyToLinkResponse res) {
                String environmentUUID = res.getYourUuid().toString();
                deviceRegistrationProperties.setProperty(Constants.ENVIRONMENT_UUID.varName, environmentUUID);
                try {
                    try (FileWriter fw = new FileWriter(deviceRegistrationFile)) {
                        deviceRegistrationProperties.store(fw, "Persisted on " + currentTimeInServerTz());
                    }
                    configuration.environmentUUID = UUID.fromString(environmentUUID);
                    log.info("Environment '{}' ready to link with profile.", environmentUUID);
                    //restart portal integration thing with assigned UUID
                    restart();
                } catch (IOException e) {
                    log.error("Local error: " + e.getMessage(), e);
                }
            }
            return null;
        }
    }

    private final long H24_INTERVAL = 24 * 60 * 60 * 1000;
    private boolean emptyBalance = false;
    private boolean notAssigned = false;

    private void applyBanalyticsWM() {
        boolean noConnectionMoreThanDay = System.currentTimeMillis() - lastConnectionTime > H24_INTERVAL;

        boolean showWM = noConnectionMoreThanDay || emptyBalance || notAssigned;

        BanalyticsBoxInstanceState
                .getInstance()
                .setShowBanalyticsWatermark(showWM);
    }

    private class AccountStatusMessageHandler implements MessageHandler<AbstractMessage> {
        @Override
        public boolean isSupport(AbstractMessage portalMessage) {
            return portalMessage instanceof AccountStatusMessage;
        }

        @Override
        public AbstractMessage handleMessage(WebSocketSession session, AbstractMessage message) throws Exception {
            if (message instanceof AccountStatusMessage asm) {
                log.info("Received account state message: {}", asm.toJson());
                Set<AccountStatusMessage.AccountState> states = asm.getStates();
                if (states == null || states.isEmpty()) {
                    emptyBalance = false;
                    notAssigned = false;
                } else {
                    emptyBalance = states.contains(EMPTY_BALANCE);
                    notAssigned = states.contains(NOT_ASSIGNED);
                }

                if (notAssigned) {
                    log.warn("Environment not linked with profile and will be stopped via 1 hour: {}", asm.toJson());
                    SYSTEM_TIMER.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            engine.getPrimaryInstance().stop();
                            log.warn("Environment not linked with profile. Instance stopped.");
                        }
                    }, 3600000);
                }

                if (emptyBalance || notAssigned) {
                    log.warn("Empty balance. Billable features stopped: {}", asm.toJson());
                    engine.stopBillableFeatures();
                } else {
                    engine.startBillableFeatures();
                }
                applyBanalyticsWM();
            }
            return null;
        }
    }

    private class ReconnectMessageHandler implements MessageHandler<ReconnectEvent> {
        @Override
        public boolean isSupport(AbstractMessage portalMessage) {
            return portalMessage instanceof ReconnectEvent;
        }

        @Override
        public ReconnectEvent handleMessage(WebSocketSession session, AbstractMessage message) throws Exception {
            if (message instanceof ReconnectEvent) {
                restart();
            }
            return null;
        }
    }

    private class RebootMessageHandler implements MessageHandler<RebootEvent> {
        @Override
        public boolean isSupport(AbstractMessage portalMessage) {
            return portalMessage instanceof RebootEvent;
        }

        @Override
        public RebootEvent handleMessage(WebSocketSession session, AbstractMessage message) throws Exception {
            if (message instanceof RebootEvent) {
                log.info("Reboot initiated via web site");
                SystemThreadsService.reboot();
            }
            return null;
        }
    }

    private class PortalShareMessageHandler implements MessageHandler<AbstractEnvironmentShareMessage> {
        @Override
        public boolean isSupport(AbstractMessage portalMessage) {
            return
                    portalMessage instanceof EnvironmentShareWithRes
                            || portalMessage instanceof EnvironmentUnShareWithRes;
        }

        @Override
        public AbstractEnvironmentShareMessage handleMessage(WebSocketSession session, AbstractMessage message) {
            try {
                if (message instanceof EnvironmentShareWithRes res) {
                    log.info("Environment shared.");
                    portalWsSession.close(CloseStatus.SERVICE_RESTARTED);//restart (via handler) Banalytics connection with new parameter for RTC Router
                } else if (message instanceof EnvironmentUnShareWithRes res) {
                    log.info("Environment un-shared.");
                    portalWsSession.close(CloseStatus.SERVICE_RESTARTED);//restart (via handler) Banalytics connection with new parameter for RTC Router
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
            return null;
        }
    }

    private SSLContext getSslContext() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509ExtendedTrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) {
                log.info(authType);
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) {
                log.info(authType);
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
                log.info(authType);
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
                log.info(authType);
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                log.info(authType);
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                log.info(authType);
            }

        }};

        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new SecureRandom());

//        SSLContext.setDefault(sslContext);
        return sslContext;
    }

    @Getter
    public class Statistics {
        int connEstablished;
        int connClosed;
        int connErrors;

        int webSockInMessages;
        int webSockOutMessages;
    }
}
