/*
 * Copyright (C) 2015-2015 L2J EventEngine
 *
 * This file is part of L2J EventEngine.
 *
 * L2J EventEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2J EventEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.eventengine.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.eventengine.EventEngineManager;
import net.sf.eventengine.datatables.ConfigData;
import net.sf.eventengine.datatables.MessageData;
import net.sf.eventengine.enums.CollectionTarget;
import net.sf.eventengine.events.holders.PlayerHolder;

import com.l2jserver.gameserver.datatables.SpawnTable;
import com.l2jserver.gameserver.model.L2Spawn;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.clientpackets.Say2;
import com.l2jserver.gameserver.network.serverpackets.CreatureSay;
import com.l2jserver.gameserver.network.serverpackets.ExEventMatchMessage;
import com.l2jserver.gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * @author fissban, Zephyr
 */
public class EventUtil
{
	private static final int NPC_RANGE = 1500;
	private static final Set<Integer> TIME_LEFT_TO_ANNOUNCE = new HashSet<>();
	
	static
	{
		TIME_LEFT_TO_ANNOUNCE.add(1800);
		TIME_LEFT_TO_ANNOUNCE.add(1200);
		TIME_LEFT_TO_ANNOUNCE.add(600);
		TIME_LEFT_TO_ANNOUNCE.add(300);
		TIME_LEFT_TO_ANNOUNCE.add(240);
		TIME_LEFT_TO_ANNOUNCE.add(120);
		TIME_LEFT_TO_ANNOUNCE.add(60);
		TIME_LEFT_TO_ANNOUNCE.add(30);
		TIME_LEFT_TO_ANNOUNCE.add(10);
		TIME_LEFT_TO_ANNOUNCE.add(5);
		TIME_LEFT_TO_ANNOUNCE.add(4);
		TIME_LEFT_TO_ANNOUNCE.add(3);
		TIME_LEFT_TO_ANNOUNCE.add(2);
		TIME_LEFT_TO_ANNOUNCE.add(1);
	}
	
	/**
	 * Announce a message replacing the %time% holder by time and another holder
	 * @param time
	 * @param textId
	 * @param say2
	 * @param type
	 */
	public static void announceTime(int time, String textId, int say2, CollectionTarget type)
	{
		announce(say2, textId, null, type, null, time);
	}
	
	/**
	 * Announce a message replacing the %time% holder by time and another holder
	 * @param time
	 * @param textId
	 * @param say2
	 * @param target
	 * @param replace
	 * @param type
	 */
	public static void announceTime(int time, String textId, int say2, String target, String replace, CollectionTarget type)
	{
		Map<String, String> map = new HashMap<>();
		map.put(target, replace);
		announce(say2, textId, map, type, null, time);
	}
	
	/**
	 * Announce a message replacing the %time% holder by time and another holders inside the map
	 * @param time
	 * @param textId
	 * @param say2
	 * @param map
	 * @param type
	 */
	public static void announceTime(int time, String textId, int say2, Map<String, String> mapToReplace, CollectionTarget type)
	{
		announce(say2, textId, mapToReplace, type, null, time);
	}
	
	/**
	 * Announce a message
	 * @param say2
	 * @param textId
	 * @param type
	 */
	public static void announceTo(int say2, String text, CollectionTarget type)
	{
		announce(say2, text, null, type, null, -1);
	}
	
	/**
	 * Announce a message replacing just a text holder
	 * @param say2
	 * @param textId
	 * @param target
	 * @parm replace
	 * @param type
	 */
	public static void announceTo(int say2, String text, String replace, String textReplace, CollectionTarget type)
	{
		Map<String, String> map = new HashMap<>();
		map.put(replace, textReplace);
		announce(say2, text, map, type, null, -1);
	}
	
	/**
	 * Announce a message replacing the text holders
	 * @param say2
	 * @param textId
	 * @param map
	 * @param type
	 */
	public static void announceTo(int say2, String text, Map<String, String> map, CollectionTarget type)
	{
		announce(say2, text, map, type, null, -1);
	}
	
