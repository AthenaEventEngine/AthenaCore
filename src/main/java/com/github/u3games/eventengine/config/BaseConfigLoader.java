package com.github.u3games.eventengine.config;

import com.github.u3games.eventengine.config.interfaces.EventConfig;
import com.github.u3games.eventengine.config.model.MainEventConfig;
import com.github.u3games.eventengine.events.types.allvsall.AllVsAllEventConfig;
import com.github.u3games.eventengine.events.types.capturetheflag.CTFEventConfig;
import com.github.u3games.eventengine.events.types.survive.SurviveEventConfig;
import com.github.u3games.eventengine.events.types.teamvsteam.TvTEventConfig;
import com.google.gson.Gson;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;

import java.io.File;

public class BaseConfigLoader {

    private static final String MAIN_CONFIG_PATH = "./config/EventEngine/EventEngine.conf";
    private static final String TVT_CONFIG_PATH = "./config/EventEngine/TeamVsTeam.conf";
    private static final String CTF_CONFIG_PATH = "./config/EventEngine/CaptureTheFlag.conf";
    private static final String ALLVSALL_CONFIG_PATH = "./config/EventEngine/AllVsAll.conf";
    private static final String SURVIVE_CONFIG_PATH = "./config/EventEngine/Survive.conf";


    private static BaseConfigLoader sInstance;

    private MainEventConfig mMainConfig;
    private TvTEventConfig mTvTConfig;
    private CTFEventConfig mCtfConfig;
    private AllVsAllEventConfig mAllVsAllConfig;
    private SurviveEventConfig mSurviveConfig;

    private BaseConfigLoader() {
        mMainConfig = (MainEventConfig) loadConfig(MAIN_CONFIG_PATH, MainEventConfig.class);
        mTvTConfig = (TvTEventConfig) loadConfig(TVT_CONFIG_PATH, TvTEventConfig.class);
        mCtfConfig = (CTFEventConfig) loadConfig(CTF_CONFIG_PATH, CTFEventConfig.class);
        mAllVsAllConfig = (AllVsAllEventConfig) loadConfig(ALLVSALL_CONFIG_PATH, AllVsAllEventConfig.class);
        mSurviveConfig = (SurviveEventConfig) loadConfig(SURVIVE_CONFIG_PATH, SurviveEventConfig.class);
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

    public CTFEventConfig getCtfConfig() {
        return mCtfConfig;
    }

    public AllVsAllEventConfig getAllVsAllConfig() {
        return mAllVsAllConfig;
    }

    public SurviveEventConfig getSurviveConfig() {
        return mSurviveConfig;
    }

    public static BaseConfigLoader getInstance() {
        if (sInstance == null) sInstance = new BaseConfigLoader();
        return sInstance;
    }
}