package com.github.athenaengine.core.model.template;

import com.l2jserver.gameserver.datatables.ItemTable;
import com.l2jserver.gameserver.model.items.L2Weapon;

public class ItemTemplate {

    private final int mTemplateId;

    public static ItemTemplate newInstance(int templateId) {
        return new ItemTemplate(templateId);
    }

    private ItemTemplate(int templateId) {
        mTemplateId = templateId;
    }

    public int getTemplateId() {
        return mTemplateId;
    }

    public boolean isWeapong() {
        return ItemTable.getInstance().getTemplate(mTemplateId) instanceof L2Weapon;
    }

    public boolean isScroll() {
        return ItemTable.getInstance().getTemplate(mTemplateId).isScroll();
    }

    public boolean isPotion() {
        return ItemTable.getInstance().getTemplate(mTemplateId).isPotion();
    }
}
