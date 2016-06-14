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
package com.github.u3games.eventengine.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.github.u3games.eventengine.enums.ScoreType;
import com.github.u3games.eventengine.interfaces.ParticipantHolder;

/**
 * @author Zephyr
 */
public class SortUtils
{
	public enum Order
	{
		DESCENDENT,
		ASCENDENT
	}
	
	/**
	 * Get a list of ParticipantHolder sorted by ScoreType (Kills, Points, etc). By default, it uses descendent order
	 * @param list
	 * @param type
	 * @return
	 */
	public static <T extends ParticipantHolder> ArrayList<List<T>> getOrdered(Collection<T> list, ScoreType type)
	{
		return getOrdered(list, type, Order.DESCENDENT);
	}
	
	/**
	 * Get a list of ParticipantHolder sorted by ScoreType (Kills, Points, etc)
	 * @param list
	 * @param type
	 * @param order
	 * @return
	 */
	public static <T extends ParticipantHolder> ArrayList<List<T>> getOrdered(Collection<T> list, ScoreType type, Order order)
	{
		ArrayList<Holder> holderList = adaptList(list, type, order);
		Collections.sort(holderList);
		return unadaptList(holderList);
	}
	
	/**
	 * Adapt the list to use Comparator
	 * @param list
	 * @param type
	 * @return
	 */
	private static <T extends ParticipantHolder> ArrayList<Holder> adaptList(Collection<T> list, ScoreType type, Order order)
	{
		ArrayList<Holder> listAdapted = new ArrayList<>();
		
		for (ParticipantHolder participant : list)
		{
			int value = 0;
			
			if (type == ScoreType.KILL)
			{
				value = participant.getKills();
			}
			else if (type == ScoreType.POINT)
			{
				value = participant.getPoints();
			}
			else if (type == ScoreType.DEATH)
			{
				value = participant.getDeaths();
			}
			
			listAdapted.add(new Holder(participant, value, order));
		}
		
		return listAdapted;
	}
	
	/**
	 * Transform the adapted list to the original format to be returned Also, it groups the objects with equals value into the same list
	 * @param holderList
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static <T extends ParticipantHolder> ArrayList<List<T>> unadaptList(ArrayList<Holder> holderList)
	{
		ArrayList<List<T>> finalList = new ArrayList<>();
		
		int index = -1;
		int lastValue = -1000;
		
		for (Holder holder : holderList)
		{
			if (holder.getValue() != lastValue)
			{
				lastValue = holder.getValue();
				index++;
				finalList.add(index, new ArrayList<>());
			}
			finalList.get(index).add((T) holder.getObject());
		}
		return finalList;
	}
	
	public static class Holder implements Comparable<Holder>
	{
		private Object _object;
		private int _value;
		private Order _order;
		
		public Holder(Object object, int value, Order order)
		{
			_object = object;
			_value = value;
			_order = order;
		}
		
		public Object getObject()
		{
			return _object;
		}
		
		public int getValue()
		{
			return _value;
		}
		
		@Override
		public int compareTo(Holder anotherHolder)
		{
			int compareValue = anotherHolder.getValue();
			int result = compareValue - _value;;
			
			return _order == Order.ASCENDENT ? (result * -1) : result;
		}
	}
}
