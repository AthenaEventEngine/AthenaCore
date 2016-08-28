package com.github.u3games.eventengine.dispatcher.events;

import com.github.u3games.eventengine.enums.ListenerType;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

public class OnInteractEvent extends ListenerEvent {

    private final L2PcInstance mPlayer;
    private final L2Npc mNpc;

    public OnInteractEvent(L2PcInstance player, L2Npc npc) {
        mPlayer = player;
        mNpc = npc;
    }

    public L2PcInstance getPlayer() {
        return mPlayer;
    }

    public L2Npc getNpc() {
        return mNpc;
    }

    public ListenerType getType() {
        return ListenerType.ON_INTERACT;
    }
}
