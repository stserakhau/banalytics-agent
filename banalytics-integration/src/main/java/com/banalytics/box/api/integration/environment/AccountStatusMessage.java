package com.banalytics.box.api.integration.environment;

import com.banalytics.box.api.integration.AbstractMessage;
import com.banalytics.box.api.integration.MessageType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
public class AccountStatusMessage extends AbstractMessage {

    /**
     * if null or empty it means that is success state
     */
    Set<AccountState> states;

    /**
     * Message to user browser or to environment
     */
    String message;


    public AccountStatusMessage() {
        super(MessageType.ACC_STATUS_MSG);
    }

    public AccountStatusMessage(Set<AccountState> states) {
        this();
        this.states = states;
    }

    public AccountStatusMessage(String message) {
        this();
        this.message = message;
    }

    public AccountStatusMessage(Set<AccountState> states, String message) {
        this();
        this.states = states;
        this.message = message;
    }

    public enum AccountState {
        NOT_ASSIGNED, EMPTY_BALANCE
    }
}
