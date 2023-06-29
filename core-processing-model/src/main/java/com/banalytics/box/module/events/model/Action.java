package com.banalytics.box.module.events.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class Action {
//    UUID environmentUuid; //todo for remote anv action - probably need to do separated RemoteAction
    UUID taskUuid;
}
