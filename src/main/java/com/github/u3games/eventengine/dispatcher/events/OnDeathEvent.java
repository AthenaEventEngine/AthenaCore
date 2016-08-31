package com.github.u3games.eventengine.dispatcher.events;

import com.github.u3games.eventengine.enums.ListenerType;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

public class OnDeathEvent extends ListenerEvent {

    private final L2PcInstance mTarget;

    public OnDeathEvent(L2PcInstance target) {
        mTarget = target;
    }

    public L2PcInstance getTarget() {
        return mTarget;
    }

    public ListenerType getType() {
        return ListenerType.ON_DEATH;
    }
}
