package com.github.athenaengine.core.model.entity;

import com.github.athenaengine.core.model.holder.LocationHolder;
import com.l2jserver.gameserver.model.L2Spawn;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

public abstract class Npc extends Character {

    public Npc(int objectId) {
        super(objectId);
    }

    public int getTemplateId() {
        return getL2NpcInstance().getId();
    }

    public void moveTo(LocationHolder location, int offset) {
        getL2NpcInstance().moveToLocation(location.getX(), location.getY(), location.getZ(), offset);
    }

    public void moveTo(LocationHolder location) {
        moveTo(location, 0);
    }

    public void spawn() {
        getL2NpcInstance().spawnMe();
    }

    public LocationHolder getSpawn() {
        Location location = getL2NpcInstance().getSpawn().getLocation();
        return location != null ? new LocationHolder(location) : null;
    }

    public void setSpawn(LocationHolder location) {
        L2Spawn spawn = getL2NpcInstance().getSpawn();
        if (spawn != null) spawn.setLocation(location.getLocation());
    }

    public void talkTo(Player player, String message) {
        L2PcInstance pcInstance = L2World.getInstance().getPlayer(player.getObjectId());
        if (pcInstance != null) pcInstance.sendMessage(message);
    }

    public void stopRespawn() {
        L2Npc npc = getL2NpcInstance();
        if (npc != null && npc.getSpawn() != null) npc.getSpawn().stopRespawn();
    }

    public void deleteMe() {
        L2Npc npc = getL2NpcInstance();
        if (npc != null) npc.deleteMe();
    }

    private L2Npc getL2NpcInstance() {
        return ((L2Npc) L2World.getInstance().findObject(getObjectId()));
    }
}
