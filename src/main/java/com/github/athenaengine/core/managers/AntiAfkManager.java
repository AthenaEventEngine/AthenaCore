/*
 * Copyright (C) 2015-2016 L2J EventEngine
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
package com.github.athenaengine.core.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import com.github.athenaengine.core.config.BaseConfigLoader;
import com.github.athenaengine.core.enums.MessageType;
import com.github.athenaengine.core.model.base.BaseEvent;
import com.github.athenaengine.core.EventEngineManager;
import com.github.athenaengine.core.enums.CollectionTarget;
import com.github.athenaengine.core.model.entity.Player;
import com.github.athenaengine.core.util.EventUtil;
import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.model.Location;

/**
 * System that runs for every x time if the players are afk.<br>
 * It does two actions:<br>
 * a) In each iteration, checks if the players in the _playersAfkCheck have the same location. If so, the inactive players are kicked from event. Then, it adds all the active players to _playersAfkCheck for the next iteration, saving their current location<br>
 * b) If the player do any action, will be removed from _playersAfkCheck
 * @author fissban, Zephyr
 */
public class AntiAfkManager
{
	private final Map<Player, Location> _playersAfkCheck = new ConcurrentHashMap<>();
	private ScheduledFuture<?> _taskAntiAfk;
	
	/**
	 * Add a character to the list of "excluded" from the next control system.
	 * @param ph
	 */
	public void excludePlayer(Player ph)
	{
		_playersAfkCheck.remove(ph);
	}
	
	/**
	 * Start thread responsible for controlling the actions of the players from time to time.
	 */
	public void startTask()
	{
		int checkTime = BaseConfigLoader.getInstance().getMainConfig().getAntiAfkCheckTime();

		_taskAntiAfk = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() ->
		{
			BaseEvent currentEvent = EventEngineManager.getInstance().getCurrentEvent();
			Map<Player, Location> newMap = new HashMap<>();
			
			for (Player ph : currentEvent.getPlayerEventManager().getAllEventPlayers())
			{
				Location currentLoc = ph.getPcInstance().getLocation();
				if (_playersAfkCheck.containsKey(ph))
				{
					Location previousLoc = _playersAfkCheck.get(ph);
					if (previousLoc.equals(currentLoc))
					{
						ph.cancelAllActions();
						ph.cancelAllEffects();
						currentEvent.removePlayerFromEvent(ph, true);
						EventUtil.sendMessageToPlayer(ph, "antiafk_player_kicked");
						EventUtil.announceTo(MessageType.SHOUT, "antiafk_player_kicked_announce", "%player%", ph.getPcInstance().getName(), CollectionTarget.ALL_PLAYERS_IN_EVENT);
						continue;
					}
				}
				// It's not correct use the currentLoc object
				newMap.put(ph, new Location(currentLoc.getX(), currentLoc.getY(), currentLoc.getZ(), currentLoc.getHeading(), currentLoc.getInstanceId()));
			}
			// Clear list
			_playersAfkCheck.clear();
			_playersAfkCheck.putAll(newMap);
			newMap.clear();
		}, checkTime * 1000, checkTime * 1000);
	}
	
	/**
	 * Cancel the thread that checks player actions and clear the map.
	 */
	public void finish()
	{
		_taskAntiAfk.cancel(true);
		_playersAfkCheck.clear();
	}
}