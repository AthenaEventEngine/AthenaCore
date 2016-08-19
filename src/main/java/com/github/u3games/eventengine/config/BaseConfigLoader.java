package com.github.u3games.eventengine.config;

import com.github.u3games.eventengine.config.interfaces.EventConfig;
import com.github.u3games.eventengine.config.model.MainEventConfig;
import com.github.u3games.eventengine.events.teamvsteam.TvTEventConfig;
import com.google.gson.Gson;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;

import java.io.File;

public class BaseConfigLoader {

    private static final String MAIN_CONFIG_PATH = "./config/EventEngine/EventEngine.conf";
    private static final String TVT_CONFIG_PATH = "./config/EventEngine/TeamVsTeam.conf";

    private static BaseConfigLoader sInstance;

    private MainEventConfig mMainConfig;
    private TvTEventConfig mTvTConfig;

    private BaseConfigLoader() {
        mMainConfig = (MainEventConfig) loadConfig(MAIN_CONFIG_PATH, MainEventConfig.class);
        mTvTConfig = (TvTEventConfig) loadConfig(TVT_CONFIG_PATH, TvTEventConfig.class);

        System.out.println("Global message " + mMainConfig.getGlobalMessage());
        System.out.println("Dualbox max allowed " + mMainConfig.getDualbox().getMaxAllowed());

        System.out.println("Reward kill enabled " + mTvTConfig.isRewardKillEnabled());
        System.out.println("Team Red " + mTvTConfig.getTeamRed().get(0).toString());
        System.out.println("Team Blue " + mTvTConfig.getTeamBlue().get(0).toString());
    }

    private EventConfig loadConfig(String path, Class c) {
        Gson gson = new Gson();

        File configFile = new File(path);
        Config config = ConfigFactory.parseFile(configFile);

        String configJSON = config.root().render(ConfigRenderOptions.concise());
        return (EventConfig) gson.fromJson(configJSON, c);
    }

    public MainEventConfig getMainConfig() {
        return mMainConfig;
    }

    public TvTEventConfig getTvTConfig() {
        return mTvTConfig;
    }

    public static BaseConfigLoader getInstance() {
        if (sInstance == null) sInstance = new BaseConfigLoader();
        return sInstance;
    }
}