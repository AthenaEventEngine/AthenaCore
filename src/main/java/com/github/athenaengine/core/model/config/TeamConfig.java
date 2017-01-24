package com.github.athenaengine.core.model.config;

import com.github.athenaengine.core.enums.TeamType;
import com.github.athenaengine.core.model.holder.LocationHolder;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TeamConfig {

    @SerializedName("name") private String mName;
    @SerializedName("color") private String mColor;
    @SerializedName("locations") private List<LocationHolder> mLocations;

    public String getName() {
        return mName;
    }

    public TeamType getColor() {
        return TeamType.getType(mColor);
    }

    public List<LocationHolder> getLocations() {
        return mLocations;
    }
}
