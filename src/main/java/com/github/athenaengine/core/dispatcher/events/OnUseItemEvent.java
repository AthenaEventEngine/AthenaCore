package com.github.athenaengine.core.dispatcher.events;

import com.github.athenaengine.core.enums.ListenerType;
import com.github.athenaengine.core.model.template.ItemTemplate;
import com.github.athenaengine.core.model.entity.Player;

public class OnUseItemEvent extends ListenerEvent {

    private final Player mPlayer;
    private final ItemTemplate mItem;

    public OnUseItemEvent(Player player, ItemTemplate item) {
        mPlayer = player;
        mItem = item;
    }

    public Player getPlayer() {
        return mPlayer;
    }

    public ItemTemplate getItem() {
        return mItem;
    }

    public ListenerType getType() {
        return ListenerType.ON_USE_ITEM;
    }
}
