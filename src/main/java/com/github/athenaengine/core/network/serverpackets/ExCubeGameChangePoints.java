package com.github.athenaengine.core.network.serverpackets;

import com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket;

public class ExCubeGameChangePoints extends L2GameServerPacket {

    private int mTimeLeft;
    private int mBluePoints;
    private int mRedPoints;

    /**
     * Change Client Point Counter
     * @param timeLeft Time Left before Minigame's End
     * @param bluePoints Current Blue Team Points
     * @param redPoints Current Red Team Points
     */
    public ExCubeGameChangePoints(int timeLeft, int redPoints, int bluePoints) {
        mTimeLeft = timeLeft;
        mBluePoints = bluePoints;
        mRedPoints = redPoints;
    }

    @Override
    protected void writeImpl() {
        writeC(0xfe);
        writeH(0x98);
        writeD(0x02);

        writeD(mTimeLeft);
        writeD(mBluePoints);
        writeD(mRedPoints);
    }
}
