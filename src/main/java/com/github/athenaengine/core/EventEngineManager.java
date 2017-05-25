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
package com.github.athenaengine.core;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.github.athenaengine.core.adapter.EventEngineAdapter;
import com.github.athenaengine.core.ai.NpcManager;
import com.github.athenaengine.core.config.BaseConfigLoader;
import com.github.athenaengine.core.datatables.BuffListData;
import com.github.athenaengine.core.datatables.EventLoader;
import com.github.athenaengine.core.datatables.MessageData;
import com.github.athenaengine.core.dispatcher.events.OnLogInEvent;
import com.github.athenaengine.core.dispatcher.events.OnLogOutEvent;
import com.github.athenaengine.core.enums.EventEngineState;
import com.github.athenaengine.core.handlers.AdminPanelHandler;
import com.github.athenaengine.core.interfaces.IEventContainer;
import com.github.athenaengine.core.managers.general.AutoSchedulerManager;
import com.github.athenaengine.core.managers.general.VoteManager;
import com.github.athenaengine.core.model.holder.LocationHolder;
import com.github.athenaengine.core.model.base.BaseEvent;
import com.github.athenaengine.core.model.entity.Player;
import com.github.athenaengine.core.security.DualBoxProtection;
import com.l2jserver.gameserver.handler.AdminCommandHandler;
import com.l2jserver.gameserver.network.clientpackets.Say2;
import com.l2jserver.gameserver.network.serverpackets.CreatureSay;

/**
 * @author fissban
 */
public class EventEngineManager
{
	private static final Logger LOGGER = Logger.getLogger(EventEngineManager.class.getName());
	
	/**
	 * Constructor
	 */
	public EventEngineManager()
	{
		load();
	}
	
	/**
	 * It loads all the dependencies needed by EventEngine.
	 */
	private void load()
	{
		try
		{
			// Load the adapter to L2J Core
			EventEngineAdapter.class.newInstance();
			LOGGER.info(EventEngineManager.class.getSimpleName() + ": Adapter loaded.");
			// Admin handler
			AdminCommandHandler.getInstance().registerHandler(new AdminPanelHandler());
			LOGGER.info(EventEngineManager.class.getSimpleName() + ": Admin handler loaded.");
			// Load event configs
			BaseConfigLoader.getInstance();
			LOGGER.info(EventEngineManager.class.getSimpleName() + ": New Configs loaded.");
			EventLoader.getInstance();
			LOGGER.info(EventEngineManager.class.getSimpleName() + ": Events loaded.");
			VoteManager.getInstance().initVotes();
			// Load buff list
			BuffListData.getInstance();
			LOGGER.info(EventEngineManager.class.getSimpleName() + ": Buffs loaded.");
			// Load Multi-Language System
			MessageData.getInstance();
			LOGGER.info(EventEngineManager.class.getSimpleName() + ": Multi-Language system loaded.");
			// Load Npc Manager
			NpcManager.class.newInstance();
			LOGGER.info(EventEngineManager.class.getSimpleName() + ": AI's loaded.");
			// Launch main timer
			_time = 0;
			AutoSchedulerManager.getInstance().start();
			LOGGER.info(EventEngineManager.class.getSimpleName() + ": Scheduler loaded.");
		}
		catch (Exception e)
		{
			LOGGER.warning(EventEngineManager.class.getSimpleName() + ": load() " + e);
			e.printStackTrace();
		}
	}
	
	// XXX EventEngineTask ------------------------------------------------------------------------------------
	private int _time;
	
	public int getTime()
	{
		return _time;
	}
	
	public void setTime(int time)
	{
		_time = time;
	}
	
	public void decreaseTime()
	{
		_time--;
	}
	
	// XXX NEXT EVENT ---------------------------------------------------------------------------------
	private IEventContainer _nextEvent;
	
	/**
	 * Get the next event type.
	 * @return
	 */
	public IEventContainer getNextEvent()
	{
		return _nextEvent;
	}
	
	/**
	 * Set the next event type.
	 * @param container
	 */
	public void setNextEvent(IEventContainer container)
	{
		_nextEvent = container;
	}
	
	// XXX CURRENT EVENT ---------------------------------------------------------------------------------
	// Event that is running
	private IEventContainer _currentEventContainer;
	private BaseEvent _currentEvent;

	/**
	 * Get the container of currently event.
	 * @return
	 */
	public IEventContainer getCurrentEventContainer()
	{
		return _currentEventContainer;
	}
	
	/**
	 * Get the event currently running.
	 * @return
	 */
	public BaseEvent getCurrentEvent()
	{
		return _currentEvent;
	}
	
	/**
	 * Define the event that shall begin to run.
	 * @param container
	 * @param event
	 */
	public void setCurrentEvent(IEventContainer container, BaseEvent event)
	{
		_currentEventContainer = container;
		_currentEvent = event;
	}
	
