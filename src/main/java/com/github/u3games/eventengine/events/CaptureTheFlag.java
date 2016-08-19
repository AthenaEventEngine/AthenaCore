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
package com.github.u3games.eventengine.events;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.u3games.eventengine.builders.TeamsBuilder;
import com.github.u3games.eventengine.config.BaseConfigLoader;
import com.github.u3games.eventengine.datatables.ConfigData;
import com.github.u3games.eventengine.datatables.MessageData;
import com.github.u3games.eventengine.enums.CollectionTarget;
import com.github.u3games.eventengine.enums.ScoreType;
import com.github.u3games.eventengine.enums.TeamType;
import com.github.u3games.eventengine.events.handler.AbstractEvent;
import com.github.u3games.eventengine.events.holders.NpcHolder;
import com.github.u3games.eventengine.events.holders.PlayerHolder;
import com.github.u3games.eventengine.events.holders.TeamHolder;
import com.github.u3games.eventengine.util.EventUtil;
import com.github.u3games.eventengine.util.SortUtils;
import com.l2jserver.gameserver.datatables.ItemTable;
import com.l2jserver.gameserver.enums.Team;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.itemcontainer.Inventory;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.items.L2Weapon;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.network.clientpackets.Say2;
import com.l2jserver.gameserver.network.serverpackets.MagicSkillUse;

/**
 * @author fissban
 */
public class CaptureTheFlag extends AbstractEvent
{
	// Flag
	private static final int FLAG = ConfigData.getInstance().CTF_NPC_FLAG_ID;
	// Holder
	private static final int HOLDER = ConfigData.getInstance().CTF_NPC_HOLDER_ID;
	// FlagItem
	private static final int FLAG_ITEM = 6718;
	// Points to conquer the flag
	private final int POINTS_CONQUER_FLAG = ConfigData.getInstance().CTF_POINTS_CONQUER_FLAG;
	private final int POINTS_KILL = ConfigData.getInstance().CTF_POINTS_KILL;
	// Time for resurrection
	private static final int TIME_RES_PLAYER = 10;
	// Radius spawn
	protected int _radius = 100;
	private final Map<NpcHolder, TeamType> _flagSpawn = new ConcurrentHashMap<>();
	private final Map<NpcHolder, TeamType> _holderSpawn = new ConcurrentHashMap<>();
	private final Map<PlayerHolder, TeamType> _flagHasPlayer = new ConcurrentHashMap<>();
	
	public CaptureTheFlag()
	{
		super(ConfigData.getInstance().CTF_INSTANCE_FILE);
	}
	
	@Override
	protected TeamsBuilder onCreateTeams()
	{
		return new TeamsBuilder().addTeams(ConfigData.getInstance().CTF_COUNT_TEAM, ConfigData.getInstance().CTF_COORDINATES_TEAM).setPlayers(getPlayerEventManager().getAllEventPlayers());
	}
	
	@Override
	protected void onEventStart()
	{
		spawnFlagsAndHolders();
		for (PlayerHolder ph : getPlayerEventManager().getAllEventPlayers())
		{
			updateTitle(ph);
		}
	}
	
	@Override
	protected void onEventFight()
	{
		// Nothing
	}
	
	@Override
	protected void onEventEnd()
	{
		clearFlags();
		giveRewardsTeams();
	}
	
