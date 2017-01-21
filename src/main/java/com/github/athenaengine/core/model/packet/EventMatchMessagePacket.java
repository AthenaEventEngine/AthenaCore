package com.github.athenaengine.core.model.packet;

import com.github.athenaengine.core.interfaces.IGamePacket;
import com.l2jserver.gameserver.network.serverpackets.ExEventMatchMessage;

public class EventMatchMessagePacket implements IGamePacket {

    private final ExEventMatchMessage mPacket;

    public EventMatchMessagePacket(int type, String msg) {
        mPacket = new ExEventMatchMessage(type, msg);
    }

    public ExEventMatchMessage getL2Packet() {
        return mPacket;
    }

}
