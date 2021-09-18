package com.chy.summer.framework.boot.autoconfigure.condition;

import lombok.Getter;

@Getter
public class ConditionOutcome {

    private final boolean match;
    private final ConditionMessage message;

    public ConditionOutcome(boolean match, String message) {
        this.match = match;
        this.message = new ConditionMessage(message);
    }
}
