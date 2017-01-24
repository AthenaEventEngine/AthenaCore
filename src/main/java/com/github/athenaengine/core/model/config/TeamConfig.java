package com.github.athenaengine.core.model.config;

import com.github.athenaengine.core.enums.TeamType;
import com.github.athenaengine.core.model.holder.LocationHolder;

import java.util.List;

public class TeamConfig {

    private String name;
    private String color;
    private List<LocationHolder> locations;

    public String getName() {
        return name;
    }

    public TeamType getColor() {
        return TeamType.getType(color);
    }

    public List<LocationHolder> getLocations() {
        return locations;
    }
}
