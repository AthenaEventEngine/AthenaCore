package com.github.u3games.eventengine.model;

import com.github.u3games.eventengine.events.handler.AbstractEvent;

public abstract class EventContainer {

    public abstract Class<? extends AbstractEvent> getEventClass();

    public abstract String getBaseConfigPath();
}
