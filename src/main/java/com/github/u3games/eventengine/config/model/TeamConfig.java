package com.github.u3games.eventengine.config.model;

import com.github.u3games.eventengine.enums.TeamType;
import com.github.u3games.eventengine.model.ELocation;
import com.github.u3games.eventengine.util.ConvertUtils;
import com.l2jserver.gameserver.model.Location;

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

    public List<Location> getLocations() {
        return ConvertUtils.convertToListLocations(locations);
    }
}
