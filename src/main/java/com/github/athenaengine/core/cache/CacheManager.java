package com.github.athenaengine.core.cache;

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

    public Player getPlayer(L2PcInstance l2PcInstance, boolean initialize) {
        Player player = mPlayers.get(l2PcInstance.getObjectId());
        if (player == null && initialize) player = addPlayer(l2PcInstance);
        return player;
    }

    public Summon getSummon(L2Summon l2Summon, boolean initialize) {
        Summon summon = mSummons.get(l2Summon.getObjectId());
        if (summon == null && initialize) summon = addSummon(l2Summon);
        return summon;
    }


    public Playable getPlayable(L2Playable l2Playable, boolean initialize) {
        if (l2Playable instanceof L2PcInstance) return getPlayer((L2PcInstance) l2Playable, initialize);
        return getSummon((L2Summon) l2Playable, initialize);
    }

    public Npc getNpc(L2Npc l2Npc, boolean initialize) {
        Npc npc = mNpcs.get(l2Npc.getObjectId());
        if (npc == null && initialize) npc = addNpc(l2Npc);
        return npc;
    }

    public Character getCharacter(L2Character l2Character, boolean initialize) {
        if (l2Character instanceof L2Playable) return getPlayable((L2Playable) l2Character, initialize);
        return getNpc((L2Npc) l2Character, initialize);
    }

    public Player addPlayer(L2PcInstance l2PcInstance) {
        Player player = new Player(l2PcInstance.getObjectId());
        mPlayers.put(l2PcInstance.getObjectId(), player);
        return player;
    }

    public Summon addSummon(L2Summon l2Summon) {
        Summon summon = new Summon(l2Summon.getObjectId());
        mSummons.put(l2Summon.getObjectId(), new Summon(l2Summon.getObjectId()));
        return summon;
    }

    public Npc addNpc(L2Npc l2Npc) {
        if (l2Npc instanceof L2MonsterInstance) {
            Monster monster = new Monster(l2Npc.getObjectId());
            mNpcs.put(l2Npc.getObjectId(), new Monster(l2Npc.getObjectId()));
            return monster;
        }

        FriendlyNpc friendlyNpc = new FriendlyNpc(l2Npc.getObjectId());
        mNpcs.put(l2Npc.getObjectId(), new FriendlyNpc(l2Npc.getObjectId()));
        return friendlyNpc;
    }

    public void removePlayer(int objectId) {
        mPlayers.remove(objectId);
    }

    public void removeSummon(int objectId) {
        mSummons.remove(objectId);
    }

    public void removeNpc(int objectId) {
        mNpcs.remove(objectId);
    }

    public static CacheManager getInstance()
    {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder
    {
        protected static final CacheManager _instance = new CacheManager();
    }
}
