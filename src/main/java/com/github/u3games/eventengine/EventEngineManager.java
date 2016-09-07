/*
 * Copyright (C) 2015-2016 Athena Event Engine.
 *
 * This file is part of Athena Event Engine.
 *
 * Athena Event Engine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Athena Event Engine is distributed in the hope that it will be useful,
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
import com.github.u3games.eventengine.datatables.BuffListData;
import com.github.u3games.eventengine.datatables.ConfigData;
import com.github.u3games.eventengine.datatables.EventData;
import com.github.u3games.eventengine.datatables.MessageData;
import com.github.u3games.eventengine.enums.EventEngineState;
import com.github.u3games.eventengine.events.handler.AbstractEvent;
import com.github.u3games.eventengine.events.holders.PlayerHolder;
import com.github.u3games.eventengine.security.DualBoxProtection;
import com.github.u3games.eventengine.task.EventEngineTask;
import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.L2Playable;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.skills.Skill;
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
			ConfigData.getInstance();
			LOGGER.info(EventEngineManager.class.getSimpleName() + ": Configs loaded.");
			EventData.getInstance();
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
	private Class<? extends AbstractEvent> _nextEvent;
	
	/**
	 * Get the next event type.
	 * @return
	 */
	public Class<? extends AbstractEvent> getNextEvent()
	{
		return _nextEvent;
	}
	
	/**
	 * Set the next event type.
	 * @param event
	 */
	public void setNextEvent(Class<? extends AbstractEvent> event)
	{
		_nextEvent = event;
	}
	
	// XXX CURRENT EVENT ---------------------------------------------------------------------------------
	// Event that is running
	private AbstractEvent _currentEvent;
	
	/**
	 * Get the event currently running.
	 * @return
	 */
	public AbstractEvent getCurrentEvent()
	{
		return _currentEvent;
	}
	
	/**
	 * Define the event that shall begin to run.
	 * @param event
	 */
	public void setCurrentEvent(AbstractEvent event)
	{
		_currentEvent = event;
	}
	
	// XXX LISTENERS -------------------------------------------------------------------------------------
	/**
	 * @param playable Character or Summon.
	 * @param target Can't be null.
	 * @return true Just in case we do not want an attack continue their normal progress.
	 */
	public boolean listenerOnAttack(L2Playable playable, L2Character target)
	{
		if (_currentEvent != null)
		{
			try
			{
				return _currentEvent.listenerOnAttack(playable, target);
			}
			catch (Exception e)
			{
				LOGGER.warning(EventEngineManager.class.getSimpleName() + ": listenerOnAttack() " + e);
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * @param playable Character or Summon.
	 * @param target Can be null.
	 * @param skill
	 * @return true Just in case we do not want a skill not continue its normal progress.
	 */
	public boolean listenerOnUseSkill(L2Playable playable, L2Character target, Skill skill)
	{
		// If it is not running, not continue the listener
		if (_currentEvent != null)
		{
			try
			{
				return _currentEvent.listenerOnUseSkill(playable, target, skill);
			}
			catch (Exception e)
			{
				LOGGER.warning(EventEngineManager.class.getSimpleName() + ": listenerOnUseSkill() " + e);
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * @param playable Character or summon.
	 * @param target Can't be null.
	 */
	public void listenerOnKill(L2Playable playable, L2Character target)
	{
		if (_currentEvent != null)
		{
			try
			{
				_currentEvent.listenerOnKill(playable, target);
			}
			catch (Exception e)
			{
				LOGGER.warning(EventEngineManager.class.getSimpleName() + ": listenerOnKill() " + e);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @param player
	 * @param target
	 */
	public void listenerOnInteract(L2PcInstance player, L2Npc target)
	{
		if (_currentEvent != null)
		{
			try
			{
				_currentEvent.listenerOnInteract(player, target);
			}
			catch (Exception e)
			{
				LOGGER.warning(EventEngineManager.class.getSimpleName() + ": listenerOnInteract() " + e);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @param player
	 */
	public void listenerOnDeath(L2PcInstance player)
	{
		// If it is not running, not continue the listener
		if (_currentEvent != null)
		{
			try
			{
				_currentEvent.listenerOnDeath(player);
			}
			catch (Exception e)
			{
				LOGGER.warning(EventEngineManager.class.getSimpleName() + ": listenerOnDeath() " + e);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Listener when the player logout.
	 * @param player
	 */
	public void listenerOnLogout(L2PcInstance player)
	{
		if (_currentEvent == null)
		{
			if ((_state == EventEngineState.REGISTER) || (_state == EventEngineState.VOTING))
			{
				DualBoxProtection.getInstance().removeConnection(player.getClient());
				removeVote(player);
				unRegisterPlayer(player);
				return;
			}
		}
		else
		{
			try
			{
				_currentEvent.listenerOnLogout(player);
			}
			catch (Exception e)
			{
				LOGGER.warning(EventEngineManager.class.getSimpleName() + ": listenerOnLogout() " + e);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @param player
	 */
	public void listenerOnLogin(L2PcInstance player)
	{
		returnPlayerDisconnected(player);
		player.sendPacket(new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "", MessageData.getInstance().getMsgByLang(player, "event_login_participate", true)));
		player.sendPacket(new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "", MessageData.getInstance().getMsgByLang(player, "event_login_vote", true)));
	}
	
	/**
	 * @param player
	 * @param item
	 * @return boolean True only if we do not want that you can not use an item.
	 */
	public boolean listenerOnUseItem(L2PcInstance player, L2Item item)
	{
		// If it is not running, not continue the listener
		if (_currentEvent != null)
		{
			try
			{
				return _currentEvent.listenerOnUseItem(player, item);
			}
			catch (Exception e)
			{
				LOGGER.warning(EventEngineManager.class.getSimpleName() + ": listenerOnUseItem() " + e);
				e.printStackTrace();
			}
		}
		return false;
	}
	
	// XXX EVENT VOTE ------------------------------------------------------------------------------------
	// Id's list of characters who voted
	private final Set<Integer> _playersAlreadyVoted = ConcurrentHashMap.newKeySet();
	// Map of the Id's of the characters who voted
	private final Map<Class<? extends AbstractEvent>, Set<Integer>> _currentEventVotes = new HashMap<>();
	
	/**
	 * Init votes
	 */
	public void initVotes()
	{
		for (Class<? extends AbstractEvent> type : EventData.getInstance().getEnabledEvents())
		{
			_currentEventVotes.put(type, ConcurrentHashMap.newKeySet());
		}
	}
	
	/**
	 * Method responsible of initializing the votes of each event.
	 */
	public void clearVotes()
	{
		// The map is restarted
		for (Class<? extends AbstractEvent> event : _currentEventVotes.keySet())
		{
			_currentEventVotes.get(event).clear();
		}
		// The list of players who voted cleaned
		_playersAlreadyVoted.clear();
	}
	
	/**
	 * Increase by 1, the number of votes.
	 * @param player The character who is voting.
	 * @param event Event voting.
	 */
	public void increaseVote(L2PcInstance player, Class<? extends AbstractEvent> event)
	{
		// Add character at the list of those who voted
		// If it was, continue
		// If it wasn't, adds a vote to the event
		if (_playersAlreadyVoted.add(player.getObjectId()))
		{
			_currentEventVotes.get(event).add(player.getObjectId());
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
			for (Class<? extends AbstractEvent> event : _currentEventVotes.keySet())
			{
				_currentEventVotes.get(event).remove(player.getObjectId());
			}
		}
	}
	
	/**
	 * Get the number of votes it has a certain event.
	 * @param event AVA, TVT, CFT.
	 * @return int
	 */
	public int getCurrentVotesInEvent(Class<? extends AbstractEvent> event)
	{
		return _currentEventVotes.get(event).size();
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
	public Class<? extends AbstractEvent> getEventMoreVotes()
	{
		int maxVotes = 0;
		List<Class<? extends AbstractEvent>> topEvents = new ArrayList<>();
		for (Class<? extends AbstractEvent> event : _currentEventVotes.keySet())
		{
			int eventVotes = _currentEventVotes.get(event).size();
			if (eventVotes > maxVotes)
			{
				topEvents.clear();
				topEvents.add(event);
				maxVotes = eventVotes;
			}
			else if (eventVotes == maxVotes)
			{
				topEvents.add(event);
			}
		}
		
		int topEventsSize = topEvents.size();
		if (topEventsSize > 1)
		{
			return topEvents.get(Rnd.get(0, topEventsSize - 1));
		}
		return topEvents.get(0);
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
	public void addPlayerDisconnected(PlayerHolder ph)
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