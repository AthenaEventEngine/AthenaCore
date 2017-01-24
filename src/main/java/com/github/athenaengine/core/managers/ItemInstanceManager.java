package com.github.athenaengine.core.managers;

import com.github.athenaengine.core.model.entity.Player;
import com.github.athenaengine.core.model.instance.ItemInstance;
import com.l2jserver.gameserver.datatables.ItemTable;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;

public class ItemInstanceManager {

    public final ItemInstance createItem(int id, Player player) {
        L2ItemInstance l2Item = ItemTable.getInstance().createItem("", id, 1, player.getPcInstance(), null);
        return ItemInstance.newInstance(l2Item.getObjectId());
    }

    public static ItemInstanceManager getInstance() {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder {
        private static final ItemInstanceManager _instance = new ItemInstanceManager();
    }
}
