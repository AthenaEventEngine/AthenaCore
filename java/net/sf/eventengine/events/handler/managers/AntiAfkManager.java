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
package net.sf.eventengine.events.handler.managers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import com.l2jserver.gameserver.ThreadPoolManager;

import net.sf.eventengine.EventEngineManager;
import net.sf.eventengine.datatables.ConfigData;
import net.sf.eventengine.events.handler.AbstractEvent;
import net.sf.eventengine.events.holders.PlayerHolder;

/**
 * @author fissban
 */
public class AntiAfkManager
{
	private final Map<PlayerHolder, ActionsPlayers> _playersAfkCheck = new ConcurrentHashMap<>();
	
	private ScheduledFuture<?> _taskAntiAfk;
	
	public AntiAfkManager()
	{
		// Initialize variable.
		for (PlayerHolder ph : EventEngineManager.getInstance().getCurrentEvent().getPlayerEventManager().getAllEventPlayers())
		{
			_playersAfkCheck.put(ph, new ActionsPlayers());
		}
	}
	
	public void addActionPlayer(PlayerHolder ph, ActionsPlayerType action)
	{
		_playersAfkCheck.get(ph).addAction(action);
	}
	
	public void removePlayerFromCheckAfk(PlayerHolder ph)
	{
		_playersAfkCheck.remove(ph);
	}
	
	/**
	 * Start thread responsible for controlling the actions of the players from time to time.
	 * @param time -> It defines the time when the thread starts
	 */
	public void startTask(int time)
	{
		_taskAntiAfk = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() ->
		{
			AbstractEvent currentEvent = EventEngineManager.getInstance().getCurrentEvent();
			
			for (PlayerHolder ph : currentEvent.getPlayerEventManager().getAllEventPlayers())
			{
				ActionsPlayers phOldActions = _playersAfkCheck.get(ph);
				if (phOldActions != null)
				{
					if (!phOldActions.getActions())
					{
						currentEvent.cancelAllEffects(ph);
						currentEvent.removePlayerFromEvent(ph, true);
						
						removePlayerFromCheckAfk(ph);
						continue;
					}
				}
			}
		} , time, ConfigData.getInstance().AFK_CHECK_TIME * 1000);
	}
	
	/**
	 * Stop the task responsible for controlling the locations of the players.
	 * @param time -> It defines the time when the thread end
	 */
	public void stopTask(int time)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			_taskAntiAfk.cancel(true);
		} , time);
	}
	
	public enum ActionsPlayerType
	{
		ATTACK,
		KILL,
		SKILL,
		ITEM,
		INTERACT_NPC,
		MOVEMENT,
	}
	
	public class ActionsPlayers
	{
		private boolean _attack;
		private boolean _kill;
		private boolean _useSkill;
		private boolean _useItem;
		private boolean _interactNpc;
		private boolean _movement;
		
		public ActionsPlayers()
		{
			_kill = false;
			_useSkill = false;
			_useItem = false;
			_interactNpc = false;
			_movement = false;
		}
		
		public void addAction(ActionsPlayerType action)
		{
			switch (action)
			{
				case ATTACK:
					_attack = true;
					break;
				case KILL:
					_kill = true;
					break;
				case SKILL:
					_useSkill = true;
					break;
				case ITEM:
					_useItem = true;
					break;
				case INTERACT_NPC:
					_interactNpc = true;
					break;
				case MOVEMENT:
					_movement = true;
					break;
			}
		}
		
		public boolean getActions()
		{
			return _attack || _kill || _useSkill || _useItem || _interactNpc || _movement;
		}
	}
}
