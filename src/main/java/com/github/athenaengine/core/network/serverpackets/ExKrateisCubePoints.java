package com.github.athenaengine.core.network.serverpackets;

import com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket;

public class ExKrateisCubePoints extends L2GameServerPacket {

    private final int mTimeLeft;
    private final int mPoints;

    public ExKrateisCubePoints(int timeLeft, int points) {
        mTimeLeft = timeLeft;
        mPoints = points;
    }

    @Override
    protected void writeImpl() {
        writeC(0xfe);
        writeH(0x98);

        writeD(mTimeLeft);
        writeD(mPoints);
    }
}
