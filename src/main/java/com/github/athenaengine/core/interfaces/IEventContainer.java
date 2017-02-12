package com.github.athenaengine.core.interfaces;

import com.github.athenaengine.core.model.base.BaseEvent;

public interface IEventContainer {

    Class<? extends BaseEvent> getEventClass();

    String getEventName();

    String getSimpleEventName();

    String getDescription();

    int getMinLevel();

    int getMaxLevel();

    int getMinParticipants();

    int getMaxParticipants();

    int getRunningTime();

    String getRewards();

    BaseEvent newEventInstance();
}
