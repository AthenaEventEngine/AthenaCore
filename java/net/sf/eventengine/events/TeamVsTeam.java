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

import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.network.clientpackets.Say2;

import net.sf.eventengine.datatables.ConfigData;
import net.sf.eventengine.datatables.MessageData;
import net.sf.eventengine.enums.CollectionTarget;
import net.sf.eventengine.enums.EventState;
import net.sf.eventengine.enums.TeamType;
import net.sf.eventengine.events.handler.AbstractEvent;
import net.sf.eventengine.events.holders.PlayerHolder;
import net.sf.eventengine.events.holders.TeamHolder;
import net.sf.eventengine.events.schedules.AnnounceNearEndEvent;
import net.sf.eventengine.util.EventUtil;
import net.sf.eventengine.util.SortUtil;

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
		super();
		// Definimos la instancia en que transcurria el evento
		setInstanceFile(ConfigData.getInstance().TVT_INSTANCE_FILE);
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
				createTeams(ConfigData.getInstance().TVT_COUNT_TEAM);
				teleportAllPlayers(RADIUS_SPAWN_PLAYER);
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
	public boolean onUseSkill(PlayerHolder ph, L2Character target, Skill skill)
	{
		return false;
	}
	
	@Override
	public boolean onAttack(PlayerHolder ph, L2Character target)
	{
		return false;
	}
	
	@Override
	public void onKill(PlayerHolder ph, L2Character target)
	{
		// We increased the team's points.
		getPlayerTeam(ph).increasePoints(1);
		
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
	
	@Override
	public boolean onInteract(PlayerHolder ph, L2Npc npc)
	{
		return true;
	}
	
	@Override
	public boolean onUseItem(PlayerHolder ph, L2Item item)
	{
		return false;
	}
	
	@Override
	public void onLogout(PlayerHolder ph)
	{
		//
	}
	
	// VARIOUS METHODS -------------------------------------------------
	
	/**
	 * We all players who are at the event and generate the teams
	 * @param int countTeams
	 */
	private void createTeams(int countTeams)
	{
		// Definimos la cantidad de teams que se requieren
		setCountTeams(countTeams);
		// We define each team spawns
		setSpawnTeams(ConfigData.getInstance().TVT_COORDINATES_TEAM);
		// We create the instance and the world
		InstanceWorld world = createNewInstanceWorld();
		
		int aux = 1;
		
		for (PlayerHolder ph : getAllEventPlayers())
		{
			// Obtenemos el team
			TeamType team = getEnabledTeams()[aux - 1];
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
		// Obtenemos una lista ordenada de los que obtuvieron mas puntos.
		List<TeamHolder> winners = SortUtil.getOrderedByPoints(getAllTeams(), 1).get(0);
		
		for (PlayerHolder ph : getAllEventPlayers())
		{
			// We deliver rewards
			if (winners.contains(ph.getTeamType()))
			{
				// We deliver rewards
				giveItems(ph, ConfigData.getInstance().TVT_REWARD_PLAYER_WIN);
			}
		}
		
		for (TeamHolder team : getAllTeams())
		{
			if (winners.contains(team))
			{
				EventUtil.announceTo(Say2.BATTLEFIELD, "team_winner", "%holder%", team.getTeamType().name(), CollectionTarget.ALL_PLAYERS_IN_EVENT);
			}
			else
			{
				EventUtil.announceTo(Say2.BATTLEFIELD, "teams_tie", "%holder%", team.getTeamType().name(), CollectionTarget.ALL_PLAYERS_IN_EVENT);
			}
		}
	}
	
	/**
	 * Show on screen the number of points that each team
	 */
	private void showPoint()
	{
		StringBuilder sb = new StringBuilder();
		
		for (TeamHolder team : getAllTeams())
		{
			sb.append(" | ");
			sb.append(team.getTeamType().name());
			sb.append(" ");
			sb.append(team.getPoints());
		}
		sb.append(" | ");
		
		for (PlayerHolder ph : getAllEventPlayers())
		{
			EventUtil.sendEventScreenMessage(ph, sb.toString(), 10000);
			// ph.getPcInstance().sendPacket(new EventParticipantStatus(_pointsRed, _pointsBlue));
		}
	}
}
