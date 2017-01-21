package com.github.athenaengine.core.dispatcher.events;

import com.github.athenaengine.core.enums.ListenerType;
import com.github.athenaengine.core.model.entity.Character;
import com.github.athenaengine.core.model.entity.Playable;

public class OnKillEvent extends ListenerEvent {

    private final Playable mAttacker;
    private final Character mTarget;

    public OnKillEvent(Playable attacker, Character target) {
        mAttacker = attacker;
        mTarget = target;
    }

    public Playable getAttacker() {
        return mAttacker;
    }

    public Character getTarget() {
        return mTarget;
    }

    public ListenerType getType() {
        return ListenerType.ON_KILL;
    }
}
