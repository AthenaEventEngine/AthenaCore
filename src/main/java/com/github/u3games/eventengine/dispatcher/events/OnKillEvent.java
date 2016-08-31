package com.github.u3games.eventengine.dispatcher.events;

import com.github.u3games.eventengine.enums.ListenerType;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Playable;

public class OnKillEvent extends ListenerEvent {

    private final L2Playable mAttacker;
    private final L2Character mTarget;

    public OnKillEvent(L2Playable attacker, L2Character target) {
        mAttacker = attacker;
        mTarget = target;
    }

    public L2Playable getAttacker() {
        return mAttacker;
    }

    public L2Character getTarget() {
        return mTarget;
    }

    public ListenerType getType() {
        return ListenerType.ON_KILL;
    }
}
