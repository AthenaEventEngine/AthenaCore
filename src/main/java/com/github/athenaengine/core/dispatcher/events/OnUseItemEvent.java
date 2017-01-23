package com.github.athenaengine.core.dispatcher.events;

import com.github.athenaengine.core.enums.ListenerType;
import com.github.athenaengine.core.model.entity.Player;
import com.l2jserver.gameserver.model.items.L2Item;

public class OnUseItemEvent extends ListenerEvent {

    private final Player mPlayer;
    private final L2Item mItem;

    public OnUseItemEvent(Player player, L2Item item) {
        mPlayer = player;
        mItem = item;
    }

    public Player getPlayer() {
        return mPlayer;
    }

    public L2Item getItem() {
        return mItem;
    }

    public ListenerType getType() {
        return ListenerType.ON_USE_ITEM;
    }
}
