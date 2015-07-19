package net.sf.eventengine.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sf.eventengine.datatables.ConfigData;
import net.sf.eventengine.enums.EventType;
import net.sf.eventengine.handler.AbstractEvent;

import com.l2jserver.util.Rnd;

/**
 * @author Zephyr
 */
public class EventLoader
{
	private static final ArrayList<EventType> EVENTS = new ArrayList<>();
	private static final Map<EventType, Class<? extends AbstractEvent>> EVENTS_MAP = new HashMap<>();
	
	public static void load()
	{
		if (ConfigData.AVA_EVENT_ENABLED)
		{
			EVENTS.add(EventType.AVA);
			EVENTS_MAP.put(EventType.AVA, AllVsAll.class);
		}
		
		// EVENTS.put(EventType.CTF, CaptureTheFlag.class);
		
		if (ConfigData.OVO_EVENT_ENABLED)
		{
			EVENTS.add(EventType.OVO);
			EVENTS_MAP.put(EventType.OVO, OneVsOne.class);
		}
		
		if (ConfigData.SURVIVE_EVENT_ENABLED)
		{
			EVENTS.add(EventType.SURVIVE);
			EVENTS_MAP.put(EventType.SURVIVE, Survive.class);
		}
		
		if (ConfigData.TVT_EVENT_ENABLED)
		{
			EVENTS.add(EventType.TVT);
			EVENTS_MAP.put(EventType.TVT, TeamVsTeam.class);
		}
	}
	
	public static Class<? extends AbstractEvent> getClass(EventType type)
	{
		return EVENTS_MAP.get(type);
	}
	
	public static EventType getRandomEventType()
	{
		return EVENTS.get(Rnd.get(EVENTS.size() - 1));
	}
}
