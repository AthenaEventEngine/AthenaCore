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
package net.sf.eventengine.events.handler.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.sf.eventengine.EventEngineManager;

import com.l2jserver.gameserver.instancemanager.InstanceManager;
import com.l2jserver.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;

/**
 * @author fissban
 */
public class InstanceWorldManager
{
	private static final Logger LOGGER = Logger.getLogger(InstanceWorldManager.class.getName());
	
	private static final int TEMPLATE_ID = 100;
	
	private String _instanceFile = "";
	private int _mainInstanceWorldId = 0;
	private final List<Integer> _instanceWorldIds = new ArrayList<>();
	
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
	public InstanceWorld createNewInstanceWorld(boolean mainWorld)
	{
		InstanceWorld world = null;
		try
		{
			int instanceId = InstanceManager.getInstance().createDynamicInstance(_instanceFile);
			world = new InstanceWorld();
			world.setInstanceId(TEMPLATE_ID);
			world.setStatus(0);
			InstanceManager.getInstance().addWorld(world);
			_instanceWorldIds.add(instanceId);
			closeTheDoors(instanceId);
			
			if (mainWorld)
			{
				_mainInstanceWorldId = instanceId;
			}
		}
		catch (Exception e)
		{
			LOGGER.warning(EventEngineManager.class.getSimpleName() + ": -> createDynamicInstances() " + e);
			e.printStackTrace();
		}
		
		return world;
	}
	
	public void closeTheDoors(int instanceId)
	{
		for (L2DoorInstance door : InstanceManager.getInstance().getInstance(instanceId).getDoors())
		{
			door.closeMe();
		}
	}
	
	public List<Integer> getAllInstancesWorldId()
	{
		return _instanceWorldIds;
	}
	
	public InstanceWorld getWorld(int id)
	{
		return InstanceManager.getInstance().getWorld(id);
	}
	
	public void setMainWorldId(int id)
	{
		_mainInstanceWorldId = id;
	}
	
	public InstanceWorld getMainWorld()
	{
		return InstanceManager.getInstance().getWorld(_mainInstanceWorldId);
	}
	
	public int getMainWorldId()
	{
		return _mainInstanceWorldId;
	}
}
