package com.github.athenaengine.core.model;

import com.l2jserver.gameserver.model.Location;

public class ELocation {

    private int x;
    private int y;
    private int z;
    private int heading;
    private int instanceId;

    public ELocation(Location location) {
        x = location.getX();
        y = location.getY();
        z = location.getZ();
        heading = location.getHeading();
        instanceId = location.getInstanceId();
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

    public int getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(int instanceId) {
        this.instanceId = instanceId;
    }

    public Location getLocation() {
        Location loc = new Location(x, y, z, heading);
        loc.setInstanceId(instanceId);
        return loc;
    }
}
