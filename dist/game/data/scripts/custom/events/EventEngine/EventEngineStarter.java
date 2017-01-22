/*
 * Copyright (C) 2004-2016 L2J EventEngine
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
package custom.events.EventEngine;

import com.github.athenaengine.core.EventEngineManager;
import ai.npc.AbstractNpcAI;

/**
 * This is the starter to load the Event Engine
 *  * @author Zephyr
 */
public class EventEngineStarter extends AbstractNpcAI
{
	private EventEngineStarter()
	{
		super(EventEngineStarter.class.getSimpleName(), "ai/npc");
		EventEngineManager.getInstance();
	}
	
	public static void main(String[] args)
	{
		new EventEngineStarter();
	}
}