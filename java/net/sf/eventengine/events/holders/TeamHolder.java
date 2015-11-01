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
package net.sf.eventengine.events.holders;

import java.util.concurrent.atomic.AtomicInteger;

import com.l2jserver.gameserver.model.Location;

import net.sf.eventengine.enums.TeamType;
import net.sf.eventengine.interfaces.ParticipantHolder;

/**
 * Class responsible for managing the information and actions of equipment.
 * @author fissban
 */
public class TeamHolder implements ParticipantHolder
{
	// Type of team
	private TeamType _teamType;
	// Number of points
	private AtomicInteger _points = new AtomicInteger(0);
	// Spawn team.
	private Location _teamSpawn = new Location(0, 0, 0);
	
	public TeamHolder(TeamType teamColor)
	{
		_teamType = teamColor;
	}
	
	/**
	 * TeamType.
	 * @return TeamType
	 */
	public TeamType getTeamType()
	{
		return _teamType;
	}
	
	/**
	 * Team spawn defined.
	 * @param loc
	 */
	public void setSpawn(Location loc)
	{
		_teamSpawn = loc;
	}
	
	/**
	 * Team Spawn
	 * @return Location
	 */
	public Location getSpawn()
	{
		return _teamSpawn;
	}
	
	/**
	 * Points of team
	 * @return int
	 */
	public int getPoints()
	{
		return _points.intValue();
	}
	
	/**
	 * Team kills
	 * @return int
	 */
	public int getKills()
	{
		return 0; // TODO: Do it
	}
	
	/**
	 * Team deaths
	 * @return int
	 */
	public int getDeaths()
	{
		return 0; // TODO: Do it
	}
	
	/**
	 * Increasing the amount of points
	 */
	public void increasePoints(int points)
	{
		_points.getAndAdd(points);
	}
	
	/**
	 * Decrease the amount of points
	 */
	public void decreasePoints(int points)
	{
		_points.getAndAdd(-points);
		// We warn that the team has negative points
		// FIXME check if some event requires to be negative.
		if (_points.intValue() < 0)
		{
			_points.set(0);
		}
	}
}
