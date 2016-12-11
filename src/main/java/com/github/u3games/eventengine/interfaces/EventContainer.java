package com.github.u3games.eventengine.interfaces;

import com.github.u3games.eventengine.events.handler.AbstractEvent;

public interface EventContainer {

    Class<? extends AbstractEvent> getEventClass();

    String getBaseConfigPath();

    String getEventName();

    String getDescription();

    AbstractEvent newEventInstance();
}
