package com.github.u3games.eventengine.events.types.capturetheflag;

import com.github.u3games.eventengine.config.model.TeamConfig;
import com.github.u3games.eventengine.model.ELocation;
import com.l2jserver.gameserver.model.Location;

public class CTFTeamConfig extends TeamConfig {

    private ELocation flagLoc;
    private ELocation holderLoc;

    public Location getFlagLoc() {
        return new Location(flagLoc.getX(), flagLoc.getY(), flagLoc.getZ());
    }

    public Location getHolderLoc() {
        return new Location(holderLoc.getX(), holderLoc.getY(), holderLoc.getZ());
    }
}
