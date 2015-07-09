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

import net.sf.eventengine.EventEngineManager;
import net.sf.eventengine.configs.Configs;
import net.sf.eventengine.enums.EventEngineState;
import net.sf.eventengine.enums.EventType;
import net.sf.eventengine.events.AllVsAll;
import net.sf.eventengine.events.CaptureTheFlag;
import net.sf.eventengine.events.EventLoader;
import net.sf.eventengine.events.OneVsOne;
import net.sf.eventengine.events.Survive;
import net.sf.eventengine.events.TeamVsTeam;
import net.sf.eventengine.handler.MsgHandler;
import net.sf.eventengine.util.EventUtil;

import com.l2jserver.gameserver.network.clientpackets.Say2;

/**
 * It handles the different state's behaviour of EventEngineManager
 * @author fissban, Zephyr
 */
public class EventEngineTask implements Runnable
{
	@Override
	public void run()
	{
		EventEngineState state = EventEngineManager.getEventEngineState();
		switch (state)
		{
			case WAITING:
			{
				// Cleanup
				if (EventEngineManager.getCurrentEvent() != null)
				{
					EventEngineManager.setCurrentEvent(null);
					EventEngineManager.clearVotes();
					EventEngineManager.getInstancesWorlds().clear();
				}
				
				if (EventEngineManager.getTime() <= 0)
				{
					if (Configs.EVENT_VOTING_ENABLED)
					{
						EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, MsgHandler.getMsg("event_voting_started"));
						EventEngineManager.setTime(Configs.EVENT_VOTING_TIME * 60);
						EventEngineManager.setEventEngineState(EventEngineState.VOTING);
					}
					else
					{
						EventEngineManager.setNextEvent(EventLoader.getRandomEventType());
						EventEngineManager.setTime(Configs.EVENT_REGISTER_TIME * 60);
						String msg = MsgHandler.getMsg("event_register_started").replace("%event%", EventEngineManager.getNextEvent().toString());
						EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, msg);
						EventEngineManager.setEventEngineState(EventEngineState.REGISTER);
					}
				}
				break;
			}
			case VOTING:
			{
				if (EventEngineManager.getTime() > 0)
				{
					EventUtil.announceTimeLeft(EventEngineManager.getTime(), MsgHandler.getMsg("event_voting_state"), Say2.CRITICAL_ANNOUNCE, true);
				}
				else
				{
					EventType nextEvent = EventEngineManager.getEventMoreVotes();
					EventEngineManager.setNextEvent(nextEvent);
					EventEngineManager.setTime(Configs.EVENT_REGISTER_TIME * 60);
					EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, MsgHandler.getMsg("event_voting_ended"));
					EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, MsgHandler.getMsg("event_register_started").replace("%event%", nextEvent.toString()));
					EventEngineManager.setEventEngineState(EventEngineState.REGISTER);
				}
				break;
			}
			case REGISTER:
			{
				if (EventEngineManager.getTime() > 0)
				{
					EventUtil.announceTimeLeft(EventEngineManager.getTime(), MsgHandler.getMsg("event_register_state").replace("%event%", EventEngineManager.getNextEvent().toString()), Say2.CRITICAL_ANNOUNCE, true);
				}
				else
				{
					if (EventEngineManager.isEmptyRegisteredPlayers()) // TODO: handle min register players
					{
						EventEngineManager.setTime(Configs.EVENT_TASK * 60);
						EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, MsgHandler.getMsg("event_aborted"));
						EventUtil.announceTimeLeft(EventEngineManager.getTime(), MsgHandler.getMsg("event_next"), Say2.CRITICAL_ANNOUNCE, true);
						EventEngineManager.setEventEngineState(EventEngineState.WAITING);
					}
					else
					{
						EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, MsgHandler.getMsg("event_register_ended"));
						EventEngineManager.setEventEngineState(EventEngineState.RUN_EVENT);
					}
				}
				break;
			}
			case RUN_EVENT:
			{
				EventType event = EventEngineManager.getNextEvent();
				
				// Initialize the event with more votes
				switch (event)
				{
					case AVA:
						EventEngineManager.setCurrentEvent(new AllVsAll());
						break;
					
					case CTF:
						EventEngineManager.setCurrentEvent(new CaptureTheFlag());
						break;
					
					case TVT:
						EventEngineManager.setCurrentEvent(new TeamVsTeam());
						break;
					
					case OVO:
						EventEngineManager.setCurrentEvent(new OneVsOne());
						break;
					
					case SURVIVE:
						EventEngineManager.setCurrentEvent(new Survive());
						break;
				
				}
				
				EventEngineManager.setEventEngineState(EventEngineState.RUNNING_EVENT);
				EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, MsgHandler.getMsg("event_started"));
				break;
			}
			case RUNNING_EVENT:
				// Nothing
				break;
			case EVENT_ENDED:
				// Cleanup
				EventEngineManager.setCurrentEvent(null);
				EventEngineManager.clearVotes();
				EventEngineManager.getInstancesWorlds().clear();
				
				EventEngineManager.setTime(Configs.EVENT_TASK * 60);
				EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, MsgHandler.getMsg("event_end"));
				EventUtil.announceTimeLeft(Say2.CRITICAL_ANNOUNCE, MsgHandler.getMsg("event_next"), Say2.CRITICAL_ANNOUNCE, true);
				EventEngineManager.setEventEngineState(EventEngineState.WAITING);
				break;
		}
		
		if (state != EventEngineState.RUNNING_EVENT)
		{
			EventEngineManager.decreaseTime();
		}
	}
}