	// XXX LISTENERS -------------------------------------------------------------------------------------
	/**
	 * Listener when the player logout.
	 * @param event
	 */
	public void listenerOnLogout(OnLogOutEvent event)
	{
		if (_currentEvent == null)
		{
			if ((_state == EventEngineState.REGISTER) || (_state == EventEngineState.VOTING))
			{
				Player player = event.getPlayer();
				DualBoxProtection.getInstance().removeConnection(player);
				VoteManager.getInstance().removeVote(player);
				unRegisterPlayer(player);
			}
		}
	}
	
	/**
	 * @param event
	 */
	public void listenerOnLogin(OnLogInEvent event)
	{
		Player player = event.getPlayer();
		returnPlayerDisconnected(player);
		player.getPcInstance().sendPacket(new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "", MessageData.getInstance().getMsgByLang(player, "event_login_participate", true)));
		player.getPcInstance().sendPacket(new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "", MessageData.getInstance().getMsgByLang(player, "event_login_vote", true)));
	}
	
	// XXX EVENT STATE -----------------------------------------------------------------------------------
	// Variable charge of controlling at what moment will be able to register users to events
	private EventEngineState _state = EventEngineState.WAITING;
	
	/**
	 * Check what is the state that have the engine.
	 * @return EventState
	 */
	public EventEngineState getState()
	{
		return _state;
	}
	
	/**
	 * Define the state in which the event is.<br>
	 * <u>Observations:</u>
	 * <li>REGISTER Indicate that it is.</li><br>
	 * @param state
	 */
	public void setEventEngineState(EventEngineState state)
	{
		_state = state;
	}
	
	/**
	 * Get if the EventEngine is waiting to start a register or voting time.
	 * @return boolean
	 */
	public boolean isWaiting()
	{
		return _state == EventEngineState.WAITING;
	}
	
	/**
	 * Get if the EventEngine is running an event.
	 * @return boolean
	 */
	public boolean isRunning()
	{
		return (_state == EventEngineState.RUNNING_EVENT) || (_state == EventEngineState.RUN_EVENT);
	}
	
	/**
	 * Check whether you can continue registering more users to events.
	 * @return boolean
	 */
	public boolean isOpenRegister()
	{
		return _state == EventEngineState.REGISTER;
	}
	
	/**
	 * Check whether you can continue registering more users to events.
	 * @return boolean
	 */
	public boolean isOpenVote()
	{
		return _state == EventEngineState.VOTING;
	}
	
	// XXX PLAYERS REGISTER -----------------------------------------------------------------------------
	// List of players at the event
	private final Set<Player> _eventRegisterdPlayers = ConcurrentHashMap.newKeySet();
	
	/**
	 * Get the collection of registered players.
	 * @return Collection<L2PcInstance>
	 */
	public Collection<Player> getAllRegisteredPlayers()
	{
		return _eventRegisterdPlayers;
	}
	
	/**
	 * Clean collection of players.
	 */
	public void clearRegisteredPlayers()
	{
		_eventRegisterdPlayers.clear();
	}
	
	/**
	 * Get if the number of registered players is 0.
	 * @return
	 *         <li>True No registered players.</li>
	 *         <li>False There is at least one registered player.</li>
	 */
	public boolean isEmptyRegisteredPlayers()
	{
		return _eventRegisterdPlayers.isEmpty();
	}
	
	/**
	 * We get if the player is registered.
	 * @param player
	 * @return
	 *         <li>True It is registered.</li>
	 *         <li>False It's not registered.</li>
	 */
	public boolean isRegistered(Player player)
	{
		return _eventRegisterdPlayers.contains(player);
	}
	
	/**
	 * Add a player to register.
	 * @param player
	 * @return
	 *         <li>True If the registration is successful.</li>
	 *         <li>False If the player already registered.</li>
	 */
	public boolean registerPlayer(Player player)
	{
		return _eventRegisterdPlayers.add(player);
	}
	
	/**
	 * Remove one player from register.
	 * @param player
	 * @return
	 *         <li>True If the player was registered.</li>
	 *         <li>False If the player was not registered.</li>
	 */
	public boolean unRegisterPlayer(Player player)
	{
		return _eventRegisterdPlayers.remove(player);
	}
	
	// XXX MISC ---------------------------------------------------------------------------------------
	
	private final Map<Integer, LocationHolder> _playersDisconnected = new ConcurrentHashMap<>();
	
	/**
	 * When the player is disconnected inside event. It adds him to a list saving the original location.
	 * @param ph
	 */
	public void addPlayerDisconnected(Player ph)
	{
		_playersDisconnected.put(ph.getPcInstance().getObjectId(), ph.getReturnLocation());
		
	}
	
	/**
	 * When the player relogs. It teleports him to the original location if he disconnected inside event.
	 * @param player
	 */
	public void returnPlayerDisconnected(Player player)
	{
		LocationHolder returnLoc = _playersDisconnected.get(player.getObjectId());
		if (returnLoc != null)
		{
			player.teleportTo(returnLoc);
		}
	}
	
	/**
	 * Cleanup variables to the next event.
	 */
	public void cleanUp()
	{
		DualBoxProtection.getInstance().clearAllConnections();
		setCurrentEvent(null,null);
		VoteManager.getInstance().clearVotes();
		clearRegisteredPlayers();
	}
	
	public static EventEngineManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final EventEngineManager _instance = new EventEngineManager();
	}
}