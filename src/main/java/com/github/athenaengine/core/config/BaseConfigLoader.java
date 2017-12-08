package com.github.athenaengine.core.config;

import com.github.athenaengine.core.model.config.MainEventConfig;
import com.luksdlt92.winstonutils.GsonHelper;

import java.io.File;

public class BaseConfigLoader {

    private static final String MAIN_CONFIG_PATH = "./eventengine/EventEngine.conf";

    private MainEventConfig mMainConfig;

    private BaseConfigLoader() {
        mMainConfig = (MainEventConfig) GsonHelper.load(new File(MAIN_CONFIG_PATH), new MainEventConfig());
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