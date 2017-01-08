package com.github.u3games.eventengine.model;

import com.github.u3games.eventengine.config.interfaces.EventConfig;
import com.github.u3games.eventengine.events.handler.AbstractEvent;
import com.github.u3games.eventengine.interfaces.EventContainer;
import com.github.u3games.eventengine.util.GsonUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseEventContainer implements EventContainer {

    private static final Logger LOGGER = Logger.getLogger(BaseEventContainer.class.getName());
    private static final String EVENTS_PATH = "./eventengine/events/";

    private EventConfig _config;

    public BaseEventContainer()
    {

    }

    public String getSimpleEventName()
    {
        return getEventName().toLowerCase().replace(" ", "");
    }

    protected abstract Class<? extends EventConfig> getConfigClass();

    protected EventConfig getConfig()
    {
        if (_config == null) _config = (EventConfig) GsonUtils.loadConfig(EVENTS_PATH + getSimpleEventName() + "/config.conf", getConfigClass());
        return _config;
    }

    public AbstractEvent newEventInstance()
    {
        EventBuilder builder = new EventBuilder();
        builder.setEventClass(getEventClass());
        builder.setConfig(getConfig());
        return builder.build();
    }

    private static class EventBuilder {

        private final Logger LOGGER = Logger.getLogger(EventBuilder.class.getName());

        private Class<? extends AbstractEvent> _eventClass;
        private EventConfig _config;

        private EventBuilder setEventClass(Class<? extends AbstractEvent> eventClass)
        {
            _eventClass = eventClass;
            return this;
        }

        public EventBuilder setConfig(EventConfig config)
        {
            _config = config;
            return this;
        }

        private AbstractEvent build()
        {
            AbstractEvent event;

            try
            {
                event = _eventClass.newInstance();
                event.setConfig(_config);
                event.initialize();
                return event;
            }
            catch (Exception e)
            {
                LOGGER.log(Level.WARNING, e.getMessage());
            }

            return null;
        }
    }
}
