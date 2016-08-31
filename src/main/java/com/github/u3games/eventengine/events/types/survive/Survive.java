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
package com.github.u3games.eventengine.events.types.survive;

import java.util.List;

import com.github.u3games.eventengine.builders.TeamsBuilder;
import com.github.u3games.eventengine.config.BaseConfigLoader;
import com.github.u3games.eventengine.dispatcher.events.OnAttackEvent;
import com.github.u3games.eventengine.dispatcher.events.OnKillEvent;
import com.github.u3games.eventengine.enums.CollectionTarget;
import com.github.u3games.eventengine.enums.ListenerType;
import com.github.u3games.eventengine.enums.ScoreType;
import com.github.u3games.eventengine.events.handler.AbstractEvent;
import com.github.u3games.eventengine.events.holders.PlayerHolder;
import com.github.u3games.eventengine.events.holders.TeamHolder;
import com.github.u3games.eventengine.util.EventUtil;
import com.github.u3games.eventengine.util.SortUtils;
import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.enums.Team;
import com.l2jserver.gameserver.model.Location;
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
	// Monsters Id's
	private final List<Integer> MONSTERS_ID = getConfig().getMobsID();
	
	public Survive()
	{
		super(getConfig().getInstanceFile());
	}

	private static SurviveEventConfig getConfig()
	{
		return BaseConfigLoader.getInstance().getSurviveConfig();
	}
	
	@Override
	protected TeamsBuilder onCreateTeams()
	{
		return new TeamsBuilder()
				.addTeam(getConfig().getCoordinates())
				.setPlayers(getPlayerEventManager().getAllEventPlayers());
	}
	
	@Override
	protected void onEventStart()
	{
		addSuscription(ListenerType.ON_KILL);
		addSuscription(ListenerType.ON_ATTACK);

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
	public void onKill(OnKillEvent event)
	{
		PlayerHolder ph = getPlayerEventManager().getEventPlayer(event.getAttacker());
		L2Character target = event.getTarget();

		// Incremented by one the amount of points of the team
		getTeamsManager().getPlayerTeam(ph).increasePoints(1);
		// Update title character
		updateTitle(ph);
		// One increasing the amount of dead mobs
		_auxKillMonsters++;
		// Verify the number of dead mobs, if any killed all increase by one the stage
		if (_auxKillMonsters >= (_stage * getConfig().getMobsSpawnForStage()))
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
		if (BaseConfigLoader.getInstance().getMainConfig().isKillerMessageEnabled())
		{
			EventUtil.messageKill(ph, target);
		}
	}
	
	@Override
	public void onAttack(OnAttackEvent event)
	{
		event.setCancel(event.getTarget().isPlayable());
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
				giveItems(ph, getConfig().getReward());
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
			for (int i = 0; i < (_stage * getConfig().getMobsSpawnForStage()); i++)
			{
				Location rndLoc = getConfig().getCoordinatesMobs().get(Rnd.get(getConfig().getCoordinatesMobs().size() - 1));
				getSpawnManager().addEventNpc(MONSTERS_ID.get(Rnd.get(MONSTERS_ID.size() - 1)), rndLoc, Team.RED, true, getInstanceWorldManager().getAllInstancesWorlds().get(0).getInstanceId());
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