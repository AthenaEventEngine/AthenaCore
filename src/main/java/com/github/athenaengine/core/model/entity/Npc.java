package com.github.athenaengine.core.model.entity;

import com.github.athenaengine.core.model.ELocation;
import com.l2jserver.gameserver.model.L2Spawn;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

public abstract class Npc extends Character {

    public Npc(int objectId) {
        super(objectId);
    }

    public void moveTo(ELocation location, int offset) {
        ((L2Npc) L2World.getInstance().findObject(getObjectId())).moveToLocation(location.getX(), location.getY(), location.getZ(), offset);
    }

    public void moveTo(ELocation location) {
        moveTo(location, 0);
    }

    public void spawn() {
        L2World.getInstance().findObject(getObjectId()).spawnMe();
    }

    public ELocation getSpawn() {
        Location location = ((L2Npc) L2World.getInstance().findObject(getObjectId())).getSpawn().getLocation();
        return location != null ? new ELocation(location) : null;
    }

    public void setSpawn(ELocation location) {
        L2Spawn spawn = ((L2Npc) L2World.getInstance().findObject(getObjectId())).getSpawn();
        if (spawn != null) spawn.setLocation(location.getLocation());
    }

    public void talkTo(Player player, String message) {
        L2PcInstance pcInstance = L2World.getInstance().getPlayer(player.getObjectId());
        if (pcInstance != null) pcInstance.sendMessage(message);
    }
}
