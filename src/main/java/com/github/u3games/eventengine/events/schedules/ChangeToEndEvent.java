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
package com.github.u3games.eventengine.events.schedules;

import com.github.u3games.eventengine.EventEngineManager;
import com.github.u3games.eventengine.enums.EventEngineState;
import com.github.u3games.eventengine.enums.EventState;
import com.github.u3games.eventengine.events.handler.AbstractEvent;
import com.github.u3games.eventengine.events.holders.PlayerHolder;
import com.github.u3games.eventengine.events.schedules.interfaces.EventScheduled;
import com.github.u3games.eventengine.util.EventUtil;

/**
 * @author Zephyr
 */

public class ChangeToEndEvent implements EventScheduled
{
	int _time;
	
	public ChangeToEndEvent(int time)
	{
		_time = time;
	}
	
	@Override
	public int getTime()
	{
		return _time;
	}
	
	@Override
	public void run()
	{
		AbstractEvent currentEvent = EventEngineManager.getInstance().getCurrentEvent();
		
		currentEvent.runEventState(EventState.END);
		
		// Clear all the npcs spawned
		currentEvent.getSpawnManager().removeAllEventNpc();
		
		// Finish antiAfkTask
		if (currentEvent.getAntiAfkManager() != null)
		{
			currentEvent.getAntiAfkManager().finish();
		}
		
		// Enviamos un mensaje especial para los participantes
		for (PlayerHolder player : currentEvent.getPlayerEventManager().getAllEventPlayers())
		{
			EventUtil.sendEventSpecialMessage(player, 1, "status_finished");
		}
		
		EventEngineManager.getInstance().setEventEngineState(EventEngineState.EVENT_ENDED);
	}
}