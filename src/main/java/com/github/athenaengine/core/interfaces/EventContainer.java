package com.github.athenaengine.core.interfaces;

import com.github.athenaengine.core.model.base.BaseEvent;

public interface EventContainer {

    Class<? extends BaseEvent> getEventClass();

    String getEventName();

    String getSimpleEventName();

    String getDescription();

    BaseEvent newEventInstance();
}
