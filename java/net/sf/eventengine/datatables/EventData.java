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
package net.sf.eventengine.datatables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.l2jserver.util.Rnd;

import net.sf.eventengine.events.AllVsAll;
import net.sf.eventengine.events.CaptureTheFlag;
import net.sf.eventengine.events.OneVsOne;
import net.sf.eventengine.events.Survive;
import net.sf.eventengine.events.TeamVsTeam;
import net.sf.eventengine.events.handler.AbstractEvent;

/**
 * @author Zephyr
 */
public class EventData
{
	private static final Logger LOGGER = Logger.getLogger(EventData.class.getName());
	
	private final ArrayList<Class<? extends AbstractEvent>> _eventList = new ArrayList<>();
	private final Map<String, Class<? extends AbstractEvent>> _eventMap = new HashMap<>();
	
	private EventData()
	{
		load();
	}
	
	private void load()
	{
		if (ConfigData.getInstance().AVA_EVENT_ENABLED)
		{
			_eventList.add(AllVsAll.class);
			_eventMap.put(AllVsAll.class.getSimpleName(), AllVsAll.class);
		}
		
		if (ConfigData.getInstance().OVO_EVENT_ENABLED)
		{
			_eventList.add(OneVsOne.class);
			_eventMap.put(OneVsOne.class.getSimpleName(), OneVsOne.class);
		}
		
		if (ConfigData.getInstance().SURVIVE_EVENT_ENABLED)
		{
			_eventList.add(Survive.class);
			_eventMap.put(Survive.class.getSimpleName(), Survive.class);
		}
		
		if (ConfigData.getInstance().TVT_EVENT_ENABLED)
		{
			_eventList.add(TeamVsTeam.class);
			_eventMap.put(TeamVsTeam.class.getSimpleName(), TeamVsTeam.class);
		}
		
		if (ConfigData.getInstance().CTF_EVENT_ENABLED)
		{
			_eventList.add(CaptureTheFlag.class);
			_eventMap.put(CaptureTheFlag.class.getSimpleName(), CaptureTheFlag.class);
		}
	}
	
	public Class<? extends AbstractEvent> getEvent(String name)
	{
		return _eventMap.get(name);
	}
	
	public Class<? extends AbstractEvent> getRandomEventType()
	{
		return _eventList.get(Rnd.get(_eventList.size() - 1));
	}
	
	public ArrayList<Class<? extends AbstractEvent>> getEnabledEvents()
	{
		return _eventList;
	}
	
	public AbstractEvent getNewEventInstance(Class<? extends AbstractEvent> type)
	{
		try
		{
			return type.newInstance();
		}
		catch (Exception e)
		{
			LOGGER.warning(EventData.class.getSimpleName() + ": " + e);
		}
		return null;
	}
	
	public static EventData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final EventData _instance = new EventData();
	}
}
