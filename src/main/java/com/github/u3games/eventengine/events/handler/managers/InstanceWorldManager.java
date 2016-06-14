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
package com.github.u3games.eventengine.events.handler.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.github.u3games.eventengine.EventEngineManager;
import com.github.u3games.eventengine.EventEngineWorld;
import com.l2jserver.gameserver.instancemanager.InstanceManager;
import com.l2jserver.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;

/**
 * @author fissban
 */
public class InstanceWorldManager
{
	private static final Logger LOGGER = Logger.getLogger(InstanceWorldManager.class.getName());
	
	private String _instanceFile = "";
	private final List<InstanceWorld> _instanceWorlds = new ArrayList<>();
	
	/**
	 * Constructor
	 */
	public InstanceWorldManager()
	{
		//
	}
	
	public void setInstanceFile(String instanceFile)
	{
		_instanceFile = instanceFile;
	}
	
	/**
	 * Create dynamic instances and a world for her
	 * @param count
	 * @return InstanceWorld
	 */
	public InstanceWorld createNewInstanceWorld()
	{
		InstanceWorld world = null;
		try
		{
			int instanceId = InstanceManager.getInstance().createDynamicInstance(_instanceFile);
			InstanceManager.getInstance().getInstance(instanceId).setAllowSummon(false);
			InstanceManager.getInstance().getInstance(instanceId).setPvPInstance(true);
			InstanceManager.getInstance().getInstance(instanceId).setEjectTime(10 * 60 * 1000); // prevent eject death players.
			InstanceManager.getInstance().getInstance(instanceId).setEmptyDestroyTime(1000 + 60000L);
			// We closed the doors of the instance if there
			for (L2DoorInstance door : InstanceManager.getInstance().getInstance(instanceId).getDoors())
			{
				door.closeMe();
			}
			
			world = new EventEngineWorld();
			world.setInstanceId(instanceId);
			world.setTemplateId(100); // TODO hardcode
			world.setStatus(0);
			InstanceManager.getInstance().addWorld(world);
			_instanceWorlds.add(world);
			
		}
		catch (Exception e)
		{
			LOGGER.warning(EventEngineManager.class.getSimpleName() + ": -> createDynamicInstances() " + e);
			e.printStackTrace();
		}
		
		return world;
	}
	
	public List<InstanceWorld> getAllInstancesWorlds()
	{
		return _instanceWorlds;
	}
	
	public void destroyWorldInstances()
	{
		for (InstanceWorld instance : _instanceWorlds)
		{
			InstanceManager.getInstance().destroyInstance(instance.getInstanceId());
		}
	}
}
