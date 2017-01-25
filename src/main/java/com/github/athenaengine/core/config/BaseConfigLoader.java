package com.github.athenaengine.core.config;

import com.github.athenaengine.core.model.config.MainEventConfig;
import com.github.athenaengine.core.util.GsonUtils;

public class BaseConfigLoader {

    private static final String MAIN_CONFIG_PATH = "./eventengine/EventEngine.conf";

    private MainEventConfig mMainConfig;

    private BaseConfigLoader() {
        mMainConfig = (MainEventConfig) GsonUtils.loadConfig(MAIN_CONFIG_PATH, MainEventConfig.class);
    }

    public MainEventConfig getMainConfig() {
        return mMainConfig;
    }

    public static BaseConfigLoader getInstance() {
        return BaseConfigLoader.SingletonHolder._instance;
    }

    private static class SingletonHolder {
        private static final BaseConfigLoader _instance = new BaseConfigLoader();
    }
}