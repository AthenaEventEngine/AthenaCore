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

import java.util.List;

import net.sf.eventengine.datatables.ConfigData;
import net.sf.eventengine.datatables.MessageData;
import net.sf.eventengine.enums.CollectionTarget;
import net.sf.eventengine.enums.ScoreType;
import net.sf.eventengine.enums.TeamType;
import net.sf.eventengine.events.handler.AbstractEvent;
import net.sf.eventengine.events.holders.PlayerHolder;
import net.sf.eventengine.events.holders.TeamHolder;
import net.sf.eventengine.util.EventUtil;
import net.sf.eventengine.util.SortUtils;

import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;
import com.l2jserver.gameserver.network.clientpackets.Say2;

/**
 * @author fissban
 */
public class TeamVsTeam extends AbstractEvent
{
	// Radius spawn
	private static final int RADIUS_SPAWN_PLAYER = 100;
	// Time for resurrection
	private static final int TIME_RES_PLAYER = 10;
	
	public TeamVsTeam()
	{
		super(ConfigData.getInstance().TVT_INSTANCE_FILE);
	}
	
	@Override
	protected void onEventStart()
	{
		createTeams(ConfigData.getInstance().TVT_COUNT_TEAM);
		teleportAllPlayers(RADIUS_SPAWN_PLAYER);
	}
	
	@Override
	protected void onEventFight()
	{
		showPoint();
	}
	
	@Override
	protected void onEventEnd()
	{
		// showResult();
		giveRewardsTeams();
	}
	
	// LISTENERS ------------------------------------------------------
	@Override
	public void onKill(PlayerHolder ph, L2Character target)
	{
		// We increased the team's points.
		getTeamsManager().getPlayerTeam(ph).increasePoints(1);
		
		// Reward for kills
		if (ConfigData.getInstance().TVT_REWARD_KILLER_ENABLED)
		{
			giveItems(ph, ConfigData.getInstance().TVT_REWARD_KILLER);
		}
		// Reward pvp for kills
		if (ConfigData.getInstance().TVT_REWARD_PVP_KILLER_ENABLED)
		{
			ph.getPcInstance().setPvpKills(ph.getPcInstance().getPvpKills() + ConfigData.getInstance().TVT_REWARD_PVP_KILLER);
			EventUtil.sendEventMessage(ph, MessageData.getInstance().getMsgByLang(ph.getPcInstance(), "reward_text_pvp", true).replace("%count%", ConfigData.getInstance().TVT_REWARD_PVP_KILLER + ""));
		}
		// Reward fame for kills
		if (ConfigData.getInstance().TVT_REWARD_FAME_KILLER_ENABLED)
		{
			ph.getPcInstance().setFame(ph.getPcInstance().getFame() + ConfigData.getInstance().TVT_REWARD_FAME_KILLER);
			EventUtil.sendEventMessage(ph, MessageData.getInstance().getMsgByLang(ph.getPcInstance(), "reward_text_fame", true).replace("%count%", ConfigData.getInstance().TVT_REWARD_FAME_KILLER + ""));
		}
		// Message Kill
		if (ConfigData.getInstance().EVENT_KILLER_MESSAGE)
		{
			EventUtil.messageKill(ph, target);
		}
		
		showPoint();
	}
	
	@Override
	public void onDeath(PlayerHolder ph)
	{
		giveResurrectPlayer(ph, TIME_RES_PLAYER, RADIUS_SPAWN_PLAYER);
		// Incremented by one the number of deaths Character
		ph.increaseDeaths();
	}
	
	// VARIOUS METHODS -------------------------------------------------
	
	/**
	 * We all players who are at the event and generate the teams
	 * @param int countTeams
	 */
	private void createTeams(int countTeams)
	{
		// Definimos la cantidad de teams que se requieren
		getTeamsManager().setCountTeams(countTeams);
		// We define each team spawns
		getTeamsManager().setSpawnTeams(ConfigData.getInstance().TVT_COORDINATES_TEAM);
		// We create the instance and the world
		InstanceWorld world = getInstanceWorldManager().createNewInstanceWorld();
		
		int aux = 1;
		
		for (PlayerHolder ph : getPlayerEventManager().getAllEventPlayers())
		{
			// Obtenemos el team
			TeamType team = getTeamsManager().getEnabledTeams()[aux - 1];
			// Definimos el team del jugador
			ph.setTeam(team);
			// Ajustamos el titulo del personaje segun su team
			ph.setNewTitle("[ " + team.toString() + " ]");// [ BLUE ], [ RED ] ....
			
			// We add the character to the world and then be teleported
			world.addAllowed(ph.getPcInstance().getObjectId());
			// Adjust the instance which will own the character
			ph.setDinamicInstanceId(world.getInstanceId());
			
			if (aux % countTeams == 0)
			{
				aux = 1;
			}
			else
			{
				aux++;
			}
		}
	}
	
	/**
	 * Entregamos los rewards
	 */
	private void giveRewardsTeams()
	{
		if (getPlayerEventManager().getAllEventPlayers().isEmpty())
		{
			return;
		}
		// Get the teams winner by total points
		List<TeamHolder> teamWinners = SortUtils.getOrdered(getTeamsManager().getAllTeams(), ScoreType.POINT).get(0);
		
		for (PlayerHolder ph : getPlayerEventManager().getAllEventPlayers())
		{
			TeamHolder phTeam = getTeamsManager().getPlayerTeam(ph);
			// We deliver rewards
			if (teamWinners.contains(phTeam))
			{
				// We deliver rewards
				giveItems(ph, ConfigData.getInstance().TVT_REWARD_PLAYER_WIN);
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
	 * Show on screen the number of points that each team
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
			// ph.getPcInstance().sendPacket(new EventParticipantStatus(_pointsRed, _pointsBlue));
		}
	}
}
