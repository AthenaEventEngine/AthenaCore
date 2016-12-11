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
package com.github.u3games.eventengine.datatables;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.u3games.eventengine.config.BaseConfigLoader;
import com.github.u3games.eventengine.events.types.allvsall.AllVsAll;
import com.github.u3games.eventengine.events.types.capturetheflag.CaptureTheFlag;
import com.github.u3games.eventengine.events.types.survive.Survive;
import com.github.u3games.eventengine.events.handler.AbstractEvent;
import com.l2jserver.util.Rnd;

/**
 * @author Zephyr
 */
public class EventData
{
	private static final Logger LOGGER = Logger.getLogger(EventData.class.getName());
	private final ArrayList<Class<? extends AbstractEvent>> _eventList = new ArrayList<>();
	private final Map<String, Class<? extends AbstractEvent>> _eventMap = new HashMap<>();
	
	private EventData()
	{
		load();
	}
	
	private void load()
	{
		if (BaseConfigLoader.getInstance().getAllVsAllConfig().isEnabled())
		{
			_eventList.add(AllVsAll.class);
			_eventMap.put(AllVsAll.class.getSimpleName(), AllVsAll.class);
		}
		if (BaseConfigLoader.getInstance().getSurviveConfig().isEnabled())
		{
			_eventList.add(Survive.class);
			_eventMap.put(Survive.class.getSimpleName(), Survive.class);
		}
		if (BaseConfigLoader.getInstance().getTvTConfig().isEnabled())
		{
			Description descr = new Description(new File("./events/TeamVsTeam.jar"), "com.github.athenaeventengine.events.TeamVsTeam");
			Class<? extends AbstractEvent> classToLoad = loadJar(descr);

			if (classToLoad != null)
			{
				_eventList.add(classToLoad);
				_eventMap.put(classToLoad.getSimpleName(), classToLoad);
			}
		}
		if (BaseConfigLoader.getInstance().getCtfConfig().isEnabled())
		{
			_eventList.add(CaptureTheFlag.class);
			_eventMap.put(CaptureTheFlag.class.getSimpleName(), CaptureTheFlag.class);
		}
	}
	
	public Class<? extends AbstractEvent> getEvent(String name)
	{
		return _eventMap.get(name);
	}
	
	public Class<? extends AbstractEvent> getRandomEventType()
	{
		return _eventList.get(Rnd.get(_eventList.size() - 1));
	}
	
	public ArrayList<Class<? extends AbstractEvent>> getEnabledEvents()
	{
		return _eventList;
	}
	
	public AbstractEvent getNewEventInstance(Class<? extends AbstractEvent> type)
	{
		try
		{
			return type.newInstance();
		}
		catch (Exception e)
		{
			LOGGER.warning(EventData.class.getSimpleName() + ": " + e);
		}
		return null;
	}

	private Class<? extends AbstractEvent> loadJar(Description descr)
	{
		try {
			URL classUrl;
			URL[] classUrls;

			try {
				classUrl = descr.getFile().toURI().toURL();
				classUrls = new URL[] { classUrl };
			} catch (MalformedURLException ex) {
				throw new InvalidJarLoadException("File URL malformed " + descr.getFile().getName());
			}

			URLClassLoader child = new URLClassLoader(classUrls, this.getClass().getClassLoader());
			Class classToLoad;

			try {
				classToLoad = Class.forName(descr.getClassName(), true, child);
			} catch (ClassNotFoundException ex) {
				throw new InvalidJarLoadException("Cannot find main class " + descr.getClassName());
			}

			if (!classToLoad.getSuperclass().getSimpleName().equalsIgnoreCase("AbstractEvent")) {
				throw new InvalidJarLoadException("Wrong inheritance for " + classToLoad.getSimpleName());
			}

			return classToLoad;
		} catch (InvalidJarLoadException ex) {
			LOGGER.log(Level.SEVERE, ex.getMessage());
		}

		return null;
	}
	
	public static EventData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final EventData _instance = new EventData();
	}

	private class Description {

		private final File mFile;
		private final String mClassPath;

		private Description(File file, String classPath) {
			mFile = file;
			mClassPath = classPath;
		}

		public File getFile() {
			return mFile;
		}

		private String getClassName() {
			return mClassPath;
		}
	}

	private class InvalidJarLoadException extends Exception {

		private final String mMessage;

		private InvalidJarLoadException(String message) {
			super();
			mMessage = message;
		}

		@Override
		public final String getMessage() {
			return InvalidJarLoadException.class.getSimpleName() + mMessage;
		}
	}
}