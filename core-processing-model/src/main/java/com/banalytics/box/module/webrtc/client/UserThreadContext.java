package com.banalytics.box.module.webrtc.client;

import com.banalytics.box.api.integration.model.Share;
import com.banalytics.box.api.integration.model.SharePermission;

import java.util.Map;
import java.util.UUID;

import static com.banalytics.box.module.Thing.*;

public class UserThreadContext {
    private static final ThreadLocal<RTCClient> rtcClient = new ThreadLocal<>();
    private static final ThreadLocal<String> myAccountEmail = new ThreadLocal<>();
    private static final ThreadLocal<Share> shareDetailsHolder = new ThreadLocal<>();

    public static void init(RTCClient rtcClient_, String accountEmail, Share share) {
        rtcClient.set(rtcClient_);
        myAccountEmail.set(accountEmail);
        shareDetailsHolder.set(share);
    }

    public static boolean isMyEnvironment() {
        return shareDetailsHolder.get() == null;
    }

    public static RTCClient rtcClient() {
        return rtcClient.get();
    }

    public static String myAccountEmail() {
        return myAccountEmail.get();
    }

    public static Share share() {
        return shareDetailsHolder.get();
    }

    public static boolean hasReadPermission(UUID thingUuid) {
        Share share = shareDetailsHolder.get();
        if (share == null) {
            return true;
        }
        Map<UUID, SharePermission> permissionMap = share.getSharePermissions();
        SharePermission perm = permissionMap.get(thingUuid);
        if (perm == null) {
            return false;
        }
        return perm.generalPermissions.contains(PERMISSION_READ);
    }

    public static boolean hasUpdatePermission(UUID thingUuid) {
        Share share = shareDetailsHolder.get();
        if (share == null) {
            return true;
        }
        Map<UUID, SharePermission> permissionMap = share.getSharePermissions();
        SharePermission perm = permissionMap.get(thingUuid);
        if (perm == null) {
            return false;
        }
        return perm.generalPermissions.contains(PERMISSION_UPDATE);
    }

    public static boolean hasStartStopPermission(UUID thingUuid) {
        Share share = shareDetailsHolder.get();
        if (share == null) {
            return true;
        }
        Map<UUID, SharePermission> permissionMap = share.getSharePermissions();
        SharePermission perm = permissionMap.get(thingUuid);
        if (perm == null) {
            return false;
        }
        return perm.generalPermissions.contains(PERMISSION_START_STOP);
    }

    public static boolean hasActionPermission(UUID thingUuid) {
        Share share = shareDetailsHolder.get();
        if (share == null) {
            return true;
        }
        Map<UUID, SharePermission> permissionMap = share.getSharePermissions();
        SharePermission perm = permissionMap.get(thingUuid);
        if (perm == null) {
            return false;
        }
        return perm.generalPermissions.contains(PERMISSION_ACTION);
    }

    public static void clear() {
        rtcClient.set(null);
        myAccountEmail.set(null);
        shareDetailsHolder.set(null);
    }
}
