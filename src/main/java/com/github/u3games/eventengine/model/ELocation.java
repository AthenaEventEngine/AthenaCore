package com.github.u3games.eventengine.model;

import com.l2jserver.gameserver.model.Location;

public class ELocation {

    private int x;
    private int y;
    private int z;
    private int heading;

    public ELocation(Location location) {
        x = location.getX();
        y = location.getY();
        z = location.getZ();
        heading = location.getHeading();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public int getHeading() {
        return heading;
    }

    public Location getLocation() {
        return new Location(x, y, z, heading);
    }
}
