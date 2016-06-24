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

import com.github.u3games.eventengine.enums.TeamType;
import com.github.u3games.eventengine.interfaces.ParticipantHolder;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * It manages player's info that participates in an event
 * @author fissban
 */
public class PlayerHolder implements ParticipantHolder
{
	private final L2PcInstance _player;
	// Player kills in current event
	private int _kills = 0;
	// Player deaths in current event
	private int _deaths = 0;
	// Original title color before teleporting to the event
	private int _oriColorTitle;
	// Original title before teleporting to the event
	private String _oriTitle;
	// Player's team in the event
	private TeamType _team;
	// Previous location before participating in the event
	private Location _returnLocation;
	private int _dinamicInstanceId = 0;
	
	// Variable used to know the protection time inside events
	private long _protectionTimeEnd = 0;
	
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
	 * Get L2PcInstance
	 * @return
	 */
	public L2PcInstance getPcInstance()
	{
		return _player;
	}
	
	/**
	 * <ul>
	 * <b>Actions:</b>
	 * </ul>
	 * <li>Set the event team.</li>
	 * <li>Change the player color by team</li>
	 * @param team
	 */
	public void setTeam(TeamType team)
	{
		// Set original title character
		_oriTitle = _player.getTitle();
		// Set original title color character
		_oriColorTitle = _player.getAppearance().getTitleColor();
		// Set team character
		_team = team;
		// Set team title character
		_player.setTitle(team.name());
		// Set team color character
		_player.getAppearance().setTitleColor(team.getColor());
	}
	
	/**
	 * Get the player's team
	 * @return
	 */
	public TeamType getTeamType()
	{
		return _team;
	}
	
	/**
	 * Get the event instance id
	 * @return
	 */
	public int getDinamicInstanceId()
	{
		return _dinamicInstanceId;
	}
	
	/**
	 * Set the event instance id
	 * @param dinamicInstanceId
	 */
	public void setDinamicInstanceId(int dinamicInstanceId)
	{
		_dinamicInstanceId = dinamicInstanceId;
	}
	
	/**
	 * Increase the kills by one
	 */
	public void increaseKills()
	{
		_kills++;
	}
	
	/**
	 * Get the kills count
	 * @return
	 */
	public int getKills()
	{
		return _kills;
	}
	
	/**
	 * Increase the deaths by one
	 */
	public void increaseDeaths()
	{
		_deaths++;
	}
	
	/**
	 * Get the deaths count
	 * @return
	 */
	public int getDeaths()
	{
		return _deaths;
	}
	
	/**
	 * Get the player's points<br>
	 * @return
	 */
	public int getPoints()
	{
		return _kills - _deaths;
	}
	
	/**
	 * Set a player's title
	 * @param title
	 */
	public void setNewTitle(String title)
	{
		_player.setTitle(title);
	}
	
	/**
	 * Recover the original player title
	 */
	public void recoverOriginalTitle()
	{
		_player.setTitle(_oriTitle);
	}
	
	/**
	 * Recover the original color player title
	 */
	public void recoverOriginalColorTitle()
	{
		_player.getAppearance().setTitleColor(_oriColorTitle);
	}
	
	/**
	 * Get the original location before teleporting to the event
	 */
	public Location getReturnLoc()
	{
		return _returnLocation;
	}
	
	/**
	 * Set the original location before teleporting to the event
	 */
	public void setReturnLoc(Location loc)
	{
		_returnLocation = loc;
	}
	
	public long getProtectionTimeEnd()
	{
		return _protectionTimeEnd;
	}
	
	public boolean isProtected()
	{
		return _protectionTimeEnd > System.currentTimeMillis();
	}
	
	public void setProtectionTimeEnd(long time) 
	{
		_protectionTimeEnd = time;
	}
	
	public void sendMessage(String message)
	{
		_player.sendMessage(message);
	}
}