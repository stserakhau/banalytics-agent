package com.banalytics.box.api.integration;

import com.banalytics.box.api.integration.environment.*;
import com.banalytics.box.api.integration.suc.EnvironmentModuleUpdateStatusEvent;
import com.banalytics.box.api.integration.suc.GetEnvironmentModulesRequest;
import com.banalytics.box.api.integration.suc.GetEnvironmentModulesResponse;
import com.banalytics.box.api.integration.suc.SynchronizeSoftwareEvent;
import com.banalytics.box.api.integration.webrtc.*;
import com.banalytics.box.api.integration.webrtc.channel.ExceptionMessage;
import com.banalytics.box.api.integration.webrtc.channel.callback.ThingCallbackReq;
import com.banalytics.box.api.integration.webrtc.channel.callback.ThingCallbackRes;
import com.banalytics.box.api.integration.webrtc.channel.environment.*;
import com.banalytics.box.api.integration.webrtc.channel.environment.auth.*;
import com.banalytics.box.api.integration.webrtc.channel.events.*;
import com.banalytics.box.api.integration.webrtc.channel.events.measurement.*;
import com.banalytics.box.api.integration.webrtc.channel.events.measurement.gamepad.GPAxisChangeEvent;
import com.banalytics.box.api.integration.webrtc.channel.events.measurement.gamepad.GPButtonChangeEvent;
import com.banalytics.box.api.integration.webrtc.channel.events.measurement.gamepad.GamePadStateChangedEvent;
import com.banalytics.box.api.integration.webrtc.channel.events.peer.ConnectionStateEvent;
import com.banalytics.box.api.integration.websocket.YourSessionId;

public enum MessageType {

    WS_YOUR_SESSION_ID(YourSessionId.class),

    ENV_STATUS_MSG(EnvironmentStatusMessage.class),

    ACC_STATUS_MSG(AccountStatusMessage.class),

    /**
     * Pair process Environment with Portal Account
     */
    REGISTER_ME_REQ(RegisterMeRequest.class),
    READY_TO_LINK(ReadyToLinkResponse.class),


    ENV_PWD_RST_MSG(EnvironmentPasswordResetMessage.class),
    /**
     * Share / Unshare access to environment things
     */
    ENV_SHARE_WITH_REQ(EnvironmentShareWithReq.class), ENV_SHARE_WITH_RES(EnvironmentShareWithRes.class),
    ENV_UN_SHARE_WITH_REQ(EnvironmentUnShareWithReq.class), ENV_UN_SHARE_WITH_RES(EnvironmentUnShareWithRes.class),

    /**
     * Share / Unshare public access to environment things
     */
    ENV_PUB_SHARE_REQ(EnvironmentPublicShareReq.class), ENV_PUB_SHARE_RES(EnvironmentPublicShareRes.class),
    ENV_PUB_UN_SHARE_REQ(EnvironmentPublicUnShareReq.class), ENV_PUB_UN_SHARE_RES(EnvironmentPublicUnShareRes.class),

    /**
     * Upgrade software process
     */
    EVT_RECONNECT(ReconnectEvent.class),
    EVT_REBOOT(RebootEvent.class),
    EVT_SYNC_SOFT(SynchronizeSoftwareEvent.class),
    EVT_ENV_MOD_UPD_STS(EnvironmentModuleUpdateStatusEvent.class),
    GET_ENV_MOD_REQ(GetEnvironmentModulesRequest.class), GET_ENV_MOD_RES(GetEnvironmentModulesResponse.class),

    offer(Offer.class),
    answer(Answer.class),
    candidate(IceCandidate.class),
    ready(Ready.class),
    bye(Bye.class),

    /**
     * Channel messages
     */
    ENV_DESCR_REQ(EnvironmentDescriptorReq.class), ENV_DESCR_RES(EnvironmentDescriptorRes.class),

    THNG_DSCR_REQ(ThingDescriptionReq.class), THNG_DSCR_RES(ThingDescriptionRes.class),

    I18N_REQ(I18NReq.class), I18N_RES(I18NRes.class),

    THNGS_GRPS_REQ(ThingsGroupsReq.class), THNGS_GRPS_RES(ThingsGroupsRes.class),
    THNGS_REQ(ThingsReq.class), ISSUES_REQ(IssuesReq.class), THNGS_RES(ThingsRes.class),
    THNG_DSCVR_REQ(ThingDiscoveryReq.class), THNG_DSCVR_RES(ThingDiscoveryRes.class),
    THNG_TSKS_REQ(ThingTasksReq.class), THNG_TSKS_RES(ThingTasksRes.class),

