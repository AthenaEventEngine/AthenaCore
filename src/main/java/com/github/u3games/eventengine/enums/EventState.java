/*
 * Copyright (C) 2015-2016 Athena Event Engine.
 *
 * This file is part of Athena Event Engine.
 *
 * Athena Event Engine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Athena Event Engine is distributed in the hope that it will be useful,
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
public enum EventState
{
	/** General and specific actions of each event are prepared. */
	START,
	
	/** The event is enabled for users to start fighting. */
	FIGHT,
	
	/** General and specific actions of each event are prepared. */
	END
}