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
package com.github.athenaengine.core.events.handler.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import com.github.athenaengine.core.events.schedules.interfaces.EventScheduled;
import com.l2jserver.gameserver.ThreadPoolManager;

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
	
	public void startTaskControlTime()
	{
		_currentTime = 0;
		_taskControlTime = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() ->
		{
			_currentTime += 1000;
			checkScheduledEvents();
		}, 10 * 1000, 1000);
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
		if (!_scheduledEvents.containsKey(event.getTime()))
		{
			_scheduledEvents.put(event.getTime(), new ArrayList<>());
		}
		_scheduledEvents.get(event.getTime()).add(event);
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