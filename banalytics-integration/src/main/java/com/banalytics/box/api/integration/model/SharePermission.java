package com.banalytics.box.api.integration.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class SharePermission {
    public Set<String> generalPermissions = new HashSet<>();
    public Set<String> apiMethodsPermissions = new HashSet<>();
}
