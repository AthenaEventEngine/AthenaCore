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
import java.util.Collections;
import java.util.List;

import net.sf.eventengine.EventEngineManager;
import net.sf.eventengine.datatables.ConfigData;
import net.sf.eventengine.enums.EventState;
import net.sf.eventengine.enums.PlayerColorType;
import net.sf.eventengine.handler.AbstractEvent;
import net.sf.eventengine.holder.PlayerHolder;
import net.sf.eventengine.util.EventUtil;

import com.l2jserver.gameserver.enums.Team;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.util.Rnd;

/**
 * @author fissban
 */
public class AllVsAll extends AbstractEvent
{
	// Radius spawn
	private static final int RADIUS_SPAWN_PLAYER = 100;
	
	public AllVsAll()
	{
		super();
		setInstanceFile(ConfigData.getInstance().AVA_INSTANCE_FILE);
		setTeamSpawn(Team.NONE, ConfigData.getInstance().AVA_COORDINATES_PLAYER);
	}
	
	@Override
	public void runEventState(EventState state)
	{
		switch (state)
		{
			case START:
				prepareToStart();
				createTeam();
				teleportAllPlayers(RADIUS_SPAWN_PLAYER);
				break;
			
			case FIGHT:
				prepareToFight();
				break;
			
			case END:
				giveRewardsTeams();
				prepareToEnd();
				break;
		}
		
	}
	
	// -------------------------------------------------------------------------------------
	// LISTENERS
	// -------------------------------------------------------------------------------------
	
	@Override
	public void onKill(PlayerHolder player, L2Character target)
	{
		player.increaseKills();
		giveItems(player, ConfigData.getInstance().AVA_REWARD_KILL_PLAYER);
		updateTitle(player);
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
		giveResurrectPlayer(player, ConfigData.getInstance().EVENT_TELEPORT_PLAYER_DELAY, RADIUS_SPAWN_PLAYER);
		player.increaseDeaths();
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
		// empty
	}
	
	// -------------------------------------------------------------------------------------
	// OTHERS METHODS
	// -------------------------------------------------------------------------------------
	
	/**
	 * Create teams
	 */
	private void createTeam()
	{
		// New Instance for event
		InstanceWorld world = EventEngineManager.getInstance().createNewInstanceWorld();
		
		for (PlayerHolder player : getAllEventPlayers())
		{
			world.addAllowed(player.getPcInstance().getObjectId());
			player.getPcInstance().setTeam(Team.NONE);
			player.setDinamicInstanceId(world.getInstanceId());
			player.setNewColorTitle(PlayerColorType.values()[Rnd.get(PlayerColorType.values().length - 1)]);
			updateTitle(player);
		}
	}
	
	/**
	 * Update the title of a character depending on the number of deaths or kills have
	 * @param player
	 */
	private void updateTitle(PlayerHolder player)
	{
		player.setNewTitle("Kills " + player.getKills() + " | " + player.getDeaths() + " Death");
		player.getPcInstance().updateAndBroadcastStatus(2);
	}
	
	/**
	 * Rewards
	 */
	public void giveRewardsTeams()
	{
		// Check player
		if (getAllEventPlayers().isEmpty())
		{
			return;
		}
		
		// Auxiliary list
		List<PlayerHolder> playersInEvent = new ArrayList<>();
		playersInEvent.addAll(getAllEventPlayers());
		
		// Ordered list
		Collections.sort(playersInEvent, PlayerHolder._pointsComparator);
		
		// We deliver the awards and announce the winners
		for (PlayerHolder player : playersInEvent)
		{
			int aux = 0;
			if (aux <= (playersInEvent.size() / 2))
			{
				EventUtil.sendEventScreenMessage(player, "Ganador " + player.getPcInstance().getName() + " con " + player.getPoints());
				giveItems(player, ConfigData.getInstance().AVA_REWARD_PLAYER_WIN);
			}
			else
			{
				EventUtil.sendEventScreenMessage(player, "Perdedor " + player.getPcInstance().getName() + " con " + player.getPoints());
			}
			
			aux++;
		}
	}
}
