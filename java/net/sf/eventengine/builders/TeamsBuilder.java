package net.sf.eventengine.builders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import net.sf.eventengine.enums.DistributionType;
import net.sf.eventengine.enums.TeamType;
import net.sf.eventengine.events.holders.PlayerHolder;
import net.sf.eventengine.events.holders.TeamHolder;

import com.l2jserver.gameserver.model.Location;

public class TeamsBuilder
{
	private static final Logger LOGGER = Logger.getLogger(TeamsBuilder.class.getName());
	
	private int _teamsAmount;
	private List<List<Location>> _locations = new ArrayList<>();
	private DistributionType _distribution = DistributionType.DEFAULT;
	private Collection<PlayerHolder> _players = new ArrayList<>();
	
	public TeamsBuilder addTeams(int amount, List<Location> locations)
	{
		_teamsAmount = amount;
		for (Location loc : locations)
		{
			List<Location> list = new ArrayList<>();
			list.add(loc);
			_locations.add(list);
		}
		return this;
	}
	
	public TeamsBuilder setPlayers(Collection<PlayerHolder> list)
	{
		_players.addAll(list);
		return this;
	}
	
	public TeamsBuilder setDistribution(DistributionType type)
	{
		_distribution = type;
		return this;
	}
	
	public List<TeamHolder> build()
	{
		List<TeamHolder> teams = createTeams();
		
		if (teams == null)
		{
			return null;
		}
		
		return distributePlayers(teams);
	}
	
	private List<TeamHolder> createTeams()
	{
		List<TeamHolder> teams = new ArrayList<>();
		
		if (_teamsAmount != _locations.size())
		{
			LOGGER.warning(TeamsBuilder.class.getSimpleName() + ": The count of teams and locations doesn't match. Event cancelled!");
			LOGGER.warning(TeamsBuilder.class.getSimpleName() + ": Count of teams: " + teams.size());
			LOGGER.warning(TeamsBuilder.class.getSimpleName() + ": Count of locations: " + _locations.size());
			return null;
		}
		
		if (_teamsAmount == 1)
		{
			TeamType type = TeamType.WHITE;
			teams.add(newTeam(type, _locations.get(0).get(0))); // TODO: change when we have multiple locations
		}
		else
		{
			for (int i = 1; i <= _teamsAmount; i++)
			{
				TeamType type = TeamType.values()[i];
				teams.add(newTeam(type, _locations.get(i - 1).get(0))); // TODO: change when we have multiple locations
			}
		}
		
		return teams;
	}
	
	private TeamHolder newTeam(TeamType type, Location loc)
	{
		TeamHolder team = new TeamHolder(type);
		team.setSpawn(loc);
		return team;
	}
	
	private List<TeamHolder> distributePlayers(List<TeamHolder> teams)
	{
		switch (_distribution)
		{
			case DEFAULT:
			default:
				int i = 0;
				for (PlayerHolder player : _players)
				{
					player.setTeam(teams.get(i).getTeamType());
					
					if (teams.size() <= i + 1)
					{
						i = 0;
					}
					else
					{
						i++;
					}
				}
				break;
		}
		return teams;
	}
}
