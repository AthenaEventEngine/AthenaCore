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
package com.github.u3games.eventengine.task;

import com.github.u3games.eventengine.EventEngineManager;
import com.github.u3games.eventengine.datatables.ConfigData;
import com.github.u3games.eventengine.datatables.EventData;
import com.github.u3games.eventengine.enums.CollectionTarget;
import com.github.u3games.eventengine.enums.EventEngineState;
import com.github.u3games.eventengine.events.handler.AbstractEvent;
import com.github.u3games.eventengine.util.EventUtil;
import com.l2jserver.gameserver.network.clientpackets.Say2;

/**
 * It handles the different state's behavior of EventEngineManager
 * @author fissban, Zephyr
 */
public class EventEngineTask implements Runnable
{
	// Type Message System
	private CollectionTarget _type = ConfigData.getInstance().EVENT_MESSAGE_GLOBAL ? CollectionTarget.ALL_PLAYERS : CollectionTarget.ALL_NEAR_PLAYERS;
	
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
						EventUtil.announceTo(Say2.CRITICAL_ANNOUNCE, "event_voting_started", _type);
						
						EventEngineManager.getInstance().setTime(ConfigData.getInstance().EVENT_VOTING_TIME * 60);
						EventEngineManager.getInstance().setEventEngineState(EventEngineState.VOTING);
					}
					else
					{
						EventEngineManager.getInstance().setNextEvent(EventData.getInstance().getRandomEventType());
						EventEngineManager.getInstance().setTime(ConfigData.getInstance().EVENT_REGISTER_TIME * 60);
						
						String eventName = EventEngineManager.getInstance().getNextEvent().getSimpleName();
						EventUtil.announceTo(Say2.CRITICAL_ANNOUNCE, "event_register_started", "%event%", eventName, _type);
						
						EventEngineManager.getInstance().setEventEngineState(EventEngineState.REGISTER);
					}
				}
				break;
			}
			case VOTING:
			{
				if (EventEngineManager.getInstance().getTime() > 0)
				{
					EventUtil.announceTime(EventEngineManager.getInstance().getTime(), "event_voting_state", Say2.CRITICAL_ANNOUNCE, _type);
				}
				else
				{
					Class<? extends AbstractEvent> nextEvent = EventEngineManager.getInstance().getEventMoreVotes();
					EventEngineManager.getInstance().setNextEvent(nextEvent);
					EventEngineManager.getInstance().setTime(ConfigData.getInstance().EVENT_REGISTER_TIME * 60);
					
					EventUtil.announceTo(Say2.CRITICAL_ANNOUNCE, "event_voting_ended", _type);
					EventUtil.announceTo(Say2.CRITICAL_ANNOUNCE, "event_register_started", "%event%", nextEvent.getSimpleName(), _type);
					
					EventEngineManager.getInstance().setEventEngineState(EventEngineState.REGISTER);
				}
				break;
			}
			case REGISTER:
			{
				if (EventEngineManager.getInstance().getTime() > 0)
				{
					int time = EventEngineManager.getInstance().getTime();
					String eventName = EventEngineManager.getInstance().getNextEvent().getSimpleName();
					EventUtil.announceTime(time, "event_register_state", Say2.CRITICAL_ANNOUNCE, "%event%", eventName, _type);
				}
				else
				{
					if (EventEngineManager.getInstance().getAllRegisteredPlayers().size() < ConfigData.getInstance().MIN_PLAYERS_IN_EVENT)
					{
						EventEngineManager.getInstance().cleanUp();
						EventEngineManager.getInstance().setTime(ConfigData.getInstance().EVENT_TASK * 60);
						EventUtil.announceTo(Say2.CRITICAL_ANNOUNCE, "event_aborted", _type);
						EventUtil.announceTime(EventEngineManager.getInstance().getTime(), "event_next", Say2.CRITICAL_ANNOUNCE, _type);
						EventEngineManager.getInstance().setEventEngineState(EventEngineState.WAITING);
					}
					else
					{
						EventUtil.announceTo(Say2.CRITICAL_ANNOUNCE, "event_register_ended", _type);
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
					EventUtil.announceTo(Say2.CRITICAL_ANNOUNCE, "wrong_run", _type);
					EventUtil.announceTime(EventEngineManager.getInstance().getTime(), "event_next", Say2.CRITICAL_ANNOUNCE, _type);
					EventEngineManager.getInstance().setEventEngineState(EventEngineState.WAITING);
					return;
				}
				
				EventEngineManager.getInstance().setCurrentEvent(event);
				EventEngineManager.getInstance().setEventEngineState(EventEngineState.RUNNING_EVENT);
				EventUtil.announceTo(Say2.CRITICAL_ANNOUNCE, "event_started", _type);
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
				EventUtil.announceTo(Say2.CRITICAL_ANNOUNCE, "event_end", _type);
				EventUtil.announceTime(EventEngineManager.getInstance().getTime(), "event_next", Say2.CRITICAL_ANNOUNCE, _type);
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
