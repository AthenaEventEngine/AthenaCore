package com.github.athenaengine.core.model.entity;

public abstract class Playable extends Character {

    public Playable(int objectId) {
        super(objectId);
    }

    public boolean isPlayer() {
        return false;
    }

    public boolean isSummon() {
        return false;
    }
}
