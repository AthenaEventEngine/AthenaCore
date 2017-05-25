/*
 * Copyright (C) 2015-2016 L2J EventEngine
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
package com.github.athenaengine.core.util;

import java.util.*;

import com.github.athenaengine.core.interfaces.IParticipant;
import com.github.athenaengine.core.managers.general.CacheManager;
import com.github.athenaengine.core.config.BaseConfigLoader;
import com.github.athenaengine.core.datatables.MessageData;
import com.github.athenaengine.core.enums.MessageType;
import com.github.athenaengine.core.model.entity.Character;
import com.github.athenaengine.core.model.entity.Player;
import com.github.athenaengine.core.EventEngineManager;
import com.github.athenaengine.core.enums.CollectionTarget;
import com.github.athenaengine.core.model.packet.CreatureSayPacket;
import com.github.athenaengine.core.model.packet.EventMatchMessagePacket;
import com.github.athenaengine.core.model.packet.ShowScreenMessagePacket;
import com.l2jserver.gameserver.datatables.SpawnTable;
import com.l2jserver.gameserver.model.L2Spawn;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.serverpackets.CreatureSay;

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
	 * Announce a message replacing the %time% holder by time and another holder.
	 * @param time
	 * @param textId
	 * @param say2
	 * @param type
	 */
	public static void announceTime(int time, String textId, MessageType say2, CollectionTarget type)
	{
		announce(say2, textId, null, type, null, time);
	}
	
	/**
	 * Announce a message replacing the %time% holder by time and another holder.
	 * @param time
	 * @param textId
	 * @param say2
	 * @param target
	 * @param replace
	 * @param type
	 */
	public static void announceTime(int time, String textId, MessageType say2, String target, String replace, CollectionTarget type)
	{
		Map<String, String> map = new HashMap<>();
		map.put(target, replace);
		announce(say2, textId, map, type, null, time);
	}
	
	/**
	 * Announce a message replacing the %time% holder by time and another holders inside the map.
	 * @param time
	 * @param textId
	 * @param say2
	 * @param mapToReplace
	 * @param type
	 */
	public static void announceTime(int time, String textId, MessageType say2, Map<String, String> mapToReplace, CollectionTarget type)
	{
		announce(say2, textId, mapToReplace, type, null, time);
	}
	
	/**
	 * Announce a message.
	 * @param say2
	 * @param text
	 * @param type
	 */
	public static void announceTo(MessageType say2, String text, CollectionTarget type)
	{
		announce(say2, text, null, type, null, -1);
	}
	
	/**
	 * Announce a message replacing just a text holder.
	 * @param say2
	 * @param text
	 * @param replace
	 * @param textReplace
	 * @parm replace
	 * @param type
	 */
	public static void announceTo(MessageType say2, String text, String replace, String textReplace, CollectionTarget type)
	{
		Map<String, String> map = new HashMap<>();
		map.put(replace, textReplace);
		announce(say2, text, map, type, null, -1);
	}
	
	/**
	 * Announce a message replacing the text holders.
	 * @param say2
	 * @param text
	 * @param map
	 * @param type
	 */
	public static void announceTo(MessageType say2, String text, Map<String, String> map, CollectionTarget type)
	{
		announce(say2, text, map, type, null, -1);
	}
	
	/**
	 * Announce a message by npc.
	 * @param say2
	 * @param text
	 * @param type
	 * @param npcId
	 */
	public static void npcAnnounceTo(MessageType say2, String text, CollectionTarget type, int npcId)
	{
		announce(say2, text, null, type, getNpcSpawned(npcId), -1);
	}
	
	/**
	 * Announce a message by npc replacing just a holder.
	 * @param say2
	 * @param text
	 * @param replace
	 * @param textReplace
	 * @param type
	 * @param npcId
	 */
	public static void npcAnnounceTo(MessageType say2, String text, String replace, String textReplace, CollectionTarget type, int npcId)
	{
		Map<String, String> map = new HashMap<>();
		map.put(replace, textReplace);
		announce(say2, text, map, type, getNpcSpawned(npcId), -1);
	}
	
	/**
	 * Announce a message by npc replacing a map of holders.
	 * @param say2
	 * @param text
	 * @param map
	 * @param type
	 * @param npcId
	 */
	public static void npcAnnounceTo(MessageType say2, String text, Map<String, String> map, CollectionTarget type, int npcId)
	{
		announce(say2, text, map, type, getNpcSpawned(npcId), -1);
	}
	
	/**
	 * Announce the proper message for each player.
	 * @param messageType
	 * @param textId
	 * @param mapToReplace
	 * @param type
	 * @param npcs
	 * @param time
	 */
	private static void announce(MessageType messageType, String textId, Map<String, String> mapToReplace, CollectionTarget type, Set<L2Npc> npcs, int time)
	{
		if ((time > -1) && !TIME_LEFT_TO_ANNOUNCE.contains(time))
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
					player.sendPacket(new CreatureSay(0, messageType.getValue(), "", getAnnounce(CacheManager.getInstance().getPlayer(player, true), textId, mapToReplace, time)));
				}
				break;
			case ALL_NEAR_PLAYERS:
			case ALL_NEAR_PLAYERS_REGISTERED:
				if (npcs == null)
				{
					// Use by default the Npc Manager
					npcs = getNpcSpawned(BaseConfigLoader.getInstance().getMainConfig().getNpcId());
				}
				
				Map<L2Npc, Collection<L2PcInstance>> npcPlayerMap = getNpcPlayerCollection(type, npcs);
				for (L2Npc npc : npcPlayerMap.keySet())
				{
					for (L2PcInstance player : npcPlayerMap.get(npc))
					{
						player.sendPacket(new CreatureSay(npc.getObjectId(), 18, npc.getName(), getAnnounce(CacheManager.getInstance().getPlayer(player, true), textId, mapToReplace, time)));
					}
				}
				break;
		}
	}
	
	/**
	 * Get the proper announce string to a player.
	 * @param player
	 * @param textId
	 * @param map
	 * @param time
	 * @return announce
	 */
	private static String getAnnounce(Player player, String textId, Map<String, String> map, int time)
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
	 * Get a collection with all players that match with the type condition.
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
				// TODO: fix this
				Collection<Player> eventPlayers = EventEngineManager.getInstance().getAllRegisteredPlayers();

				for (Player player : eventPlayers) {
					players.add(player.getPcInstance());
				}
				break;
			case ALL_PLAYERS_IN_EVENT:
				players = new ArrayList<>();
				for (Player ph : EventEngineManager.getInstance().getCurrentEvent().getPlayerEventManager().getAllEventPlayers())
				{
					players.add(ph.getPcInstance());
				}
				break;
			default:
				break;
		}
		return players;
	}
	
	/**
	 * Get a map with all known players by each npc from collection.
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
						if (EventEngineManager.getInstance().isRegistered(CacheManager.getInstance().getPlayer(player,false)))
						{
							list.add(player);
						}
					}
					players.put(npc, list);
				}
				break;
			default:
				break;
		}
		return players;
	}
	
	/**
	 * Send a message to a player inside the event.
	 * @param player
	 * @param text
	 */
	public static void sendEventMessage(Player player, String text)
	{
		player.sendPacket(new CreatureSayPacket(0, MessageType.PARTYMATCH_ROOM, "", text));
	}
	
	/**
	 * Create an event match message type.
	 * @param player
	 * @param type 0 - gm, 1 - finish, 2 - start, 3 - game over, 4 - 1, 5 - 2, 6 - 3, 7 - 4, 8 - 5
	 * @param msg
	 */
	public static void sendEventSpecialMessage(Player player, int type, String msg)
	{
		player.sendPacket(new EventMatchMessagePacket(type, msg));
	}
	
	/**
	 * Send a screen message to player inside the event.
	 * @param player
	 * @param text
	 */
	public static void sendEventScreenMessage(Player player, String text)
	{
		player.sendPacket(new ShowScreenMessagePacket(text, 2000));
	}
	
	/**
	 * Send a screen message to all players in the event.
	 * @param player
	 * @param text
	 * @param time
	 */
	public static void sendEventScreenMessage(Player player, String text, int time)
	{
		player.sendPacket(new ShowScreenMessagePacket(text, time));
	}

	public static void sendEventScreenMessage(IParticipant participant, String text, int time)
	{
		participant.sendPacket(new ShowScreenMessagePacket(text, time));
	}
	
	/**
	 * Get all the instances of npc.
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
	 * Send killer message to all players in the event.
	 * @param player
	 * @param target
	 */
	public static void messageKill(Player player, Character target)
	{
		Map<String, String> map = new HashMap<>();
		map.put("%killer%", player.getPcInstance().getName());
		map.put("%target%", target.getName());
		EventUtil.announceTo(MessageType.TRADE, "event_player_killer", map, CollectionTarget.ALL_PLAYERS_IN_EVENT);
	}
	
	/**
	 * Send a message to player.
	 * @param ph
	 * @param textId
	 */
	public static void sendMessageToPlayer(Player ph, String textId)
	{
		ph.sendMessage(MessageData.getInstance().getMsgByLang(ph, textId, true));
	}
}