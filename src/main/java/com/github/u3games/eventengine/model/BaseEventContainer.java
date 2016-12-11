package com.github.u3games.eventengine.model;

import com.github.u3games.eventengine.config.interfaces.EventConfig;
import com.github.u3games.eventengine.events.handler.AbstractEvent;
import com.github.u3games.eventengine.interfaces.EventContainer;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseEventContainer<T extends EventConfig> implements EventContainer {

    private static final Logger LOGGER = Logger.getLogger(BaseEventContainer.class.getName());

    private T _eventConfig;

    public AbstractEvent newEventInstance()
    {
        try
        {
            return getEventClass().newInstance();
        }
        catch (Exception e)
        {
            LOGGER.log(Level.WARNING, e.getMessage());
        }
        return null;
    }

}
