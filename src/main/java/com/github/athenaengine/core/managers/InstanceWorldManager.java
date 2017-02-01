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
package com.github.athenaengine.core.managers;

import java.util.ArrayList;
import java.util.List;

import com.github.athenaengine.core.model.instance.WorldInstance;

public class InstanceWorldManager {
	private final List<WorldInstance> _instanceWorlds = new ArrayList<>();

	public static InstanceWorldManager newInstance() {
		return new InstanceWorldManager();
	}

	private InstanceWorldManager() {}

	public WorldInstance createNewInstanceWorld(String instanceFile) {
		WorldInstance instance = WorldInstance.newInstance(instanceFile);
		_instanceWorlds.add(instance);
		return instance;
	}
	
	public List<WorldInstance> getAllInstances() {
		return _instanceWorlds;
	}
	
	public void destroyAllInstances() {
		for (WorldInstance instance : _instanceWorlds) {
			instance.destroy();
		}
	}
}