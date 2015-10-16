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
import java.util.List;
import java.util.Map;

import net.sf.eventengine.EventEngineManager;
import net.sf.eventengine.enums.EventEngineState;
import net.sf.eventengine.events.holders.PlayerHolder;

/**
 * @author swarlog
 */

public class AntiAfkTask implements Runnable
{
	private final Map<Integer, List<PlayerHolder>> _players = new HashMap<>();
	
	@Override
	public void run()
	{
		EventEngineState state = EventEngineManager.getInstance().getEventEngineState();
		switch (state)
		{
			case RUNNING_EVENT:
			{
				// TODO: Save, check position and effect player.
				break;
			}
		}
		
		// Clear list players
		if (state != EventEngineState.RUNNING_EVENT)
		{
			_players.clear();
		}
	}
}
