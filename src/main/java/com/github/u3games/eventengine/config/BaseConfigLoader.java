package com.github.u3games.eventengine.config;

import com.github.u3games.eventengine.config.model.MainEventConfig;
import com.github.u3games.eventengine.events.types.allvsall.AllVsAllEventConfig;
import com.github.u3games.eventengine.events.types.capturetheflag.CTFEventConfig;
import com.github.u3games.eventengine.events.types.survive.SurviveEventConfig;
import com.github.u3games.eventengine.events.types.teamvsteam.TvTEventConfig;
import com.github.u3games.eventengine.util.GsonUtils;

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
        mMainConfig = (MainEventConfig) GsonUtils.loadConfig(MAIN_CONFIG_PATH, MainEventConfig.class);
        mTvTConfig = (TvTEventConfig) GsonUtils.loadConfig(TVT_CONFIG_PATH, TvTEventConfig.class);
        mCtfConfig = (CTFEventConfig) GsonUtils.loadConfig(CTF_CONFIG_PATH, CTFEventConfig.class);
        mAllVsAllConfig = (AllVsAllEventConfig) GsonUtils.loadConfig(ALLVSALL_CONFIG_PATH, AllVsAllEventConfig.class);
        mSurviveConfig = (SurviveEventConfig) GsonUtils.loadConfig(SURVIVE_CONFIG_PATH, SurviveEventConfig.class);
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