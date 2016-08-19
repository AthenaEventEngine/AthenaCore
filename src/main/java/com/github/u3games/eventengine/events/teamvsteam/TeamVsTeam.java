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
package com.github.u3games.eventengine.events.teamvsteam;

import java.util.List;

import com.github.u3games.eventengine.builders.TeamsBuilder;
import com.github.u3games.eventengine.config.BaseConfigLoader;
import com.github.u3games.eventengine.datatables.MessageData;
import com.github.u3games.eventengine.enums.CollectionTarget;
import com.github.u3games.eventengine.enums.ScoreType;
import com.github.u3games.eventengine.events.handler.AbstractEvent;
import com.github.u3games.eventengine.events.holders.PlayerHolder;
import com.github.u3games.eventengine.events.holders.TeamHolder;
import com.github.u3games.eventengine.util.EventUtil;
import com.github.u3games.eventengine.util.SortUtils;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.network.clientpackets.Say2;

/**
 * @author fissban
 */
public class TeamVsTeam extends AbstractEvent
{
	// Time for resurrection
	private static final int TIME_RES_PLAYER = 10;
	// Radius spawn
	protected int _radius = 100;
	
	public TeamVsTeam()
	{
		super(getConfig().getInstanceFile());
	}

	private static TvTEventConfig getConfig() {
		return BaseConfigLoader.getInstance().getTvTConfig();
	}
	
	@Override
	protected TeamsBuilder onCreateTeams()
	{
		return new TeamsBuilder().addTeam(getConfig().getTeamBlue())
				.addTeam(getConfig().getTeamRed())
				.setPlayers(getPlayerEventManager().getAllEventPlayers());
	}
	
	@Override
	protected void onEventStart()
	{
		// Nothing
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
		// We increased the team's points
		getTeamsManager().getPlayerTeam(ph).increasePoints(1);
		
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
	public void onDeath(PlayerHolder ph)
	{
		giveResurrectPlayer(ph, TIME_RES_PLAYER, _radius);
		// Incremented by one the number of deaths Character
		ph.increaseDeaths();
	}
	
	// VARIOUS METHODS -------------------------------------------------
	/**
	 * Give the rewards.
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
				giveItems(ph, getConfig().getReward());
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
			// ph.getPcInstance().sendPacket(new EventParticipantStatus(_pointsRed, _pointsBlue));
		}
	}
}