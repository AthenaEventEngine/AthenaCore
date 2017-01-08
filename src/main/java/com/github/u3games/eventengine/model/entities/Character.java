package com.github.u3games.eventengine.model.entities;

import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.L2Character;

public abstract class Character extends Entity {

    public Character(int objectId) {
        super(objectId);
    }

    public String getName() {
        return L2World.getInstance().findObject(getObjectId()).getName();
    }

    public String getTitle() {
        return ((L2Character) L2World.getInstance().findObject(getObjectId())).getTitle();
    }

    public void setTitle(String title) {
        ((L2Character) L2World.getInstance().findObject(getObjectId())).setTitle(title);
    }

    public double getHP() {
        return ((L2Character) L2World.getInstance().findObject(getObjectId())).getCurrentHp();
    }

    public double getMP() {
        return ((L2Character) L2World.getInstance().findObject(getObjectId())).getCurrentMp();
    }

    public double getMaxHP() {
        return ((L2Character) L2World.getInstance().findObject(getObjectId())).getMaxHp();
    }

    public double getMaxMP() {
        return ((L2Character) L2World.getInstance().findObject(getObjectId())).getMaxMp();
    }

    public void setHP(double hp) {
        ((L2Character) L2World.getInstance().findObject(getObjectId())).setCurrentHp(hp);
    }

    public void setMP(double mp) {
        ((L2Character) L2World.getInstance().findObject(getObjectId())).setCurrentMp(mp);
    }
}
