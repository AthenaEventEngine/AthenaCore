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
package com.github.athenaengine.core.events.holders;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.github.athenaengine.core.enums.ScoreType;
import com.github.athenaengine.core.enums.TeamType;
import com.github.athenaengine.core.interfaces.ParticipantHolder;
import com.github.athenaengine.core.model.ELocation;
import com.l2jserver.util.Rnd;

/**
 * @author fissban
 */
public class TeamHolder implements ParticipantHolder
{
	private String _teamName;
	private final TeamType _teamType;
	private final List<ELocation> _teamSpawns = new ArrayList<>();
	private final Map<ScoreType, Integer> _points = new ConcurrentHashMap<>();
	private int _instanceId;
	
	/**
	 * Constructor.
	 * @param teamColor
	 */
	public TeamHolder(String teamName, TeamType teamColor, Collection<ELocation> spawns)
	{
		_teamName = teamName;
		_teamType = teamColor;
		setSpawns(spawns);
	}

	public String getName()
	{
		return _teamName;
	}

	/**
	 * Get the spawn of a team.
	 * @return ELocation
	 */
	public ELocation getRndSpawn()
	{
		return _teamSpawns.size() <= 0 ? null : _teamSpawns.get(Rnd.get(_teamSpawns.size() - 1));
	}

	public List<ELocation> getSpawns() {
		return _teamSpawns;
	}

	public void setSpawns(Collection<ELocation> locs)
	{
		_teamSpawns.addAll(locs);
	}

	public int getInstanceId()
	{
		return _instanceId;
	}

	public void addInstanceIdToSpawns(int instanceId)
	{
		_instanceId = instanceId;

		for (ELocation loc : getSpawns()) {
			loc.setInstanceId(instanceId);
		}
	}

	/**
	 * Get Team color.
	 * @return TeamType
	 */
	public TeamType getTeamType()
	{
		return _teamType;
	}

	/**
	 * Get the player's points.
	 * @return Amount of points
	 */
	@Override
	public int getPoints(ScoreType type) {
		if (!_points.containsKey(type)) _points.put(type, 0);
		return _points.get(type);
	}

	/**
	 * Add an amount of points.
	 */
	@Override
	public void increasePoints(ScoreType type, int points) {
		if (!_points.containsKey(type)) _points.put(type, 0);
		_points.put(type, getPoints(type) + points);
	}
}