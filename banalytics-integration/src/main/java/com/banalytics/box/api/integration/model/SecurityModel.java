package com.banalytics.box.api.integration.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Getter
@Setter
public class SecurityModel {
    private Map<String, Share> accountShare = new HashMap<>();
    private Map<String, Share> userGroup = new HashMap<>();
    private Map<String, Share> publicShare = new HashMap<>();

    public Map<String, Share> targetNodeValues(TargetNode targetNode) {
        return targetNode.value(this);
    }

    public boolean hasPublicShare(String token) {
        for (Share share : publicShare.values()) {
            if(share.getIdentity().equals(token)) {
                return true;
            }
        }
        return false;
    }

    public enum TargetNode {
        accountShare(securityModel -> securityModel.accountShare),
        userGroup(securityModel -> securityModel.userGroup),
        publicShare(securityModel -> securityModel.publicShare);

        final Function<SecurityModel, Map<String, Share>> func;

        TargetNode(Function<SecurityModel, Map<String, Share>> func) {
            this.func = func;
        }

        public Map<String, Share> value(SecurityModel sm){
            return func.apply(sm);
        }
    }

    public Share accountShareGroupsOverride(String accountEmail) {
        Share nativeShare = accountShareNative(accountEmail);

        Share result = new Share();
        result.setMd5Password(nativeShare.getMd5Password());
        result.setIdentity(nativeShare.getIdentity());

        for (String groupName : nativeShare.getGroups()) {
            Share groupShare = this.userGroup.get(groupName);
            mergePermissions(groupShare.sharePermissions, result.sharePermissions);
        }

        mergePermissions(nativeShare.sharePermissions, result.sharePermissions);

        return result;
    }

    public Share accountPublicShareGroupsOverride(String token) {
        Share nativeShare = accountPublicShareNative(token);

        Share result = new Share();
        result.setMd5Password(nativeShare.getMd5Password());
        result.setIdentity(nativeShare.getIdentity());
        result.setTitle(nativeShare.getTitle());

        for (String groupName : nativeShare.getGroups()) {
            Share groupShare = this.userGroup.get(groupName);
            mergePermissions(groupShare.sharePermissions, result.sharePermissions);
        }

        mergePermissions(nativeShare.sharePermissions, result.sharePermissions);

        return result;
    }

    private void mergePermissions(Map<UUID, SharePermission> src, Map<UUID, SharePermission> dest) {
        for (Map.Entry<UUID, SharePermission> entry : src.entrySet()) {
            UUID uuid = entry.getKey();
            SharePermission sharePerm = entry.getValue();
            SharePermission merge = dest.computeIfAbsent(uuid, a -> new SharePermission());
            merge.generalPermissions.addAll(sharePerm.generalPermissions);
            merge.apiMethodsPermissions.addAll(sharePerm.apiMethodsPermissions);
        }
    }

    public Share accountShareNative(String accountEmail) {
        return accountShare.get(accountEmail);
    }

    public Share accountPublicShareNative(String token) {
        for (Share share : publicShare.values()) {
            if(share.getIdentity().equals(token)) {
                return share;
            }
        }
        throw new RuntimeException("Share not found");
    }
}
