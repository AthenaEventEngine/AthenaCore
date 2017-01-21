package com.github.athenaengine.core.model.entity;

import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jserver.gameserver.util.MinionList;

import java.util.Collection;
import java.util.LinkedList;

public class Monster extends Npc {

    public Monster(int objectId) {
        super(objectId);
    }

    public Monster getBoss() {
        L2MonsterInstance leader = ((L2MonsterInstance) L2World.getInstance().findObject(getObjectId())).getLeader();
        return leader != null ? new Monster(leader.getObjectId()) : null;
    }

    public Collection<Monster> getMinions() {
        Collection<Monster> list = null;
        MinionList minions = ((L2MonsterInstance) L2World.getInstance().findObject(getObjectId())).getMinionList();

        if (minions.getSpawnedMinions() != null) {
            list = new LinkedList<>();

            for (L2MonsterInstance monster : minions.getSpawnedMinions()) {
                list.add(new Monster(monster.getObjectId()));
            }
        }

        return list;
    }
}
