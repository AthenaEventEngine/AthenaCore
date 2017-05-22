package com.github.athenaengine.core.model.packet;

import com.github.athenaengine.core.interfaces.IGamePacket;
import com.github.athenaengine.core.network.serverpackets.ExCubeGameChangePoints;

public class TopCenterTextPacket implements IGamePacket {

    private final ExCubeGameChangePoints mPacket;

    public TopCenterTextPacket(int timeLeft, int redPoints, int bluePoints) {
        mPacket = new ExCubeGameChangePoints(timeLeft, redPoints, bluePoints);
    }

    public ExCubeGameChangePoints getL2Packet() {
        return mPacket;
    }
}
