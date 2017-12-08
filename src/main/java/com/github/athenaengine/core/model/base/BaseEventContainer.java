package com.github.athenaengine.core.model.base;

import com.github.athenaengine.core.config.BaseConfigLoader;
import com.github.athenaengine.core.interfaces.IEventConfig;
import com.github.athenaengine.core.interfaces.IEventContainer;
import com.luksdlt92.winstonutils.GsonHelper;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseEventContainer implements IEventContainer {

    private static final Logger LOGGER = Logger.getLogger(BaseEventContainer.class.getName());
    private static final String EVENTS_PATH = "./eventengine/events/";

    private IEventConfig mConfig;

    public BaseEventContainer() {}

    public String getSimpleEventName() {
        return getEventName().toLowerCase().replace(" ", "");
    }

    @Override
    public int getMinLevel() {
        return BaseConfigLoader.getInstance().getMainConfig().getMinPlayerLevel();
    }

    @Override
    public int getMaxLevel() {
        return BaseConfigLoader.getInstance().getMainConfig().getMaxPlayerLevel();
    }

    @Override
    public int getMinParticipants() {
        return BaseConfigLoader.getInstance().getMainConfig().getMinPlayers();
    }

    @Override
    public int getMaxParticipants() {
        return BaseConfigLoader.getInstance().getMainConfig().getMaxPlayers();
    }

    @Override
    public int getRunningTime() {
        return BaseConfigLoader.getInstance().getMainConfig().getRunningTime();
    }

    @Override
    public String getRewards() {
        return "-";
    }

    protected abstract Class<? extends IEventConfig> getConfigClass();

    protected IEventConfig getConfig() {
        try {
            if (mConfig == null) mConfig = (IEventConfig) GsonHelper.load(new File(EVENTS_PATH + getSimpleEventName() + "/config.conf"), getConfigClass().newInstance());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        }

        return mConfig;
    }

    public BaseEvent newEventInstance() {
        EventBuilder builder = new EventBuilder();
        builder.setEventClass(getEventClass());
        builder.setConfig(getConfig());
        return builder.build();
    }

    private static class EventBuilder {

        private final Logger LOGGER = Logger.getLogger(EventBuilder.class.getName());

        private Class<? extends BaseEvent> mEventClass;
        private IEventConfig mConfig;

        private EventBuilder setEventClass(Class<? extends BaseEvent> eventClass) {
            mEventClass = eventClass;
            return this;
        }

        public EventBuilder setConfig(IEventConfig config) {
            mConfig = config;
            return this;
        }

        private BaseEvent build() {
            BaseEvent event;

            try {
                event = mEventClass.newInstance();
                event.setConfig(mConfig);
                event.initialize();
                return event;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, e.getMessage());
            }

            return null;
        }
    }
}
