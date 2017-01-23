package com.github.athenaengine.core.model.packet;

import com.github.athenaengine.core.enums.MessageType;
import com.github.athenaengine.core.interfaces.IGamePacket;
import com.l2jserver.gameserver.network.serverpackets.CreatureSay;

public class CreatureSayPacket implements IGamePacket {

    private final CreatureSay mPacket;

    public CreatureSayPacket(int objectId, MessageType messageType, String charName, String message) {
        mPacket = new CreatureSay(objectId, messageType.getValue(), charName, message);
    }

    public CreatureSay getL2Packet() {
        return mPacket;
    }
}
