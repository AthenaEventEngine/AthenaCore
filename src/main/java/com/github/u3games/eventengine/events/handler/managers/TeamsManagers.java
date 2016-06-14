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
package com.github.u3games.eventengine.events.handler.managers;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.u3games.eventengine.builders.TeamsBuilder;
import com.github.u3games.eventengine.enums.TeamType;
import com.github.u3games.eventengine.events.holders.PlayerHolder;
import com.github.u3games.eventengine.events.holders.TeamHolder;
import com.l2jserver.gameserver.model.Location;

/**
 * @author fissban
 */
public class TeamsManagers
{
	private final Map<TeamType, TeamHolder> _teams = new HashMap<>();
	
	public void createTeams(TeamsBuilder builder)
	{
		// TODO: do something if the teams object is null
		List<TeamHolder> teams = builder.build();
		
		for (TeamHolder team : teams)
		{
			_teams.put(team.getTeamType(), team);
		}
		teams.clear();
	}
	
	/**
	 * Get the collection of created teams
	 * @return
	 */
	public Collection<TeamHolder> getAllTeams()
	{
		return _teams.values();
	}
	
	/**
	 * Get a team by type
	 * @param type
	 * @return
	 */
	public TeamHolder getTeam(TeamType type)
	{
		return _teams.get(type);
	}
	
	/**
	 * Get the team of player
	 * @return
	 */
	public TeamHolder getPlayerTeam(PlayerHolder player)
	{
		return _teams.get(player.getTeamType());
	}
	
	/**
	 * Set the team spawn
	 * @param team
	 * @param loc
	 */
	public void setSpawn(TeamType team, Location loc)
	{
		_teams.get(team).setSpawn(loc);
	}
	
	/**
	 * Get the team spawn
	 * @param team
	 * @return Location
	 */
	public Location getTeamSpawn(TeamType team)
	{
		return _teams.get(team).getSpawn();
	}
}
