package com.github.athenaengine.core.dispatcher.events;

import com.github.athenaengine.core.enums.ListenerType;
import com.github.athenaengine.core.model.entity.Npc;
import com.github.athenaengine.core.model.entity.Player;

public class OnInteractEvent extends ListenerEvent {

    private final Player mPlayer;
    private final Npc mNpc;

    public OnInteractEvent(Player player, Npc npc) {
        mPlayer = player;
        mNpc = npc;
    }

    public Player getPlayer() {
        return mPlayer;
    }

    public Npc getNpc() {
        return mNpc;
    }

    public ListenerType getType() {
        return ListenerType.ON_INTERACT;
    }
}
