package com.github.u3games.eventengine.interfaces;

import com.github.u3games.eventengine.model.base.BaseEvent;

public interface EventContainer {

    Class<? extends BaseEvent> getEventClass();

    String getEventName();

    String getSimpleEventName();

    String getDescription();

    BaseEvent newEventInstance();
}
