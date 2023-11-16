package com.banalytics.box.module.webrtc.client;

import com.banalytics.box.LocalizedException;
import com.banalytics.box.api.integration.AbstractMessage;
import com.banalytics.box.api.integration.model.Share;
import com.banalytics.box.api.integration.model.SharePermission;
import com.banalytics.box.api.integration.webrtc.IceCandidate;
import com.banalytics.box.api.integration.webrtc.channel.AbstractChannelMessage;
import com.banalytics.box.api.integration.webrtc.channel.ExceptionMessage;
import com.banalytics.box.api.integration.webrtc.channel.NodeDescriptor;
import com.banalytics.box.api.integration.webrtc.channel.environment.*;
import com.banalytics.box.api.integration.webrtc.channel.environment.auth.*;
import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import com.banalytics.box.api.integration.webrtc.channel.events.peer.ConnectionStateEvent;
import com.banalytics.box.module.BoxEngine;
import com.banalytics.box.module.ITask;
import com.banalytics.box.module.MediaCaptureCallbackSupport;
import com.banalytics.box.module.Thing;
import com.banalytics.box.module.utils.Utils;
import com.banalytics.box.module.webrtc.ChannelsUtils;
import com.banalytics.box.module.webrtc.PortalWebRTCIntegrationConfiguration;
import com.banalytics.box.module.webrtc.PortalWebRTCIntegrationThing;
import com.banalytics.box.module.webrtc.client.channel.*;
import com.banalytics.box.module.webrtc.client.channel.observer.DataTransferChannelObserver;
import com.banalytics.box.module.webrtc.client.channel.observer.MediaChannelObserver;
import com.banalytics.box.service.SystemThreadsService;
import com.banalytics.box.service.utility.GZIPUtils;
import dev.onvoid.webrtc.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Consumer;

import static com.banalytics.box.api.integration.webrtc.channel.environment.ThingApiCallReq.PARAM_METHOD;
import static com.banalytics.box.module.Thing.*;
import static com.banalytics.box.service.SystemThreadsService.SYSTEM_TIMER;
import static com.banalytics.box.service.SystemThreadsService.getExecutorService;

@Slf4j
public class RTCClient implements PeerConnectionObserver {

    private final static PeerConnectionFactory factory = new PeerConnectionFactory();

    public final String transactionId;
    private final boolean myProfileConnection;
    private final String identity;
    private final PortalWebRTCIntegrationThing webRTCIntegrationThing;
    public final BoxEngine engine;
    public final RTCPeerConnection peerConnection;
    private static final String ENVIRONMENT_CHANNEL_ID = "environment-channel";
    private static final String DATA_TRANSFER_CHANNEL_ID = "data-transfer";
    private static final String MEDIA_TRANSFER_CHANNEL_ID = "media-transfer";
    //    private static final String STUB_CHANNEL_ID = "stub-channel";
    private final Map<String, Map<Class<? extends AbstractChannelMessage>, ChannelRequestHandler>> environmentChannelRequestHandlerMap = new HashMap<>();
    public final UUID environmentUUID;
    public final boolean publicShare;
    private final Share share;
    public long lastInteractionTime = System.currentTimeMillis();

