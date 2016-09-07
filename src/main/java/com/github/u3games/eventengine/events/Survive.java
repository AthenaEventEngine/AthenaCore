/*
 * Copyright (C) 2015-2016 Athena Event Engine.
 *
 * This file is part of Athena Event Engine.
 *
 * Athena Event Engine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Athena Event Engine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.u3games.eventengine.events;

import java.util.List;

import com.github.u3games.eventengine.builders.TeamsBuilder;
import com.github.u3games.eventengine.datatables.ConfigData;
import com.github.u3games.eventengine.enums.CollectionTarget;
import com.github.u3games.eventengine.enums.ScoreType;
import com.github.u3games.eventengine.events.handler.AbstractEvent;
import com.github.u3games.eventengine.events.holders.PlayerHolder;
import com.github.u3games.eventengine.events.holders.TeamHolder;
import com.github.u3games.eventengine.util.EventUtil;
import com.github.u3games.eventengine.util.SortUtils;
import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.enums.Team;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.network.clientpackets.Say2;
import com.l2jserver.util.Rnd;

/**
 * Event survival.<br>
 * One team will be created and will have to survive several waves of mobs.<br>
 * @author fissban
 */
public class Survive extends AbstractEvent
{
	// Variable that controls the level of the stage
	private int _stage = 1;
	// Variable that helps us keep track of the number of dead mobs
	private int _auxKillMonsters = 0;
	// Radius spawn
	protected int _radius = 200;
	// Monsters Id's
	private final List<Integer> MONSTERS_ID = ConfigData.getInstance().SURVIVE_MONSTERS_ID;
	
	public Survive()
	{
		super(ConfigData.getInstance().SURVIVE_INSTANCE_FILE);
	}
	
	@Override
	protected TeamsBuilder onCreateTeams()
	{
		return new TeamsBuilder().addTeams(ConfigData.getInstance().SURVIVE_COUNT_TEAM, ConfigData.getInstance().SURVIVE_COORDINATES_TEAM).setPlayers(getPlayerEventManager().getAllEventPlayers());
	}
	
	@Override
	protected void onEventStart()
	{
		for (PlayerHolder ph : getPlayerEventManager().getAllEventPlayers())
		{
			updateTitle(ph);
		}
	}
	
	@Override
	protected void onEventFight()
	{
		spawnsMobs();
	}
	
	@Override
	protected void onEventEnd()
	{
		giveRewardsTeams();
	}
	
	@Override
	public void onKill(PlayerHolder ph, L2Character target)
	{
		// Incremented by one the amount of points of the team
		getTeamsManager().getPlayerTeam(ph).increasePoints(1);
		// Update title character
		updateTitle(ph);
		// One increasing the amount of dead mobs
		_auxKillMonsters++;
		// Verify the number of dead mobs, if any killed all increase by one the stage
		if (_auxKillMonsters >= (_stage * ConfigData.getInstance().SURVIVE_MONSTER_SPAWN_FOR_STAGE))
		{
			// Increase by one the stage
			_stage++;
			// We restart our assistant
			_auxKillMonsters = 0;
			// Spawns Mobs
			spawnsMobs();
			// Give rewards
			giveRewardsTeams();
		}
		// Message Kill
		if (ConfigData.getInstance().EVENT_KILLER_MESSAGE)
		{
			EventUtil.messageKill(ph, target);
		}
	}
	
	@Override
	public boolean onAttack(PlayerHolder ph, L2Character target)
	{
		if (target.isPlayable())
		{
			return true;
		}
		return false;
	}
	
	// MISC ---------------------------------------------------------------------------------------
	/**
	 * Only we deliver reward the team that killed more monsters.
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
			// FIXME add to language system
			EventUtil.sendEventScreenMessage(ph, "Congratulations survivor!");
			TeamHolder phTeam = getTeamsManager().getPlayerTeam(ph);
			// We deliver rewards
			if (teamWinners.contains(phTeam))
			{
				giveItems(ph, ConfigData.getInstance().SURVIVE_REWARD_PLAYER_WIN);
			}
		}
		
		for (TeamHolder team : getTeamsManager().getAllTeams())
		{
			if (teamWinners.contains(team))
			{
				EventUtil.announceTo(Say2.BATTLEFIELD, "team_winner", "%holder%", team.getTeamType().name(), CollectionTarget.ALL_PLAYERS_IN_EVENT);
			}
			else
			{
				EventUtil.announceTo(Say2.BATTLEFIELD, "teams_tie", "%holder%", team.getTeamType().name(), CollectionTarget.ALL_PLAYERS_IN_EVENT);
			}
		}
	}
	
	private void spawnsMobs()
	{
		EventUtil.announceTo(Say2.BATTLEFIELD, "survive_spawns_mobs", CollectionTarget.ALL_PLAYERS_IN_EVENT);
		// After 5 secs spawn run
		ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			for (int i = 0; i < (_stage * ConfigData.getInstance().SURVIVE_MONSTER_SPAWN_FOR_STAGE); i++)
			{
				getSpawnManager().addEventNpc(MONSTERS_ID.get(Rnd.get(MONSTERS_ID.size() - 1)), ConfigData.getInstance().SURVIVE_COORDINATES_MOBS, Team.RED, true, getInstanceWorldManager().getAllInstancesWorlds().get(0).getInstanceId());
			}
			// We notify the characters in the event that stage they are currently
			for (PlayerHolder ph : getPlayerEventManager().getAllEventPlayers())
			{
				// FIXME add to language system
				EventUtil.sendEventScreenMessage(ph, "Stage " + _stage, 5000);
			}
		}, 5000L);
	}
	
	/**
	 * We update the title of a character depending on the number of murders that have.<br>
	 * @param player
	 */
	private void updateTitle(PlayerHolder player)
	{
		// FIXME add to language system
		// Adjust the title character
		player.setNewTitle("Monster Death " + player.getKills());
		// Adjust the status character
		player.getPcInstance().updateAndBroadcastStatus(2);
	}
}