	@Override
	public void onInteract(PlayerHolder ph, NpcHolder npcHolder)
	{
		if (npcHolder.getNpcInstance().getId() == FLAG)
		{
			if (hasFlag(ph))
			{
				return;
			}
			
			TeamType flagTeam = _flagSpawn.get(npcHolder);
			if (ph.getTeamType() != flagTeam)
			{
				// Animation
				ph.getPcInstance().broadcastPacket(new MagicSkillUse(ph.getPcInstance(), ph.getPcInstance(), 1034, 1, 1, 1));
				// Delete the flag from the map
				_flagSpawn.remove(npcHolder);
				// Save the player has the flag
				_flagHasPlayer.put(ph, flagTeam);
				// Remove the flag from its position
				getSpawnManager().removeNpc(npcHolder);
				// Equip the flag
				equipFlag(ph, flagTeam);
				// Announce the flag was taken
				EventUtil.announceTo(Say2.BATTLEFIELD, "ctf_captured_the_flag", "%holder%", ph.getTeamType().name(), CollectionTarget.ALL_PLAYERS_IN_EVENT);
			}
		}
		else if (npcHolder.getNpcInstance().getId() == HOLDER)
		{
			if (ph.getTeamType() == _holderSpawn.get(npcHolder))
			{
				if (hasFlag(ph))
				{
					// Animation Large FireWork
					ph.getPcInstance().broadcastPacket(new MagicSkillUse(ph.getPcInstance(), ph.getPcInstance(), 2025, 1, 1, 1));
					// Increase the points
					getTeamsManager().getPlayerTeam(ph).increasePoints(POINTS_CONQUER_FLAG);
					// Remove the flag from player
					unequiFlag(ph);
					TeamHolder th = getTeamsManager().getTeam(_flagHasPlayer.remove(ph));
					// Spawn the flag again
					_flagSpawn.put(getSpawnManager().addEventNpc(FLAG, th.getSpawn().getX(), th.getSpawn().getY(), th.getSpawn().getZ(), 0, Team.NONE, th.getTeamType().name(), false, getInstanceWorldManager().getAllInstancesWorlds().get(0).getInstanceId()), th.getTeamType());
					// Announce the flag was taken
					EventUtil.announceTo(Say2.BATTLEFIELD, "ctf_conquered_the_flag", "%holder%", ph.getTeamType().name(), CollectionTarget.ALL_PLAYERS_IN_EVENT);
					// Show team points
					showPoint();
				}
			}
		}
	}
	
	@Override
	public void onKill(PlayerHolder ph, L2Character target)
	{
		PlayerHolder targetEvent = getPlayerEventManager().getEventPlayer(target);
		if (hasFlag(targetEvent))
		{
			// Remove the flag character
			unequiFlag(targetEvent);
			// Drop flag
			dropFlag(targetEvent);
		}
		// We increased the team's points
		getTeamsManager().getPlayerTeam(ph).increasePoints(POINTS_KILL);
		
		// Reward for kills
		if (ConfigData.getInstance().CTF_REWARD_KILLER_ENABLED)
		{
			giveItems(ph, ConfigData.getInstance().CTF_REWARD_KILLER);
		}
		// Reward PvP for kills
		if (ConfigData.getInstance().CTF_REWARD_PVP_KILLER_ENABLED)
		{
			ph.getPcInstance().setPvpKills(ph.getPcInstance().getPvpKills() + ConfigData.getInstance().CTF_REWARD_PVP_KILLER);
			EventUtil.sendEventMessage(ph, MessageData.getInstance().getMsgByLang(ph.getPcInstance(), "reward_text_pvp", true).replace("%count%", ConfigData.getInstance().CTF_REWARD_PVP_KILLER + ""));
		}
		// Reward fame for kills
		if (ConfigData.getInstance().CTF_REWARD_FAME_KILLER_ENABLED)
		{
			ph.getPcInstance().setFame(ph.getPcInstance().getFame() + ConfigData.getInstance().CTF_REWARD_FAME_KILLER);
			EventUtil.sendEventMessage(ph, MessageData.getInstance().getMsgByLang(ph.getPcInstance(), "reward_text_fame", true).replace("%count%", ConfigData.getInstance().CTF_REWARD_FAME_KILLER + ""));
		}
		// Message Kill
		if (BaseConfigLoader.getInstance().getMainConfig().isKillerMessageEnabled())
		{
			EventUtil.messageKill(ph, target);
		}
		showPoint();
	}
	
	@Override
	public void onDeath(PlayerHolder ph)
	{
		giveResurrectPlayer(ph, TIME_RES_PLAYER, _radius);
	}
	
	@Override
	public boolean onUseItem(PlayerHolder ph, L2Item item)
	{
		if (item.getId() == FLAG_ITEM)
		{
			return true;
		}
		if (hasFlag(ph) && (item instanceof L2Weapon))
		{
			return true;
		}
		return false;
	}
	
	@Override
	public void onLogout(PlayerHolder ph)
	{
		if (hasFlag(ph))
		{
			// Remove the flag character
			unequiFlag(ph);
			// Drop flag
			dropFlag(ph);
		}
	}
	
