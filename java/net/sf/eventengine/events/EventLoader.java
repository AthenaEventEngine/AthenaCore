package net.sf.eventengine.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.eventengine.datatables.ConfigData;
import net.sf.eventengine.handler.AbstractEvent;

import com.l2jserver.util.Rnd;

/**
 * @author Zephyr
 */
public class EventLoader
{
	private static final Logger LOG = Logger.getLogger(EventLoader.class.getName());
	private static final ArrayList<Class<? extends AbstractEvent>> EVENTS_LIST = new ArrayList<>();
	private static final Map<String, Class<? extends AbstractEvent>> EVENTS_MAP = new HashMap<>();
	
	public static void load()
	{
		if (ConfigData.getInstance().AVA_EVENT_ENABLED)
		{
			EVENTS_LIST.add(AllVsAll.class);
			EVENTS_MAP.put(AllVsAll.class.getSimpleName(), AllVsAll.class);
		}
		
		if (ConfigData.getInstance().CTF_EVENT_ENABLED)
		{
			EVENTS_LIST.add(CaptureTheFlag.class);
			EVENTS_MAP.put(CaptureTheFlag.class.getSimpleName(), CaptureTheFlag.class);
		}
		
		if (ConfigData.getInstance().OVO_EVENT_ENABLED)
		{
			EVENTS_LIST.add(OneVsOne.class);
			EVENTS_MAP.put(OneVsOne.class.getSimpleName(), OneVsOne.class);
		}
		
		if (ConfigData.getInstance().SURVIVE_EVENT_ENABLED)
		{
			EVENTS_LIST.add(Survive.class);
			EVENTS_MAP.put(Survive.class.getSimpleName(), Survive.class);
		}
		
		if (ConfigData.getInstance().TVT_EVENT_ENABLED)
		{
			EVENTS_LIST.add(TeamVsTeam.class);
			EVENTS_MAP.put(TeamVsTeam.class.getSimpleName(), TeamVsTeam.class);
		}
	}
	
	public static Class<? extends AbstractEvent> getEvent(String name)
	{
		return EVENTS_MAP.get(name);
	}
	
	public static Class<? extends AbstractEvent> getRandomEventType()
	{
		return EVENTS_LIST.get(Rnd.get(EVENTS_LIST.size() - 1));
	}
	
	public static ArrayList<Class<? extends AbstractEvent>> getEnabledEvents()
	{
		return EVENTS_LIST;
	}
	
	public static AbstractEvent getNewEventInstance(Class<? extends AbstractEvent> type)
	{
		try
		{
			return type.newInstance();
		}
		catch (Exception e)
		{
			LOG.warning(EventLoader.class.getSimpleName() + ": " + e);
		}
		return null;
	}
}
