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

import net.sf.eventengine.enums.TeamType;
import net.sf.eventengine.model.Locations;

/**
 * @author fissban
 */
public class TeamHolder
{
	// Team type
	private TeamType _teamType;
	// Amount of points
	private AtomicInteger _points = new AtomicInteger(0);
	// List of team spawns
	private Locations _teamSpawns;
	
	/**
	 * Constructor
	 * @param teamColor
	 */
	public TeamHolder(TeamType teamColor)
	{
		_teamType = teamColor;
	}
	
	/**
	 * Get the team color
	 * @return
	 */
	public TeamType getTeamType()
	{
		return _teamType;
	}
	
	/**
	 * Set the team spawns
	 * @param loc
	 */
	public void setSpawns(Locations locs)
	{
		_teamSpawns = locs;
	}
	
	/**
	 * Get the team spawns
	 * @return
	 */
	public Locations getSpawns()
	{
		return _teamSpawns;
	}
	
	/**
	 * Get team points
	 * @return
	 */
	public int getPoints()
	{
		return _points.intValue();
	}
	
	/**
	 * Increase the points
	 */
	public void increasePoints(int points)
	{
		_points.getAndAdd(points);
	}
	
	/**
	 * Reduce the points
	 */
	public void decreasePoints(int points)
	{
		_points.getAndAdd(-points);
		// prevenimos q el equipo tenga puntos negativos
		// FIXME revisar por si algun evento requiera que sean negativos.
		if (_points.intValue() < 0)
		{
			_points.set(0);
		}
	}
}
