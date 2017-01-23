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
package com.github.athenaengine.core.managers;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.athenaengine.core.events.holders.TeamHolder;
import com.github.athenaengine.core.builders.TeamsBuilder;
import com.github.athenaengine.core.enums.TeamType;
import com.github.athenaengine.core.model.ELocation;
import com.github.athenaengine.core.model.entity.Player;

/**
 * @author fissban
 */
public class TeamsManagers
{
	private final Map<TeamType, TeamHolder> _teams = new HashMap<>();
	
	public void createTeams(TeamsBuilder builder, int instanceId)
	{
		// TODO: do something if the teams object is null
		List<TeamHolder> teams = builder.build();
		for (TeamHolder team : teams)
		{
			_teams.put(team.getTeamType(), team);
			team.addInstanceIdToSpawns(instanceId);
		}
		teams.clear();
	}
	
	/**
	 * Get the collection of created teams.
	 * @return
	 */
	public Collection<TeamHolder> getAllTeams()
	{
		return _teams.values();
	}
	
	/**
	 * Get a team by type.
	 * @param type
	 * @return
	 */
	public TeamHolder getTeam(TeamType type)
	{
		return _teams.get(type);
	}
	
	/**
	 * Get the team of player.
	 * @param player
	 * @return
	 */
	public TeamHolder getPlayerTeam(Player player)
	{
		return _teams.get(player.getTeamType());
	}
	
	/**
	 * Get the team spawn.
	 * @param team
	 * @return ELocation
	 */
	public ELocation getTeamSpawn(TeamType team)
	{
		return _teams.get(team).getRndSpawn();
	}
}