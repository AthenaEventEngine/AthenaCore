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

import java.util.concurrent.atomic.AtomicInteger;

import com.github.u3games.eventengine.enums.TeamType;
import com.github.u3games.eventengine.interfaces.ParticipantHolder;
import com.l2jserver.gameserver.model.Location;

/**
 * @author fissban
 */
public class TeamHolder implements ParticipantHolder
{
	// Tipo de team
	private TeamType _teamType;
	// Cantidad de puntos
	private AtomicInteger _points = new AtomicInteger(0);
	// Spawn del team
	private Location _teamSpawn = new Location(0, 0, 0);
	
	/**
	 * Constructor
	 * @param teamColor
	 */
	public TeamHolder(TeamType teamColor)
	{
		_teamType = teamColor;
	}
	
	/**
	 * Obtenemos el color del team
	 * @return
	 */
	public TeamType getTeamType()
	{
		return _teamType;
	}
	
	/**
	 * Definimos el spawn de un team.
	 * @param loc
	 */
	public void setSpawn(Location loc)
	{
		_teamSpawn = loc;
	}
	
	/**
	 * Obtenemos el spawn de un team.
	 * @return
	 */
	public Location getSpawn()
	{
		return _teamSpawn;
	}
	
	/**
	 * Puntos del team
	 * @return
	 */
	public int getPoints()
	{
		return _points.intValue();
	}
	
	/**
	 * Team kills
	 * @return
	 */
	public int getKills()
	{
		return 0; // TODO: Do it
	}
	
	/**
	 * Team deaths
	 * @return
	 */
	public int getDeaths()
	{
		return 0; // TODO: Do it
	}
	
	/**
	 * Incrementamos la cantidad de puntos
	 */
	public void increasePoints(int points)
	{
		_points.getAndAdd(points);
	}
	
	/**
	 * Disminiumos la cantidad de puntos
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