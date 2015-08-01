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

import net.sf.eventengine.datatables.ConfigData;
import net.sf.eventengine.enums.EventState;
import net.sf.eventengine.enums.PlayerColorType;
import net.sf.eventengine.handler.AbstractEvent;
import net.sf.eventengine.holder.PlayerHolder;
import net.sf.eventengine.network.serverpackets.EventParticipantStatus;
import net.sf.eventengine.util.EventUtil;

import com.l2jserver.gameserver.enums.Team;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.skills.Skill;

/**
 * @author fissban
 */
public class TeamVsTeam extends AbstractEvent
{
	// Points that each team.
	private int _pointsRed = 0;
	private int _pointsBlue = 0;
	
	public TeamVsTeam()
	{
		super();
		setInstanceFile(ConfigData.getInstance().TVT_INSTANCE_FILE);
		// We define each team spawns
		setTeamSpawn(Team.RED, ConfigData.getInstance().TVT_COORDINATES_TEAM_RED);
		setTeamSpawn(Team.BLUE, ConfigData.getInstance().TVT_COORDINATES_TEAM_BLUE);
	}
	
	@Override
	public void runEventState(EventState state)
	{
		switch (state)
		{
			case START:
				prepareToStart(); // General Method
				createTeams();
				teleportAllPlayers(300);
				break;
			
			case FIGHT:
				prepareToFight(); // General Method
				showPoint();
				break;
			
			case END:
				// showResult();
				giveRewardsTeams();
				prepareToEnd(); // General Method
				break;
		}
	}
	
	// LISTENERS ------------------------------------------------------
	@Override
	public boolean onUseSkill(PlayerHolder player, L2Character target, Skill skill)
	{
		return false;
	}
	
	@Override
	public boolean onAttack(PlayerHolder player, L2Character target)
	{
		return false;
	}
	
	@Override
	public void onKill(PlayerHolder player, L2Character target)
	{
		// We increased the team's points.
		switch (player.getPcInstance().getTeam())
		{
			case RED:
				_pointsRed++;
				break;
			case BLUE:
				_pointsBlue++;
				break;
		}
		
		showPoint();
	}
	
	@Override
	public void onDeath(PlayerHolder player)
	{
		giveResurrectPlayer(player, 10, 300);
		// Incremented by one the number of deaths Character
		player.increaseDeaths();
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
	
	// VARIOUS METHODS -------------------------------------------------
	
	/**
	 * We all players who are at the event and generate the teams
	 */
	private void createTeams()
	{
		// We create the instance and the world
		InstanceWorld world = createNewInstanceWorld();
		
		int aux = 0;
		
		for (PlayerHolder player : getAllEventPlayers())
		{
			if ((aux % 2) == 0)
			{
				// Adjust the color of the title and the title character.
				player.getPcInstance().setTeam(Team.BLUE);
				player.setNewColorTitle(PlayerColorType.BLUE);
				player.setNewTitle("[ BLUE ]");
			}
			else
			{
				// Adjust the color of the title and the title character.
				player.getPcInstance().setTeam(Team.RED);
				player.setNewColorTitle(PlayerColorType.RED);
				player.setNewTitle("[ RED ]");
			}
			
			// We add the character to the world and then be teleported
			world.addAllowed(player.getPcInstance().getObjectId());
			// Character status update
			player.getPcInstance().updateAndBroadcastStatus(2);
			// Adjust the instance which will own the character
			player.setDinamicInstanceId(world.getInstanceId());
			
			aux++;
		}
	}
	
	/**
	 * Entregamos los rewards
	 */
	private void giveRewardsTeams()
	{
		if (getAllEventPlayers().isEmpty())
		{
			return;
		}
		
		Team teamWinner = getWinTeam();
		
		for (PlayerHolder player : getAllEventPlayers())
		{
			if (teamWinner == Team.NONE)
			{
				// Send Message
				EventUtil.sendEventScreenMessage(player, "El evento resulto en un empate entre ambos teams!");
			}
			else
			{
				// Send Message
				EventUtil.sendEventScreenMessage(player, "Equipo ganador -> " + teamWinner.toString() + "!");
				
				// Entregamos los rewards
				if (player.getPcInstance().getTeam() == teamWinner)
				{
					giveItems(player, ConfigData.getInstance().TVT_REWARD_PLAYER_WIN);
				}
			}
		}
		
	}
	
	/**
	 * Small code for the winning team<br>
	 */
	private Team getWinTeam()
	{
		Team teamWinner;
		
		if (_pointsRed == _pointsBlue)
		{
			teamWinner = Team.NONE;
		}
		else if (_pointsRed > _pointsBlue)
		{
			teamWinner = Team.RED;
		}
		else
		{
			teamWinner = Team.BLUE;
		}
		
		return teamWinner;
	}
	
	/**
	 * Show on screen the number of points that each team
	 */
	private void showPoint()
	{
		for (PlayerHolder ph : getAllEventPlayers())
		{
			EventUtil.sendEventScreenMessage(ph, "RED " + _pointsRed + " | " + _pointsBlue + " BLUE", 10000);
			// ph.getPcInstance().sendPacket(new EventParticipantStatus(_pointsRed, _pointsBlue));
		}
	}
	
	/**
	 * Create lists with all participants and send to each final results of the event
	 */
	private void showResult()
	{
		List<PlayerHolder> teamBlue = new ArrayList<>();
		List<PlayerHolder> teamRed = new ArrayList<>();
		
		// Creamos listamos con los jugadores de cada team
		for (PlayerHolder ph : getAllEventPlayers())
		{
			if (ph.getPcInstance().getTeam() == Team.RED)
			{
				teamBlue.add(ph);
			}
			else
			{
				teamRed.add(ph);
			}
		}
		
		for (PlayerHolder ph : getAllEventPlayers())
		{
			ph.getPcInstance().sendPacket(new EventParticipantStatus(_pointsRed, teamBlue, _pointsBlue, teamRed));
		}
	}
}
