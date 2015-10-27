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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

import com.l2jserver.gameserver.ThreadPoolManager;

import net.sf.eventengine.EventEngineManager;
import net.sf.eventengine.datatables.ConfigData;
import net.sf.eventengine.events.handler.AbstractEvent;
import net.sf.eventengine.events.holders.PlayerHolder;

/**
 * Simple system to run every x time and controls the actions of the players.<br>
 * Each you see that a player performs an action that will be added to "_playersAfkCheck".<br>
 * If the character is not in this list will be sent event.<br>
 * @author fissban
 */
public class AntiAfkManager
{
	private final List<PlayerHolder> _playersAfkCheck = new CopyOnWriteArrayList<>();
	
	private ScheduledFuture<?> _taskAntiAfk;
	
	public AntiAfkManager()
	{
		//
	}
	
	/**
	 * Add a character to the list of "excluded" from the next control system
	 * @param ph
	 */
	public void addPlayer(PlayerHolder ph)
	{
		_playersAfkCheck.add(ph);
	}
	
	/**
	 * @param ph
	 */
	public void removePlayer(PlayerHolder ph)
	{
		_playersAfkCheck.remove(ph);
	}
	
	/**
	 * Start thread responsible for controlling the actions of the players from time to time.
	 */
	public void startTask()
	{
		_taskAntiAfk = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() ->
		{
			AbstractEvent currentEvent = EventEngineManager.getInstance().getCurrentEvent();
			
			for (PlayerHolder ph : currentEvent.getPlayerEventManager().getAllEventPlayers())
			{
				if (!_playersAfkCheck.contains(ph))
				{
					currentEvent.cancelAllEffects(ph);
					currentEvent.removePlayerFromEvent(ph, true);
				}
			}
			
			// Init list
			_playersAfkCheck.clear();
			
		} , ConfigData.getInstance().AFK_CHECK_TIME * 1000, ConfigData.getInstance().AFK_CHECK_TIME * 1000);
	}
	
	/**
	 * Stop the task responsible for controlling the locations of the players.
	 */
	public void stopTask()
	{
		_taskAntiAfk.cancel(true);
	}
}
