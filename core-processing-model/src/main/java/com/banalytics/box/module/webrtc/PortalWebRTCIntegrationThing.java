package com.banalytics.box.module.webrtc;

import com.banalytics.box.TimeUtil;
import com.banalytics.box.api.integration.AbstractMessage;
import com.banalytics.box.api.integration.MessageHandler;
import com.banalytics.box.api.integration.model.ComponentRelation;
import com.banalytics.box.api.integration.model.SecurityModel;
import com.banalytics.box.api.integration.model.Share;
import com.banalytics.box.api.integration.model.SharePermission;
import com.banalytics.box.api.integration.utils.CommonUtils;
import com.banalytics.box.api.integration.webrtc.*;
import com.banalytics.box.api.integration.webrtc.channel.NodeDescriptor;
import com.banalytics.box.api.integration.webrtc.channel.environment.ThingApiCallReq;
import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import com.banalytics.box.api.integration.webrtc.channel.events.peer.ConnectionStateEvent;
import com.banalytics.box.module.AbstractThing;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.Singleton;
import com.banalytics.box.module.cloud.portal.PortalIntegrationConfiguration;
import com.banalytics.box.module.cloud.portal.PortalIntegrationThing;
import com.banalytics.box.module.events.jpa.TokenEntity;
import com.banalytics.box.module.events.jpa.WebRTCConnectionHistory;
import com.banalytics.box.module.standard.EventConsumer;
import com.banalytics.box.module.utils.Utils;
import com.banalytics.box.module.webrtc.client.PeerConnectionListenerAdaptor;
import com.banalytics.box.module.webrtc.client.RTCClient;
import com.banalytics.box.module.webrtc.client.UserThreadContext;
import com.banalytics.box.module.webrtc.client.adaptor.SetSessionDescriptionObserverAdaptor;
import com.banalytics.box.module.webrtc.processors.OfferProcessor;
import com.banalytics.box.module.webrtc.processors.ReadyProcessor;
import com.banalytics.box.service.JpaService;
import com.banalytics.box.service.utility.TrafficControl;
import com.fasterxml.jackson.core.type.TypeReference;
import dev.onvoid.webrtc.RTCSdpType;
import dev.onvoid.webrtc.RTCSessionDescription;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.web.socket.WebSocketSession;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static com.banalytics.box.TimeUtil.currentTimeInServerTz;
import static com.banalytics.box.api.integration.utils.CommonUtils.DEFAULT_OBJECT_MAPPER;
import static com.banalytics.box.module.Thing.StarUpOrder.DATA_EXCHANGE;
import static com.banalytics.box.service.SystemThreadsService.SYSTEM_TIMER;

/**
 * https://github.com/devopvoid/webrtc-java
 * <p>
 * https://github.com/muaz-khan/WebRTC-Experiment
 */
@Slf4j
@Order(DATA_EXCHANGE + 300)
public class PortalWebRTCIntegrationThing extends AbstractThing<PortalWebRTCIntegrationConfiguration> implements MessageHandler<AbstractWebRTCMessage>, EventConsumer, Consumer<AbstractEvent>, Singleton {
    @Override
    public String getTitle() {
        return getSelfClassName();
    }

    public PortalWebRTCIntegrationThing(BoxEngine engine) {
        super(engine);
    }

    private PortalIntegrationThing portalIntegrationThing;

    /**
     * string format: {url}@{username}:{password}
     * <p>
     * https://gist.github.com/zziuni/3741933
     * https://gist.github.com/sagivo/3a4b2f2c7ac6e1b5267c2f1f59ac6c6b
     * "stun:stun.banalytics.live:3478",
     * "turn:turn.banalytics.live:3478@banalytics:banalytics"
     * "stun:stun1.l.google.com:19302",
     * "stun:stun2.l.google.com:19302",
     * "stun:stun3.l.google.com:19302",
     * "stun:stun4.l.google.com:19302",
     */
    private String[] iceServersList = new String[]{
            "stun:stun.banalytics.live:3478",
            "turn:turn.banalytics.live:3478@banalytics:banalytics"
    };

