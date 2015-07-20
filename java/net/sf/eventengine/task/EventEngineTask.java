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
package net.sf.eventengine.task;

import java.util.HashMap;
import java.util.Map;

import net.sf.eventengine.EventEngineManager;
import net.sf.eventengine.datatables.ConfigData;
import net.sf.eventengine.datatables.EventData;
import net.sf.eventengine.enums.EventEngineState;
import net.sf.eventengine.handler.AbstractEvent;
import net.sf.eventengine.util.EventUtil;

import com.l2jserver.gameserver.network.clientpackets.Say2;

/**
 * It handles the different state's behavior of EventEngineManager
 * @author fissban, Zephyr
 */
public class EventEngineTask implements Runnable
{
	@Override
	public void run()
	{
		EventEngineState state = EventEngineManager.getInstance().getEventEngineState();
		switch (state)
		{
			case WAITING:
			{
				if (EventEngineManager.getInstance().getTime() <= 0)
				{
					if (ConfigData.getInstance().EVENT_VOTING_ENABLED)
					{
						EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, "event_voting_started");
						EventEngineManager.getInstance().setTime(ConfigData.getInstance().EVENT_VOTING_TIME * 60);
						EventEngineManager.getInstance().setEventEngineState(EventEngineState.VOTING);
					}
					else
					{
						EventEngineManager.getInstance().setNextEvent(EventData.getInstance().getRandomEventType());
						EventEngineManager.getInstance().setTime(ConfigData.getInstance().EVENT_REGISTER_TIME * 60);
						EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, "event_register_started", "%event%", EventEngineManager.getInstance().getNextEvent().getSimpleName());
						EventEngineManager.getInstance().setEventEngineState(EventEngineState.REGISTER);
					}
				}
				break;
			}
			case VOTING:
			{
				if (EventEngineManager.getInstance().getTime() > 0)
				{
					EventUtil.announceTimeLeft(EventEngineManager.getInstance().getTime(), "event_voting_state", Say2.CRITICAL_ANNOUNCE, true);
				}
				else
				{
					Class<? extends AbstractEvent> nextEvent = EventEngineManager.getInstance().getEventMoreVotes();
					EventEngineManager.getInstance().setNextEvent(nextEvent);
					EventEngineManager.getInstance().setTime(ConfigData.getInstance().EVENT_REGISTER_TIME * 60);
					EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, "event_voting_ended");
					EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, "event_register_started", "%event%", nextEvent.getSimpleName());
					EventEngineManager.getInstance().setEventEngineState(EventEngineState.REGISTER);
				}
				break;
			}
			case REGISTER:
			{
				if (EventEngineManager.getInstance().getTime() > 0)
				{
					Map<String, String> holdersText = new HashMap<>();
					holdersText.put("%event%", EventEngineManager.getInstance().getNextEvent().getSimpleName());
					EventUtil.announceTimeLeft(EventEngineManager.getInstance().getTime(), "event_register_state", Say2.CRITICAL_ANNOUNCE, holdersText, true);
				}
				else
				{
					if (EventEngineManager.getInstance().getAllRegisteredPlayers().size() < ConfigData.getInstance().MIN_PLAYERS_IN_EVENT)
					{
						EventEngineManager.getInstance().cleanUp();
						EventEngineManager.getInstance().setTime(ConfigData.getInstance().EVENT_TASK * 60);
						EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, "event_aborted");
						EventUtil.announceTimeLeft(EventEngineManager.getInstance().getTime(), "event_next", Say2.CRITICAL_ANNOUNCE, true);
						EventEngineManager.getInstance().setEventEngineState(EventEngineState.WAITING);
					}
					else
					{
						EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, "event_register_ended");
						EventEngineManager.getInstance().setEventEngineState(EventEngineState.RUN_EVENT);
					}
				}
				break;
			}
			case RUN_EVENT:
			{
				AbstractEvent event = EventData.getInstance().getNewEventInstance(EventEngineManager.getInstance().getNextEvent());
				
				if (event == null)
				{
					EventEngineManager.getInstance().cleanUp();
					EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, "wrong_run");
					EventUtil.announceTimeLeft(Say2.CRITICAL_ANNOUNCE, "event_next", Say2.CRITICAL_ANNOUNCE, true);
					EventEngineManager.getInstance().setEventEngineState(EventEngineState.WAITING);
					return;
				}
				
				EventEngineManager.getInstance().setCurrentEvent(event);
				EventEngineManager.getInstance().setEventEngineState(EventEngineState.RUNNING_EVENT);
				EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, "event_started");
				break;
			}
			case RUNNING_EVENT:
			{
				// Nothing
				break;
			}
			case EVENT_ENDED:
			{
				EventEngineManager.getInstance().cleanUp();
				EventEngineManager.getInstance().setTime(ConfigData.getInstance().EVENT_TASK * 60);
				EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, "event_end");
				EventUtil.announceTimeLeft(EventEngineManager.getInstance().getTime(), "event_next", Say2.CRITICAL_ANNOUNCE, true);
				EventEngineManager.getInstance().setEventEngineState(EventEngineState.WAITING);
				break;
			}
		}
		
		if (state != EventEngineState.RUNNING_EVENT)
		{
			EventEngineManager.getInstance().decreaseTime();
		}
	}
}
