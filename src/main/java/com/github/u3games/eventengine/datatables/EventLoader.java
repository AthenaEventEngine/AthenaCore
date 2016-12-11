package com.github.u3games.eventengine.datatables;

import com.github.u3games.eventengine.config.model.EventsListConfig;
import com.github.u3games.eventengine.config.model.MainEventConfig;
import com.github.u3games.eventengine.events.handler.AbstractEvent;
import com.github.u3games.eventengine.util.GsonUtils;
import com.l2jserver.util.Rnd;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventLoader {

    private static final Logger LOGGER = Logger.getLogger(EventLoader.class.getName());
    private static final String MAIN_CONFIG_PATH = "./data/eventengine/EventEngine.conf";
    private static final String EVENTS_CONFIG_PATH = "./data/eventengine/Events.conf";
    private static final String EVENT_JAR_PATH = "./data/eventengine/events/";

    private final ArrayList<Class<? extends AbstractEvent>> _eventList = new ArrayList<>();
    private final Map<String, Class<? extends AbstractEvent>> _eventMap = new HashMap<>();
    private MainEventConfig mMainConfig;
    private EventsListConfig mEventsListConfig;

    private EventLoader()
    {
        loadEvents();
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
            LOGGER.log(Level.WARNING, e.getMessage());
        }
        return null;
    }

    private void loadEvents()
    {
        mMainConfig = (MainEventConfig) GsonUtils.loadConfig(MAIN_CONFIG_PATH, MainEventConfig.class);
        EventsListConfig eventsListConfig = (EventsListConfig) GsonUtils.loadConfig(EVENTS_CONFIG_PATH, EventsListConfig.class);

        for (EventsListConfig.Event event : eventsListConfig.getEvents())
        {
            Class<? extends AbstractEvent> eventClass = loadJar(new File(EVENT_JAR_PATH + event.getJarName()), event.getClassPath());
            
            if (eventClass != null)
            {
                _eventList.add(eventClass);
                _eventMap.put(eventClass.getSimpleName(), eventClass);
            }
        }
    }

    private Class<? extends AbstractEvent> loadJar(File jarPath, String classPath)
    {
        try {
            URL classUrl;
            URL[] classUrls;

            try {
                classUrl = jarPath.toURI().toURL();
                classUrls = new URL[] { classUrl };
            } catch (MalformedURLException ex) {
                throw new InvalidJarLoadException("File URL malformed " + jarPath.getName());
            }

            URLClassLoader child = new URLClassLoader(classUrls, this.getClass().getClassLoader());
            Class classToLoad;

            try {
                classToLoad = Class.forName(classPath, true, child);
            } catch (ClassNotFoundException ex) {
                throw new InvalidJarLoadException("Cannot find main class " + classPath);
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

    public static EventLoader getInstance()
    {
        return EventLoader.SingletonHolder._instance;
    }

    private static class SingletonHolder
    {
        protected static final EventLoader _instance = new EventLoader();
    }
}