	/**
	 * Announce a message by npc
	 * @param say2
	 * @param textId
	 * @param type
	 * @param npcId
	 */
	public static void npcAnnounceTo(int say2, String text, CollectionTarget type, int npcId)
	{
		announce(say2, text, null, type, getNpcSpawned(npcId), -1);
	}
	
	/**
	 * Announce a message by npc replacing just a holder
	 * @param say2
	 * @param textId
	 * @param target
	 * @param replace
	 * @param type
	 * @param npcId
	 */
	public static void npcAnnounceTo(int say2, String text, String replace, String textReplace, CollectionTarget type, int npcId)
	{
		Map<String, String> map = new HashMap<>();
		map.put(replace, textReplace);
		announce(say2, text, map, type, getNpcSpawned(npcId), -1);
	}
	
	/**
	 * Announce a message by npc replacing a map of holders
	 * @param say2
	 * @param textId
	 * @param map
	 * @param type
	 * @param npcId
	 */
	public static void npcAnnounceTo(int say2, String text, Map<String, String> map, CollectionTarget type, int npcId)
	{
		announce(say2, text, map, type, getNpcSpawned(npcId), -1);
	}
	
	/**
	 * Announce the proper message for each player
	 * @param say2
	 * @param textId
	 * @param map
	 * @param type
	 * @param npcs
	 * @param time
	 */
	private static void announce(int say2, String textId, Map<String, String> mapToReplace, CollectionTarget type, Set<L2Npc> npcs, int time)
	{
		if (time > -1 && !TIME_LEFT_TO_ANNOUNCE.contains(time))
		{
			return;
		}
		
		switch (type)
		{
			case ALL_PLAYERS:
			case ALL_PLAYERS_REGISTERED:
			case ALL_PLAYERS_IN_EVENT:
				for (L2PcInstance player : getPlayersCollection(type))
				{
					player.sendPacket(new CreatureSay(0, say2, "", getAnnounce(player, textId, mapToReplace, time)));
				}
				break;
			case ALL_NEAR_PLAYERS:
			case ALL_NEAR_PLAYERS_REGISTERED:
				if (npcs == null)
				{
					// Use by default the Npc Manager
					npcs = getNpcSpawned(ConfigData.getInstance().NPC_MANAGER_ID);
				}
				
				Map<L2Npc, Collection<L2PcInstance>> npcPlayerMap = getNpcPlayerCollection(type, npcs);
				for (L2Npc npc : npcPlayerMap.keySet())
				{
					for (L2PcInstance player : npcPlayerMap.get(npc))
					{
						player.sendPacket(new CreatureSay(npc.getObjectId(), say2, npc.getName(), getAnnounce(player, textId, mapToReplace, time)));
					}
				}
				break;
		}
	}
	
	/**
	 * Get the proper announce string to a player
	 * @param player
	 * @param textId
	 * @param map
	 * @param time
	 * @return announce
	 */
	private static String getAnnounce(L2PcInstance player, String textId, Map<String, String> map, int time)
	{
		String announce = MessageData.getInstance().getMsgByLang(player, textId, true);
		
		if (time > -1)
		{
			String timeLeft;
			if (time > 60)
			{
				timeLeft = (time / 60) + " " + MessageData.getInstance().getMsgByLang(player, "time_minutes", false);
			}
			else
			{
				timeLeft = time + " " + MessageData.getInstance().getMsgByLang(player, "time_seconds", false);
			}
			
			announce = announce.replace("%time%", timeLeft);
		}
		
		if (map != null)
		{
			for (String key : map.keySet())
			{
				announce = announce.replace(key, map.get(key));
			}
		}
		
		return announce;
	}
	