    /**
     * Transaction UUID based on web socket session -> Rtc Client
     */
    private final Map<String, RTCClient> clientMap = new ConcurrentHashMap<>();
    private final Map<String, Long> clientConnectionTimeMap = new ConcurrentHashMap<>();

    private File callersFile;

    private TimerTask dropExpiredConnections;
    private JpaService jpaService;

    @Override
    protected void doInit() throws Exception {
        this.portalIntegrationThing = engine.getThing(PortalIntegrationConfiguration.THING_UUID);
        this.portalIntegrationThing.subscribe(this);
        this.portalIntegrationThing.subscribeHandler(this);

        this.jpaService = engine.getJpaService();

        File iceServersFile = new File(engine.applicationConfigFolder(), "ice-servers.list");
        if (iceServersFile.exists()) {
            List<String> servers = FileUtils.readLines(iceServersFile, StandardCharsets.UTF_8);
            iceServersList = servers.toArray(new String[0]);
        }
    }

    /**
     * Email -> Permissions
     * [
     * 'abcd@email.com' -> {
     * 'thinguuid' -> {
     * thingUuid: '1234-1234-12341234',
     * generalPermissions: [media, modify, ... cust perm],
     * apiMethodsPermissions: [method1, method2, ...],
     * },
     * ........
     * }
     */
    private SecurityModel securityModel = new SecurityModel();

    @Override
    public void reloadConfig() {
        TrafficControl.INSTANCE.setBandwidthConfig(
                configuration.maxConnectionBandwidth,
                configuration.reservedBandwidthForFileTransmissionPercent
        );
    }

