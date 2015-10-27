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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import com.l2jserver.gameserver.ThreadPoolManager;

import net.sf.eventengine.datatables.ConfigData;
import net.sf.eventengine.events.schedules.AnnounceTeleportEvent;
import net.sf.eventengine.events.schedules.ChangeToEndEvent;
import net.sf.eventengine.events.schedules.ChangeToFightEvent;
import net.sf.eventengine.events.schedules.ChangeToStartEvent;
import net.sf.eventengine.events.schedules.interfaces.EventScheduled;

/**
 * @author Zephyr
 */
public class ScheduledEventsManager
{
	// Event time
	private int _currentTime;
	// Scheduled events
	private final Map<Integer, List<EventScheduled>> _scheduledEvents = new HashMap<>();
	// Task that control the event time
	private ScheduledFuture<?> _taskControlTime;
	
	public ScheduledEventsManager()
	{
		//
	}
	
	/**
	 * We control the timing of events<br>
	 * <ul>
	 * <b>Actions: </b>
	 * </ul>
	 * <li>-> step 1: We announced that participants will be teleported</li>
	 * <li>Wait 3 secs</li>
	 * <li>-> step 2: Adjust the status of the event -> START</li>
	 * <li>We hope 1 sec to actions within each event is executed..</li>
	 * <li>-> step 3: Adjust the status of the event -> FIGHT</li>
	 * <li>-> step 3: We sent a message that they are ready to fight.</li>
	 * <li>We wait until the event ends</li>
	 * <li>-> step 4: Adjust the status of the event -> END</li>
	 * <li>-> step 4: We sent a message warning that term event</li>
	 * <li>Esperamos 1 seg</li>
	 * <li>-> step 5: We alerted the event ended EventEngineManager</li>
	 */
	public void startScheduledEvents()
	{
		int time = 1000;
		addScheduledEvent(new AnnounceTeleportEvent(time));
		time += 3000;
		addScheduledEvent(new ChangeToStartEvent(time));
		time += 1000;
		addScheduledEvent(new ChangeToFightEvent(time));
		
		// TODO: Maybe some events don't need a finish time, like korean pvp style
		time += ConfigData.getInstance().EVENT_DURATION * 60 * 1000;
		addScheduledEvent(new ChangeToEndEvent(time));
	}
	
	public void startTaskControlTime()
	{
		_currentTime = 0;
		_taskControlTime = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() ->
		{
			_currentTime += 1000;
			checkScheduledEvents();
		} , 10 * 1000, 1000);
	}
	
	public void cancelTaskControlTime()
	{
		_taskControlTime.cancel(true);
	}
	
	public ScheduledFuture<?> getTaskControlTime()
	{
		return _taskControlTime;
	}
	
	public void addScheduledEvent(EventScheduled event)
	{
		List<EventScheduled> list;
		if (!_scheduledEvents.containsKey(event.getTime()))
		{
			list = new ArrayList<>();
			_scheduledEvents.put(event.getTime(), list);
		}
		else
		{
			list = _scheduledEvents.get(event.getTime());
		}
		
		list.add(event);
	}
	
	public void checkScheduledEvents()
	{
		List<EventScheduled> list = _scheduledEvents.get(_currentTime);
		if (list != null)
		{
			for (EventScheduled event : list)
			{
				event.run();
			}
		}
	}
}
