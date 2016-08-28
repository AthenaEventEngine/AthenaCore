package com.github.u3games.eventengine.dispatcher.events;

import com.github.u3games.eventengine.enums.ListenerType;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

public class OnLogInEvent extends ListenerEvent {

    private final L2PcInstance mPlayer;

    public OnLogInEvent(L2PcInstance player) {
        mPlayer = player;
    }

    public L2PcInstance getPlayer() {
        return mPlayer;
    }

    public ListenerType getType() {
        return ListenerType.ON_LOG_IN;
    }
}
