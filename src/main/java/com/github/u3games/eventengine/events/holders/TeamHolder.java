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
package com.github.u3games.eventengine.events.holders;

import java.util.concurrent.atomic.AtomicInteger;

import com.github.u3games.eventengine.enums.TeamType;
import com.github.u3games.eventengine.interfaces.ParticipantHolder;
import com.l2jserver.gameserver.model.Location;

/**
 * @author fissban
 */
public class TeamHolder implements ParticipantHolder
{
	// Type of team
	private final TeamType _teamType;
	// Amount of points
	private final AtomicInteger _points = new AtomicInteger(0);
	// Team Spawn
	private Location _teamSpawn = new Location(0, 0, 0);
	
	/**
	 * Constructor.
	 * @param teamColor
	 */
	public TeamHolder(TeamType teamColor)
	{
		_teamType = teamColor;
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
	
	/**
	 * Get the spawn of a team.
	 * @return
	 */
	public Location getSpawn()
	{
		return _teamSpawn;
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