    @Override
    protected void doStart() throws Exception {
        reloadConfig();

        engine.addEventConsumer(this);
        dropExpiredConnections = new TimerTask() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                Set<String> dropSet = new HashSet<>();
                int timeout = configuration.clientTimeoutMinutes * 60 * 1000;
                clientMap.forEach((tx, client) -> {
                    if (now > client.lastInteractionTime + timeout) {
                        dropSet.add(tx);
                    }
                });
                for (String txId : dropSet) {
                    clientConnectionTimeMap.remove(txId);

                    stopClient(txId, "Connection expired.");
                }
                if (jpaService.isOpen()) {
                    jpaService.cleanUpExpiredTokens();
                }
            }
        };
        SYSTEM_TIMER.schedule(dropExpiredConnections, 5000, 3000);
        File applicationConfigFolder = engine.applicationConfigFolder();
        File instanceFolder = new File(applicationConfigFolder, "instances");
        this.callersFile = new File(instanceFolder, getUuid().toString() + ".allowed-callers");
        if (!callersFile.exists()) {
            if (!callersFile.createNewFile()) {
                log.error("Can't create {} file", callersFile.getAbsolutePath());
            } else {
                try (FileWriter fw = new FileWriter(callersFile)) {
                    fw.write("{}");
                }
            }
        } else {
            try {
                loadSecurityModel();
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        }
        log.info("Web RTC started");
    }

    @Override
    protected void doStop() throws Exception {
        engine.removeEventConsumer(this);
        dropExpiredConnections.cancel();
        clientMap.forEach((txId, rtc) -> {
            try {
                rtc.stop();
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        });
        clientMap.clear();
        clientConnectionTimeMap.clear();
    }

    @Override
    public void destroy() {
        if (portalIntegrationThing != null) {
            portalIntegrationThing.unSubscribeHandler(this);
            portalIntegrationThing.unSubscribe(this);
            portalIntegrationThing = null;
        }
    }

    @Override
    public boolean isSupport(AbstractMessage message) {
        return message instanceof AbstractWebRTCMessage;
    }

    private void fireStateEvent(String transactionId, ConnectionStateEvent.ConnectionType type, ConnectionStateEvent.State state, String connectionIdentity) {
        engine.fireEvent(new ConnectionStateEvent(
                NodeDescriptor.NodeType.THING,
                getUuid(),
                getSelfClassName(),
                getSelfClassName(),
                transactionId,
                type,
                state,
                connectionIdentity
        ));
    }


    public RTCClient findAgent(UUID agentUuid) {
        for (RTCClient ag : clientMap.values()) {
            if (agentUuid.equals(ag.environmentUUID)) {
                return ag;
            }
        }
        return null;
    }
    public void sendReady(UUID agentUuid) {
        Ready ready = new Ready();
//        ready.fromAgentUuid = ;
        ready.toAgentUuid = agentUuid;
        portalIntegrationThing.sendMessage(ready);
    }

    public void sendBye(UUID agentUuid) {
        Bye bye = new Bye();
        bye.toAgentUuid = agentUuid;
        portalIntegrationThing.sendMessage(bye);
    }

    private final PeerConnectionListenerAdaptor peerConnectionListenerAdaptor = new PeerConnectionListenerAdaptor() {
        @Override
        public void onConnecting(ConnectionEvent event) {
            super.onConnecting(event);
            RTCClient c = event.getRtcClient();
            fireStateEvent(c.transactionId,
                    c.publicShare ? ConnectionStateEvent.ConnectionType.PUBLIC : ConnectionStateEvent.ConnectionType.ACCOUNT,
                    ConnectionStateEvent.State.CONNECTING, c.getIdentity());
        }

        @Override
        public void onConnected(ConnectionEvent event) {
            super.onConnected(event);
            RTCClient c = event.getRtcClient();
            fireStateEvent(c.transactionId,
                    c.publicShare ? ConnectionStateEvent.ConnectionType.PUBLIC : ConnectionStateEvent.ConnectionType.ACCOUNT,
                    ConnectionStateEvent.State.CONNECTED, event.getRtcClient().getIdentity());
        }

        @Override
        public void onFailed(ConnectionEvent event) {
            super.onFailed(event);
            RTCClient c = event.getRtcClient();
            fireStateEvent(c.transactionId,
                    c.publicShare ? ConnectionStateEvent.ConnectionType.PUBLIC : ConnectionStateEvent.ConnectionType.ACCOUNT,
                    ConnectionStateEvent.State.CONNECTION_FAILED, event.getRtcClient().getIdentity());
            stopClient(c.transactionId, "Peer connection failed");
        }

        @Override
        public void onDisconnected(ConnectionEvent event) {
            super.onDisconnected(event);
            RTCClient c = event.getRtcClient();
            removeClient(event);

            fireStateEvent(c.transactionId,
                    c.publicShare ? ConnectionStateEvent.ConnectionType.PUBLIC : ConnectionStateEvent.ConnectionType.ACCOUNT,
                    ConnectionStateEvent.State.DISCONNECTED, event.getRtcClient().getIdentity());
        }

        private void removeClient(ConnectionEvent event) {
            String transactionId = event.getRtcClient().transactionId;
            clientConnectionTimeMap.remove(transactionId);
            stopClient(transactionId, "Disconnected.");
        }
    };

    private /*synchronized*/ void stopClient(String transactionId, String message) {
        RTCClient client = clientMap.remove(transactionId);
        if (client != null) {
            log.info(message + " RTCClient stopped: {}", client.environmentUUID);
            client.stop();
        }
    }

    private void logConnection(AbstractWebRTCMessage rtcMsg) {
        WebRTCConnectionHistory es = new WebRTCConnectionHistory();
        es.setDateTime(currentTimeInServerTz());

        String identity = rtcMsg.fromAccountEmail;
        if(!identity.contains("@")){
            Share share = securityModel.accountPublicShareGroupsOverride(identity);
            es.setAccountId(-1L);
            es.setAccountEmail(share.getTitle());
        } else {
            es.setAccountId(rtcMsg.fromAccountId);
            es.setAccountEmail(rtcMsg.fromAccountEmail);
        }
        jpaService.persistEntity(es);
    }

    @Override
    public AbstractWebRTCMessage handleMessage(WebSocketSession session, AbstractMessage message) {
        log.debug("Processing WebRTC signal:\n{}", message);
        AbstractWebRTCMessage rtcMsg = (AbstractWebRTCMessage) message;
        if (!portalIntegrationThing.getEnvironmentUUID().equals(rtcMsg.toAgentUuid)
                || StringUtils.isEmpty(rtcMsg.fromAccountEmail)) {
            throw new RuntimeException("Invalid routing of the message: " + rtcMsg);//todo alert to portal UI
        }
        boolean publicShare = false;
        Share share = null;
        if (!rtcMsg.isFromMyAccount()) {
            String identity = rtcMsg.getFromAccountEmail();
            if(identity.contains("@")) {//if email
                checkSharedAccess(identity);
                share = securityModel.accountShareGroupsOverride(identity);
            } else {//if public share token
                publicShare = true;
                checkPublicSharedAccess(identity);
                share = securityModel.accountPublicShareGroupsOverride(identity);
            }
        }
        try {
            String transactionId = rtcMsg.clientWebSocketSession;
            switch (rtcMsg.getType()) {
                case offer -> {
                    logConnection(rtcMsg);
                    log.debug("Received offer from BB unused now. Case for direct connection between environments.");
                    Offer offer = (Offer) rtcMsg;
                    Map<Class<?>, Set<ComponentRelation>> componentsRelations = engine.componentsRelations();
                    RTCClient rtcClient = new RTCClient(this,
                            transactionId,
                            rtcMsg.fromMyAccount,
                            rtcMsg.fromAccountEmail,
                            engine,
                            rtcMsg.fromAgentUuid,
                            offer.iceServersList,
                            share,
                            publicShare,
                            componentsRelations
                    );
                    rtcClient.addIceCandidateConsumer(new IceCandidateConsumer(offer.fromAgentUuid));

                    rtcClient.addPeerConnectionListener(peerConnectionListenerAdaptor);
                    clientMap.put(transactionId, rtcClient);

                    Answer answer = OfferProcessor.execute(
                            rtcClient.peerConnection,
                            offer
                    );
                    answer.setToAgentUuid(offer.fromAgentUuid);
                    answer.setFromAgentUuid(offer.toAgentUuid);

                    CommonUtils.sendMessage(session, answer);
                    return null;
                }
                case answer -> {
                    RTCClient rtcClient = clientMap.get(transactionId);
                    if (rtcClient == null) {
                        return null;
                    }
                    Answer answer = (Answer) rtcMsg;
                    log.debug("Received answer from client");
                    rtcClient.peerConnection.setRemoteDescription(
                            new RTCSessionDescription(
                                    RTCSdpType.ANSWER, answer.sdp
                            ), new SetSessionDescriptionObserverAdaptor("Error set Remote description: {}") {
                                @Override
                                public void onSuccess() {
                                    log.debug("Set remote description success");
                                    rtcClient.start();
                                }
                            }
                    );
                }
                case candidate -> {
                    RTCClient rtcClient = clientMap.get(transactionId);
                    if (rtcClient == null) {
                        return null;
                    }
                    IceCandidate candidate = (IceCandidate) rtcMsg;
                    if (candidate.candidate != null) {
                        rtcClient.addIceCandidate(candidate);
                    }
                    return null;
                }
                case ready -> {
                    logConnection(rtcMsg);
                    if (this.clientMap.containsKey(transactionId)) {
                        Long connectionTime = clientConnectionTimeMap.get(transactionId);
                        if (connectionTime == null || System.currentTimeMillis() > (connectionTime + 30000)) {
                            log.warn("Reconnecting WebRTC client");
                            //stop and drop old connection
                            stopClient(transactionId, "Reconnecting.");
                        } else {
                            log.warn("Received duplicated ready message from client: {}. Message skipped.", transactionId);
                            return null;
                        }
                    }

                    Ready ready = (Ready) rtcMsg;
                    log.debug("Received ready from client");
                    Map<Class<?>, Set<ComponentRelation>> componentsRelations = engine.componentsRelations();
                    RTCClient rtcClient = new RTCClient(this,
                            transactionId,
                            rtcMsg.fromMyAccount,
                            rtcMsg.fromAccountEmail,
                            engine,
                            rtcMsg.toAgentUuid,
                            iceServersList,
                            share,
                            publicShare,
                            componentsRelations
                    );
                    rtcClient.addPeerConnectionListener(peerConnectionListenerAdaptor);
                    clientConnectionTimeMap.put(transactionId, System.currentTimeMillis());
                    rtcClient.addIceCandidateConsumer(new IceCandidateConsumer(ready.fromAgentUuid));
                    Offer offer = ReadyProcessor.execute(rtcClient.peerConnection);
                    offer.setToAgentUuid(ready.fromAgentUuid);
                    offer.setFromAgentUuid(ready.toAgentUuid);
                    offer.clientWebSocketSession = transactionId;
                    offer.setIceServersList(iceServersList);
                    CommonUtils.sendMessage(session, offer);
                    this.clientMap.put(transactionId, rtcClient);
                    return null;
                }
                case bye -> {
                    log.warn("Received bye from client.");
                    stopClient(transactionId, "Bye. ");
                    return null;
                }
                default -> {
                    log.warn("Unknown RTC command: {}", rtcMsg.toJson());
                }
            }
        } catch (Throwable e) {
            log.error("Failure to handle message: " + message, e);
        }
        return null;
    }


    private class IceCandidateConsumer implements Consumer<IceCandidate> {
        final UUID respondToAgentUuid;

        public IceCandidateConsumer(UUID respondToAgentUuid) {
            this.respondToAgentUuid = respondToAgentUuid;
        }

        @Override
        public void accept(IceCandidate iceCandidate) {
            try {
                iceCandidate.setToAgentUuid(this.respondToAgentUuid);
                portalIntegrationThing.sendMessage(iceCandidate);
            } catch (Throwable ex) {
                log.error("can send ice candidate message.", ex);
            }
        }
    }

    @Override
    public Set<String> apiMethodsPermissions() {
        return Set.of(PERMISSION_READ, PERMISSION_UPDATE, "share", "unShare", "shareThing", "unShareThing", "resetPassword", "setPassword");
    }

    public static final String PERMISSION_EVENTS_DELIVERY = "delivery_events";

    @Override
    public Set<String> generalPermissions() {
        Set<String> permissions = new HashSet<>(super.generalPermissions());
        permissions.add(PERMISSION_EVENTS_DELIVERY);
        return permissions;
    }

    @Override
    public void accept(AbstractEvent event) {
        for (RTCClient client : clientMap.values()) {
            if (client.isMyProfileConnection()) {
                try {
                    client.sendEvent(event);
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public Set<String> accountNames(Set<String> accountIds) {
        Set<String> result = new HashSet<>();
        for (String accountId : accountIds) {
            if (securityModel.getAccountShare().containsKey(accountId)) {
                result.add(accountId);
            } else {
                result.add("???" + accountId + "???");
            }
        }
        return result;
    }

    @Override
    public Set<Class<? extends AbstractEvent>> produceEvents() {
        Set<Class<? extends AbstractEvent>> events = new HashSet<>(super.produceEvents());
        events.add(ConnectionStateEvent.class);
        return events;
    }

    @Override
    public void consume(Recipient recipient, AbstractEvent event) {
        for (RTCClient client : clientMap.values()) {
            if (client.isMyProfileConnection()) { //if my profile, then skip, my profile receives all events via this.accept method
                continue;
            }

            String accountEmail = client.getIdentity();

            boolean deliveryGranted;
            if (recipient.isAllowed(accountEmail)) {  // or deliver to target
                Share share = securityModel.accountShareGroupsOverride(accountEmail);
                Map<UUID, SharePermission> accountPermissions = share.getSharePermissions();
                SharePermission permissions = accountPermissions.get(getUuid()); // check account permission
                deliveryGranted = permissions != null && permissions.generalPermissions.contains(PERMISSION_EVENTS_DELIVERY);
            } else {
                deliveryGranted = false;
            }


            if (deliveryGranted) {
                try {
                    client.sendEvent(event);
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    private static final String IDENTITY_PARAM = "identity";
    private static final String TARGET_NODE_PARAM = "targetNode";

    @Override
    public Object call(Map<String, Object> params) throws Exception {
        String method = (String) params.get(ThingApiCallReq.PARAM_METHOD);

        if (method.startsWith("bean:")) {
            String[] parts = method.split(":");
            String serviceName = parts[1];
            String methodName = parts[2];
            String camera = (String) params.get("params");
            return engine.serviceCall(serviceName, methodName, camera);
        }

        String targetNodeStr = (String) params.get(TARGET_NODE_PARAM);
        SecurityModel.TargetNode targetNode = targetNodeStr == null ? null : SecurityModel.TargetNode.valueOf(targetNodeStr);
        switch (method) {
            case "readAccounts" -> {
                // portal accounts
                Map<String, Share> accountShare = securityModel.getAccountShare();
                List<String> callers = new ArrayList<>();
                for (String acc : accountShare.keySet()) {
                    callers.add(acc + "~" + acc);
                }
                /* disable forwards to public shares
                Map<String, Share> publicShare = securityModel.getPublicShare();
                for (String acc : publicShare.keySet()) {
                    callers.add(acc + "~" + acc);
                }*/
                callers.sort(String::compareToIgnoreCase);
                return callers;
            }
            case "readUsersInGroup" -> {
                String groupName = (String) params.get(IDENTITY_PARAM);
                List<String> accountShares = new ArrayList<>();
                for (Share accountShare : securityModel.getAccountShare().values()) {
                    if (accountShare.getGroups().contains(groupName)) {
                        accountShares.add(accountShare.getTitle());
                    }
                }
                accountShares.sort(String::compareToIgnoreCase);
                List<String> publicShares = new ArrayList<>();
                for (Share publicShare : securityModel.getPublicShare().values()) {
                    if (publicShare.getGroups().contains(groupName)) {
                        publicShares.add(publicShare.getTitle());
                    }
                }
                publicShares.sort(String::compareToIgnoreCase);
                return Map.of(
                        "accountShares", accountShares,
                        "publicShares", publicShares
                );
            }
            case "readTargetNode" -> {
                return securityModel.targetNodeValues(targetNode);
            }
            case "setPassword" -> {
                String oldPassword = (String) params.get("oldPassword");
                String newPassword = (String) params.get("password");
                String accountEmail;
                if (UserThreadContext.isMyEnvironment()) {
                    //change agent password
                    engine.changePassword(oldPassword, newPassword);
                    accountEmail = UserThreadContext.myAccountEmail();
                } else {
                    // change share password
                    Share share = UserThreadContext.share();
                    if (share.getMd5Password().equals(Utils.md5Hash(oldPassword))) {// change share password only
                        share.setMd5Password(Utils.md5Hash(newPassword));
                        storeSecurityModel();
                        accountEmail = share.getIdentity();
                    } else {
                        throw new Exception("Invalid old password.");
                    }
                }

                jpaService.clearTokensByObjectReference(getUuid() + accountEmail);
                dropRTCConnections(accountEmail);

                return "success";
            }
            case "resetPassword" -> {// action allowed for shared environments only
                String randomPassword = RandomStringUtils.randomAlphanumeric(10);

                Share share;
                if (UserThreadContext.isMyEnvironment()) {
                    Map<String, Share> accountShare = securityModel.targetNodeValues(targetNode);
                    String callerAccountEmail = (String) params.get(IDENTITY_PARAM);
                    share = accountShare.get(callerAccountEmail);
                } else {
                    share = UserThreadContext.share();
                }
                share.setMd5Password(Utils.md5Hash(randomPassword));
                storeSecurityModel();
                portalIntegrationThing.notifyEnvironmentPasswordReset(share.getIdentity(), randomPassword);

                jpaService.clearTokensByObjectReference(getUuid() + share.getIdentity());
                dropRTCConnections(share.getIdentity());

                return "success";
            }
            case "createTargetNode" -> {
                String identity = (String) params.get(IDENTITY_PARAM);
                Share share = new Share();

                Object response = "success";
                switch (targetNode) {
                    case accountShare -> {
                        share.setIdentity(identity);
                        share.setTitle(identity);
                        String randomPassword = RandomStringUtils.randomAlphanumeric(10);
                        share.setMd5Password(Utils.md5Hash(randomPassword));
                        portalIntegrationThing.shareEnvironmentWith(identity, randomPassword);
                    }
                    case userGroup -> {
                        share.setIdentity(identity);
                        share.setTitle(identity);
                    }
                    case publicShare -> {
                        String token = RandomStringUtils.randomAlphanumeric(60);
                        share.setIdentity(token);
                        share.setTitle(identity);
                        portalIntegrationThing.publicShareEnvironment(identity, token);
                        response = token;
                    }
                }

                Map<String, Share> targetMap = securityModel.targetNodeValues(targetNode);
                targetMap.put(identity, share);
                storeSecurityModel();
                return response;
            }
            case "deleteTargetNode" -> {
                String identity = (String) params.get(IDENTITY_PARAM);
                Map<String, Share> shares = securityModel.targetNodeValues(targetNode);
                Share removedShare = shares.remove(identity);

                switch (targetNode) {
                    case accountShare -> {
                        portalIntegrationThing.unShareEnvironmentWith(identity);
                    }
                    case userGroup -> {
                        securityModel.getPublicShare().values().forEach(s -> {
                            s.getGroups().remove(identity);
                        });
                        securityModel.getAccountShare().values().forEach(s -> {
                            s.getGroups().remove(identity);
                        });
                    }
                    case publicShare -> {
                        String token = removedShare.getIdentity();
                        portalIntegrationThing.publicUnShareEnvironment(token);
                    }
                }


                dropRTCConnections(identity);
                storeSecurityModel();
                return "success";
            }
            case "saveTargetNodeGroups" -> {
                String identity = (String) params.get(IDENTITY_PARAM);
                Collection<String> newGroups = (Collection<String>) params.get("groups");
                Map<String, Share> accountShare = securityModel.targetNodeValues(targetNode);
                Share share = accountShare.get(identity);
                Set<String> groups = share.getGroups();
                groups.clear();
                groups.addAll(newGroups);
                storeSecurityModel();
                return "success";
            }
            case "shareThing" -> {
                String identity = (String) params.get(IDENTITY_PARAM);
                String uuid = (String) params.get("uuid");
                UUID thingUuid = UUID.fromString(uuid);

                Map<String, Share> accountShare = securityModel.targetNodeValues(targetNode);
                Share share = accountShare.get(identity);
                Map<UUID, SharePermission> perms = share.getSharePermissions();
                perms.put(thingUuid, new SharePermission());
                storeSecurityModel();
                return "success";
            }
            case "unShareThing" -> {
                String callerAccountEmail = (String) params.get(IDENTITY_PARAM);
                String uuid = (String) params.get("uuid");
                UUID thingUuid = UUID.fromString(uuid);

                Map<String, Share> accountShare = securityModel.targetNodeValues(targetNode);
                Share share = accountShare.get(callerAccountEmail);
                Map<UUID, SharePermission> perms = share.getSharePermissions();
                perms.remove(thingUuid);
                storeSecurityModel();
                return "success";
            }
            case "updateAddThingPerm" -> {
                String callerAccountEmail = (String) params.get(IDENTITY_PARAM);
                String uuid = (String) params.get("uuid");
                String permissionType = (String) params.get("permissionType");
                String permissionValue = (String) params.get("permissionValue");
                UUID thingUuid = UUID.fromString(uuid);

                Map<String, Share> accountShare = securityModel.targetNodeValues(targetNode);
                Share share = accountShare.get(callerAccountEmail);
                Map<UUID, SharePermission> perms = share.getSharePermissions();

                SharePermission sharePermission = perms.get(thingUuid);
                switch (permissionType) {
                    case "generalPermissions" -> sharePermission.generalPermissions.add(permissionValue);
                    case "apiMethodsPermissions" -> sharePermission.apiMethodsPermissions.add(permissionValue);
                }


                storeSecurityModel();
                return "success";
            }
            case "updateRemoveThingPerm" -> {
                String callerAccountEmail = (String) params.get(IDENTITY_PARAM);
                String uuid = (String) params.get("uuid");
                String permissionType = (String) params.get("permissionType");
                String permissionValue = (String) params.get("permissionValue");
                UUID thingUuid = UUID.fromString(uuid);

                Map<String, Share> accountShare = securityModel.targetNodeValues(targetNode);
                Share share = accountShare.get(callerAccountEmail);
                Map<UUID, SharePermission> perms = share.getSharePermissions();

                SharePermission sharePermission = perms.get(thingUuid);
                switch (permissionType) {
                    case "generalPermissions" -> sharePermission.generalPermissions.remove(permissionValue);
                    case "apiMethodsPermissions" -> sharePermission.apiMethodsPermissions.remove(permissionValue);
                }

                storeSecurityModel();
                return "success";
            }
            case "readHistory" -> {
                String query = (String) params.get("query");
                int pageNum;
                try {
                    pageNum = (Integer) params.get("pageNum");
                } catch (NumberFormatException e) {
                    pageNum = 0;
                }
                int pageSize;
                try {
                    pageSize = (Integer) params.get("pageSize");
                } catch (NumberFormatException e) {
                    pageSize = 10;
                }
                Map<String, String> orderSpec = (Map<String, String>) params.get("orderSpec");

                return jpaService.expressionQuery(pageNum, pageSize, query, orderSpec, WebRTCConnectionHistory.class);
            }
            default -> {
                throw new Exception("Method not supported: " + method);
            }
        }
    }

    private void dropRTCConnections(String callerAccountEmail) {
        SYSTEM_TIMER.schedule(new TimerTask() {//give the time to send responses to client and stop
            @Override
            public void run() {
                for (RTCClient rtc : clientMap.values()) {
                    if (rtc.getIdentity().equals(callerAccountEmail)) {
                        try {
                            rtc.stop();
                        } catch (Throwable e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                }
            }
        }, 2000);
    }

    private final TypeReference<SecurityModel> TYPE_SECURITY_MODEL = new TypeReference<>() {
    };

    private void loadSecurityModel() throws Exception {
        securityModel = DEFAULT_OBJECT_MAPPER.readValue(callersFile, TYPE_SECURITY_MODEL);
    }

    private void storeSecurityModel() throws Exception {
        DEFAULT_OBJECT_MAPPER.writeValue(callersFile, securityModel);
    }

    private void checkSharedAccess(String accountEmail) {
        if (!securityModel.getAccountShare().containsKey(accountEmail)) {
            throw new RuntimeException("Access Denied");
        }
    }

    private void checkPublicSharedAccess(String token) {

        if (!securityModel.hasPublicShare(token)) {
            throw new RuntimeException("Access Denied");
        }
    }

    public String createEnvironmentAccessToken(String accountEmail) {
        TokenEntity tokenEntity = new TokenEntity();

        String token = RandomStringUtils.randomAlphanumeric(40);
        tokenEntity.setToken(token);
        tokenEntity.setObjectReference(getUuid() + accountEmail);

        LocalDateTime expirationTime = TimeUtil.currentTimeInServerTz()
                .plus(configuration.tokenTTLMinutes * 60 * 1000L, ChronoUnit.MILLIS);

        tokenEntity.setExpirationTime(expirationTime);
        jpaService.persistEntity(tokenEntity);

        return token;
    }

    public void removeAccessToken(String token) {
        jpaService.removeEntity(token, TokenEntity.class);
    }

    public boolean hasAccessToken(String token) {
        List<TokenEntity> t = jpaService.query(
                "from TokenEntity where token=:t and expirationTime > :now",
                TokenEntity.class,
                Map.of(
                        "t", token,
                        "now", TimeUtil.currentTimeInServerTz()
                )
        );

        return !t.isEmpty();
    }
}
