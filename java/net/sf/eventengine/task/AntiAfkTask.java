/*
 * Copyright (C) 2015-2015 L2J EventEngine
 *
 * This file is part of L2J EventEngine.
 *
 * L2J EventEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2J EventEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.eventengine.task;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jserver.gameserver.instancemanager.InstanceManager;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;

import net.sf.eventengine.EventEngineManager;
import net.sf.eventengine.enums.EventEngineState;
import net.sf.eventengine.events.handler.AbstractEvent;
import net.sf.eventengine.events.holders.PlayerHolder;
import net.sf.eventengine.util.ProtectionUtil;

/**
 * @author swarlog
 */

public class AntiAfkTask implements Runnable
{
	// Debug
	static boolean debug = false;
	
	// Player List
	private final Set<L2PcInstance> _playerAfkInstance = ConcurrentHashMap.newKeySet();
	
	@Override
	public void run()
	{
		EventEngineState state = EventEngineManager.getInstance().getEventEngineState();
		switch (state)
		{
			case RUNNING_EVENT:
			{
				for (PlayerHolder ph : EventEngineManager.getInstance().getCurrentEvent().getAllEventPlayers())
				{
					if (ph == null)
					{
						continue;
					}
					
					if (ph.getPcInstance().isOnline())
					{
						// Check player list
						if (_playerAfkInstance.contains(ph))
						{
							// Execute check location
							playerCheckLocation(ph);
						}
						else
						{
							// Execute save location
							playerSaveLocation(ph);
						}
					}
				}
				break;
			}
		}
		
		// Clear list players
		if (state != EventEngineState.RUNNING_EVENT)
		{
			_playerAfkInstance.clear();
			ProtectionUtil._playersAfkLoc.clear();
		}
	}
	
	public void playerSaveLocation(PlayerHolder ph)
	{
		if (ph != null)
		{
			// Save Player in list
			_playerAfkInstance.add(ph.getPcInstance());
			
			// Save Location
			ProtectionUtil.addAfkLoc(ph);
			
			// Test I
			if (debug)
			{
				System.out.println("[ANTI-AFK] Location Test I - Player: " + ph.getPcInstance().getName() + ", locX: " + ph.getPcInstance().getX() + ", locY: " + ph.getPcInstance().getY() + ", locZ: " + ph.getPcInstance().getZ());
			}
		}
	}
	
	public void playerCheckLocation(PlayerHolder ph)
	{
		if (ph != null)
		{
			// Check Location
			Location phLoc = ProtectionUtil.getAfkLocation(ph);
			if (phLoc != null)
			{
				if (phLoc.equals(ph.getPcInstance().getLocation()))
				{
					AbstractEvent currentEvent = EventEngineManager.getInstance().getCurrentEvent();
					
					// Test II
					if (debug)
					{
						System.out.println("[ANTI-AFK] Location Test II - Player: " + ph.getPcInstance().getName() + ", locX: " + ph.getPcInstance().getX() + ", locY: " + ph.getPcInstance().getY() + ", locZ: " + ph.getPcInstance().getZ());
					}
					
					// Remove all effect of player
					ph.getPcInstance().stopAllEffects();
					
					// Recover the title and color of the participants.
					ph.recoverOriginalColorTitle();
					ph.recoverOriginalTitle();
					
					// It out of the world created for the event
					InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(ph.getPcInstance());
					world.removeAllowed(ph.getPcInstance().getObjectId());
					ph.getPcInstance().setInstanceId(0);
					
					// Dead Player
					currentEvent.revivePlayer(ph);
					
					// Teleport Player
					// FIXME We send a character to their actual instance and turn
					ph.getPcInstance().teleToLocation(83437, 148634, -3403, 0, 0);// GIRAN CENTER
					
					// Remove player from event
					currentEvent.getAllEventPlayers().remove(ph);
					ProtectionUtil.removeAfkLoc(ph);
					
					_playerAfkInstance.clear();
					ProtectionUtil._playersAfkLoc.clear();
				}
			}
		}
	}
}
