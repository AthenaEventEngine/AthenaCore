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
	
	public TeamsManagers()
	{
		//
	}
	
	/**
	 * It creates a certain amount of equipment
	 * @param count
	 */
	public void setCountTeams(int count)
	{
		_countTeams = count;
		// It starts in one to avoid using white as team.
		for (int i = 1; i <= _countTeams; i++)
		{
			TeamType team = TeamType.values()[i];
			_teams.put(team, new TeamHolder(team));
		}
	}
	
	/**
	 * It is an array of previously created TeamType equipment.
	 * @return
	 */
	public TeamType[] getEnabledTeams()
	{
		return _teams.keySet().toArray(new TeamType[_countTeams]);
	}
	
	/**
	 * It is a collection of pre-made teams.
	 * @return
	 */
	public Collection<TeamHolder> getAllTeams()
	{
		return _teams.values();
	}
	
	/**
	 * A subject is obtained from TeamType
	 * @param t
	 * @return
	 */
	public TeamHolder getTeam(TeamType t)
	{
		return _teams.get(t);
	}
	
	/**
	 * The team is a character
	 * @return
	 */
	public TeamHolder getPlayerTeam(PlayerHolder player)
	{
		return _teams.get(player.getTeamType());
	}
	
	/**
	 * It spawns teams defined.
	 * @param team
	 * @param locs
	 */
	public void setSpawnTeams(List<Location> locs)
	{
		if (locs.size() < _countTeams)
		{
			LOGGER.warning(AbstractEvent.class.getSimpleName() + ": No set correctly spawns for teams.");
			LOGGER.warning(AbstractEvent.class.getSimpleName() + ": Number of teams: " + _countTeams);
			LOGGER.warning(AbstractEvent.class.getSimpleName() + ": Number of locs: " + locs.size());
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
