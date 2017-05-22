package com.github.athenaengine.core.model.packet;

import com.github.athenaengine.core.interfaces.IGamePacket;
import com.github.athenaengine.core.network.serverpackets.ExKrateisCubePoints;

public class KrateisCubePointsPacket implements IGamePacket {

    private final ExKrateisCubePoints mPacket;

    public KrateisCubePointsPacket(int timeLeft, int points) {
        mPacket = new ExKrateisCubePoints(timeLeft, points);
    }

    @Override
    public ExKrateisCubePoints getL2Packet() {
        return mPacket;
    }
}
