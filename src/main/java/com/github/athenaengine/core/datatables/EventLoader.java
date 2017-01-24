package com.github.athenaengine.core.datatables;

import com.github.athenaengine.core.model.config.MainEventConfig;
import com.github.athenaengine.core.interfaces.IEventContainer;
import com.github.athenaengine.core.util.GsonUtils;
import com.github.athenaengine.core.util.JarUtils;
import com.l2jserver.util.Rnd;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EventLoader {

    private static final String MAIN_CONFIG_PATH = "./eventengine/EventEngine.conf";
    private static final String EVENT_JAR_PATH = "./eventengine/events/";
    private static final String EVENT_CONFIG_NAME = "config.conf";

    private final ArrayList<IEventContainer> _eventList = new ArrayList<>();
    private final Map<String, IEventContainer> _eventMap = new HashMap<>();
    private MainEventConfig mMainConfig;

    private EventLoader()
    {
        loadEvents();
    }

    public IEventContainer getEvent(String name)
    {
        return _eventMap.get(name);
    }

    public IEventContainer getRandomEventType()
    {
        return _eventList.get(Rnd.get(_eventList.size() - 1));
    }

    public ArrayList<IEventContainer> getEnabledEvents()
    {
        return _eventList;
    }

    private void loadEvents()
    {
        mMainConfig = (MainEventConfig) GsonUtils.loadConfig(MAIN_CONFIG_PATH, MainEventConfig.class);

        File files = new File(EVENT_JAR_PATH);
        File[] matchingFiles = files.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });

        if (matchingFiles != null) {
            for (File jar : matchingFiles) {
                IEventContainer container = (IEventContainer) JarUtils.loadJar(getClass().getClassLoader(), jar);

                if (container != null)
                {
                    String folderPath = EVENT_JAR_PATH + container.getSimpleEventName() + "/";

                    if (JarUtils.writeFileFromResources(container.getClass().getClassLoader(), folderPath, EVENT_CONFIG_NAME)) {
                        _eventList.add(container);
                        _eventMap.put(container.getSimpleEventName(), container);
                    }
                }
            }
        }
    }

    public static EventLoader getInstance()
    {
        return EventLoader.SingletonHolder._instance;
    }

    private static class SingletonHolder
    {
        private static final EventLoader _instance = new EventLoader();
    }
}
