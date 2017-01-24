package com.github.athenaengine.core.config;

import com.github.athenaengine.core.model.config.MainEventConfig;
import com.github.athenaengine.core.util.GsonUtils;

public class BaseConfigLoader {

    private static final String MAIN_CONFIG_PATH = "./config/EventEngine/EventEngine.conf";

    private static BaseConfigLoader sInstance;

    private MainEventConfig mMainConfig;

    private BaseConfigLoader() {
        mMainConfig = (MainEventConfig) GsonUtils.loadConfig(MAIN_CONFIG_PATH, MainEventConfig.class);
    }

    public MainEventConfig getMainConfig() {
        return mMainConfig;
    }

    public static BaseConfigLoader getInstance() {
        if (sInstance == null) sInstance = new BaseConfigLoader();
        return sInstance;
    }
}