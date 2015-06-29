/*
 * Copyright (C) 2015-2015 L2J EventEngine
 *
 * This file is part of L2J EventEngine.
 *
 * L2jAdmins is free software: you can redistribute it and/or modify
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
import java.util.Map.Entry;
import java.util.logging.Logger;

import net.sf.eventengine.ai.NpcManager;
import net.sf.eventengine.configs.Configs;
import net.sf.eventengine.enums.EventEngineState;
import net.sf.eventengine.enums.EventType;
import net.sf.eventengine.handler.AbstractEvent;
import net.sf.eventengine.holder.PlayerHolder;
import net.sf.eventengine.task.EventEngineTask;

import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.instancemanager.InstanceManager;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.L2Playable;
import com.l2jserver.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.network.clientpackets.Say2;
import com.l2jserver.gameserver.network.serverpackets.CreatureSay;

/**
 * @author fissban
 */
public class EventEngineManager
{
	private static final Logger LOG = Logger.getLogger(EventEngineManager.class.getName());

	/**
	 * Constructor
	 */
	public EventEngineManager()
	{
		load();
	}

	/**
	 * Metodo encargado de cargar todo lo necesario para el evento
	 */
	public void load()
	{
		try
		{
			// Cargamos los configs de los eventos.
			Configs.load();
			LOG.info("EventEngineManager: Configs cargados con exito");
			// Cargamos los AI
			NpcManager.class.newInstance();
			LOG.info("EventEngineManager: AI's cargados con exito");
			// lanzamos el task principal
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new EventEngineTask(), 10 * 1000, 1000);
			// inicializamos el tiempo para los eventos en minutos
			_time = Configs.EVENT_TASK * 60;
		}
		catch (Exception e)
		{
			LOG.warning(EventEngineManager.class.getSimpleName() + ": -> load() " + e);
			e.printStackTrace();
		}
	}

	// XXX EventEngineTask ------------------------------------------------------------------------------------
	private static int _time;

	public static int getTime()
	{
		return _time;
	}

	public static void setTime(int time)
	{
		_time = time;
	}

	public static void decreaseTime()
	{
		_time--;
	}

	// XXX DINAMIC INSTANCE ------------------------------------------------------------------------------
	private static final List<InstanceWorld> _instancesWorlds = new ArrayList<>();

	/**
	 * Creamos instancias dinamicas y un mundo para ella
	 * @param count
	 * @return InstanceWorld: world creado para la instancia
	 */
	public static InstanceWorld createNewInstanceWorld()
	{
		InstanceWorld world = null;
		try
		{
			int instanceId = InstanceManager.getInstance().createDynamicInstance(Configs.INSTANCE_FILE);
			InstanceManager.getInstance().getInstance(instanceId).setAllowSummon(false);
			InstanceManager.getInstance().getInstance(instanceId).setPvPInstance(true);
			InstanceManager.getInstance().getInstance(instanceId).setEmptyDestroyTime(1000 + 60000L);
			// Cerramos las puertas de la instancia si es q existen
			for (L2DoorInstance door : InstanceManager.getInstance().getInstance(instanceId).getDoors())
			{
				door.closeMe();
			}

			world = new EventEngineWorld();
			world.setInstanceId(instanceId);
			world.setTemplateId(100); // TODO hardcode
			world.setStatus(0);
			InstanceManager.getInstance().addWorld(world);
			_instancesWorlds.add(world);

		}
		catch (Exception e)
		{
			LOG.warning(EventEngineManager.class.getSimpleName() + ": -> createDynamicInstances() " + e);
			e.printStackTrace();
		}

		return world;
	}

	public static List<InstanceWorld> getInstancesWorlds()
	{
		return _instancesWorlds;
	}

	// XXX CURRENT EVENT ---------------------------------------------------------------------------------

	// Evento que esta corriendo.
	private static AbstractEvent _currentEvent;

	/**
	 * Obtenemos el evento q esta corriendo actualmente.
	 * @return
	 */
	public static AbstractEvent getCurrentEvent()
	{
		return _currentEvent;
	}

	/**
	 * Definimos el evento q comenzara a correr.
	 * @param event
	 */
	public static void setCurrentEvent(AbstractEvent event)
	{
		_currentEvent = event;
	}

	// XXX LISTENERS -------------------------------------------------------------------------------------
	/**
	 * @param player -> personaje o summon
	 * @param target -> NO puede ser null
	 * @return true -> solo en el caso de que no queremos q un ataque continue su progeso normal.
	 */
	public static boolean listenerOnAttack(L2Playable player, L2Character target)
	{
		// Si no se esta corriendo no continuar el listener.
		if (_currentEvent == null)
		{
			return false;
		}

		try
		{
			return _currentEvent.listenerOnAttack(player, target);
		}
		catch (Exception e)
		{
			LOG.warning(EventEngineManager.class.getSimpleName() + ": -> listenerOnAttack() " + e);
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * @param player -> personaje o summon
	 * @param target -> puede ser null
	 * @return true -> solo en el caso de que no queremos de una habilidad no continue su progreso normal.
	 */
	public static boolean listenerOnUseSkill(L2Playable player, L2Character target, Skill skill)
	{
		// Si no se esta corriendo no continuar el listener.
		if (_currentEvent == null)
		{
			return false;
		}

		try
		{
			return _currentEvent.listenerOnUseSkill(player, target, skill);
		}
		catch (Exception e)
		{
			LOG.warning(EventEngineManager.class.getSimpleName() + ": -> listenerOnUseSkill() " + e);
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * @param player -> personaje o summon
	 * @param target -> No puede ser null
	 */
	public static void listenerOnKill(L2Playable player, L2Character target)
	{
		// Si no se esta corriendo no continuar el listener.
		if (_currentEvent == null)
		{
			return;
		}

		try
		{
			_currentEvent.listenerOnKill(player, target);
		}
		catch (Exception e)
		{
			LOG.warning(EventEngineManager.class.getSimpleName() + ": -> listenerOnKill() " + e);
			e.printStackTrace();
		}
	}

	/**
	 * @param player
	 * @param target
	 */
	public static void listenerOnInteract(L2PcInstance player, L2Npc target)
	{
		// Si no se esta corriendo no continuar el listener.
		if (_currentEvent == null)
		{
			return;
		}

		try
		{
			_currentEvent.listenerOnInteract(player, target);
		}
		catch (Exception e)
		{
			LOG.warning(EventEngineManager.class.getSimpleName() + ": -> listenerOnInteract() " + e);
			e.printStackTrace();
		}
	}

	/**
	 * @param player
	 */
	public static void listenerOnDeath(L2PcInstance player)
	{
		// Si no se esta corriendo no continuar el listener.
		if (_currentEvent == null)
		{
			return;
		}

		try
		{
			_currentEvent.listenerOnDeath(player);
		}
		catch (Exception e)
		{
			LOG.warning(EventEngineManager.class.getSimpleName() + ": -> listenerOnDeath() " + e);
			e.printStackTrace();
		}
	}

	/**
	 * @param player
	 */
	public static void listenerOnLogout(L2PcInstance player)
	{
		// Si no se esta corriendo no continuar el listener.
		if (_currentEvent == null)
		{
			return;
		}

		if (!_currentEvent.isPlayerInEvent(player))
		{
			return;
		}

		try
		{
			PlayerHolder ph = _currentEvent.getEventPlayer(player);
			// recobramos el color del titulo original
			ph.recoverOriginalColorTitle();
			// recobramos el titulo original
			ph.recoverOriginalTitle();
			// remobemos al personaje del mundo creado
			InstanceManager.getInstance().getWorld(ph.getDinamicInstanceId()).removeAllowed(ph.getPcInstance().getObjectId());

			_currentEvent.getAllEventPlayers().remove(ph);
		}
		catch (Exception e)
		{
			LOG.warning(EventEngineManager.class.getSimpleName() + ": -> listenerOnLogout() " + e);
			e.printStackTrace();
		}
	}

	/**
	 * @param player
	 */
	public static void listenerOnLogin(L2PcInstance player)
	{
		player.sendPacket(new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "", "[EventEngine] Participa de nuestros eventos"));
		player.sendPacket(new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "", "[EventEngine] y vota por el que mas te guste"));
	}

	/**
	 * @param player
	 */
	public static void listenerOnUseItem(L2PcInstance player)
	{
		// Sin desarrollar
	}

	// XXX EVENT VOTE ------------------------------------------------------------------------------------

	// Votos de cada Evento
	private static Map<EventType, Integer> _currentEventVotes = new HashMap<>();
	{
		_currentEventVotes.put(EventType.AVA, 0);
		_currentEventVotes.put(EventType.CTF, 0);
		_currentEventVotes.put(EventType.TVT, 0);
	}

	// Lista para controlar los personajes q ya votaron
	private static List<Integer> _currentPlayersVotes = new ArrayList<>();

	/**
	 * Clase encargada de inicializar los votos de cada evento.
	 * @return Map<EventType, Integer>
	 */
	public static Map<EventType, Integer> clearVotes()
	{
		// inicializamos el mapa con todos los votos a 0
		_currentEventVotes.put(EventType.AVA, 0);
		_currentEventVotes.put(EventType.CTF, 0);
		_currentEventVotes.put(EventType.TVT, 0);

		// borramos la lista con los usuarios q han votado
		_currentPlayersVotes.clear();

		return _currentEventVotes;
	}

	/**
	 * Incrementamos en uno la cantidad de votos
	 * @param player -> personaje q esta votando
	 * @param event -> evento al q se vota
	 * @return boolean
	 */
	public static boolean increaseVote(L2PcInstance player, EventType event)
	{
		if (_currentPlayersVotes.contains(player.getObjectId()))
		{
			// Si el personaje esta en esta lista es porq ya voto.
			return false;
		}

		// Agregamos al personaje a nuestra lista de votantes
		_currentPlayersVotes.add(player.getObjectId());
		// Obtenemos la cantidad de votos de un determinado evento.
		int votes = _currentEventVotes.get(event);
		votes++;
		// Incrementamos en uno la cantdad de votos del mismo
		_currentEventVotes.put(event, votes);

		return true;
	}

	/**
	 * Obtenemos la cantidad de votos q tiene un determinado evento.
	 * @param event -> AVA, TVT, CFT.
	 * @return int
	 */
	public static int getCurrentVotesInEvent(EventType event)
	{
		return _currentEventVotes.get(event);
	}

	/**
	 * Obtenemos todos los eventos y la cantidad de votos q tienen los mismos.
	 * @return
	 */
	public static Map<EventType, Integer> getAllCurrentVotesInEvents()
	{
		return _currentEventVotes;
	}

	/**
	 * Obtenemos el evento con mayor votos<br>
	 * En caso de tener todos la misma cant de votos devolvera EventType.TVT<br>
	 * @return
	 */
	public static EventType getEventMoreVotes()
	{
		EventType eventType = EventType.TVT;
		int currentVotes = 0;

		for (Entry<EventType, Integer> event : _currentEventVotes.entrySet())
		{
			if (currentVotes < event.getValue())
			{
				eventType = event.getKey();
				currentVotes = event.getValue();
			}
		}

		return eventType;
	}

	// XXX EVENT STATE -----------------------------------------------------------------------------------

	// variable encargada de controlar en que momento se podran registrar los usuarios a los eventos.
	private static EventEngineState _state = EventEngineState.REGISTER;

	/**
	 * Revisamos en q estado se encuentra el engine
	 * @return EventState
	 */
	public static EventEngineState getEventEngineState()
	{
		return _state;
	}

	/**
	 * Definimos el estado en q se encuentra el evento<br>
	 * <u>Observaciones:</u><br>
	 * <li>REGISTER -> Indicamos q se esta</li><br>
	 * <li></li><br>
	 * <li></li><br>
	 * <li></li><br>
	 * <li></li><br>
	 * <li></li><br>
	 * <li></li><br>
	 * @param state
	 */
	public static void setEventEngineState(EventEngineState state)
	{
		_state = state;
	}

	/**
	 * Verificamos si se pueden seguir registrando mas usuarios a los eventos.
	 * @return boolean
	 */
	public static boolean isOpenRegister()
	{
		return _state == EventEngineState.REGISTER;
	}

	/**
	 * Verificamos si se pueden seguir registrando mas usuarios a los eventos.
	 * @return boolean
	 */
	public static boolean isOpenVote()
	{
		return _state == EventEngineState.REGISTER;
	}

	// XXX PLAYERS REGISTER -----------------------------------------------------------------------------

	// Lista de players en el evento.
	private static final Map<Integer, L2PcInstance> _eventRegisterPlayers = new HashMap<>();

	/**
	 * Obetenemos la lista completa de todos los players registrados en el evento.<br>
	 * @return Collection<PlayerHolder>
	 */
	public static Collection<L2PcInstance> getAllRegisterPlayers()
	{
		return _eventRegisterPlayers.values();
	}

	/**
	 * Agregamos un player al registro
	 * @param player
	 * @return <li>True - > si el registro es exitoso.</li><br>
	 *         <li>False - > si el player ya estaba registrado.</li><br>
	 */
	public static boolean registerPlayer(L2PcInstance player)
	{
		if (_eventRegisterPlayers.containsKey(player.getObjectId()))
		{
			return false;
		}

		_eventRegisterPlayers.put(player.getObjectId(), player);

		return true;
	}

	/**
	 * Eliminamos un player del registro
	 * @param player
	 * @return <li>True - > si el player estaba registrado.</li><br>
	 *         <li>False - > si el player no estaba registrado.</li><br>
	 */
	public static boolean unRegisterPlayer(L2PcInstance player)
	{
		if (!isOpenRegister())
		{
			return false;
		}

		if (!_eventRegisterPlayers.containsKey(player.getObjectId()))
		{
			return false;
		}

		_eventRegisterPlayers.remove(player.getObjectId());

		return true;
	}

	// XXX MISC ---------------------------------------------------------------------------------------

	/**
	 * Verificamos si un player participa de algun evento
	 * @param player
	 * @return
	 */
	public static boolean isPlayerInEvent(L2PcInstance player)
	{
		if (_currentEvent == null)
		{
			return false;
		}

		return _currentEvent.isPlayerInEvent(player);
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
