package com.github.athenaengine.core.enums;

public enum InventoryItemType {

    PAPERDOLL_UNDER(0),
    PAPERDOLL_HEAD(1),
    PAPERDOLL_HAIR(2),
    PAPERDOLL_HAIR2(3),
    PAPERDOLL_NECK(4),
    PAPERDOLL_RHAND(5),
    PAPERDOLL_CHEST(6),
    PAPERDOLL_LHAND(7),
    PAPERDOLL_REAR(8),
    PAPERDOLL_LEAR(9),
    PAPERDOLL_GLOVES(10),
    PAPERDOLL_LEGS(11),
    PAPERDOLL_FEET(12),
    PAPERDOLL_RFINGER(13),
    PAPERDOLL_LFINGER(14),
    PAPERDOLL_LBRACELET(15),
    PAPERDOLL_RBRACELET(16),
    PAPERDOLL_DECO1(17),
    PAPERDOLL_DECO2(18),
    PAPERDOLL_DECO3(19),
    PAPERDOLL_DECO4(20),
    PAPERDOLL_DECO5(21),
    PAPERDOLL_DECO6(22),
    PAPERDOLL_CLOAK(23),
    PAPERDOLL_BELT(24),
    PAPERDOLL_TOTALSLOTS(25);

    private final int mValue;

    InventoryItemType(int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }
}
