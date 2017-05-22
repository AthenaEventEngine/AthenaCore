package com.github.athenaengine.core.model.entity;

import com.github.athenaengine.core.managers.CacheManager;
import com.github.athenaengine.core.model.holder.LocationHolder;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.network.serverpackets.MagicSkillUse;

import java.util.LinkedList;
import java.util.List;

public class Character extends Entity {

    public Character(int objectId) {
        super(objectId);
    }

    public String getName() {
        return getL2CharacterInstance().getName();
    }

    public String getTitle() {
        return getL2CharacterInstance().getTitle();
    }

    public void setTitle(String title) {
        getL2CharacterInstance().setTitle(title);
    }

    public double getHP() {
        return getL2CharacterInstance().getCurrentHp();
    }

    public double getMP() {
        return getL2CharacterInstance().getCurrentMp();
    }

    public double getMaxHP() {
        return getL2CharacterInstance().getMaxHp();
    }

    public double getMaxMP() {
        return getL2CharacterInstance().getMaxMp();
    }

    public void setHP(double hp) {
        getL2CharacterInstance().setCurrentHp(hp);
    }

    public void setMP(double mp) {
        getL2CharacterInstance().setCurrentMp(mp);
    }

    public void die(Character killer) {
        L2Character l2Character = ((L2Character) L2World.getInstance().findObject(killer.getObjectId()));
        if (l2Character != null) getL2CharacterInstance().doDie(l2Character);
    }

    public boolean isDead() {
        return getL2CharacterInstance().isDead();
    }

    public void attack(Character character) {
        L2Character target = ((L2Character) L2World.getInstance().findObject(character.getObjectId()));
        if (target != null) getL2CharacterInstance().doAttack(target);
    }

    public void stopAttack() {
        getL2CharacterInstance().abortAttack();
    }

    public void castSkill(Character target, int skillId, int skillLevel, int hitTime, int reuseDelay) {
        L2Character l2Character = getL2CharacterInstance();
        L2Character l2Target = ((L2Character) L2World.getInstance().findObject(target.getObjectId()));
        l2Character.broadcastPacket(new MagicSkillUse(l2Character, l2Target, skillId, skillLevel, hitTime, reuseDelay));
    }

    public LocationHolder getLocation() {
        L2Character l2Character = getL2CharacterInstance();
        return new LocationHolder(l2Character.getLocation());
    }

    public boolean isInsideRadius(LocationHolder loc, int radius, boolean checkZAxis, boolean strictCheck) {
        int x = loc.getX();
        int y = loc.getY();
        int z = loc.getZ();

        return getL2CharacterInstance().isInsideRadius(x, y, z, radius, checkZAxis, strictCheck);
    }

    public boolean isInsideRadius(int x, int y, int z, int radius, boolean checkZAxis, boolean strictCheck) {
        return getL2CharacterInstance().isInsideRadius(x, y, z, radius, checkZAxis, strictCheck);
    }

    public List<Player> getPlayersInsideRadius(int radius) {
        List<Player> players = new LinkedList<>();

        getL2CharacterInstance()
                .getKnownList()
                .getKnownPlayersInRadius(radius)
                .forEach(l2PcInstance -> players.add(CacheManager.getInstance().getPlayer(l2PcInstance.getObjectId())));

        return players;
    }

    public List<Character> getCharactersInsideRadius(int radius) {
        List<Character> characters = new LinkedList<>();

        getL2CharacterInstance()
                .getKnownList()
                .getKnownCharactersInRadius(radius)
                .forEach(l2Character -> characters.add(CacheManager.getInstance().getCharacter(l2Character)));

        return characters;
    }

    private L2Character getL2CharacterInstance() {
        return ((L2Character) L2World.getInstance().findObject(getObjectId()));
    }
}
