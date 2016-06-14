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
package com.github.u3games.eventengine.enums;

/**
 * @author fissban
 */
public enum TeamType
{
	WHITE(Integer.decode("0xFFFFFF")),
	RED(Integer.decode("0x0000FF")),
	BLUE(Integer.decode("0xDF0101")),
	// Alt
	PINK(Integer.decode("0x9393FF")),
	ROSE_PINK(Integer.decode("0x7C49FC")),
	LEMON_YELOOW(Integer.decode("0x97F8FC")),
	LILAC(Integer.decode("0xFA9AEE")),
	COBAL_VIOLET(Integer.decode("0xFF5D93")),
	MINT_GREEN(Integer.decode("0x00FCA0")),
	PEACOCK_GREEN(Integer.decode("0xA0A601")),
	YELLOW_OCHRE(Integer.decode("0x7898AF")),
	CHOCOLATE(Integer.decode("0x486295")),
	SILVER(Integer.decode("0x999999"));
	
	private int _color;
	
	TeamType(int color)
	{
		_color = color;
	}
	
	public int getColor()
	{
		return _color;
	}
}
