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
package com.github.athenaengine.core.events.schedules;

import com.github.athenaengine.core.events.schedules.interfaces.EventScheduled;
import com.github.athenaengine.core.model.base.BaseEvent;
import com.github.athenaengine.core.EventEngineManager;
import com.github.athenaengine.core.enums.EventEngineState;
import com.github.athenaengine.core.enums.EventState;
import com.github.athenaengine.core.model.entity.Player;
import com.github.athenaengine.core.util.EventUtil;

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
		BaseEvent currentEvent = EventEngineManager.getInstance().getCurrentEvent();
		currentEvent.runEventState(EventState.END);
		// Clear all the npcs spawned
		currentEvent.getSpawnManager().removeAllNpcs();
		// Finish antiAfkTask
		if (currentEvent.getAntiAfkManager() != null)
		{
			currentEvent.getAntiAfkManager().finish();
		}
		
		// Send a special message to the participants
		for (Player player : currentEvent.getPlayerEventManager().getAllEventPlayers())
		{
			EventUtil.sendEventSpecialMessage(player, 1, "status_finished");
		}
		EventEngineManager.getInstance().setEventEngineState(EventEngineState.EVENT_ENDED);
	}
}