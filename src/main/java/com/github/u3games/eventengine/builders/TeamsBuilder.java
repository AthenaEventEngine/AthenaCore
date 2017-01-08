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
import com.github.u3games.eventengine.model.entities.Player;
import com.github.u3games.eventengine.events.holders.TeamHolder;
import com.l2jserver.gameserver.model.Location;

public class TeamsBuilder
{
	private static final Logger LOGGER = Logger.getLogger(TeamsBuilder.class.getName());

	private DistributionType mDistribution = DistributionType.DEFAULT;
	private final Collection<Player> mPlayers = new ArrayList<>();

	private final List<TeamHolder> mTeams = new ArrayList<>();

	public TeamsBuilder addTeam(List<Location> locations) {
		mTeams.add(new TeamHolder("", TeamType.WHITE, locations));
		return this;
	}

	public TeamsBuilder addTeams(Collection<? extends TeamConfig> teamsConfig) {
		for (TeamConfig config : teamsConfig) {
			mTeams.add(new TeamHolder(config.getName(), config.getColor(), config.getLocations()));
		}

		return this;
	}
	
	public TeamsBuilder setPlayers(Collection<Player> list) {
		mPlayers.addAll(list);
		return this;
	}
	
	public TeamsBuilder setDistribution(DistributionType type) {
		mDistribution = type;
		return this;
	}
	
	private List<TeamHolder> distributePlayers(List<TeamHolder> teams) {
		switch (mDistribution) {
			case DEFAULT:
			default:
				int i = 0;
				for (Player player : mPlayers) {
					player.setTeam(teams.get(i));

					if (teams.size() <= (i + 1)) i = 0;
					else i++;
				}
				break;
		}
		return teams;
	}

	public List<TeamHolder> build() {
		if (mTeams.size() <= 0)
		{
			LOGGER.warning(TeamsBuilder.class.getSimpleName() + ": The count of teams can't be zero!");
			return null;
		}

		return distributePlayers(mTeams);
	}
}