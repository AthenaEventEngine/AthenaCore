package com.github.athenaengine.core.model.entity;

import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

public class Summon extends Playable {

    public Summon(int objectId) {
        super(objectId);
    }

    @Override
    public boolean isSummon() {
        return true;
    }

    public Player getOwner() {
        L2PcInstance l2PcInstance = getL2SummonInstance().getOwner();
        return l2PcInstance != null ? new Player(l2PcInstance.getObjectId()) : null;
    }

    public void unsummon() {
        L2Summon l2Summon = getL2SummonInstance();
        if (l2Summon != null) l2Summon.unSummon(l2Summon.getOwner());
    }

    public void returnToOwner() {
        getL2SummonInstance().followOwner();
    }

    private L2Summon getL2SummonInstance() {
        return ((L2Summon) L2World.getInstance().findObject(getObjectId()));
    }
}
