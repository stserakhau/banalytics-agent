package com.banalytics.box.api.integration.webrtc.channel.events.peer;

import com.banalytics.box.api.integration.MessageType;
import com.banalytics.box.api.integration.form.ComponentType;
import com.banalytics.box.api.integration.form.annotation.UIComponent;
import com.banalytics.box.api.integration.webrtc.channel.NodeDescriptor;
import com.banalytics.box.api.integration.webrtc.channel.events.AbstractEvent;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.text.StringSubstitutor;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
public class ConnectionStateEvent extends AbstractEvent {
    private String transactionId;

    @UIComponent(
            index = 10, type = ComponentType.multi_select
    )
    public ConnectionType connectionType;

    @UIComponent(
            index = 20, type = ComponentType.multi_select
    )
    public State state;

    @UIComponent(
            index = 30, type = ComponentType.multi_select,
            uiConfig = {
                    @UIComponent.UIConfig(name = "sort", value = "asc"),
                    @UIComponent.UIConfig(name = "api-uuid", value = "00000000-0000-0000-0000-000000000005"),
                    @UIComponent.UIConfig(name = "api-method", value = "readAccounts")
            }
    )
    public String email;

    @Override
    public String textView() {
        return StringSubstitutor.replace(
                """
                *${ct} / ${state}:* ${email}
                tx: ${txId}
                """,
                Map.of(
                        "ct", this.connectionType,
                        "email", this.getEmail(),
                        "state", this.getState(),
                        "txId", this.getTransactionId()
                )
        );
    }

    public ConnectionStateEvent() {
        super(MessageType.EVT_CONNECTION_STATE);
    }

    public ConnectionStateEvent(NodeDescriptor.NodeType nodeType, UUID nodeUuid, String nodeClassName, String nodeTitle, String transactionId, ConnectionType connectionType, State state, String email) {
        super(MessageType.EVT_CONNECTION_STATE, nodeType, nodeUuid, nodeClassName, nodeTitle);
        this.transactionId = transactionId;
        this.connectionType = connectionType;
        this.state = state;
        this.email = email;
    }

    public enum ConnectionType {
        ACCOUNT, PUBLIC
    }


    public enum State {
        CONNECTING,
        CONNECTED,
        CONNECTION_FAILED,
        DISCONNECTED,

        AUTH_REQUESTED,
        AUTH_TOKEN_EXPIRED,
        AUTH_PASSWORD_SUCCESS,
        AUTH_TOKEN_SUCCESS,
        AUTH_REJECTED,
        AUTH_REJECTED_INVALID_PASSWORD
    }
}
