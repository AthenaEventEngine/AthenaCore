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
package net.sf.eventengine.handler;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.eventengine.EventEngineManager;
import net.sf.eventengine.configs.Configs;
import net.sf.eventengine.enums.EventState;
import net.sf.eventengine.enums.EventType;
import net.sf.eventengine.enums.PlayerClassType;
import net.sf.eventengine.holder.PlayerHolder;
import net.sf.eventengine.task.EventTask;
import net.sf.eventengine.util.EventUtil;

import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.data.xml.impl.NpcData;
import com.l2jserver.gameserver.datatables.SpawnTable;
import com.l2jserver.gameserver.enums.Team;
import com.l2jserver.gameserver.model.L2Spawn;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.L2Playable;
import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.instance.L2CubicInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.gameserver.model.holders.SkillHolder;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jserver.gameserver.taskmanager.DecayTaskManager;
import com.l2jserver.util.Rnd;

/**
 * @author fissban
 */
public abstract class AbstractEvent
{
	private static final Logger LOG = Logger.getLogger(AbstractEvent.class.getName());
	
	/**
	 * Constructor.
	 */
	public AbstractEvent()
	{
		// Agregamos todos los player registrados al evento.
		createEventPlayers();
		// Arrancamos el reloj para controlar la secuencia de eventos internos del evento.
		controlTimeEvent();
	}
	
	/** Metodo necesario para llevar el control de los estados del evento */
	public abstract void runEventState(EventState state);
	
	public abstract EventType getEventType();
	
	// NPC IN EVENT --------------------------------------------------------------------------------- //
	
	// Lista de npc en el evento.
	private final Map<Integer, L2Npc> _eventNpc = new HashMap<>();
	
	/**
	 * Obetenemos la lista completa de todos los npc dentro del evento.<br>
	 * @return Collection<PlayerHolder>
	 */
	public Collection<L2Npc> getAllEventNpc()
	{
		return _eventNpc.values();
	}
	
