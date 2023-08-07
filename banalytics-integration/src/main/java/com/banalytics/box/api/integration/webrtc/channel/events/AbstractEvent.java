package com.banalytics.box.api.integration.webrtc.channel.events;

import com.banalytics.box.api.integration.AbstractMessage;
import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.webrtc.channel.NodeDescriptor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.text.StringSubstitutor;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
public abstract class AbstractEvent extends AbstractMessage {
    private UUID messageUuid = UUID.randomUUID();
    private UUID environmentUuid;
    private NodeDescriptor.NodeType nodeType;
    private UUID nodeUuid;
    private String nodeClassName;
    private String nodeTitle;

    /**
     * Bold	                * x *	    We'll see you at *4PM*.	txtFormatting_01_200px.png
     * Italic	                _ x _	    Your driver has been _delayed_ until 6PM.	txtFormatting_02_200px.png
     * Strike-through	        ~ x ~	    We expect to see you at ~4PM~ 6PM.	txtFormatting_03_200px.png
     * Pre-formatted (Code)	``` x ```	Use the ```Message``` API to notify users.
     */
    public String textView() {
        return StringSubstitutor.replace(
                "*${type}* ${nodeTitle}",
                Map.of(
                        "type", this.type,
                        "nodeTitle", this.nodeTitle
                )
        );
    }

    public AbstractEvent(MessageType type) {
        super(type);
    }

    public AbstractEvent(MessageType type, NodeDescriptor.NodeType nodeType, UUID nodeUuid, String nodeClassName, String nodeTitle) {
        super(type);
        this.nodeType = nodeType;
        this.nodeUuid = nodeUuid;
        this.nodeClassName = nodeClassName;
        this.nodeTitle = nodeTitle;
    }
}