	// VARIOUS METHODS -------------------------------------------------
	/**
	 * Spawn flags and holders.
	 */
	private void spawnFlagsAndHolders()
	{
		int instanceId = getInstanceWorldManager().getAllInstancesWorlds().get(0).getInstanceId();
		for (TeamHolder th : getTeamsManager().getAllTeams())
		{
			int x = th.getSpawn().getX();
			int y = th.getSpawn().getY();
			int z = th.getSpawn().getZ();
			TeamType tt = th.getTeamType();
			_flagSpawn.put(getSpawnManager().addEventNpc(FLAG, x, y, z, 0, Team.NONE, th.getTeamType().name(), false, instanceId), tt);
			_holderSpawn.put(getSpawnManager().addEventNpc(HOLDER, x - 100, y, z, 0, Team.NONE, th.getTeamType().name(), false, instanceId), tt);
		}
	}
	
	/**
	 * Give rewards.
	 */
	private void giveRewardsTeams()
	{
		if (getPlayerEventManager().getAllEventPlayers().isEmpty())
		{
			return;
		}
		
		List<TeamHolder> teamWinners = SortUtils.getOrdered(getTeamsManager().getAllTeams(), ScoreType.POINT).get(0);
		for (PlayerHolder ph : getPlayerEventManager().getAllEventPlayers())
		{
			TeamHolder phTeam = getTeamsManager().getPlayerTeam(ph);
			// We deliver rewards
			if (teamWinners.contains(phTeam))
			{
				// We deliver rewards
				giveItems(ph, ConfigData.getInstance().CTF_REWARD_PLAYER_WIN);
			}
		}
		for (TeamHolder team : getTeamsManager().getAllTeams())
		{
			if (teamWinners.contains(team))
			{
				EventUtil.announceTo(Say2.BATTLEFIELD, "team_winner", "%holder%", team.getTeamType().name(), CollectionTarget.ALL_PLAYERS_IN_EVENT);
			}
		}
	}
	
	/**
	 * Show on screen the number of points that each team.
	 */
	private void showPoint()
	{
		StringBuilder sb = new StringBuilder();
		for (TeamHolder team : getTeamsManager().getAllTeams())
		{
			sb.append(" | ");
			sb.append(team.getTeamType().name());
			sb.append(" ");
			sb.append(team.getPoints());
		}
		sb.append(" | ");
		
		for (PlayerHolder ph : getPlayerEventManager().getAllEventPlayers())
		{
			EventUtil.sendEventScreenMessage(ph, sb.toString(), 10000);
		}
	}
	
	/**
	 * Check if a character has a flag.
	 * @param ph
	 * @return
	 */
	private boolean hasFlag(PlayerHolder ph)
	{
		return _flagHasPlayer.containsKey(ph);
	}
	
	/**
	 * We equip a character with a flag.
	 * @param ph
	 * @param flagTeam
	 */
	private void equipFlag(PlayerHolder ph, TeamType flagTeam)
	{
		_flagHasPlayer.put(ph, flagTeam);
		
		L2ItemInstance flag = ItemTable.getInstance().createItem("", FLAG_ITEM, 1, ph.getPcInstance(), null);
		if (flag != null)
		{
			ph.getPcInstance().useEquippableItem(flag, true);
		}
	}
	
	/**
	 * We remove the flag of a character.
	 * @param ph
	 */
	private void unequiFlag(PlayerHolder ph)
	{
		L2ItemInstance flag = ph.getPcInstance().getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if (flag != null)
		{
			ph.getPcInstance().useEquippableItem(flag, true);
		}
	}
	
	private void dropFlag(PlayerHolder ph)
	{
		TeamHolder th = getTeamsManager().getTeam(_flagHasPlayer.remove(ph));
		_flagSpawn.put(getSpawnManager().addEventNpc(FLAG, ph.getPcInstance().getX(), ph.getPcInstance().getY(), ph.getPcInstance().getZ(), 0, Team.NONE, th.getTeamType().name(), false, ph.getDinamicInstanceId()), th.getTeamType());
		Map<String, String> map = new HashMap<>();
		// We announced that a flag was taken
		map.put("%holder%", ph.getPcInstance().getName());
		map.put("%flag%", th.getTeamType().name());
		EventUtil.announceTo(Say2.BATTLEFIELD, "player_dropped_flag", map, CollectionTarget.ALL_PLAYERS_IN_EVENT);
	}
	
	/**
	 * Remove all the flags equipped.
	 */
	private void clearFlags()
	{
		for (PlayerHolder ph : _flagHasPlayer.keySet())
		{
			unequiFlag(ph);
		}
		_flagHasPlayer.clear();
	}
	
	private void updateTitle(PlayerHolder ph)
	{
		ph.setNewTitle("[ " + getTeamsManager().getPlayerTeam(ph).getTeamType().name() + " ]");
	}
}