package com.github.u3games.eventengine.events.types.teamvsteam;

import com.github.u3games.eventengine.config.interfaces.EventConfig;
import com.github.u3games.eventengine.config.model.TeamConfig;
import com.github.u3games.eventengine.model.EItemHolder;
import com.github.u3games.eventengine.util.ConvertUtils;
import com.l2jserver.gameserver.model.holders.ItemHolder;

import java.util.List;

public class TvTEventConfig implements EventConfig {

    private boolean enabled;
    private String instanceFile;
    private List<EItemHolder> reward;
    private boolean rewardKillEnabled;
    private List<EItemHolder> rewardKill;
    private boolean rewardPvPKillEnabled;
    private int rewardPvPKill;
    private boolean rewardFameKillEnabled;
    private int rewardFameKill;
    private List<TeamConfig> teams;

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
        return ConvertUtils.convertToListItemsHolders(rewardKill);
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

    public List<ItemHolder> getReward() {
        return ConvertUtils.convertToListItemsHolders(reward);
    }

    public List<TeamConfig> getTeams() {
        return teams;
    }
}
