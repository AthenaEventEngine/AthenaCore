package com.github.athenaengine.core.config.model;

import com.github.athenaengine.core.enums.TeamType;
import com.github.athenaengine.core.model.ELocation;

import java.util.List;

public class TeamConfig {

    private String name;
    private String color;
    private List<ELocation> locations;

    public String getName() {
        return name;
    }

    public TeamType getColor() {
        return TeamType.getType(color);
    }

    public List<ELocation> getLocations() {
        return locations;
    }
}
