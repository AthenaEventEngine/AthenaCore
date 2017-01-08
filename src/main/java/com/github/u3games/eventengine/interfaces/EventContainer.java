package com.github.u3games.eventengine.interfaces;

import com.github.u3games.eventengine.events.handler.AbstractEvent;

public interface EventContainer {

    Class<? extends AbstractEvent> getEventClass();

    String getEventName();

    String getSimpleEventName();

    String getDescription();

    AbstractEvent newEventInstance();

    boolean checkStructure();
}
