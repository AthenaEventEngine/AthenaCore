package com.github.athenaengine.core.model.instance;

import com.github.athenaengine.core.EventEngineManager;
import com.github.athenaengine.core.EventEngineWorld;
import com.github.athenaengine.core.model.entity.Player;
import com.l2jserver.gameserver.instancemanager.InstanceManager;
import com.l2jserver.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;

import java.util.logging.Logger;

public class WorldInstance {

    private static final Logger LOGGER = Logger.getLogger(WorldInstance.class.getName());

    private InstanceWorld mInstanceWorld;

    public static WorldInstance newInstance(String instanceFile) {
        return new WorldInstance(instanceFile);
    }

    private WorldInstance(String instanceFile) {
        mInstanceWorld = null;
        InstanceWorld world;
        try {
            int instanceId = InstanceManager.getInstance().createDynamicInstance(instanceFile);
            InstanceManager.getInstance().getInstance(instanceId).setAllowSummon(false);
            InstanceManager.getInstance().getInstance(instanceId).setPvPInstance(true);
            InstanceManager.getInstance().getInstance(instanceId).setEjectTime(10 * 60 * 1000); // Prevent eject death players
            InstanceManager.getInstance().getInstance(instanceId).setEmptyDestroyTime(1000 + 60000L);

            // We closed the doors of the instance if there
            for (L2DoorInstance door : InstanceManager.getInstance().getInstance(instanceId).getDoors())
            {
                door.closeMe();
            }
            world = new EventEngineWorld();
            world.setInstanceId(instanceId);
            world.setTemplateId(100); // TODO hardcode
            world.setStatus(0);
            InstanceManager.getInstance().addWorld(world);
            mInstanceWorld = world;
        } catch (Exception e) {
            LOGGER.warning(EventEngineManager.class.getSimpleName() + ": createNewInstanceWorld() " + e);
            e.printStackTrace();
        }
    }

    public int getInstanceId() {
        return mInstanceWorld.getInstanceId();
    }

    public void addPlayer(Player player) {
        mInstanceWorld.addAllowed(player.getObjectId());
    }

    public void destroy() {
        InstanceManager.getInstance().destroyInstance(mInstanceWorld.getInstanceId());
    }
}