    private String currentToken;
    private String newToken;
    private final TimerTask RENEW_TOKEN = new TimerTask() {
        @Override
        public void run() {
            try {
                newToken = webRTCIntegrationThing.createEnvironmentAccessToken(identity);
                AuthenticationTokenRenewReq renewMsg = new AuthenticationTokenRenewReq();
                renewMsg.setRequestId(-1);
                renewMsg.setNewToken(newToken);
                sendEnvironmentMessage(renewMsg);
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        }
    };

    public RTCClient(PortalWebRTCIntegrationThing webRTCIntegrationThing, String transactionId, boolean myProfileConnection, String identity,
                     BoxEngine engine, UUID environmentUUID,
                     String[] iceServersList,
                     Share share, boolean publicShare) {
        this.webRTCIntegrationThing = webRTCIntegrationThing;
        this.transactionId = transactionId;
        this.myProfileConnection = myProfileConnection;
        if (identity.contains("@")) {
            this.identity = identity;
        } else {
            this.identity = share.getTitle();
        }

        this.engine = engine;
        this.environmentUUID = environmentUUID;
        this.share = share;
        this.publicShare = publicShare;

        Map<Class<? extends AbstractChannelMessage>, ChannelRequestHandler> requestHandlersMap = new HashMap<>();
        requestHandlersMap.put(AvailableActionTaskClassesReq.class, new AvailableActionTaskClassesReqHandler(engine));
        requestHandlersMap.put(AvailableSingletonThingClassesReq.class, new AvailableSingletonThingClassesReqHandler(engine));
        requestHandlersMap.put(AvailableThingClassesReq.class, new AvailableThingClassesReqHandler(engine));
//        requestHandlersMap.put(AvailableTaskClassesReq.class, new AvailableTaskClassesReqHandler(engine));
        requestHandlersMap.put(FindActionTasksReq.class, new FindActionTasksReqHandler(engine));
        requestHandlersMap.put(DeleteTaskReq.class, new DeleteTaskReqHandler(engine));
        requestHandlersMap.put(DeleteThingReq.class, new DeleteThingReqHandler(engine));
        requestHandlersMap.put(EnvironmentDescriptorReq.class, new EnvironmentDescriptorReqHandler(engine));
        requestHandlersMap.put(MediaChannelCreateReq.class, new MediaChannelReqHandler(engine, this));
        requestHandlersMap.put(I18NReq.class, new I18NReqHandler(engine));
        requestHandlersMap.put(SaveTaskReq.class, new SaveTaskReqHandler(engine));
        requestHandlersMap.put(SaveThingReq.class, new SaveThingReqHandler(engine));
        requestHandlersMap.put(StartTaskReq.class, new StartTaskReqHandler(engine));
        requestHandlersMap.put(StopTaskReq.class, new StopTaskReqHandler(engine));
        requestHandlersMap.put(StartThingReq.class, new StartThingReqHandler(engine));
        requestHandlersMap.put(StopThingReq.class, new StopThingReqHandler(engine));
        requestHandlersMap.put(FireActionReq.class, new FireActionReqHandler(engine));
        requestHandlersMap.put(SubTasksReq.class, new SubTasksReqHandler(engine));
        requestHandlersMap.put(TaskConfigurationDescriptorReq.class, new TaskConfigurationDescriptorReqHandler(engine));
        requestHandlersMap.put(ThingApiCallReq.class, new ThingApiCallReqHandler(engine, this));
        requestHandlersMap.put(ThingConfigurationDescriptorReq.class, new ThingConfigurationDescriptorReqHandler(engine));
        requestHandlersMap.put(ThingDescriptionReq.class, new ThingDescriptionReqHandler(engine));
        requestHandlersMap.put(ThingTasksReq.class, new ThingTasksReqHandler(engine));
        requestHandlersMap.put(ThingsReq.class, new ThingsReqHandler(engine, share == null ? null : share.getSharePermissions()));
        requestHandlersMap.put(IssuesReq.class, new IssuesReqHandler(engine, share == null ? null : share.getSharePermissions()));
        requestHandlersMap.put(ThingsGroupsReq.class, new ThingsGroupsReqHandler(engine));

        environmentChannelRequestHandlerMap.put(ENVIRONMENT_CHANNEL_ID, requestHandlersMap);

        RTCConfiguration config = new RTCConfiguration() {
            {
                for (String s : iceServersList) {
                    RTCIceServer iceServer = new RTCIceServer();
                    String[] parts = s.split("@");
                    if (parts.length > 0) {
                        iceServer.urls.add(parts[0]);
                    }
                    if (parts.length > 1) {
                        String[] authParts = parts[1].split(":");
                        iceServer.username = authParts[0];
                        iceServer.password = authParts[1];
                    }
                    iceServers.add(iceServer);
                }
            }
        };
        this.peerConnection = factory.createPeerConnection(config, this);
        createEnvironmentChannel(null);
    }

    public boolean isMyProfileConnection() {
        return myProfileConnection;
    }

    public String getIdentity() {
        return identity;
    }

    private RTCDataChannel environmentChannel;

    private RTCDataChannel mediaChannel;

    private RTCDataChannel dataTransferChannel;

    private boolean authenticated = false;

    public boolean authenticated() {
        return authenticated;
    }

    private void createEnvironmentChannel(RTCDataChannel dataChannel) {
        if (dataChannel == null) {
            RTCDataChannelInit controlChannel = new RTCDataChannelInit();
            controlChannel.negotiated = false;
            controlChannel.priority = RTCPriorityType.MEDIUM;
            controlChannel.ordered = true;

            this.environmentChannel = this.peerConnection.createDataChannel(ENVIRONMENT_CHANNEL_ID, controlChannel);
        } else {
            this.environmentChannel = dataChannel;
        }
        log.info("Environment channel created.");
        this.environmentChannel.registerObserver(new RTCDataChannelObserver() {
            @Override
            public void onBufferedAmountChange(long previousAmount) {
                log.info("Data channel onBufferedAmountChange({})", previousAmount);
            }

            @Override
            public void onStateChange() {
                log.info("Data channel state changed to: {}", environmentChannel.getState());
                switch (environmentChannel.getState()) {
                    case OPEN -> {
                        lastInteractionTime = System.currentTimeMillis();
                        if (publicShare) {
                            authenticated = true;

                            AuthenticationRes authRes = new AuthenticationRes();
                            authRes.setRequestId(-1);
                            authRes.setAuthenticated(true);
                            authRes.setPermissions(share.getSharePermissions());

                            try {
                                sendEnvironmentMessage(authRes);
                            } catch (Throwable e) {
                                log.error(e.getMessage(), e);
                            }
                        } else {
                            try {
                                sendAuthenticationRequired(false);
                            } catch (Throwable e) {
                                log.error(e.getMessage(), e);
                            }
                        }
                    }
                    case CLOSED -> {
                        RENEW_TOKEN.cancel();
                        onConnectionChange(RTCPeerConnectionState.DISCONNECTED);//double sending of DISCONNECTED STATE
                        //stop();// lock svm shutdown - cause portal wrtc do stop clients
                    }
                }
            }

            @Override
            public void onMessage(RTCDataChannelBuffer buffer) {
                lastInteractionTime = System.currentTimeMillis();
                try {
                    String message;
                    if (buffer.binary) {
                        long transmissionId = buffer.data.getLong();
                        int batchIndex = buffer.data.getInt();
                        byte[] data = new byte[buffer.data.remaining()];
                        buffer.data.get(data);
                        try {
                            message = GZIPUtils.decompress(data);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        message = Charset.defaultCharset()
                                .decode(buffer.data)
                                .toString();
                    }
                    log.info("Environment {} Received message:\n{}", environmentUUID, message);

                    final AbstractMessage request = AbstractMessage.from(message);

                    if (request instanceof AbstractEvent evt) {
                        if (authenticated) {
                            engine.fireEvent(evt);
                        }
                    } else {
                        if (request instanceof AbstractChannelMessage req) {
                            if (req.isAsyncAllowed()) {
                                SystemThreadsService.execute(this, () -> {
                                    log.debug("Start to process message async:\n{}", message);
                                    try {
                                        executeCommand(req);
                                    } catch (Throwable e) {
                                        log.error(e.getMessage(), e);
                                    }
                                });
                            } else {
                                log.debug("Start to process message sync:\n{}", message);
                                executeCommand(req);
                            }
                        }
                    }
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                }
            }
        });
    }

    private void fireStateEvent(ConnectionStateEvent.State state, String accountEmail) {
        engine.fireEvent(new ConnectionStateEvent(
                NodeDescriptor.NodeType.THING,
                PortalWebRTCIntegrationConfiguration.WEB_RTC_UUID,
                PortalWebRTCIntegrationThing.class.getName(),
                PortalWebRTCIntegrationThing.class.getName(),
                this.transactionId,
                this.publicShare ? ConnectionStateEvent.ConnectionType.PUBLIC : ConnectionStateEvent.ConnectionType.ACCOUNT,
                state,
                accountEmail
        ));
    }

    private void executeCommand(AbstractChannelMessage request) throws Exception {
        try {
            if (!this.publicShare) {
                if (!authenticated) {
                    if (request instanceof AuthenticationPasswordReq || request instanceof AuthenticationTokenReq) {
                        AuthenticationRes authRes = new AuthenticationRes();
                        authRes.setRequestId(request.getRequestId());

                        boolean authenticated;
                        boolean wasPasswordAuth;
                        if (request instanceof AuthenticationPasswordReq pwdAuth) {
                            String password = pwdAuth.getPassword();
                            if (share == null) {
                                authenticated = password != null && engine.verifyPassword(password);
                            } else {
                                authenticated = password != null && Utils.md5Hash(password).equals(share.getMd5Password());
                            }
                            wasPasswordAuth = true;
                        } else {
                            AuthenticationTokenReq tokenAuth = (AuthenticationTokenReq) request;
                            authenticated = this.webRTCIntegrationThing.hasAccessToken(tokenAuth.getToken());
                            wasPasswordAuth = false;
                        }
                        if (authenticated) {// if password valid or token exist and not expired - auth success
                            this.authenticated = true;
                            authRes.setAuthenticated(true);
                            authRes.setPermissions(share == null ? null : share.getSharePermissions());
                            currentToken = this.webRTCIntegrationThing.createEnvironmentAccessToken(identity);
                            authRes.setToken(currentToken);
                            sendEnvironmentMessage(authRes);
                            SYSTEM_TIMER.schedule(RENEW_TOKEN, 60000, 60000);
                            fireStateEvent(
                                    wasPasswordAuth ? ConnectionStateEvent.State.AUTH_PASSWORD_SUCCESS : ConnectionStateEvent.State.AUTH_TOKEN_SUCCESS,
                                    identity
                            );
                            createDataTransferChannel();
                            createMediaChannel();
                        } else {            // otherwise on invalid token or password
                            if (request instanceof AuthenticationPasswordReq pwdAuth) {//if was password auth then reject connection
                                authRes.setAuthenticated(false);
                                sendEnvironmentMessage(authRes);
                                fireStateEvent(ConnectionStateEvent.State.AUTH_REJECTED_INVALID_PASSWORD, identity);
                            } else if (request instanceof AuthenticationTokenReq) {// if was token auth then request password input
                                fireStateEvent(ConnectionStateEvent.State.AUTH_TOKEN_EXPIRED, identity);
                                sendAuthenticationRequired(true);
                            }
                        }
                        return;
                    } else {
                        fireStateEvent(ConnectionStateEvent.State.AUTH_REJECTED, identity);
                        environmentChannel.close();
                        throw new Exception("Request not authorized.");
                    }
                }

                if (request instanceof AuthenticationTokenRenewRes renewConfirmation) {
                    if (currentToken.equals(renewConfirmation.getOldToken())) {
                        this.webRTCIntegrationThing.removeAccessToken(currentToken);
                        currentToken = newToken;
                        newToken = null;
                    }
                    return;
                }
            } else {
                createMediaChannel();
            }

            if (request instanceof AbstractNodeRequest tr) {
                if (share != null) {// if null grant all permissions
                    Map<UUID, SharePermission> sharePermissions = share.getSharePermissions();
                    if (tr instanceof MediaChannelCreateReq mcr) {// todo custom case media channel request contains taskUUID
                        UUID taskUuid = mcr.getTaskUuid();
                        ITask<?> task = engine.findTask(taskUuid);
                        if (task instanceof MediaCaptureCallbackSupport mccs) {
                            Thing<?> sourceThing = mccs.getSourceThing();
                            if (sourceThing == null) {
                                throw new Exception("mediaAccessDenied");
                            } else {
                                SharePermission permission = sharePermissions.get(sourceThing.getUuid());
                                if (permission == null || !permission.generalPermissions.contains(PERMISSION_VIDEO)) {
                                    throw new Exception("mediaAccessDenied");
                                }
                                if (!permission.generalPermissions.contains(PERMISSION_AUDIO)) {
                                    mcr.setRequestedAudio(false);
                                }
                            }
                        }
                    } else {
                        UUID nodeUuid = tr.getNodeUuid();
                        SharePermission permission = null;
                        boolean anonymousNode = false;
                        boolean isTargetAction = false;
                        if (nodeUuid != null) {
                            Thing<?> thing = engine.getThing(nodeUuid);
                            if (thing == null) {//if not thing then task
                                ITask<?> task = engine.findTask(nodeUuid);
                                thing = task.getSourceThing();//extract source thing
                                nodeUuid = thing.getUuid();
                                isTargetAction = true;
                            }

                            permission = sharePermissions.get(nodeUuid);
                        } else {
                            if (tr instanceof TaskConfigurationDescriptorReq tcdr) {
                                if (tcdr.getNodeUuid() == null) {
                                    anonymousNode = true;
                                }
                            } else if (tr instanceof SaveTaskReq str) {
                                UUID parentUuid = str.getParentTaskUuid();
                                ITask<?> task = engine.findTask(parentUuid);
                                Thing<?> thing = task.getSourceThing();//extract source thing
                                nodeUuid = thing.getUuid();
                                permission = sharePermissions.get(nodeUuid);
                            }
                        }
                        if (!anonymousNode) {
                            if (permission == null) {
                                throw new Exception("accessDenied");
                            }
                            if (tr instanceof TaskConfigurationDescriptorReq || tr instanceof ThingConfigurationDescriptorReq) {
                                if (!permission.generalPermissions.contains(PERMISSION_READ)) {
                                    throw new Exception("readDenied");
                                }
                            }
                            if (tr instanceof SaveThingReq || tr instanceof SaveTaskReq) {
                                if (!permission.generalPermissions.contains(PERMISSION_UPDATE)) {
                                    throw new Exception("updateDenied");
                                }
                            }
                            if (tr instanceof FireActionReq) {
                                if (!permission.generalPermissions.contains(PERMISSION_ACTION)) {
                                    throw new Exception("actionDenied");
                                }
                            }
                        }

                        if (tr instanceof ThingApiCallReq callReq) {
                            String methodName = (String) callReq.getParams().get(PARAM_METHOD);
                            boolean hasAccess = false;
                            for (String genPerm : permission.generalPermissions) {
                                if (genPerm.endsWith("*")) {
                                    String allowedMethodPrefix = genPerm.substring(0, genPerm.length() - 1);
                                    hasAccess |= methodName.startsWith(allowedMethodPrefix);
                                } else {
                                    hasAccess |= genPerm.equals(methodName);
                                }
                            }
                            if (!hasAccess) {
                                for (String apiPerm : permission.apiMethodsPermissions) {
                                    if (apiPerm.endsWith("*")) {
                                        String allowedMethodPrefix = apiPerm.substring(0, apiPerm.length() - 1);
                                        hasAccess |= methodName.startsWith(allowedMethodPrefix);
                                    } else {
                                        hasAccess |= apiPerm.equals(methodName);
                                    }
                                }
                            }
                            if (!hasAccess) {
                                throw new Exception("apiCallDenied");
                            }
                        }
                    }
                }
            }
            getExecutorService(this).submit(() -> {
                try {
                    UserThreadContext.init(RTCClient.this, identity, share);
                    ChannelRequestHandler channelRequestHandler =
                            environmentChannelRequestHandlerMap
                                    .get(ENVIRONMENT_CHANNEL_ID)
                                    .get(request.getClass());
                    if (channelRequestHandler == null) {
                        throw new Exception("No handler found for message: " + request.toJson());
                    }
                    AbstractChannelMessage response = channelRequestHandler.handle(request);
                    if (response != null) {
                        sendEnvironmentMessage(response);
                    }
                } catch (Throwable e) {
                    processException(request.getRequestId(), e);
                    log.info(e.getMessage(), e);
                } finally {
                    UserThreadContext.clear();
                }
            });
        } catch (Throwable e) {
            processException(request.getRequestId(), e);
        }
    }

    private void sendAuthenticationRequired(boolean resetToken) throws Exception {
        AuthenticationRequired authenticationRequired = new AuthenticationRequired();
        authenticationRequired.setRequestId(-1);
        authenticationRequired.setEnvironmentUuid(environmentUUID);
        authenticationRequired.setResetToken(resetToken);
        sendEnvironmentMessage(authenticationRequired);

        fireStateEvent(ConnectionStateEvent.State.AUTH_REQUESTED, identity);
    }

    private void processException(int requestId, Throwable e) {
        log.error(e.getMessage(), e);
        ExceptionMessage error = new ExceptionMessage();
        error.setRequestId(requestId);
        if (e instanceof LocalizedException le) {
            error.setMessage(le.getI18n());
            error.setArgs(le.getArgs());
        } else {
            error.setMessage(e.getMessage());
            error.setStackTrace(e.getStackTrace());
        }
        try {
            sendEnvironmentMessage(error);
        } catch (Throwable ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    public void sendEnvironmentMessage(AbstractChannelMessage message) throws Exception {
        String json = message.toJson();
        byte[] bytes = GZIPUtils.compress(json.getBytes(StandardCharsets.UTF_8));
        sendMessageToChannel(message.getRequestId(), environmentChannel, bytes, true);
    }

    public void sendEvent(AbstractEvent event) throws Exception {
        log.info("Send event to environment {}:\n{}", environmentUUID, event.toJson());

        String json = event.toJson();
        byte[] bytes = GZIPUtils.compress(json.getBytes(StandardCharsets.UTF_8));
        sendMessageToChannel(-1, environmentChannel, bytes, true);
    }

    private void sendMessageToChannel(int requestId, RTCDataChannel dataChannel, byte[] data, boolean binary) throws Exception {
        if (dataChannel == null || dataChannel.getState() != RTCDataChannelState.OPEN) {
            return;
        }
        lastInteractionTime = System.currentTimeMillis();
        ChannelsUtils.sendMessage(requestId, dataChannel, data);
    }

    public MediaChannelObserver mediaChannelObserver;

    private void createMediaChannel() {
        RTCDataChannelInit init = new RTCDataChannelInit();
        init.priority = RTCPriorityType.HIGH;
        init.ordered = true;

        this.mediaChannel = this.peerConnection.createDataChannel(MEDIA_TRANSFER_CHANNEL_ID, init);

        this.mediaChannelObserver = new MediaChannelObserver(this, this.engine, mediaChannel);
        mediaChannel.registerObserver(mediaChannelObserver);
    }

    private DataTransferChannelObserver dataTransferChannelObserver;

    private void createDataTransferChannel() {
        RTCDataChannelInit init = new RTCDataChannelInit();
        init.priority = RTCPriorityType.LOW;
        init.ordered = true;

        this.dataTransferChannel = this.peerConnection.createDataChannel(DATA_TRANSFER_CHANNEL_ID, init);
        this.dataTransferChannelObserver = new DataTransferChannelObserver(this, this.engine, this.dataTransferChannel);
        this.dataTransferChannel.registerObserver(this.dataTransferChannelObserver);
    }

    public void sendFile(UUID consumerUuid, String fileResourceId, long transmissionId, Thing.DownloadStream downloadStream) throws Exception {
        if (dataTransferChannel.getState() != RTCDataChannelState.OPEN) {
            throw new Exception("Data transmission channel not opened.");
        }
        SystemThreadsService.execute(this, () -> {
            try {
                File file = downloadStream.getFile();
                long contentSize = file.length();
                SeekableByteChannel randomAccessFile = Files.newByteChannel(file.toPath(), StandardOpenOption.READ);
                this.dataTransferChannelObserver.downloadTransmissionsMap.put(transmissionId, new DataTransferChannelObserver.TransmissionInfo(
                        transmissionId,
                        file,
                        randomAccessFile,
                        contentSize,
                        new ArrayList<>()
                ));

                ChannelsUtils.sendStreamCommand(
                        dataTransferChannel,
                        "transmission-start;" + consumerUuid + ";" + downloadStream.getMimeType() + ";" + contentSize + ";" + fileResourceId + ";" + transmissionId + ";" + downloadStream.getCacheTtlMillis()
                );
                log.info("Transmission of {} started.", file.getName());
            } catch (Throwable e) {
                log.error("Error on file transmission via web rtc", e);
//                            closeFileChannel();
            }
        });
    }

    /**
     * Creation of the stub channel need to move peer connection to opened state
     * and after this open Ennvironment channel on Remote connection not established outside local network,
     * cause not fires ondatachannel event when first channel cretaed.
     * This fix allow to open rtc connection and create data channel when stub channel moved to OPEN state
     */
    @Override
    public void onDataChannel(RTCDataChannel dataChannel) {
        String label = dataChannel.getLabel();
        log.info("OnDataChannel {}: {}\nnegotiated: {}\nreliable: {}\nordered: {}",
                dataChannel.getLabel(), environmentUUID,
                dataChannel.isNegotiated(), dataChannel.isReliable(),
                dataChannel.isOrdered()
        );
        if (ENVIRONMENT_CHANNEL_ID.equals(label)) {
            this.createEnvironmentChannel(dataChannel);
        }
    }

    private final List<Consumer<IceCandidate>> iceCandidateConsumer = new ArrayList<>();

    public void addIceCandidateConsumer(Consumer<IceCandidate> consumer) {
        iceCandidateConsumer.add(consumer);
    }

    @Override
    public void onIceCandidate(RTCIceCandidate candidate) {
        for (Consumer<IceCandidate> consumer : iceCandidateConsumer) {
            IceCandidate message = new IceCandidate(
                    candidate.sdp, candidate.sdpMid, candidate.sdpMLineIndex
            );
            message.setToAgentUuid(this.environmentUUID);
            message.clientWebSocketSession = this.transactionId;
            consumer.accept(message);
        }
    }

    @Override
    public void onIceCandidateError(RTCPeerConnectionIceErrorEvent event) {
        log.error("ICECandidate Error:\nurl:{}\naddr:{}\nport:{}\nError code:{}\nError text:{}", event.getUrl(), event.getAddress(), event.getPort(), event.getErrorCode(), event.getErrorText());
    }

    public void start() {
    }

    public synchronized void stop() {
        log.info("Closing peer connection ({}): {}", this.peerConnection.getConnectionState(), transactionId);
        if (
                this.peerConnection.getConnectionState() == RTCPeerConnectionState.NEW
                        || this.peerConnection.getConnectionState() == RTCPeerConnectionState.CONNECTED
                        || this.peerConnection.getConnectionState() == RTCPeerConnectionState.CONNECTING
        ) {
            this.peerConnection.close();
            log.info("Peer connection closed: {}", transactionId);
        }
    }

    private final List<PeerConnectionListener> peerConnectionListeners = new ArrayList<>(2);

    public void addPeerConnectionListener(PeerConnectionListener litener) {
        this.peerConnectionListeners.add(litener);
    }

    @Override
    public void onConnectionChange(RTCPeerConnectionState state) {
        for (PeerConnectionListener pcl : peerConnectionListeners) {
            switch (state) {
                case NEW -> pcl.onNew(PeerConnectionListener.ConnectionEvent.of(this));
                case CONNECTING -> pcl.onConnecting(PeerConnectionListener.ConnectionEvent.of(this));
                case CONNECTED -> pcl.onConnected(PeerConnectionListener.ConnectionEvent.of(this));
                case CLOSED -> pcl.onClosed(PeerConnectionListener.ConnectionEvent.of(this));
                case DISCONNECTED -> pcl.onDisconnected(PeerConnectionListener.ConnectionEvent.of(this));
                case FAILED -> pcl.onFailed(PeerConnectionListener.ConnectionEvent.of(this));
            }
        }
    }

    public void addIceCandidate(IceCandidate candidate) {
        try {
            if (StringUtils.isNotEmpty(candidate.candidate)) {
                peerConnection.addIceCandidate(
                        new RTCIceCandidate(candidate.sdpMid, candidate.sdpMLineIndex, candidate.candidate)
                );
            }
        } catch (Throwable e) {
            log.error("Invalid ICE Candidate: {}/n{}", e.getMessage(), candidate);
            throw e;
        }
    }
}
