package com.github.u3games.eventengine.events.types.allvsall;

import com.github.u3games.eventengine.config.interfaces.EventConfig;
import com.github.u3games.eventengine.model.EItemHolder;
import com.github.u3games.eventengine.model.ELocation;
import com.github.u3games.eventengine.util.ConvertUtils;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.holders.ItemHolder;

import java.util.List;

public class AllVsAllEventConfig implements EventConfig {

    private boolean enabled;
    private String instanceFile;
    private List<EItemHolder> reward;
    private boolean rewardKillEnabled;
    private List<EItemHolder> rewardKill;
    private boolean rewardPvPKillEnabled;
    private int rewardPvPKill;
    private boolean rewardFameKillEnabled;
    private int rewardFameKill;
    private List<ELocation> coordinates;

    public boolean isEnabled() {
        return enabled;
    }

    public String getInstanceFile() {
        return instanceFile;
    }

    public List<ItemHolder> getReward() {
        return ConvertUtils.convertToListItemsHolders(reward);
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

    public List<Location> getCoordinates() {
        return ConvertUtils.convertToListLocations(coordinates);
    }
}
