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
package com.github.u3games.eventengine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.github.u3games.eventengine.adapter.EventEngineAdapter;
import com.github.u3games.eventengine.ai.NpcManager;
import com.github.u3games.eventengine.config.BaseConfigLoader;
import com.github.u3games.eventengine.datatables.BuffListData;
import com.github.u3games.eventengine.datatables.EventLoader;
import com.github.u3games.eventengine.datatables.MessageData;
import com.github.u3games.eventengine.dispatcher.events.OnLogInEvent;
import com.github.u3games.eventengine.dispatcher.events.OnLogOutEvent;
import com.github.u3games.eventengine.enums.EventEngineState;
import com.github.u3games.eventengine.model.base.BaseEvent;
import com.github.u3games.eventengine.model.entities.Player;
import com.github.u3games.eventengine.interfaces.EventContainer;
import com.github.u3games.eventengine.security.DualBoxProtection;
import com.github.u3games.eventengine.task.EventEngineTask;
import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Playable;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.clientpackets.Say2;
import com.l2jserver.gameserver.network.serverpackets.CreatureSay;
import com.l2jserver.util.Rnd;

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
			// Load event configs
			BaseConfigLoader.getInstance();
			LOGGER.info(EventEngineManager.class.getSimpleName() + ": New Configs loaded.");
			EventLoader.getInstance();
			LOGGER.info(EventEngineManager.class.getSimpleName() + ": Events loaded.");
			initVotes();
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
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new EventEngineTask(), 10 * 1000, 1000);
			LOGGER.info(EventEngineManager.class.getSimpleName() + ": Timer loaded.");
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
	private EventContainer _nextEvent;
	
	/**
	 * Get the next event type.
	 * @return
	 */
	public EventContainer getNextEvent()
	{
		return _nextEvent;
	}
	
	/**
	 * Set the next event type.
	 * @param container
	 */
	public void setNextEvent(EventContainer container)
	{
		_nextEvent = container;
	}
	
	// XXX CURRENT EVENT ---------------------------------------------------------------------------------
	// Event that is running
	private BaseEvent _currentEvent;
	
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
	 * @param event
	 */
	public void setCurrentEvent(BaseEvent event)
	{
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
				L2PcInstance player = event.getPlayer();
				DualBoxProtection.getInstance().removeConnection(player.getClient());
				removeVote(player);
				unRegisterPlayer(player);
			}
		}
	}
	
	/**
	 * @param event
	 */
	public void listenerOnLogin(OnLogInEvent event)
	{
		L2PcInstance player = event.getPlayer();
		returnPlayerDisconnected(player);
		player.sendPacket(new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "", MessageData.getInstance().getMsgByLang(player, "event_login_participate", true)));
		player.sendPacket(new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "", MessageData.getInstance().getMsgByLang(player, "event_login_vote", true)));
	}
	
	// XXX EVENT VOTE ------------------------------------------------------------------------------------
	// Id's list of characters who voted
	private final Set<Integer> _playersAlreadyVoted = ConcurrentHashMap.newKeySet();
	// Map of the Id's of the characters who voted
	private final Map<String, Set<Integer>> _currentEventVotes = new HashMap<>();
	
	/**
	 * Init votes
	 */
	public void initVotes()
	{
		for (EventContainer container : EventLoader.getInstance().getEnabledEvents())
		{
			_currentEventVotes.put(container.getSimpleEventName(), ConcurrentHashMap.newKeySet());
		}
	}
	
	/**
	 * Method responsible of initializing the votes of each event.
	 */
	public void clearVotes()
	{
		// The map is restarted
		for (String eventName : _currentEventVotes.keySet())
		{
			_currentEventVotes.get(eventName).clear();
		}
		// The list of players who voted cleaned
		_playersAlreadyVoted.clear();
	}
	
	/**
	 * Increase by 1, the number of votes.
	 * @param player The character who is voting.
	 * @param eventName Event voting.
	 */
	public void increaseVote(L2PcInstance player, String eventName)
	{
		// Add character at the list of those who voted
		// If it was, continue
		// If it wasn't, adds a vote to the event
		if (_playersAlreadyVoted.add(player.getObjectId()))
		{
			_currentEventVotes.get(eventName).add(player.getObjectId());
		}
	}
	
	/**
	 * Decrease the number of votes.
	 * @param player Character that are voting.
	 */
	public void removeVote(L2PcInstance player)
	{
		// Deletes it from the list of players who voted
		if (_playersAlreadyVoted.remove(player.getObjectId()))
		{
			// If he was on the list, start looking for which event voted
			for (String eventName : _currentEventVotes.keySet())
			{
				_currentEventVotes.get(eventName).remove(player.getObjectId());
			}
		}
	}
	
	/**
	 * Get the number of votes it has a certain event.
	 * @param eventName AVA, TVT, CFT.
	 * @return int
	 */
	public int getCurrentVotesInEvent(String eventName)
	{
		return _currentEventVotes.get(eventName).size();
	}
	
	/**
	 * Get the amount of total votes.
	 * @return
	 */
	public int getAllCurrentVotesInEvents()
	{
		int count = 0;
		for (Set<Integer> set : _currentEventVotes.values())
		{
			count += set.size();
		}
		return count;
	}
	
	/**
	 * Get the event with more votes. In case all have the same amount of votes, it will make a random among those most votes have.
	 * @return
	 */
	public EventContainer getEventMoreVotes()
	{
		int maxVotes = 0;
		List<String> topEvents = new ArrayList<>();
		for (String eventName : _currentEventVotes.keySet())
		{
			int eventVotes = _currentEventVotes.get(eventName).size();
			if (eventVotes > maxVotes)
			{
				topEvents.clear();
				topEvents.add(eventName);
				maxVotes = eventVotes;
			}
			else if (eventVotes == maxVotes)
			{
				topEvents.add(eventName);
			}
		}
		
		int topEventsSize = topEvents.size();
		String topEventName;
		topEventName = topEventsSize > 1 ? topEvents.get(Rnd.get(0, topEventsSize - 1)) : topEvents.get(0);

		return EventLoader.getInstance().getEvent(topEventName);
	}
	
	// XXX EVENT STATE -----------------------------------------------------------------------------------
	// Variable charge of controlling at what moment will be able to register users to events
	private EventEngineState _state = EventEngineState.WAITING;
	
	/**
	 * Check what is the state that have the engine.
	 * @return EventState
	 */
	public EventEngineState getEventEngineState()
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
	private final Set<L2PcInstance> _eventRegisterdPlayers = ConcurrentHashMap.newKeySet();
	
	/**
	 * Get the collection of registered players.
	 * @return Collection<L2PcInstance>
	 */
	public Collection<L2PcInstance> getAllRegisteredPlayers()
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
	public boolean isRegistered(L2PcInstance player)
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
	public boolean registerPlayer(L2PcInstance player)
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
	public boolean unRegisterPlayer(L2PcInstance player)
	{
		return _eventRegisterdPlayers.remove(player);
	}
	
	// XXX MISC ---------------------------------------------------------------------------------------
	
	private final Map<Integer, Location> _playersDisconnected = new ConcurrentHashMap<>();
	
	/**
	 * When the player is disconnected inside event. It adds him to a list saving the original location.
	 * @param ph
	 */
	public void addPlayerDisconnected(Player ph)
	{
		_playersDisconnected.put(ph.getPcInstance().getObjectId(), ph.getReturnLoc());
		
	}
	
	/**
	 * When the player relogs. It teleports him to the original location if he disconnected inside event.
	 * @param player
	 */
	public void returnPlayerDisconnected(L2PcInstance player)
	{
		Location returnLoc = _playersDisconnected.get(player.getObjectId());
		if (returnLoc != null)
		{
			player.teleToLocation(returnLoc);
		}
	}
	
	/**
	 * Cleanup variables to the next event.
	 */
	public void cleanUp()
	{
		DualBoxProtection.getInstance().clearAllConnections();
		setCurrentEvent(null);
		clearVotes();
		clearRegisteredPlayers();
	}
	
	/**
	 * Check if a player participates in some event.
	 * @param player
	 * @return
	 */
	public boolean isPlayerInEvent(L2PcInstance player)
	{
		if (_currentEvent == null)
		{
			return false;
		}
		return _currentEvent.getPlayerEventManager().isPlayableInEvent(player);
	}
	
	/**
	 * Check if a playable participates in some event.
	 * @param playable
	 * @return
	 */
	public boolean isPlayableInEvent(L2Playable playable)
	{
		if (_currentEvent == null)
		{
			return false;
		}
		return _currentEvent.getPlayerEventManager().isPlayableInEvent(playable);
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