	/**
	 * Generamos un nuevo spawn dentro de nuestro evento y lo agregamos a la lista.
	 * @param npcId
	 * @param x
	 * @param y
	 * @param z
	 * @param heading
	 * @param randomOffset -> si queremos generar un spawn aleatorio en un radio de 100 de la posicion indicada
	 */
	public void addEventNpc(int npcId, int x, int y, int z, int heading, boolean randomOffset, int instanceId)
	{
		// Generamos el spawn de nuestro npc -> sacado de la clase Quest
		L2Npc npc = null;
		try
		{
			L2NpcTemplate template = NpcData.getInstance().getTemplate(npcId);
			if (template != null)
			{
				if (randomOffset)
				{
					x += Rnd.get(-1000, 1000);
					y += Rnd.get(-1000, 1000);
				}
				
				L2Spawn spawn = new L2Spawn(template);
				spawn.setHeading(heading);
				spawn.setX(x);
				spawn.setY(y);
				spawn.setZ(z + 20);
				spawn.setAmount(1);
				spawn.setInstanceId(instanceId);
				npc = spawn.doSpawn();// isSummonSpawn.
				
				SpawnTable.getInstance().addNewSpawn(spawn, false);
				spawn.init();
				// Animacion.
				spawn.getLastSpawn().broadcastPacket(new MagicSkillUse(spawn.getLastSpawn(), spawn.getLastSpawn(), 1034, 1, 1, 1));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
		// Agregamos nuestro npc a la lista.
		_eventNpc.put(npc.getId(), npc);
	}
	
	/**
	 * Borramos todos los npc generados dentro de nuestro evento.
	 */
	public void removeAllEventNpc()
	{
		for (L2Npc npc : _eventNpc.values())
		{
			if (npc == null)
			{
				continue;
			}
			
			// Paramos el respawn del npc.
			npc.getSpawn().stopRespawn();
			// Borramos al npc.
			npc.deleteMe();
		}
		
		_eventNpc.clear();
	}
	
	/**
	 * Verificamos si un npc pertenece a nuestro evento.
	 * @param npcId
	 * @return
	 */
	public boolean isNpcInEvent(L2Npc npc)
	{
		return _eventNpc.containsValue(npc);
	}
	
	// BUFFS TEAMS ---------------------------------------------------------------------------------- //
	private final Map<PlayerClassType, List<SkillHolder>> _playerBuffs = new HashMap<>();

	/**
	 * Definimos el listado de buffs de los personajes dependiendo si son magos o warriors.
	 * @param type
	 * @param list
	 */
	public void setPlayerBuffs(PlayerClassType type, List<SkillHolder> list)
	{
		_playerBuffs.put(type, list);
	}

	/**
	 * Obtenemos un listado con los buffs de un personaje dependiendo si es mago o warrior.
	 * @param type
	 * @return List<SkillHolder>
	 */
	public List<SkillHolder> getPlayerBuffs(PlayerClassType type)
	{
		return _playerBuffs.get(type);
	}
	
	// SAWNS TEAMS ---------------------------------------------------------------------------------- //
	private final Map<Team, Location> _teamSapwn = new HashMap<>();
	
	/**
	 * Definimos los spawns de un team.
	 * @param team
	 * @param loc
	 */
	public void setTeamSpawn(Team team, Location loc)
	{
		_teamSapwn.put(team, loc);
	}
	
	/**
	 * Obtenemos el spawn de un team en particular.
	 * @param team
	 * @return Location
	 */
	public Location getTeamSpawn(Team team)
	{
		return _teamSapwn.get(team);
	}
	
	// PLAYERS IN EVENT ----------------------------------------------------------------------------- //
	private final Map<Integer, PlayerHolder> _eventPlayers = new HashMap<>();
	
	/**
	 * Obetenemos la lista completa de todos los players dentro de un evento.<br>
	 * @return Collection<PlayerHolder>
	 */
	public Collection<PlayerHolder> getAllEventPlayers()
	{
		return _eventPlayers.values();
	}
	
	/**
	 * Agregamos todos los personajes registrado a nuestra lista de personajes dentro del evento
	 */
	private void createEventPlayers()
	{
		for (L2PcInstance player : EventEngineManager.getAllRegisterPlayers())
		{
			_eventPlayers.put(player.getObjectId(), new PlayerHolder(player));
		}
		
		// Limpiamos la lista, ya no la necesitaremos.
		EventEngineManager.getAllRegisterPlayers().clear();
	}
	
	/**
	 * Verificamos si un player esta participando de algun evento. En el caso de tratar de un summon verificamos al dueño.<br>
	 * En el caso de no perticipar de un evento se retorna <u>false</u>
	 * @param player
	 * @return
	 */
	public boolean isPlayerInEvent(L2Character character)
	{
		if (character.isSummon())
		{
			return _eventPlayers.containsKey(((L2Summon) character).getOwner().getObjectId());
		}
		
		if (character.isPlayer())
		{
			return _eventPlayers.containsKey(character.getObjectId());
		}
		
		return false;
	}
	
	/**
	 * Verificamos si un player esta participando de algun evento.<br>
	 * En el caso de tratar de un summon verificamos al dueño.<br>
	 * En el caso de no perticipar de un evento se retorna <u>null</u>
	 * @param character
	 * @return PlayerHolder
	 */
	public PlayerHolder getEventPlayer(L2Character character)
	{
		if (character.isSummon())
		{
			return _eventPlayers.get(((L2Summon) character).getOwner().getObjectId());
		}
		
		if (character.isPlayer())
		{
			return _eventPlayers.get(character.getObjectId());
		}
		
		return null;
	}
	
	// LISTENERS ------------------------------------------------------------------------------------ //
	// Obs -> solo definiremos aqui algunas pequeñas acciones generales.
	
	/**
	 * @param player
	 * @param target
	 */
	public void listenerOnInteract(L2PcInstance player, L2Npc target)
	{
		if (!isPlayerInEvent(player) && !isNpcInEvent(target))
		{
			return;
		}
		
		onInteract(getEventPlayer(player), target);
	}
	
	/**
	 * @param player
	 * @param npc
	 */
	public abstract void onInteract(PlayerHolder player, L2Npc npc);
	
	/**
	 * @param player -> personaje o summon
	 * @param target -> No puede ser null
	 */
	public void listenerOnKill(L2Playable player, L2Character target)
	{
		if (!isPlayerInEvent(player))
		{
			return;
		}
		
		onKill(getEventPlayer(player), target);
	}
	
	/**
	 * @param player
	 * @param target
	 */
	public abstract void onKill(PlayerHolder player, L2Character target);
	
	/**
	 * @param player
	 */
	public void listenerOnDeath(L2PcInstance player)
	{
		if (!isPlayerInEvent(player))
		{
			return;
		}
		
		onDeath(getEventPlayer(player));
	}
	
	/**
	 * @param player
	 */
	public abstract void onDeath(PlayerHolder player);
	
	public boolean listenerOnAttack(L2Playable player, L2Character target)
	{
		// Si player no participa del evento terminar el listener.
		if (!isPlayerInEvent(player))
		{
			return false;
		}
		
		// Obtenemos el player en cuestion dentro de nuestro evento
		PlayerHolder activePlayer = getEventPlayer(player);
		
		// CHECK FRIENDLY_FIRE ----------------------------------------
		if (Configs.FRIENDLY_FIRE)
		{
			// Si nuestro target es de tipo L2Playable y esta dentro del evento hacemos el control.
			PlayerHolder activeTarget = getEventPlayer(target);
			
			if (activeTarget != null)
			{
				// Los eventos estilo AllVsAll no tienen un team definido los players.
				if ((activePlayer.getPcInstance().getTeam() == Team.NONE) || (activeTarget.getPcInstance().getTeam() == Team.NONE))
				{
					// Sin accion, dejamos q se ejecute el listener.
				}
				else if (activePlayer.getPcInstance().getTeam() == activeTarget.getPcInstance().getTeam())
				{
					return true;
				}
			}
		}
		// CHECK FRIENDLY_FIRE ----------------------------------------
		return onAttack(activePlayer, target);
	}
	
	/**
	 * @param player
	 * @param target
	 * @return true -> solo en el caso de que no queremos q un ataque continue su progeso normal.
	 */
	public abstract boolean onAttack(PlayerHolder player, L2Character target);
	
	/**
	 * @param player -> personaje o summon
	 * @param target -> puede ser null
	 * @return true -> solo en el caso de que no queremos de una habilidad no continue su progreso normal.
	 */
	public boolean listenerOnUseSkill(L2Playable player, L2Character target, Skill skill)
	{
		// Si el personaje/summon no esta participando del evento terminar el listener.
		if (!isPlayerInEvent(player))
		{
			return false;
		}
		
		// Si el personaje no tiene target terminar el listener.
		// XXX quizas en algun evento pueda ser requerido el uso de habilidades sin necesidad de target....revisar.
		if (target == null)
		{
			return false;
		}
		
		// Si el personaje esta usando una habilidad sobre si mismo terminar el listener.
		if (player.equals(target))
		{
			return false;
		}
		
		// Obtenemos el player en cuestion dentro de nuestro evento.
		PlayerHolder activePlayer = getEventPlayer(player);
		
		// CHECK FRIENDLY_FIRE ----------------------------------------
		if (Configs.FRIENDLY_FIRE)
		{
			// Si nuestro target es de tipo L2Playable y esta dentro del evento hacemos el control.
			PlayerHolder activeTarget = getEventPlayer(target);
			
			if ((activeTarget != null) && skill.isDamage())
			{
				// Los eventos estilo AllVsAll no tienen un team definido los players.
				if ((activePlayer.getPcInstance().getTeam() == Team.NONE) || (activeTarget.getPcInstance().getTeam() == Team.NONE))
				{
					// Sin accion, dejamos q se ejecute el listener.
				}
				else if (activePlayer.getPcInstance().getTeam() == activeTarget.getPcInstance().getTeam())
				{
					return true;
				}
			}
		}
		// CHECK FRIENDLY_FIRE ----------------------------------------
		
		return onUseSkill(activePlayer, target, skill);
	}
	
	/**
	 * @param player
	 * @param target
	 * @param skill
	 * @return true -> solo en el caso de no queremos de una habilidad no continue su progrso normal.
	 */
	public abstract boolean onUseSkill(PlayerHolder player, L2Character target, Skill skill);
	
	// METODOS VARIOS -------------------------------------------------------------------------------- //
	
	/**
	 * Teletransportamos a los players de cada team a su respectivos puntos de inicio.<br>
	 */
	public void teleportAllPlayers()
	{
		for (PlayerHolder player : getAllEventPlayers())
		{
			teleportPlayer(player);
		}
	}
	
	/**
	 * Teletransportamos a un player especifico a su localizacion inicial dentro del evento.
	 * @param player
	 */
	public void teleportPlayer(PlayerHolder player)
	{
		// obtenemos el spawn definido al inicia de cada evento
		Location loc = getTeamSpawn(player.getPcInstance().getTeam());
		// teletransportamos al personaje
		player.getPcInstance().teleToLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), player.getDinamicInstanceId());
		
	}
	
