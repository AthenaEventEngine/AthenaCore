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
package com.github.u3games.eventengine.events.types.capturetheflag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.u3games.eventengine.builders.TeamsBuilder;
import com.github.u3games.eventengine.config.BaseConfigLoader;
import com.github.u3games.eventengine.datatables.MessageData;
import com.github.u3games.eventengine.dispatcher.events.*;
import com.github.u3games.eventengine.enums.CollectionTarget;
import com.github.u3games.eventengine.enums.ListenerType;
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
import com.l2jserver.gameserver.model.Location;
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
	private static final int FLAG = getConfig().getFlagNpcId();
	// Holder
	private static final int HOLDER = getConfig().getHolderNpcId();
	// FlagItem
	private static final int FLAG_ITEM = 6718;
	// Points to conquer the flag
	private final int POINTS_CONQUER_FLAG = getConfig().getPointsConquerFlag();
	private final int POINTS_KILL = getConfig().getPointsKill();
	// Time for resurrection
	private static final int TIME_RES_PLAYER = 10;
	// Radius spawn
	private final Map<NpcHolder, TeamType> _flagSpawn = new ConcurrentHashMap<>();
	private final Map<NpcHolder, TeamType> _holderSpawn = new ConcurrentHashMap<>();
	private final Map<PlayerHolder, TeamType> _flagHasPlayer = new ConcurrentHashMap<>();
	private final Map<String, Location> _flagsLoc = new HashMap<>();
	
	public CaptureTheFlag()
	{
		super(getConfig().getInstanceFile());
	}

	private static CTFEventConfig getConfig()
	{
		return BaseConfigLoader.getInstance().getCtfConfig();
	}
	
	@Override
	protected TeamsBuilder onCreateTeams()
	{
		return new TeamsBuilder()
				.addTeams(getConfig().getTeams())
				.setPlayers(getPlayerEventManager().getAllEventPlayers());
	}
	
	@Override
	protected void onEventStart()
	{
		addSuscription(ListenerType.ON_INTERACT);
		addSuscription(ListenerType.ON_KILL);
		addSuscription(ListenerType.ON_DEATH);
		addSuscription(ListenerType.ON_USE_ITEM);
		addSuscription(ListenerType.ON_LOG_OUT);

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
	public void onInteract(OnInteractEvent event)
	{
		PlayerHolder ph = getPlayerEventManager().getEventPlayer(event.getPlayer());
		NpcHolder npcHolder = getSpawnManager().getEventNpc(event.getNpc());

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
				EventUtil.announceTo(Say2.BATTLEFIELD, "ctf_captured_the_flag", "%holder%", ph.getTeam().getName(), CollectionTarget.ALL_PLAYERS_IN_EVENT);
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
					_flagSpawn.put(getSpawnManager().addEventNpc(FLAG, _flagsLoc.get(th.getName()).getX(), _flagsLoc.get(th.getName()).getY(), _flagsLoc.get(th.getName()).getZ(), 0, Team.NONE, th.getName(), false, getInstanceWorldManager().getAllInstancesWorlds().get(0).getInstanceId()), th.getTeamType());
					// Announce the flag was taken
					EventUtil.announceTo(Say2.BATTLEFIELD, "ctf_conquered_the_flag", "%holder%", ph.getTeam().getName(), CollectionTarget.ALL_PLAYERS_IN_EVENT);
					// Show team points
					showPoint();
				}
			}
		}
	}
	
	@Override
	public void onKill(OnKillEvent event)
	{
		PlayerHolder ph = getPlayerEventManager().getEventPlayer(event.getAttacker());
		L2Character target = event.getTarget();

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
		if (getConfig().isRewardKillEnabled())
		{
			giveItems(ph, getConfig().getRewardKill());
		}
		// Reward PvP for kills
		if (getConfig().isRewardPvPKillEnabled())
		{
			ph.getPcInstance().setPvpKills(ph.getPcInstance().getPvpKills() + getConfig().getRewardPvPKill());
			EventUtil.sendEventMessage(ph, MessageData.getInstance().getMsgByLang(ph.getPcInstance(), "reward_text_pvp", true).replace("%count%", getConfig().getRewardPvPKill() + ""));
		}
		// Reward fame for kills
		if (getConfig().isRewardFameKillEnabled())
		{
			ph.getPcInstance().setFame(ph.getPcInstance().getFame() + getConfig().getRewardFameKill());
			EventUtil.sendEventMessage(ph, MessageData.getInstance().getMsgByLang(ph.getPcInstance(), "reward_text_fame", true).replace("%count%", getConfig().getRewardFameKill() + ""));
		}
		// Message Kill
		if (BaseConfigLoader.getInstance().getMainConfig().isKillerMessageEnabled())
		{
			EventUtil.messageKill(ph, target);
		}
		showPoint();
	}
	
	@Override
	public void onDeath(OnDeathEvent event)
	{
		giveResurrectPlayer(getPlayerEventManager().getEventPlayer(event.getTarget()), TIME_RES_PLAYER);
	}
	
	@Override
	public void onUseItem(OnUseItemEvent event)
	{
		PlayerHolder ph = getPlayerEventManager().getEventPlayer(event.getPlayer());
		L2Item item = event.getItem();

		if (item.getId() == FLAG_ITEM)
		{
			return;
		}
		else if (hasFlag(ph) && (item instanceof L2Weapon))
		{
			return;
		}

		event.setCancel(true);
	}
	
	@Override
	public void onLogout(OnLogOutEvent event)
	{
		PlayerHolder ph = getPlayerEventManager().getEventPlayer(event.getPlayer());

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

		Map<String, Location> mapFlags = new HashMap<>();
		Map<String, Location> mapHolders = new HashMap<>();

		for (CTFTeamConfig config : getConfig().getTeams())
		{
			mapFlags.put(config.getName(), config.getFlagLoc());
			mapHolders.put(config.getName(), config.getHolderLoc());
		}

		for (TeamHolder th : getTeamsManager().getAllTeams())
		{
			if (mapFlags.containsKey(th.getName()))
			{
				Location flagLocation = mapFlags.get(th.getName());
				int flagX = flagLocation.getX();
				int flagY = flagLocation.getY();
				int flagZ = flagLocation.getZ();

				_flagsLoc.put(th.getName(), flagLocation);

				Location holderLocation = mapHolders.get(th.getName());
				int holderX = holderLocation.getX();
				int holderY = holderLocation.getY();
				int holderZ = holderLocation.getZ();

				_flagSpawn.put(getSpawnManager().addEventNpc(FLAG, flagX, flagY, flagZ, 0, Team.NONE, th.getName(), false, instanceId), th.getTeamType());
				_holderSpawn.put(getSpawnManager().addEventNpc(HOLDER, holderX, holderY, holderZ, 0, Team.NONE, th.getName(), false, instanceId), th.getTeamType());
			}
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
				giveItems(ph, getConfig().getReward());
			}
		}
		for (TeamHolder team : getTeamsManager().getAllTeams())
		{
			if (teamWinners.contains(team))
			{
				EventUtil.announceTo(Say2.BATTLEFIELD, "team_winner", "%holder%", team.getName(), CollectionTarget.ALL_PLAYERS_IN_EVENT);
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
			sb.append(team.getName());
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
		_flagSpawn.put(getSpawnManager().addEventNpc(FLAG, ph.getPcInstance().getX(), ph.getPcInstance().getY(), ph.getPcInstance().getZ(), 0, Team.NONE, th.getName(), false, ph.getDinamicInstanceId()), th.getTeamType());
		Map<String, String> map = new HashMap<>();
		// We announced that a flag was taken
		map.put("%holder%", ph.getPcInstance().getName());
		map.put("%flag%", th.getName());
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
		ph.setNewTitle("[ " + getTeamsManager().getPlayerTeam(ph).getName() + " ]");
	}
}