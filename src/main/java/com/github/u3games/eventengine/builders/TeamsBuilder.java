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
package com.github.u3games.eventengine.builders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import com.github.u3games.eventengine.config.model.TeamConfig;
import com.github.u3games.eventengine.enums.DistributionType;
import com.github.u3games.eventengine.enums.TeamType;
import com.github.u3games.eventengine.events.holders.PlayerHolder;
import com.github.u3games.eventengine.events.holders.TeamHolder;
import com.l2jserver.gameserver.model.Location;

public class TeamsBuilder
{
	private static final Logger LOGGER = Logger.getLogger(TeamsBuilder.class.getName());
	private final List<List<Location>> mLocations = new ArrayList<>();
	private DistributionType mDistribution = DistributionType.DEFAULT;
	private final Collection<PlayerHolder> mPlayers = new ArrayList<>();

	private final List<TeamHolder> mTeams = new ArrayList<>();

	public TeamsBuilder addTeams(Collection<? extends TeamConfig> teamsConfig) {
		for (TeamConfig config : teamsConfig) {
			mTeams.add(new TeamHolder(config.getName(), config.getColor(), config.getLocations()));
		}

		return this;
	}

	public TeamsBuilder addTeam(List<Location> locations) {
		mLocations.add(locations);
		return this;
	}
	
	public TeamsBuilder addTeams(int amount, List<Location> locations) {
		for (Location loc : locations) {
			List<Location> list = new ArrayList<>();
			list.add(loc);
			mLocations.add(list);
		}
		return this;
	}
	
	public TeamsBuilder setPlayers(Collection<PlayerHolder> list) {
		mPlayers.addAll(list);
		return this;
	}
	
	public TeamsBuilder setDistribution(DistributionType type) {
		mDistribution = type;
		return this;
	}
	
	public List<TeamHolder> build() {
		return mTeams.size() <= 0 ? null : distributePlayers(mTeams);
	}
	
	private List<TeamHolder> createTeams() {
		List<TeamHolder> teams = new ArrayList<>();
		if (mTeams.size() != mLocations.size()) {
			LOGGER.warning(TeamsBuilder.class.getSimpleName() + ": The count of teams and locations doesn't match. Event cancelled!");
			LOGGER.warning(TeamsBuilder.class.getSimpleName() + ": Count of teams: " + teams.size());
			LOGGER.warning(TeamsBuilder.class.getSimpleName() + ": Count of locations: " + mLocations.size());
			return null;
		}

		if (mTeams.size() == 1) {
			TeamType type = TeamType.WHITE;
			teams.add(newTeam(type, mLocations.get(0).get(0))); // TODO: change when we have multiple locations
		} else {
			for (int i = 1; i <= mTeams.size(); i++) {
				TeamType type = TeamType.values()[i];
				teams.add(newTeam(type, mLocations.get(i - 1).get(0))); // TODO: change when we have multiple locations
			}
		}
		return teams;
	}
	
	private TeamHolder newTeam(TeamType type, Location loc) {
		TeamHolder team = new TeamHolder(type);
		team.setSpawn(loc);
		return team;
	}
	
	private List<TeamHolder> distributePlayers(List<TeamHolder> teams) {
		switch (mDistribution) {
			case DEFAULT:
			default:
				int i = 0;
				for (PlayerHolder player : mPlayers) {
					player.setTeam(teams.get(i).getTeamType());

					if (teams.size() <= (i + 1)) i = 0;
					else i++;
				}
				break;
		}
		return teams;
	}
}