	/**
	 * Preparamos a los players para iniciar el eventos<br>
	 * <ul>
	 * <b>Acciones: </b>
	 * </ul>
	 * <li>Cancelamos cualquier ataque en progreso</li><br>
	 * <li>Cancelamos cualquier habilidad en progreso</li><br>
	 * <li>Paralizamos al player</li><br>
	 * <li>Cancelamos todos los effectos de los personajes</li><br>
	 * <li>Cancelamos todos los effectos de los pets en caso de existir</li><br>
	 * <li>Cancelamos todos los cubics</li><br>
	 * <li>Cancelamos todos los Pets/Summons</li><br>
	 */
	public void prepareToStart()
	{
		for (PlayerHolder player : getAllEventPlayers())
		{
			// cancelamos target
			player.getPcInstance().setTarget(null);
			// cancelos cualquier ataque en progrso.
			player.getPcInstance().abortAttack();
			player.getPcInstance().breakAttack();
			// cancelamos cualquier cast en progreso.
			player.getPcInstance().abortCast();
			player.getPcInstance().breakCast();
			// paramos todos los effectos del evento.
			player.getPcInstance().stopAllEffects();
			
			if (player.getPcInstance().getSummon() != null)
			{
				player.getPcInstance().getSummon().stopAllEffects();// paramos todos los effectos del pet
				player.getPcInstance().getSummon().unSummon(player.getPcInstance());// cancelamos el summon del pet
			}
			
			// cancelamos todos los cubics
			for (L2CubicInstance cubic : player.getPcInstance().getCubics().values())
			{
				cubic.cancelDisappear();
			}
		}
	}
	
