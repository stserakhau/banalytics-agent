package com.banalytics.box.api.integration.model;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class Share {
    /**
     * <li>Account email for account share</li>
     * <li>Group name for user group</li>
     * <li>Token string for public share</li>
     */
    String identity;

    String title;

    /**
     * md5 hash for account password share, for another cases contains null
     */
    String md5Password;

    Set<String> groups = new HashSet<>();

    Map<UUID, SharePermission> sharePermissions = new HashMap<>();
}