	/**
	 * Get a collection with all players that matchs with the type condition
	 * @param type
	 * @return collection
	 */
	private static Collection<L2PcInstance> getPlayersCollection(CollectionTarget type)
	{
		Collection<L2PcInstance> players = Collections.emptyList();
		
		switch (type)
		{
			case ALL_PLAYERS:
				players = L2World.getInstance().getPlayers();
				break;
			case ALL_PLAYERS_REGISTERED:
				players = EventEngineManager.getInstance().getAllRegisteredPlayers();
				break;
			case ALL_PLAYERS_IN_EVENT:
				players = new ArrayList<>();
				for (PlayerHolder ph : EventEngineManager.getInstance().getCurrentEvent().getAllEventPlayers())
				{
					players.add(ph.getPcInstance());
				}
				break;
		}
		
		return players;
	}
	
	/**
	 * Get a map with all known players by each npc from collection
	 * @param type
	 * @param npcs
	 * @return map
	 */
	private static Map<L2Npc, Collection<L2PcInstance>> getNpcPlayerCollection(CollectionTarget type, Set<L2Npc> npcs)
	{
		Map<L2Npc, Collection<L2PcInstance>> players = new HashMap<>();
		
		switch (type)
		{
			case ALL_NEAR_PLAYERS:
				for (L2Npc npc : npcs)
				{
					Collection<L2PcInstance> list = npc.getKnownList().getKnownPlayersInRadius(NPC_RANGE);
					players.put(npc, list);
				}
				break;
			case ALL_NEAR_PLAYERS_REGISTERED:
				for (L2Npc npc : npcs)
				{
					Collection<L2PcInstance> list = new ArrayList<>();
					for (L2PcInstance player : npc.getKnownList().getKnownPlayersInRadius(NPC_RANGE))
					{
						if (EventEngineManager.getInstance().isRegistered(player))
						{
							list.add(player);
						}
					}
					players.put(npc, list);
				}
				break;
		}
		
		return players;
	}
	
	/**
	 * Send a message to a player inside the event
	 * @param player
	 * @param text
	 */
	public static void sendEventMessage(PlayerHolder player, String text)
	{
		player.getPcInstance().sendPacket(new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "", text));
	}
	
	/**
	 * Create an event match message. type
	 * @param type -> 0 - gm, 1 - finish, 2 - start, 3 - game over, 4 - 1, 5 - 2, 6 - 3, 7 - 4, 8 - 5
	 * @param msg ->
	 */
	public static void sendEventSpecialMessage(PlayerHolder player, int type, String msg)
	{
		player.getPcInstance().sendPacket(new ExEventMatchMessage(type, msg));
	}
	
	/**
	 * Send a screen message to player inside the event
	 * @param player
	 * @param text
	 */
	public static void sendEventScreenMessage(PlayerHolder player, String text)
	{
		player.getPcInstance().sendPacket(new ExShowScreenMessage(text, 2000));
	}
	
	/**
	 * Send a screen message to all players in the event
	 * @param player
	 * @param text
	 * @param time
	 */
	public static void sendEventScreenMessage(PlayerHolder player, String text, int time)
	{
		player.getPcInstance().sendPacket(new ExShowScreenMessage(text, time));
	}
	
	/**
	 * Get all the instances of npc
	 * @param npcId
	 * @return npcs
	 */
	public static Set<L2Npc> getNpcSpawned(int npcId)
	{
		Set<L2Npc> npcs = new HashSet<>();
		for (L2Spawn spawn : SpawnTable.getInstance().getSpawns(npcId))
		{
			L2Npc npc = spawn.getLastSpawn();
			if (npc != null)
			{
				npcs.add(npc);
			}
		}
		
		return npcs;
	}
	
	/**
	 * Send killer message to all players in the event
	 * @param player
	 * @param target
	 */
	public static void messageKill(PlayerHolder player, L2Character target)
	{
		Map<String, String> map = new HashMap<>();
		map.put("%killer%", player.getPcInstance().getName());
		map.put("%target%", target.getName());
		EventUtil.announceTo(Say2.TRADE, "event_player_killer", map, CollectionTarget.ALL_PLAYERS_IN_EVENT);
	}
}
