package com.github.athenaengine.core.managers;

import com.github.athenaengine.core.model.entity.*;
import com.github.athenaengine.core.model.entity.Character;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.L2Playable;
import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheManager {

    private final Map<Integer, Player> mPlayers = new ConcurrentHashMap<>();
    private final Map<Integer, Summon> mSummons = new ConcurrentHashMap<>();
    private final Map<Integer, Npc> mNpcs = new ConcurrentHashMap<>();
    private final Map<Integer, Character> mOtherCharacters = new ConcurrentHashMap<>();

    public Player getPlayer(L2PcInstance l2PcInstance, boolean initialize) {
        Player player = mPlayers.get(l2PcInstance.getObjectId());
        if (player == null && initialize) player = addPlayer(l2PcInstance);
        return player;
    }

    public Player getPlayer(int objectId) {
        return mPlayers.get(objectId);
    }

    private Summon getSummon(L2Summon l2Summon, boolean initialize) {
        Summon summon = mSummons.get(l2Summon.getObjectId());
        if (summon == null && initialize) summon = addSummon(l2Summon);
        return summon;
    }

    public Playable getPlayable(L2Playable l2Playable, boolean initialize) {
        if (l2Playable instanceof L2PcInstance) return getPlayer((L2PcInstance) l2Playable, initialize);
        return getSummon((L2Summon) l2Playable, initialize);
    }

    public Character getCharacter(L2Character l2Character) {
        return getCharacter(l2Character, false);
    }

    public Character getCharacter(L2Character l2Character, boolean initialize) {
        if (l2Character instanceof L2Playable) return getPlayable((L2Playable) l2Character, initialize);
        if (l2Character instanceof L2Npc) return getNpc((L2Npc) l2Character, initialize);

        return getOtherCharacter(l2Character, initialize);
    }

    public Npc getNpc(int npcId) {
        return mNpcs.get(npcId);
    }

    private Npc getNpc(L2Npc l2Npc, boolean initialize) {
        Npc npc = mNpcs.get(l2Npc.getObjectId());
        if (npc == null && initialize) npc = addNpc(l2Npc);
        return npc;
    }

    private Character getOtherCharacter(L2Character l2Character, boolean initialize) {
        Character character = mOtherCharacters.get(l2Character.getObjectId());
        if (character == null && initialize) character = addOtherCharacter(l2Character);
        return character;
    }

    private Player addPlayer(L2PcInstance l2PcInstance) {
        return mPlayers.computeIfAbsent(l2PcInstance.getObjectId(), p -> new Player(l2PcInstance.getObjectId()));
    }

    private Summon addSummon(L2Summon l2Summon) {
        return mSummons.computeIfAbsent(l2Summon.getObjectId(), s -> new Summon(l2Summon.getObjectId()));
    }

    private Npc addNpc(L2Npc l2Npc) {
        if (l2Npc instanceof L2MonsterInstance) {
            return mNpcs.computeIfAbsent(l2Npc.getObjectId(), m -> new Monster(l2Npc.getObjectId()));
        }

        return mNpcs.computeIfAbsent(l2Npc.getObjectId(), f -> new FriendlyNpc(l2Npc.getObjectId()));
    }

    private Character addOtherCharacter(L2Character l2Character) {
        return mOtherCharacters.computeIfAbsent(l2Character.getObjectId(), c -> new Character(l2Character.getObjectId()));
    }

    public void removePlayer(int objectId) {
        mPlayers.remove(objectId);
    }

    public void removeSummon(int objectId) {
        mSummons.remove(objectId);
    }

    public void removeCharacter(int objectId) {
        removeNpc(objectId);
        removeOtherCharacter(objectId);
        removeSummon(objectId);
    }

    public void removeNpc(int objectId) {
        mNpcs.remove(objectId);
    }

    public void removeOtherCharacter(int objectId) {
        mOtherCharacters.remove(objectId);
    }

    public static CacheManager getInstance() {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder {
        protected static final CacheManager _instance = new CacheManager();
    }
}
