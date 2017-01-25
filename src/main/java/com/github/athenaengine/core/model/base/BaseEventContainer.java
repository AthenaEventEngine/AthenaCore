package com.github.athenaengine.core.model.base;

import com.github.athenaengine.core.interfaces.IEventConfig;
import com.github.athenaengine.core.interfaces.IEventContainer;
import com.github.athenaengine.core.util.GsonUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseEventContainer implements IEventContainer {

    private static final Logger LOGGER = Logger.getLogger(BaseEventContainer.class.getName());
    private static final String EVENTS_PATH = "./eventengine/events/";

    private IEventConfig _config;

    public BaseEventContainer()
    {

    }

    public String getSimpleEventName()
    {
        return getEventName().toLowerCase().replace(" ", "");
    }

    protected abstract Class<? extends IEventConfig> getConfigClass();

    protected IEventConfig getConfig()
    {
        if (_config == null) _config = (IEventConfig) GsonUtils.loadConfig(EVENTS_PATH + getSimpleEventName() + "/config.conf", getConfigClass());
        return _config;
    }

    public BaseEvent newEventInstance()
    {
        EventBuilder builder = new EventBuilder();
        builder.setEventClass(getEventClass());
        builder.setConfig(getConfig());
        return builder.build();
    }

    private static class EventBuilder {

        private final Logger LOGGER = Logger.getLogger(EventBuilder.class.getName());

        private Class<? extends BaseEvent> _eventClass;
        private IEventConfig _config;

        private EventBuilder setEventClass(Class<? extends BaseEvent> eventClass)
        {
            _eventClass = eventClass;
            return this;
        }

        public EventBuilder setConfig(IEventConfig config)
        {
            _config = config;
            return this;
        }

        private BaseEvent build()
        {
            BaseEvent event;

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
