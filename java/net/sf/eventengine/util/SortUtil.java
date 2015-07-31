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

import java.util.ArrayList;
import java.util.List;

import net.sf.eventengine.holder.PlayerHolder;

/**
 * @author Zephyr
 */
public class SortUtil
{
	public static ArrayList<List<PlayerHolder>> getOrderedByKills(List<PlayerHolder> list, int level)
	{
		int max = -1;
		int previousMax = 10000;
		int index = -1;
		ArrayList<List<PlayerHolder>> result = new ArrayList<>();
		
		while (level > 0)
		{
			// Get the max value
			for (PlayerHolder player : list)
			{
				if (player.getKills() < previousMax && player.getKills() > max)
				{
					max = player.getKills();
				}
			}
			
			result.add(new ArrayList<>());
			index++;
			
			// Put the players with the max value
			for (PlayerHolder player : list)
			{
				if (player.getKills() == max)
				{
					result.get(index).add(player);
				}
			}
			previousMax = max;
			max = -1;
			level--;
		}
		
		return result;
	}
}
