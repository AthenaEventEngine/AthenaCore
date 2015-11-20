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
package net.sf.eventengine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.eventengine.adapter.EventEngineAdapter;
import net.sf.eventengine.ai.NpcManager;
import net.sf.eventengine.datatables.BuffListData;
import net.sf.eventengine.datatables.ConfigData;
import net.sf.eventengine.datatables.EventData;
import net.sf.eventengine.datatables.MessageData;
import net.sf.eventengine.enums.EventEngineState;
import net.sf.eventengine.events.handler.AbstractEvent;
import net.sf.eventengine.events.holders.PlayerHolder;
import net.sf.eventengine.task.EventEngineTask;

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
	private EventEngineManager()
	{
		load();
	}
	
	/**
	 * It loads all the dependencies needed by EventEngine
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
			LOGGER.info(EventEngineManager.class.getSimpleName() + ": Configs loaded");
			EventData.getInstance();
			LOGGER.info(EventEngineManager.class.getSimpleName() + ": Events loaded");
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
			LOGGER.warning(EventEngineManager.class.getSimpleName() + ": -> load() " + e);
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
	 * Get the next event type
	 * @return
	 */
	public Class<? extends AbstractEvent> getNextEvent()
	{
		return _nextEvent;
	}
	
	/**
	 * Set the next event type
	 * @param event
	 */
	public void setNextEvent(Class<? extends AbstractEvent> event)
	{
		_nextEvent = event;
	}
	
	// XXX CURRENT EVENT ---------------------------------------------------------------------------------
	
	// Evento que esta corriendo.
	private AbstractEvent _currentEvent;
	
	/**
	 * Obtenemos el evento q esta corriendo actualmente.
	 * @return
	 */
	public AbstractEvent getCurrentEvent()
	{
		return _currentEvent;
	}
	
	/**
	 * Definimos el evento q comenzara a correr.
	 * @param event
	 */
	public void setCurrentEvent(AbstractEvent event)
	{
		_currentEvent = event;
	}
	
	// XXX LISTENERS -------------------------------------------------------------------------------------
	/**
	 * @param playable -> personaje o summon
	 * @param target -> NO puede ser null
	 * @return true -> solo en el caso de que no queremos q un ataque continue su progeso normal.
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
				LOGGER.warning(EventEngineManager.class.getSimpleName() + ": -> listenerOnAttack() " + e);
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * @param player -> personaje o summon
	 * @param target -> puede ser null
	 * @return true -> solo en el caso de que no queremos de una habilidad no continue su progreso normal.
	 */
	public boolean listenerOnUseSkill(L2Playable playable, L2Character target, Skill skill)
	{
		// Si no se esta corriendo no continuar el listener.
		if (_currentEvent != null)
		{
			try
			{
				return _currentEvent.listenerOnUseSkill(playable, target, skill);
			}
			catch (Exception e)
			{
				LOGGER.warning(EventEngineManager.class.getSimpleName() + ": -> listenerOnUseSkill() " + e);
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * @param playable -> personaje o summon
	 * @param target -> No puede ser null
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
				LOGGER.warning(EventEngineManager.class.getSimpleName() + ": -> listenerOnKill() " + e);
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
				LOGGER.warning(EventEngineManager.class.getSimpleName() + ": -> listenerOnInteract() " + e);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @param player
	 */
	public void listenerOnDeath(L2PcInstance player)
	{
		// Si no se esta corriendo no continuar el listener.
		if (_currentEvent != null)
		{
			try
			{
				_currentEvent.listenerOnDeath(player);
			}
			catch (Exception e)
			{
				LOGGER.warning(EventEngineManager.class.getSimpleName() + ": -> listenerOnDeath() " + e);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Listener when the player logouts
	 * @param player
	 */
	public void listenerOnLogout(L2PcInstance player)
	{
		if (_currentEvent == null)
		{
			if (_state == EventEngineState.REGISTER || _state == EventEngineState.VOTING)
			{
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
				LOGGER.warning(EventEngineManager.class.getSimpleName() + ": -> listenerOnLogout() " + e);
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
	 * @return boolean -> true solo en el caso de que no queremos que no se pueda usar un item
	 */
	public boolean listenerOnUseItem(L2PcInstance player, L2Item item)
	{
		// Si no se esta corriendo no continuar el listener.
		if (_currentEvent != null)
		{
			try
			{
				return _currentEvent.listenerOnUseItem(player, item);
			}
			catch (Exception e)
			{
				LOGGER.warning(EventEngineManager.class.getSimpleName() + ": -> listenerOnUseItem() " + e);
				e.printStackTrace();
			}
		}
		return false;
	}
	
	// XXX EVENT VOTE ------------------------------------------------------------------------------------
	
	// Lista de id's de personajes que votaron
	private final Set<Integer> _playersAlreadyVoted = ConcurrentHashMap.newKeySet();
	// Mapa de con los id's de los personajes que los votaron
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
	 * Clase encargada de inicializar los votos de cada evento.
	 * @return Map<EventType, Integer>
	 */
	public void clearVotes()
	{
		// Se reinicia el mapa
		for (Class<? extends AbstractEvent> event : _currentEventVotes.keySet())
		{
			_currentEventVotes.get(event).clear();
		}
		// Se limpia la lista de jugadores que votaron
		_playersAlreadyVoted.clear();
	}
	
	/**
	 * Incrementamos en uno la cantidad de votos
	 * @param player -> personaje q esta votando
	 * @param event -> evento al q se vota
	 * @return boolean
	 */
	public void increaseVote(L2PcInstance player, Class<? extends AbstractEvent> event)
	{
		// Agrega al personaje a la lista de los que votaron
		// Si ya estaba, sigue de largo
		// Sino, agrega un voto al evento
		if (_playersAlreadyVoted.add(player.getObjectId()))
		{
			_currentEventVotes.get(event).add(player.getObjectId());
		}
	}
	
	/**
	 * Disminuímos la cantidad de votos
	 * @param player -> personaje q esta votando
	 * @return
	 */
	public void removeVote(L2PcInstance player)
	{
		// Lo borra de la lista de jugadores que votaron
		if (_playersAlreadyVoted.remove(player.getObjectId()))
		{
			// Si estaba en la lista, empieza a buscar para qué evento votó
			for (Class<? extends AbstractEvent> event : _currentEventVotes.keySet())
			{
				_currentEventVotes.get(event).remove(player.getObjectId());
			}
		}
	}
	
	/**
	 * Obtenemos la cantidad de votos q tiene un determinado evento.
	 * @param event -> AVA, TVT, CFT.
	 * @return int
	 */
	public int getCurrentVotesInEvent(Class<? extends AbstractEvent> event)
	{
		return _currentEventVotes.get(event).size();
	}
	
	/**
	 * Obtenemos la cantidad de votos totales.
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
	 * Obtenemos el evento con mayor votos<br>
	 * En caso de tener todos la misma cant de votos se hace un random<br>
	 * entre los que más votos tienen<br>
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
	
	// variable encargada de controlar en que momento se podran registrar los usuarios a los eventos.
	private EventEngineState _state = EventEngineState.WAITING;
	
	/**
	 * Revisamos en q estado se encuentra el engine
	 * @return EventState
	 */
	public EventEngineState getEventEngineState()
	{
		return _state;
	}
	
	/**
	 * Definimos el estado en q se encuentra el evento<br>
	 * <u>Observaciones:</u><br>
	 * <li>REGISTER -> Indicamos q se esta</li><br>
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
	 * Verificamos si se pueden seguir registrando mas usuarios a los eventos.
	 * @return boolean
	 */
	public boolean isOpenRegister()
	{
		return _state == EventEngineState.REGISTER;
	}
	
	/**
	 * Verificamos si se pueden seguir registrando mas usuarios a los eventos.
	 * @return boolean
	 */
	public boolean isOpenVote()
	{
		return _state == EventEngineState.VOTING;
	}
	
	// XXX PLAYERS REGISTER -----------------------------------------------------------------------------
	
	// Lista de players en el evento.
	private final Set<L2PcInstance> _eventRegisterdPlayers = ConcurrentHashMap.newKeySet();
	
	/**
	 * Obtenemos la colección de jugadores registrados
	 * @return Collection<L2PcInstance>
	 */
	public Collection<L2PcInstance> getAllRegisteredPlayers()
	{
		return _eventRegisterdPlayers;
	}
	
	/**
	 * Limpia la colección de jugadores
	 * @return
	 */
	public void clearRegisteredPlayers()
	{
		_eventRegisterdPlayers.clear();
	}
	
	/**
	 * Obtenemos si la cantidad de jugadores registrados es 0
	 * @return <li>True - > no hay jugadores registrados.</li><br>
	 *         <li>False - > hay al menos un jugador registrado.</li><br>
	 */
	public boolean isEmptyRegisteredPlayers()
	{
		return _eventRegisterdPlayers.isEmpty();
	}
	
	/**
	 * Obtenemos si el jugador se encuentra registrado
	 * @return <li>True - > Está registrado.</li><br>
	 *         <li>False - > No está registrado.</li><br>
	 */
	public boolean isRegistered(L2PcInstance player)
	{
		return _eventRegisterdPlayers.contains(player);
	}
	
	/**
	 * Agregamos un player al registro
	 * @param player
	 * @return <li>True - > si el registro es exitoso.</li><br>
	 *         <li>False - > si el player ya estaba registrado.</li><br>
	 */
	public boolean registerPlayer(L2PcInstance player)
	{
		return _eventRegisterdPlayers.add(player);
	}
	
	/**
	 * Eliminamos un player del registro
	 * @param player
	 * @return <li>True - > si el player estaba registrado.</li><br>
	 *         <li>False - > si el player no estaba registrado.</li><br>
	 */
	public boolean unRegisterPlayer(L2PcInstance player)
	{
		return _eventRegisterdPlayers.remove(player);
	}
	
	// XXX MISC ---------------------------------------------------------------------------------------
	
	private Map<Integer, Location> _playersDisconnected = new ConcurrentHashMap<>();
	
	/**
	 * When the player is disconnected inside event<br>
	 * It adds him to a list saving the original location<br>
	 * @param ph
	 */
	public void addPlayerDisconnected(PlayerHolder ph)
	{
		_playersDisconnected.put(ph.getPcInstance().getObjectId(), ph.getReturnLoc());
		
	}
	
	/**
	 * When the player relogs<br>
	 * It teleports him to the original location if he disconnected inside event<br>
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
	 * Cleanup variables to the next event
	 */
	public void cleanUp()
	{
		setCurrentEvent(null);
		clearVotes();
		clearRegisteredPlayers();
	}
	
	/**
	 * Verificamos si un player participa de algun evento
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
	 * Verificamos si un playable participa de algun evento
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
