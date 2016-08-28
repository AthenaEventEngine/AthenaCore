package com.github.u3games.eventengine.dispatcher.events;

import com.github.u3games.eventengine.enums.ListenerType;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.items.L2Item;

public class OnUseItemEvent extends ListenerEvent {

    private final L2PcInstance mPlayer;
    private final L2Item mItem;

    public OnUseItemEvent(L2PcInstance player, L2Item item) {
        mPlayer = player;
        mItem = item;
    }

    public L2PcInstance getPlayer() {
        return mPlayer;
    }

    public L2Item getItem() {
        return mItem;
    }

    public ListenerType getType() {
        return ListenerType.ON_USE_ITEM;
    }
}
