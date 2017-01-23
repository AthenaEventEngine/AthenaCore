package com.github.athenaengine.core.enums;

public enum MessageType {

    ALL(0),
    SHOUT(1),
    TELL(2),
    PARTY(3),
    CLAN(4),
    GM(5),
    PETITION_PLAYER(6),
    PETITION_GM(7),
    TRADE(8),
    ALLIANCE(9),
    ANNOUNCEMENT(10),
    BOAT(11),
    L2FRIEND(12),
    MSNCHAT(13),
    PARTYMATCH_ROOM(14),
    PARTYROOM_COMMANDER(15),
    PARTYROOM_ALL(16),
    HERO_VOICE(17),
    CRITICAL_ANNOUNCE(18),
    SCREEN_ANNOUNCE(19),
    BATTLEFIELD(20),
    MPCC_ROOM(21),
    NPC_ALL(22),
    NPC_SHOUT(23);

    private final int mValue;

    MessageType(int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }
}
