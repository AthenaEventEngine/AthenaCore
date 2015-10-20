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
package net.sf.eventengine.events.handler.managers;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.l2jserver.gameserver.model.Location;

import net.sf.eventengine.enums.TeamType;
import net.sf.eventengine.events.handler.AbstractEvent;
import net.sf.eventengine.events.holders.PlayerHolder;
import net.sf.eventengine.events.holders.TeamHolder;

/**
 * @author fissban
 */
public class TeamsManagers
{
	private static final Logger LOGGER = Logger.getLogger(TeamsManagers.class.getName());
	
	private final Map<TeamType, TeamHolder> _teams = new HashMap<>();
	private int _countTeams = 0;
	
	/**
	 * Constructor
	 */
	public TeamsManagers()
	{
		//
	}
	
	/**
	 * Creamos la cantidad de teams indicados
	 * @param count
	 */
	public void setCountTeams(int count)
	{
		_countTeams = count;
		// inicializamos el uno para evitar usar el color blanco como team.
		for (int i = 1; i <= _countTeams; i++)
		{
			TeamType team = TeamType.values()[i];
			_teams.put(team, new TeamHolder(team));
		}
	}
	
	/**
	 * Obtenemos un array con los TeamType de equipos creados previamente.
	 * @return
	 */
	public TeamType[] getEnabledTeams()
	{
		return _teams.keySet().toArray(new TeamType[_countTeams]);
	}
	
	/**
	 * Obtenemos una colleccion con los teams creados previamente.
	 * @return
	 */
	public Collection<TeamHolder> getAllTeams()
	{
		return _teams.values();
	}
	
	/**
	 * Obtenemos un team a partir del TeamType
	 * @param t
	 * @return
	 */
	public TeamHolder getTeam(TeamType t)
	{
		return _teams.get(t);
	}
	
	/**
	 * Obtenemos el team de un personaje
	 * @return
	 */
	public TeamHolder getPlayerTeam(PlayerHolder player)
	{
		return _teams.get(player.getTeamType());
	}
	
	/**
	 * We define a team spawns.
	 * @param team
	 * @param locs
	 */
	public void setSpawnTeams(List<Location> locs)
	{
		if (locs.size() < _countTeams)
		{
			LOGGER.warning(AbstractEvent.class.getSimpleName() + ": No se han definido correctamente los spawns para los teams.");
			LOGGER.warning(AbstractEvent.class.getSimpleName() + ": Cantidad de teams: " + _countTeams);
			LOGGER.warning(AbstractEvent.class.getSimpleName() + ": Cantidad de locs: " + locs.size());
			return;
		}
		
		for (int i = 0; i < _countTeams; i++)
		{
			setSpawn(TeamType.values()[i + 1], locs.get(i));
		}
	}
	
	/**
	 * We define a team spawns.
	 * @param team
	 * @param loc
	 */
	public void setSpawn(TeamType team, Location loc)
	{
		_teams.get(team).setSpawn(loc);
	}
	
	/**
	 * We get the spawn of a particular team.
	 * @param team
	 * @return Location
	 */
	public Location getTeamSpawn(TeamType team)
	{
		return _teams.get(team).getSpawn();
	}
}
