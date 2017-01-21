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

import com.github.athenaengine.core.enums.MessageType;
import com.github.athenaengine.core.events.schedules.interfaces.EventScheduled;
import com.github.athenaengine.core.enums.CollectionTarget;
import com.github.athenaengine.core.util.EventUtil;

/**
 * @author Zephyr
 */

public class AnnounceNearEndEvent implements EventScheduled
{
	int _time;
	int _timeAnnounce;
	
	public AnnounceNearEndEvent(int time, int timeAnnounce)
	{
		_time = time;
		_timeAnnounce = timeAnnounce;
	}
	
	@Override
	public int getTime()
	{
		return _time;
	}
	
	@Override
	public void run()
	{
		EventUtil.announceTo(MessageType.CRITICAL_ANNOUNCE, "event_end_soon", "%time%", String.valueOf(_timeAnnounce), CollectionTarget.ALL_PLAYERS_IN_EVENT);
	}
}