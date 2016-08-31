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
package com.github.u3games.eventengine.events.holders;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.u3games.eventengine.enums.TeamType;
import com.github.u3games.eventengine.interfaces.ParticipantHolder;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.util.Rnd;

/**
 * @author fissban
 */
public class TeamHolder implements ParticipantHolder
{
	private String _teamName;
	// Type of team
	private final TeamType _teamType;
	// Amount of points
	private final AtomicInteger _points = new AtomicInteger(0);
	// Team Spawn
	private Location _teamSpawn = new Location(0, 0, 0);
	private final List<Location> _teamSpawns = new ArrayList<>();
	
	/**
	 * Constructor.
	 * @param teamColor
	 */
	public TeamHolder(String teamName, TeamType teamColor, Collection<Location> spawns)
	{
		_teamName = teamName;
		_teamType = teamColor;
		setSpawns(spawns);
	}

	public TeamHolder(TeamType teamColor)
	{
		_teamType = teamColor;
	}

	public String getName()
	{
		return _teamName;
	}
	
	/**
	 * Get Team color.
	 * @return
	 */
	public TeamType getTeamType()
	{
		return _teamType;
	}
	
	/**
	 * Define the spawn of a team.
	 * @param loc
	 */
	public void setSpawn(Location loc)
	{
		_teamSpawn = loc;
	}

	public void setSpawns(Collection<Location> locs)
	{
		_teamSpawns.addAll(locs);
	}
	
	/**
	 * Get the spawn of a team.
	 * @return
	 */
	public Location getSpawn()
	{
		// TODO Remove this
		if (_teamSpawns.size() <= 0)
		{
			return _teamSpawn;
		}

		return _teamSpawns.get(Rnd.get(_teamSpawns.size() - 1));
	}
	
	/**
	 * Points of team.
	 * @return
	 */
	@Override
	public int getPoints()
	{
		return _points.intValue();
	}
	
	/**
	 * Team kills.
	 * @return
	 */
	@Override
	public int getKills()
	{
		return 0; // TODO: Do it
	}
	
	/**
	 * Team deaths.
	 * @return
	 */
	@Override
	public int getDeaths()
	{
		return 0; // TODO: Do it
	}
	
	/**
	 * Increase the number of points.
	 * @param points
	 */
	public void increasePoints(int points)
	{
		_points.getAndAdd(points);
	}
	
	/**
	 * Decreasing the amount of points.
	 * @param points
	 */
	public void decreasePoints(int points)
	{
		_points.getAndAdd(-points);
		// Prevent that the team has negative points
		// FIXME Check if any event requires to be negative.
		if (_points.intValue() < 0)
		{
			_points.set(0);
		}
	}
}