package com.github.athenaengine.core.model.config;

import com.github.athenaengine.core.interfaces.IEventConfig;
import com.google.gson.annotations.SerializedName;

public class MainEventConfig implements IEventConfig {

    @SerializedName("npcId") private int mNpcId;
    @SerializedName("globalMessage") private boolean mGlobalMessage;
    @SerializedName("interval") private int mInterval;
    @SerializedName("votingEnabled") private boolean mVotingEnabled;
    @SerializedName("votingTime") private int mVotingTime;
    @SerializedName("registerTime") private int mRegisterTime;
    @SerializedName("runningTime") private int mRunningTime;
    @SerializedName("textTimeForEnd") private int mTextTimeForEnd;
    @SerializedName("chaoticPlayerRegister") private boolean mChaoticPlayerRegister;
    @SerializedName("killerMessage") private boolean mKillerMessage;
    @SerializedName("friendlyFire") private boolean mFriendlyFire;
    @SerializedName("minPlayers") private int mMinPlayers;
    @SerializedName("maxPlayers") private int mMaxPlayers;
    @SerializedName("minPlayerLevel") private int mMinPlayerLevel;
    @SerializedName("maxPlayerLevel") private int mMaxPlayerLevel;
    @SerializedName("maxBuffCount") private int mMaxBuffCount;
    @SerializedName("antiAfkEnabled") private boolean mAntiAfkEnabled;
    @SerializedName("antiAfkCheckTime") private int mAntiAfkCheckTime;
    @SerializedName("spawnProtectionTime") private int mSpawnProtectionTime;
    @SerializedName("dualbox") private DualboxEventConfig mDualbox;

    public int getNpcId() {
        return mNpcId;
    }

    public boolean getGlobalMessage() {
        return mGlobalMessage;
    }

    public int getInterval() {
        return mInterval;
    }

    public boolean isVotingEnabled() {
        return mVotingEnabled;
    }

    public int getVotingTime() {
        return mVotingTime;
    }

    public int getRegisterTime() {
        return mRegisterTime;
    }

    public int getRunningTime() {
        return mRunningTime;
    }

    public int getTextTimeForEnd() {
        return mTextTimeForEnd;
    }

    public boolean isChaoticPlayerRegisterAllowed() {
        return mChaoticPlayerRegister;
    }

    public boolean isKillerMessageEnabled() {
        return mKillerMessage;
    }

    public boolean isFriendlyFireEnabled() {
        return mFriendlyFire;
    }

    public int getMinPlayers() {
        return mMinPlayers;
    }

    public int getMaxPlayers() {
        return mMaxPlayers;
    }

    public int getMinPlayerLevel() {
        return mMinPlayerLevel;
    }

    public int getMaxPlayerLevel() {
        return mMaxPlayerLevel;
    }

    public int getMaxBuffCount() {
        return mMaxBuffCount;
    }

    public boolean isAntiAfkEnabled() {
        return mAntiAfkEnabled;
    }

    public int getAntiAfkCheckTime() {
        return mAntiAfkCheckTime;
    }

    public int getSpawnProtectionTime() {
        return mSpawnProtectionTime;
    }

    public DualboxEventConfig getDualbox() {
        return mDualbox;
    }
}