	/**
	 * Preparamos al player para la pelea.<br>
	 * <ul>
	 * <b>Acciones: </b>
	 * </ul>
	 * <li>Cancelamos el paralizis realizado en -> <u>prepareToTeleport()</u></li><br>
	 * <li>Entregamos los buffs definidos en los configs</li>
	 */
	public void prepareToFight()
	{
		for (PlayerHolder player : getAllEventPlayers())
		{
			giveBuffPlayer(player.getPcInstance()); // entregamos buffs
		}
	}
	
	/**
	 * Preparamos al player para el fin del evento<br>
	 * <ul>
	 * <b>Acciones: </b>
	 * </ul>
	 * <li>Cancelamos cualquier ataque y habilidad en progreso.</li><br>
	 * <li>Cancelamos todos los effectos propios del evento.</li><br>
	 * <li>Recuperamos el titulo y su color original.</li><br>
	 * <li>Cancelamos el Team</li><br>
	 * <li>Lo sacamos del mundo q creamos para el evento</li>
	 */
	public void prepareToEnd()
	{
		for (PlayerHolder player : getAllEventPlayers())
		{
			// cancelamos target
			player.getPcInstance().setTarget(null);
			// cancelos cualquier ataque en progrso.
			player.getPcInstance().abortAttack();
			player.getPcInstance().breakAttack();
			// cancelamos cualquier cast en progreso.
			player.getPcInstance().abortCast();
			player.getPcInstance().breakCast();
			// paramos todos los effectos del evento.
			player.getPcInstance().stopAllEffects();
			// Recuperamos el titulo y su color de los participantes.
			player.recoverOriginalColorTitle();
			player.recoverOriginalTitle();
			// Le quitamos el team al personaje
			player.getPcInstance().setTeam(Team.NONE);
			// Lo sacamos del mundo creado para el evento
			for (InstanceWorld world : EventEngineManager.getInstancesWorlds())
			{
				if (player.getDinamicInstanceId() == world.getInstanceId())
				{
					world.removeAllowed(player.getPcInstance().getObjectId());
				}
			}
			// Enviamos al personaje a su instancia real y a giran
			player.getPcInstance().teleToLocation(83437, 148634, -3403, 0, 0);// GIRAN CENTER
		}
	}
	
