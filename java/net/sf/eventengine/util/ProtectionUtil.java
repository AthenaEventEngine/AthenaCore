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
package net.sf.eventengine.util;

import java.util.HashMap;
import java.util.Map;

import com.l2jserver.gameserver.model.Location;

import net.sf.eventengine.events.holders.PlayerHolder;

/**
 * @author swarlog, fissban
 */
public class ProtectionUtil
{
	// Map List
	public final static Map<PlayerHolder, Location> _playersAfkLoc = new HashMap<>();
	
	public static Location getAfkLocation(PlayerHolder ph)
	{
		return _playersAfkLoc.get(ph);
	}
	
	public static void addAfkLoc(PlayerHolder ph)
	{
		_playersAfkLoc.put(ph, ph.getPcInstance().getLocation());
	}
	
	public static void removeAfkLoc(PlayerHolder ph)
	{
		_playersAfkLoc.remove(ph);
	}
}
