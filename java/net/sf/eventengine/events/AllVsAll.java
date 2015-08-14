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
package net.sf.eventengine.events;

import java.util.ArrayList;
import java.util.List;

import com.l2jserver.gameserver.enums.Team;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.network.clientpackets.Say2;
import com.l2jserver.util.Rnd;

import net.sf.eventengine.datatables.ConfigData;
import net.sf.eventengine.enums.CollectionTarget;
import net.sf.eventengine.enums.EventState;
import net.sf.eventengine.enums.PlayerColorType;
import net.sf.eventengine.events.schedules.AnnounceNearEndEvent;
import net.sf.eventengine.handler.AbstractEvent;
import net.sf.eventengine.holder.PlayerHolder;
import net.sf.eventengine.util.EventUtil;
import net.sf.eventengine.util.SortUtil;

/**
 * @author fissban
 */
public class AllVsAll extends AbstractEvent
{
	// Radius spawn
	private static final int RADIUS_SPAWN_PLAYER = 1000;
	// Time for resurrection
	private static final int TIME_RES_PLAYER = 10;
	
	public AllVsAll()
	{
		super();
		setInstanceFile(ConfigData.getInstance().AVA_INSTANCE_FILE);
		// We define the main spawn of equipment
		setTeamSpawn(Team.NONE, ConfigData.getInstance().AVA_COORDINATES_PLAYER);
		// Announce near end event
		int timeLeft = (ConfigData.getInstance().EVENT_DURATION * 60 * 1000) - (ConfigData.getInstance().EVENT_TEXT_TIME_FOR_END * 1000);
		addScheduledEvent(new AnnounceNearEndEvent(timeLeft));
	}
	
	@Override
	public void runEventState(EventState state)
	{
		switch (state)
		{
			case START:
				prepareToStart(); // General Method
				createTeam();
				teleportAllPlayers(RADIUS_SPAWN_PLAYER);
				break;
				
			case FIGHT:
				prepareToFight(); // General Method
				break;
				
			case END:
				giveRewardsTeams();
				prepareToEnd(); // General Method
				break;
		}
		
	}
	
	// LISTENERS -----------------------------------------------------------------------
	@Override
	public void onKill(PlayerHolder player, L2Character target)
	{
		// Increase the amount of one character kills.
		player.increaseKills();
		updateTitle(player);
		
		// Reward for kills
		if (ConfigData.getInstance().AVA_REWARD_KILLER_ENABLED)
		{
			giveItems(player, ConfigData.getInstance().AVA_REWARD_KILLER);
		}
		
		// Reward pvp for kills
		if (ConfigData.getInstance().AVA_REWARD_PVP_KILLER_ENABLED)
		{
			player.getPcInstance().setPvpKills(player.getPcInstance().getPvpKills() + ConfigData.getInstance().AVA_REWARD_PVP_KILLER);
		}
		
		// Reward fame for kills
		if (ConfigData.getInstance().AVA_REWARD_FAME_KILLER_ENABLED)
		{
			player.getPcInstance().setFame(player.getPcInstance().getFame() + ConfigData.getInstance().AVA_REWARD_FAME_KILLER);
		}
		
		// Message Kill
		if (ConfigData.getInstance().EVENT_KILLER_MESSAGE)
		{
			EventUtil.messageKill(player, target);
		}
	}
	
	@Override
	public boolean onAttack(PlayerHolder player, L2Character target)
	{
		return false;
	}
	
	@Override
	public boolean onUseSkill(PlayerHolder player, L2Character target, Skill skill)
	{
		return false;
	}
	
	@Override
	public void onDeath(PlayerHolder player)
	{
		// We generated a task to revive the player
		giveResurrectPlayer(player, TIME_RES_PLAYER, RADIUS_SPAWN_PLAYER);
		// Increase the amount of one character deaths.
		player.increaseDeaths();
		// We update the title character
		updateTitle(player);
	}
	
	@Override
	public void onInteract(PlayerHolder player, L2Npc npc)
	{
		return;
	}
	
	@Override
	public boolean onUseItem(PlayerHolder player, L2Item item)
	{
		return false;
	}
	
	@Override
	public void onLogout(PlayerHolder player)
	{
		//
	}
	
	// VARIOUS METHODS ------------------------------------------------------------------
	
	/**
	 * We create teams
	 */
	private void createTeam()
	{
		// We create the instance and the world
		InstanceWorld world = createNewInstanceWorld();
		
		for (PlayerHolder player : getAllEventPlayers())
		{
			// We add the character to the world and then be teleported
			world.addAllowed(player.getPcInstance().getObjectId());
			// Adjust the character team
			player.getPcInstance().setTeam(Team.NONE);
			// Adjust the instance that owns the character
			player.setDinamicInstanceId(world.getInstanceId());
			// Adjust the color of the title
			player.setNewColorTitle(PlayerColorType.values()[Rnd.get(PlayerColorType.values().length - 1)]);
			// Update Title
			updateTitle(player);
		}
	}
	
	/**
	 * Update the title of a character depending on the number of deaths or kills have
	 * @param player
	 */
	private void updateTitle(PlayerHolder player)
	{
		// Adjust the title character
		player.setNewTitle("Kills " + player.getKills() + " | " + player.getDeaths() + " Death");
		// We update the status of the character
		player.getPcInstance().updateAndBroadcastStatus(2);
	}
	
	/**
	 * We deliver rewards.<br>
	 */
	public void giveRewardsTeams()
	{
		if (getAllEventPlayers().isEmpty())
		{
			return;
		}
		
		// auxiliary list
		List<PlayerHolder> playersInEvent = new ArrayList<>();
		
		playersInEvent.addAll(getAllEventPlayers());
		ArrayList<List<PlayerHolder>> listOrdered = SortUtil.getOrderedByKills(playersInEvent, 1);
		
		String winners = "";
		
		for (PlayerHolder player : listOrdered.get(0))
		{
			winners += player.getPcInstance().getName() + " // ";
			giveItems(player, ConfigData.getInstance().AVA_REWARD_PLAYER_WIN);
		}
		
		EventUtil.announceTo(Say2.CRITICAL_ANNOUNCE, "ava_first_place", "%holder%", winners, CollectionTarget.ALL_PLAYERS_IN_EVENT);
	}
}
