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

import net.sf.eventengine.ai.NpcManager;
import net.sf.eventengine.datatables.ConfigData;
import net.sf.eventengine.datatables.MessageData;
import net.sf.eventengine.enums.EventEngineState;
import net.sf.eventengine.enums.EventType;
import net.sf.eventengine.events.EventLoader;
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
			ConfigData.load();
			LOG.info("EventEngineManager: Configs cargados con exito");
			EventLoader.load();
			// Multi-Language System
			MessageData.load();
			LOG.info("EventEngineManager: Multi-Language System cargado.");
			// Cargamos los AI
			NpcManager.class.newInstance();
			LOG.info("EventEngineManager: AI's cargados con exito");
			// lanzamos el task principal
			TIME = 0;
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new EventEngineTask(), 10 * 1000, 1000);
		}
		catch (Exception e)
		{
			LOG.warning(EventEngineManager.class.getSimpleName() + ": -> load() " + e);
			e.printStackTrace();
		}
	}
	
	// XXX EventEngineTask ------------------------------------------------------------------------------------
	private static int TIME;
	
	public static int getTime()
	{
		return TIME;
	}
	
	public static void setTime(int time)
	{
		TIME = time;
	}
	
	public static void decreaseTime()
	{
		TIME--;
	}
	
	// XXX DINAMIC INSTANCE ------------------------------------------------------------------------------
	private static final List<InstanceWorld> INSTANCE_WORLDS = new ArrayList<>();
	
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
			int instanceId = InstanceManager.getInstance().createDynamicInstance(ConfigData.INSTANCE_FILE);
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
			INSTANCE_WORLDS.add(world);
			
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
		return INSTANCE_WORLDS;
	}
	
	// XXX NEXT EVENT ---------------------------------------------------------------------------------
	
	private static EventType NEXT_EVENT;
	
	/**
	 * Get the next event type
	 * @return
	 */
	public static EventType getNextEvent()
	{
		return NEXT_EVENT;
	}
	
	/**
	 * Set the next event type
	 * @param event
	 */
	public static void setNextEvent(EventType event)
	{
		NEXT_EVENT = event;
	}
	
	// XXX CURRENT EVENT ---------------------------------------------------------------------------------
	
	// Evento que esta corriendo.
	private static AbstractEvent CURRENT_EVENT;
	
	/**
	 * Obtenemos el evento q esta corriendo actualmente.
	 * @return
	 */
	public static AbstractEvent getCurrentEvent()
	{
		return CURRENT_EVENT;
	}
	
	/**
	 * Definimos el evento q comenzara a correr.
	 * @param event
	 */
	public static void setCurrentEvent(AbstractEvent event)
	{
		CURRENT_EVENT = event;
	}
	
	// XXX LISTENERS -------------------------------------------------------------------------------------
	/**
	 * @param playable -> personaje o summon
	 * @param target -> NO puede ser null
	 * @return true -> solo en el caso de que no queremos q un ataque continue su progeso normal.
	 */
	public static boolean listenerOnAttack(L2Playable playable, L2Character target)
	{
		if (CURRENT_EVENT != null)
		{
			try
			{
				return CURRENT_EVENT.listenerOnAttack(playable, target);
			}
			catch (Exception e)
			{
				LOG.warning(EventEngineManager.class.getSimpleName() + ": -> listenerOnAttack() " + e);
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
	public static boolean listenerOnUseSkill(L2Playable playable, L2Character target, Skill skill)
	{
		// Si no se esta corriendo no continuar el listener.
		if (CURRENT_EVENT != null)
		{
			try
			{
				return CURRENT_EVENT.listenerOnUseSkill(playable, target, skill);
			}
			catch (Exception e)
			{
				LOG.warning(EventEngineManager.class.getSimpleName() + ": -> listenerOnUseSkill() " + e);
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * @param playable -> personaje o summon
	 * @param target -> No puede ser null
	 */
	public static void listenerOnKill(L2Playable playable, L2Character target)
	{
		if (CURRENT_EVENT != null)
		{
			try
			{
				CURRENT_EVENT.listenerOnKill(playable, target);
			}
			catch (Exception e)
			{
				LOG.warning(EventEngineManager.class.getSimpleName() + ": -> listenerOnKill() " + e);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @param player
	 * @param target
	 */
	public static void listenerOnInteract(L2PcInstance player, L2Npc target)
	{
		if (CURRENT_EVENT != null)
		{
			try
			{
				CURRENT_EVENT.listenerOnInteract(player, target);
			}
			catch (Exception e)
			{
				LOG.warning(EventEngineManager.class.getSimpleName() + ": -> listenerOnInteract() " + e);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @param player
	 */
	public static void listenerOnDeath(L2PcInstance player)
	{
		// Si no se esta corriendo no continuar el listener.
		if (CURRENT_EVENT != null)
		{
			try
			{
				CURRENT_EVENT.listenerOnDeath(player);
			}
			catch (Exception e)
			{
				LOG.warning(EventEngineManager.class.getSimpleName() + ": -> listenerOnDeath() " + e);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Listener when the player logouts
	 * @param player
	 */
	public static void listenerOnLogout(L2PcInstance player)
	{
		// Si no se esta corriendo no continuar el listener.
		if (CURRENT_EVENT == null)
		{
			if (STATE == EventEngineState.REGISTER || STATE == EventEngineState.VOTING)
			{
				removeVote(player);
				unRegisterPlayer(player);
				return;
			}
		}
		else
		{
			if (CURRENT_EVENT.isPlayableInEvent(player))
			{
				try
				{
					PlayerHolder ph = CURRENT_EVENT.getEventPlayer(player);
					// recobramos el color del titulo original
					ph.recoverOriginalColorTitle();
					// recobramos el titulo original
					ph.recoverOriginalTitle();
					// remobemos al personaje del mundo creado
					InstanceManager.getInstance().getWorld(ph.getDinamicInstanceId()).removeAllowed(ph.getPcInstance().getObjectId());
					
					CURRENT_EVENT.getAllEventPlayers().remove(ph);
				}
				catch (Exception e)
				{
					LOG.warning(EventEngineManager.class.getSimpleName() + ": -> listenerOnLogout() " + e);
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * @param player
	 */
	public static void listenerOnLogin(L2PcInstance player)
	{
		player.sendPacket(new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "", MessageData.getMsgByLang(player, "event_login_participate", true)));
		player.sendPacket(new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "", MessageData.getMsgByLang(player, "event_login_vote", true)));
	}
	
	/**
	 * @param player
	 * @return boolean -> true solo en el caso de que no queremos que no se pueda usar un item
	 */
	public static boolean listenerOnUseItem(L2PcInstance player, L2Item item)
	{
		// Si no se esta corriendo no continuar el listener.
		if (CURRENT_EVENT != null)
		{
			try
			{
				CURRENT_EVENT.listenerOnUseItem(player, item);
			}
			catch (Exception e)
			{
				LOG.warning(EventEngineManager.class.getSimpleName() + ": -> listenerOnUseItem() " + e);
				e.printStackTrace();
			}
		}
		return false;
	}
	
	// XXX EVENT VOTE ------------------------------------------------------------------------------------
	
	// Lista de id's de personajes que votaron
	private static final Set<Integer> PLAYERS_ALREADY_VOTED = ConcurrentHashMap.newKeySet();
	// Mapa de con los id's de los personajes que los votaron
	private static final Map<EventType, Set<Integer>> CURRENT_EVENT_VOTES = new HashMap<>();
	{
		for (EventType type : EventType.values())
		{
			CURRENT_EVENT_VOTES.put(type, ConcurrentHashMap.newKeySet());
		}
	}
	
	/**
	 * Clase encargada de inicializar los votos de cada evento.
	 * @return Map<EventType, Integer>
	 */
	public static void clearVotes()
	{
		// Se reinicia el mapa
		for (EventType event : CURRENT_EVENT_VOTES.keySet())
		{
			CURRENT_EVENT_VOTES.get(event).clear();
		}
		// Se limpia la lista de jugadores que votaron
		PLAYERS_ALREADY_VOTED.clear();
	}
	
	/**
	 * Incrementamos en uno la cantidad de votos
	 * @param player -> personaje q esta votando
	 * @param event -> evento al q se vota
	 * @return boolean
	 */
	public static void increaseVote(L2PcInstance player, EventType event)
	{
		// Agrega al personaje a la lista de los que votaron
		// Si ya estaba, sigue de largo
		// Sino, agrega un voto al evento
		if (PLAYERS_ALREADY_VOTED.add(player.getObjectId()))
		{
			CURRENT_EVENT_VOTES.get(event).add(player.getObjectId());
		}
	}
	
	/**
	 * Disminuímos la cantidad de votos
	 * @param player -> personaje q esta votando
	 * @return
	 */
	public static void removeVote(L2PcInstance player)
	{
		// Lo borra de la lista de jugadores que votaron
		if (PLAYERS_ALREADY_VOTED.remove(player.getObjectId()))
		{
			// Si estaba en la lista, empieza a buscar para qué evento votó
			for (EventType event : CURRENT_EVENT_VOTES.keySet())
			{
				CURRENT_EVENT_VOTES.get(event).remove(player.getObjectId());
			}
		}
	}
	
	/**
	 * Obtenemos la cantidad de votos q tiene un determinado evento.
	 * @param event -> AVA, TVT, CFT.
	 * @return int
	 */
	public static int getCurrentVotesInEvent(EventType event)
	{
		return CURRENT_EVENT_VOTES.get(event).size();
	}
	
	/**
	 * Obtenemos la cantidad de votos totales.
	 * @return
	 */
	public static int getAllCurrentVotesInEvents()
	{
		int count = 0;
		for (Set<Integer> set : CURRENT_EVENT_VOTES.values())
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
	public static EventType getEventMoreVotes()
	{
		int maxVotes = 0;
		List<EventType> topEvents = new ArrayList<>();
		
		for (EventType event : CURRENT_EVENT_VOTES.keySet())
		{
			int eventVotes = CURRENT_EVENT_VOTES.get(event).size();
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
	private static EventEngineState STATE = EventEngineState.WAITING;
	
	/**
	 * Revisamos en q estado se encuentra el engine
	 * @return EventState
	 */
	public static EventEngineState getEventEngineState()
	{
		return STATE;
	}
	
	/**
	 * Definimos el estado en q se encuentra el evento<br>
	 * <u>Observaciones:</u><br>
	 * <li>REGISTER -> Indicamos q se esta</li><br>
	 * @param state
	 */
	public static void setEventEngineState(EventEngineState state)
	{
		STATE = state;
	}
	
	/**
	 * Get if the EventEngine is waiting to start a register or voting time.
	 * @return boolean
	 */
	public static boolean isWaiting()
	{
		return STATE == EventEngineState.WAITING;
	}
	
	/**
	 * Get if the EventEngine is running an event.
	 * @return boolean
	 */
	public static boolean isRunning()
	{
		return (STATE == EventEngineState.RUNNING_EVENT) || (STATE == EventEngineState.RUN_EVENT);
	}
	
	/**
	 * Verificamos si se pueden seguir registrando mas usuarios a los eventos.
	 * @return boolean
	 */
	public static boolean isOpenRegister()
	{
		return STATE == EventEngineState.REGISTER;
	}
	
	/**
	 * Verificamos si se pueden seguir registrando mas usuarios a los eventos.
	 * @return boolean
	 */
	public static boolean isOpenVote()
	{
		return STATE == EventEngineState.VOTING;
	}
	
	// XXX PLAYERS REGISTER -----------------------------------------------------------------------------
	
	// Lista de players en el evento.
	private static final Set<L2PcInstance> EVENT_REGISTERED_PLAYERS = ConcurrentHashMap.newKeySet();
	
	/**
	 * Obtenemos la colección de jugadores registrados
	 * @return Collection<L2PcInstance>
	 */
	public static Collection<L2PcInstance> getAllRegisteredPlayers()
	{
		return EVENT_REGISTERED_PLAYERS;
	}
	
	/**
	 * Limpia la colección de jugadores
	 * @return
	 */
	public static void clearRegisteredPlayers()
	{
		EVENT_REGISTERED_PLAYERS.clear();
	}
	
	/**
	 * Obtenemos si la cantidad de jugadores registrados es 0
	 * @return <li>True - > no hay jugadores registrados.</li><br>
	 *         <li>False - > hay al menos un jugador registrado.</li><br>
	 */
	public static boolean isEmptyRegisteredPlayers()
	{
		return EVENT_REGISTERED_PLAYERS.isEmpty();
	}
	
	/**
	 * Obtenemos si el jugador se encuentra registrado
	 * @return <li>True - > Está registrado.</li><br>
	 *         <li>False - > No está registrado.</li><br>
	 */
	public static boolean isRegistered(L2PcInstance player)
	{
		return EVENT_REGISTERED_PLAYERS.contains(player);
	}
	
	/**
	 * Agregamos un player al registro
	 * @param player
	 * @return <li>True - > si el registro es exitoso.</li><br>
	 *         <li>False - > si el player ya estaba registrado.</li><br>
	 */
	public static boolean registerPlayer(L2PcInstance player)
	{
		return EVENT_REGISTERED_PLAYERS.add(player);
	}
	
	/**
	 * Eliminamos un player del registro
	 * @param player
	 * @return <li>True - > si el player estaba registrado.</li><br>
	 *         <li>False - > si el player no estaba registrado.</li><br>
	 */
	public static boolean unRegisterPlayer(L2PcInstance player)
	{
		return EVENT_REGISTERED_PLAYERS.remove(player);
	}
	
	// XXX MISC ---------------------------------------------------------------------------------------
	/**
	 * Verificamos si un player participa de algun evento
	 * @param player
	 * @return
	 */
	public static boolean isPlayerInEvent(L2PcInstance player)
	{
		if (CURRENT_EVENT == null)
		{
			return false;
		}
		
		return CURRENT_EVENT.isPlayableInEvent(player);
	}
	
	/**
	 * Verificamos si un playable participa de algun evento
	 * @param playable
	 * @return
	 */
	public static boolean isPlayableInEvent(L2Playable playable)
	{
		if (CURRENT_EVENT == null)
		{
			return false;
		}
		
		return CURRENT_EVENT.isPlayableInEvent(playable);
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
