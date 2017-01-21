package com.github.athenaengine.core.dispatcher.events;

import com.github.athenaengine.core.enums.ListenerType;
import com.github.athenaengine.core.model.entity.Player;

public class OnDeathEvent extends ListenerEvent {

    private final Player mTarget;

    public OnDeathEvent(Player target) {
        mTarget = target;
    }

    public Player getTarget() {
        return mTarget;
    }

    public ListenerType getType() {
        return ListenerType.ON_DEATH;
    }
}
