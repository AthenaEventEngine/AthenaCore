package com.github.u3games.eventengine.config.model;

import com.github.u3games.eventengine.config.interfaces.EventConfig;

public class MainEventConfig implements EventConfig {

    private int npcId;
    private boolean globalMessage;
    private int interval;
    private boolean votingEnabled;
    private int votingTime;
    private int registerTime;
    private int runningTime;
    private int textTimeForEnd;
    private boolean chaoticPlayerRegister;
    private boolean killerMessage;
    private boolean friendlyFire;
    private int minPlayers;
    private int maxPlayers;
    private int minPlayerLevel;
    private int maxPlayerLevel;
    private int maxBuffCount;
    private boolean antiAfkEnabled;
    private int antiAfkCheckTime;
    private int spawnProtectionTime;
    private DualboxEventConfig dualbox;

    public int getNpcId() {
        return npcId;
    }

    public boolean getGlobalMessage() {
        return globalMessage;
    }

    public int getInterval() {
        return interval;
    }

    public boolean isVotingEnabled() {
        return votingEnabled;
    }

    public int getVotingTime() {
        return votingTime;
    }

    public int getRegisterTime() {
        return registerTime;
    }

    public int getRunningTime() {
        return runningTime;
    }

    public int getTextTimeForEnd() {
        return textTimeForEnd;
    }

    public boolean isChaoticPlayerRegisterAllowed() {
        return chaoticPlayerRegister;
    }

    public boolean isKillerMessageEnabled() {
        return killerMessage;
    }

    public boolean isFriendlyFireEnabled() {
        return friendlyFire;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getMinPlayerLevel() {
        return minPlayerLevel;
    }

    public int getMaxPlayerLevel() {
        return maxPlayerLevel;
    }

    public int getMaxBuffCount() {
        return maxBuffCount;
    }

    public boolean isAntiAfkEnabled() {
        return antiAfkEnabled;
    }

    public int getAntiAfkCheckTime() {
        return antiAfkCheckTime;
    }

    public int getSpawnProtectionTime() {
        return spawnProtectionTime;
    }

    public DualboxEventConfig getDualbox() {
        return dualbox;
    }
}