    SBTSK_REQ(SubTasksReq.class), SBTSK_RES(SubTasksRes.class),

    THNG_CNF_DSCR_REQ(ThingConfigurationDescriptorReq.class), THNG_CNF_DSCR_RES(ThingConfigurationDescriptorRes.class),
    TSK_CNF_DSCR_REQ(TaskConfigurationDescriptorReq.class), TSK_CNF_DSCR_RES(TaskConfigurationDescriptorRes.class),

    AVL_THNG_CLSS_REQ(AvailableThingClassesReq.class), AVL_THNG_CLSS_RES(AvailableThingClassesRes.class),

    AVL_SNGL_THNG_CLSS_REQ(AvailableSingletonThingClassesReq.class), AVL_SNGL_THNG_CLSS_RES(AvailableSingletonThingClassesRes.class),

    SAVE_THNG_REQ(SaveThingReq.class), SAVE_THNG_RES(SaveThingRes.class),

    DEL_THNG_REQ(DeleteThingReq.class), DEL_THNG_RES(DeleteThingRes.class),


    AVL_ACT_TSK_CLSS_REQ(AvailableActionTaskClassesReq.class), AVL_ACT_TSK_CLSS_RES(AvailableActionTaskClassesRes.class),

    AVL_TSK_CLSS_REQ(AvailableTaskClassesReq.class), AVL_TSK_CLSS_RES(AvailableTaskClassesRes.class),

    FIND_ACT_TSKS_REQ(FindActionTasksReq.class), FIND_ACT_TSKS_RES(FindActionTasksRes.class),

    SAVE_TSK_REQ(SaveTaskReq.class), SAVE_TSK_RES(SaveTaskRes.class),

    DEL_TSK_REQ(DeleteTaskReq.class), DEL_TSK_RES(DeleteTaskRes.class),

    THNG_API_CALL_REQ(ThingApiCallReq.class), THNG_API_CALL_RES(ThingApiCallRes.class),

    STRT_TSK_REQ(StartTaskReq.class),
    STP_TSK_REQ(StopTaskReq.class),
    STRT_THNG_REQ(StartThingReq.class),
    STP_THNG_REQ(StopThingReq.class),
    FIRE_ACTION_REQ(FireActionReq.class),
    EMPT_RES(EmptyRes.class),

    /**
     * Thing callback channel message (Thing Api call)
     */
    THNG_CLBK_REQ(ThingCallbackReq.class), THNG_CLBK_RES(ThingCallbackRes.class),

    /**
     * Request Media channel
     */
    MEDIA_CHNL_CRT_REQ(MediaChannelCreateReq.class), MEDIA_CHNL_CRT_RES(MediaChannelCreateRes.class),


    /**
     * Event types
     */
    EVT_STATUS(StatusEvent.class),
    EVT_ACTION(ActionEvent.class),
    EVT_CONNECTION_STATE(ConnectionStateEvent.class),
    EVT_MOTION(MotionEvent.class),
    EVT_SOUND(SoundEvent.class),
    EVT_SYS_MON(SystemMonitorEvent.class),
    EVT_FILE_CRT(FileCreatedEvent.class),

    /**
     * System events
     */
    EVT_SYS_KEYB(KeyboardEvent.class), EVT_SYS_GPS(GPSEvent.class),
    EVT_SYS_GYRO(GyroscopeEvent.class), EVT_SYS_GRAVITY(GravityEvent.class),

    /**
     * Gamepad events
     */
    EVT_SYS_GAMEPAD_SCH(GamePadStateChangedEvent.class),//client level
    EVT_SYS_GAMEPAD_AXS(GPAxisChangeEvent.class),//agent level
    EVT_SYS_GAMEPAD_BTN(GPButtonChangeEvent.class),//agent level

    /**
     * Error Types
     */
    ERROR(ExceptionMessage.class),

    /**
     * Authentication messages
     */
    AUTH_REQUIRED(AuthenticationRequired.class),
    AUTH_PWD_REQ(AuthenticationPasswordReq.class),
    AUTH_TKN_REQ(AuthenticationTokenReq.class),
    AUTH_TKN_RENEW_REQ(AuthenticationTokenRenewReq.class),
    AUTH_TKN_RENEW_RES(AuthenticationTokenRenewRes.class),
    AUTH_RES(AuthenticationRes.class);

    public final Class<? extends AbstractMessage> clazz;

    MessageType(Class<? extends AbstractMessage> clazz) {
        this.clazz = clazz;
    }
}
