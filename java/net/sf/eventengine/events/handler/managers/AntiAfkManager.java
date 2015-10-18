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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.model.Location;

import net.sf.eventengine.EventEngineManager;
import net.sf.eventengine.datatables.ConfigData;
import net.sf.eventengine.events.handler.AbstractEvent;
import net.sf.eventengine.events.holders.PlayerHolder;

/**
 * @author fissban
 */
public class AntiAfkManager
{
	private final Map<PlayerHolder, Location> _playersAfkCheck = new HashMap<>();
	
	private ScheduledFuture<?> _taskAntiAfk;
	
	/**
	 * Constructor
	 */
	public AntiAfkManager()
	{
		//
	}
	
	/**
	 * Comenzamos el thread encargado de controlar los locs de los players cada un determinado tiempo.
	 * @param time -> comienzo del control
	 */
	public void startTask(int time)
	{
		if (ConfigData.getInstance().ANTI_AFK_ENABLE)
		{
			_taskAntiAfk = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() ->
			{
				AbstractEvent currentEvent = EventEngineManager.getInstance().getCurrentEvent();
				
				for (PlayerHolder ph : currentEvent.getAllEventPlayers())
				{
					if (ph == null)
					{
						continue;
					}
					
					Location phOldLoc = _playersAfkCheck.get(ph);;
					if (phOldLoc != null)
					{
						if (phOldLoc.equals(ph.getPcInstance().getLocation()))
						{
							currentEvent.prepareToRemovePlayerFromEvent(ph);
							currentEvent.getAllEventPlayers().remove(ph);
							_playersAfkCheck.remove(ph);
							continue;
						}
					}
					
					_playersAfkCheck.put(ph, ph.getPcInstance().getLocation());
				}
			} , 1000, ConfigData.getInstance().AFK_CHECK_TIME * 1000);
		}
	}
	
	/**
	 * Detenemos el task encargado de controlar los locs de los players
	 * @param time -> fin del control
	 */
	public void stopTask(int time)
	{
		if (ConfigData.getInstance().ANTI_AFK_ENABLE)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(() ->
			{
				_taskAntiAfk.cancel(true);
			} , time);
		}
	}
}
