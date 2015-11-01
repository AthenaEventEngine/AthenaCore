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

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

import net.sf.eventengine.enums.TeamType;
import net.sf.eventengine.interfaces.ParticipantHolder;

/**
 * Class responsible for managing the data of players participating in the event.
 * @author fissban
 */
public class PlayerHolder implements ParticipantHolder
{
	private final L2PcInstance _player;
	
	// Count kills obtained.
	private int _kills = 0;
	// Count deaths obtained.
	private int _deaths = 0;
	// Original color of a character
	private int _oriColorTitle;
	// Title of a character.
	private String _oriTitle;
	// Team that owns the character.
	private TeamType _team;
	
	private int _dinamicInstanceId = 0;
	
	/**
	 * Constructor
	 * @param player
	 */
	public PlayerHolder(L2PcInstance player)
	{
		_player = player;
	}
	
	// METODOS VARIOS -----------------------------------------------------------
	
	/**
	 * Direct access to all methods of L2PcInstance.
	 * @return L2PcInstance
	 */
	public L2PcInstance getPcInstance()
	{
		return _player;
	}
	
	/**
	 * <ul>
	 * <b>Actions:</b>
	 * </ul>
	 * <li>The color of the title and the title character is stored.</li>
	 * <li>The team that defined the character belongs.</li>
	 * <li>The color of the character fits./li>
	 * @param team
	 */
	public void setTeam(TeamType team)
	{
		// The color of the title and the title character is stored..
		_oriColorTitle = _player.getAppearance().getTitleColor();
		_oriTitle = _player.getTitle();
		// Set team of character
		_team = team;
		// New Color of title character by team
		_player.getAppearance().setTitleColor(team.getColor());
	}
	
	/**
	 * The team's character is obtained.
	 * @return TeamType
	 */
	public TeamType getTeamType()
	{
		return _team;
	}
	
	/**
	 * The id of the instance in which it participates in the events is obtained.
	 * @return
	 */
	public int getDinamicInstanceId()
	{
		return _dinamicInstanceId;
	}
	
	/**
	 * The id of the instance of which participates in the events defined.
	 * @param dinamicInstanceId
	 */
	public void setDinamicInstanceId(int dinamicInstanceId)
	{
		_dinamicInstanceId = dinamicInstanceId;
	}
	
	/**
	 * Increases by one the number of muerders.
	 */
	public void increaseKills()
	{
		_kills++;
	}
	
	/**
	 * The number of murders is obtained.
	 * @return int
	 */
	public int getKills()
	{
		return _kills;
	}
	
	/**
	 * Increases by one the number of deaths.
	 */
	public void increaseDeaths()
	{
		_deaths++;
	}
	
	/**
	 * Number of deaths has.
	 * @return int
	 */
	public int getDeaths()
	{
		return _deaths;
	}
	
	/**
	 * The amount of points a character is obtained.<br>
	 * The formula is obtained from: (_kills-_deaths)<br>
	 * @return
	 */
	public int getPoints()
	{
		return _kills - _deaths;
	}
	
	/**
	 * A new title for the character
	 * @param title
	 */
	public void setNewTitle(String title)
	{
		_player.setTitle(title);
	}
	
	/**
	 * Recover original title
	 */
	public void recoverOriginalTitle()
	{
		_player.setTitle(_oriTitle);
	}
	
	/**
	 * Recover original color title
	 */
	public void recoverOriginalColorTitle()
	{
		_player.getAppearance().setTitleColor(_oriColorTitle);
	}
}
