package com.github.u3games.eventengine.datatables;

import com.github.u3games.eventengine.config.model.EventsListConfig;
import com.github.u3games.eventengine.config.model.MainEventConfig;
import com.github.u3games.eventengine.interfaces.EventContainer;
import com.github.u3games.eventengine.util.GsonUtils;
import com.l2jserver.util.Rnd;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventLoader {

    private static final Logger LOGGER = Logger.getLogger(EventLoader.class.getName());
    private static final String MAIN_CONFIG_PATH = "./data/eventengine/EventEngine.conf";
    private static final String EVENTS_CONFIG_PATH = "./data/eventengine/Events.conf";
    private static final String EVENT_JAR_PATH = "./data/eventengine/events/";

    private final ArrayList<EventContainer> _eventList = new ArrayList<>();
    private final Map<String, EventContainer> _eventMap = new HashMap<>();
    private MainEventConfig mMainConfig;

    private EventLoader()
    {
        loadEvents();
    }

    public EventContainer getEvent(String name)
    {
        return _eventMap.get(name);
    }

    public EventContainer getRandomEventType()
    {
        return _eventList.get(Rnd.get(_eventList.size() - 1));
    }

    public ArrayList<EventContainer> getEnabledEvents()
    {
        return _eventList;
    }

    private void loadEvents()
    {
        mMainConfig = (MainEventConfig) GsonUtils.loadConfig(MAIN_CONFIG_PATH, MainEventConfig.class);
        EventsListConfig eventsListConfig = (EventsListConfig) GsonUtils.loadConfig(EVENTS_CONFIG_PATH, EventsListConfig.class);

        for (EventsListConfig.Event event : eventsListConfig.getEvents())
        {
            EventContainer container = (EventContainer) loadJar(new File(EVENT_JAR_PATH + event.getJarName() + ".jar"), event.getClassPath());

            if (container != null)
            {
                _eventList.add(container);
                _eventMap.put(container.getEventName(), container);
            }
        }
    }

    private Object loadJar(File jarPath, String classPath)
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

            if (!classToLoad.getSuperclass().getSimpleName().equalsIgnoreCase("BaseEventContainer")) {
                throw new InvalidJarLoadException("Wrong inheritance for " + classToLoad.getSimpleName());
            }

            try {
                return classToLoad.newInstance();
            } catch (InstantiationException ex) {
                throw new InvalidJarLoadException("The class " + classToLoad.getSimpleName() + " cannot be instantiated");
            } catch (IllegalAccessException ex) {
                throw new InvalidJarLoadException("The class " + classToLoad.getSimpleName() + " cannot be accessed");
            }
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