	/**
	 * Generamos un task para revivir a un personaje.<br>
	 * <ul>
	 * <b>Acciones: </b>
	 * </ul>
	 * <li>Generamos una pausa antes de ejecutar accion alguna.</li><br>
	 * <li>Revivimos al personaje.</li><br>
	 * <li>Le entregamos los buff dependiendo del evento en el q esta.</li><br>
	 * <li>Teletransportamos al personaje dependiendo del evento en el q esta.</li><br>
	 * <li>Lo hacemos invulnerable por 5 seg y no le permitimos moverse.</li><br>
	 * <li>Cancelamos el invul y le permitimos moverse</li><br>
	 * @param player -> personaje a revivir
	 * @param time -> tiempo antes de revivir a un personaje
	 */
	public void giveResurectPlayer(final PlayerHolder player, int time)
	{
		try
		{
			EventUtil.sendEventMessage(player, "En " + time + " segundos seras revivido");
			// Cuenta regresiva
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					DecayTaskManager.getInstance().cancel(player.getPcInstance());
					player.getPcInstance().doRevive();
					// lo curamos por completo
					player.getPcInstance().setCurrentCp(player.getPcInstance().getMaxCp());
					player.getPcInstance().setCurrentHp(player.getPcInstance().getMaxHp());
					player.getPcInstance().setCurrentMp(player.getPcInstance().getMaxMp());
					// lo teletransportamos
					EventEngineManager.getCurrentEvent().teleportPlayer(player);
					// le entregamos los buffs
					EventEngineManager.getCurrentEvent().giveBuffPlayer(player.getPcInstance());
				}
				
			}, time * 1000);
		}
		catch (Exception e)
		{
			LOG.warning(AbstractEvent.class.getSimpleName() + ": " + e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Le damos los buff a un player seteados dentro de los configs
	 * @param player
	 */
	public void giveBuffPlayer(L2PcInstance player)
	{
		if (player.isMageClass())
		{
			for (SkillHolder sh : getPlayerBuffs(PlayerClassType.MAGE))
			{
				sh.getSkill().applyEffects(player, player);
			}
		}
		else
		{
			for (SkillHolder sh : getPlayerBuffs(PlayerClassType.WARRIOR))
			{
				sh.getSkill().applyEffects(player, player);
			}
		}
	}

	/**
	 * Entrgamos los items definidos ya en alguna lista<br>
	 * Creado con el fin de entregar los rewards dentro de los eventos
	 * @param ph
	 * @param items
	 */
	public void giveItems(PlayerHolder ph, List<ItemHolder> items)
	{
		for (ItemHolder reward : items)
		{
			ph.getPcInstance().addItem("eventReward", reward.getId(), reward.getCount(), null, true);
		}
	}
	
	/**
	 * Controlamos los tiempo de los eventos<br>
	 * <ul>
	 * <b>Acciones: </b>
	 * </ul>
	 * <li>-> step 1: Anunciamos q los participantes seran teletransportados</li><br>
	 * <li>Esperamos 3 segs</li><br>
	 * <li>-> step 2: Ajustamos el estado del evento -> START</li><br>
	 * <li>Esperamos 1 seg a q se realicen acciones individuales de cada evento</li><br>
	 * <li>-> step 3: Ajustamos el estado del evento -> FIGHT</li><br>
	 * <li>-> step 3: Enviamos un mensaje de que ya estan listos para pelear.</li><br>
	 * <li>Esperamos a que el evento termine</li><br>
	 * <li>-> step 4: Ajustamos el estado del evento -> END</li><br>
	 * <li>-> step 4: Enviamos un mensaje avisando de q termino el evento</li><br>
	 * <li>Esperamos 1 seg</li><br>
	 * <li>-> step 5: Volvemos a habilitar el registro</li><br>
	 */
	private void controlTimeEvent()
	{
		int time = 1000;
		ThreadPoolManager.getInstance().scheduleGeneral(new EventTask(1), time);
		time += 3000;
		ThreadPoolManager.getInstance().scheduleGeneral(new EventTask(2), time);
		time += 1000;
		ThreadPoolManager.getInstance().scheduleGeneral(new EventTask(3), time);
		time += Configs.EVENT_DURATION * 60 * 1000;
		ThreadPoolManager.getInstance().scheduleGeneral(new EventTask(4), time);
		time += 1000;
		ThreadPoolManager.getInstance().scheduleGeneral(new EventTask(5), time);
	}
}
