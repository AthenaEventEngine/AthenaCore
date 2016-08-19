package com.github.u3games.eventengine.events.teamvsteam;

import com.github.u3games.eventengine.config.interfaces.EventConfig;
import com.github.u3games.eventengine.model.ItemHolder;
import com.github.u3games.eventengine.model.Location;

import java.util.List;

public class TvTEventConfig implements EventConfig {

    private boolean enabled;
    private String instanceFile;
    private List<ItemHolder> reward;
    private boolean rewardKillEnabled;
    private List<ItemHolder> rewardKill;
    private boolean rewardPvPKillEnabled;
    private int rewardPvPKill;
    private boolean rewardFameKillEnabled;
    private int rewardFameKill;
    private int countTeam;
    private List<Location> teamRed;
    private List<Location> teamBlue;

    public boolean isEnabled() {
        return enabled;
    }

    public String getInstanceFile() {
        return instanceFile;
    }

    public boolean isRewardKillEnabled() {
        return rewardKillEnabled;
    }

    public List<ItemHolder> getRewardKill() {
        return rewardKill;
    }

    public boolean isRewardPvPKillEnabled() {
        return rewardPvPKillEnabled;
    }

    public int getRewardPvPKill() {
        return rewardPvPKill;
    }

    public boolean isRewardFameKillEnabled() {
        return rewardFameKillEnabled;
    }

    public int getRewardFameKill() {
        return rewardFameKill;
    }

    public int getCountTeam() {
        return countTeam;
    }

    public List<ItemHolder> getReward() {
        return reward;
    }

    public List<Location> getTeamRed() {
        return teamRed;
    }

    public List<Location> getTeamBlue() {
        return teamBlue;
    }
}
