package com.banalytics.box.api.integration.webrtc.channel;

import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(callSuper = true)
public class SimpleNodeDescriptor {
    private UUID uuid;
    private String className;
    private String title;


}
