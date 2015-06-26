/*
 * Copyright (C) 2014-2015 L2jAdmins
 *
 * This file is part of L2jAdmins.
 *
 * L2jAdmins is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2jAdmins is distributed in the hope that it will be useful,
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
import java.util.Map;
import java.util.logging.Logger;

import net.sf.eventengine.EventEngineManager;
import net.sf.eventengine.configs.Configs;
import net.sf.eventengine.enums.EventState;
import net.sf.eventengine.enums.EventType;
import net.sf.eventengine.holder.PlayerHolder;
import net.sf.eventengine.task.EventTask;
import net.sf.eventengine.util.EventUtil;

import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.data.xml.impl.NpcData;
import com.l2jserver.gameserver.model.L2Spawn;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2CubicInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;
import com.l2jserver.gameserver.model.holders.SkillHolder;
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
		// agregamos todos los player registrados al evento.
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
	public void addEventNpc(int npcId, int x, int y, int z, int heading, boolean randomOffset)
	{
		// generamos el spawn de nuestro npc -> sacado de la clase Quest
		L2Npc npc = null;
		try
		{
			L2NpcTemplate template = NpcData.getInstance().getTemplate(npcId);
			if (template != null)
			{
				if (randomOffset)
				{
					x += Rnd.get(-100, 100);
					y += Rnd.get(-100, 100);
				}
				
				L2Spawn spawn = new L2Spawn(template);
				spawn.setHeading(heading);
				spawn.setX(x);
				spawn.setY(y);
				spawn.setZ(z + 20);
				spawn.stopRespawn();
				spawn.getLastSpawn().setEventMob(true);
				spawn.setInstanceId(1); // indicar bien la instancia
				npc = spawn.doSpawn();// isSummonSpawn
				
				spawn.init();
				spawn.getLastSpawn().setCurrentHp(999999999);
				spawn.getLastSpawn().setEventMob(true);
				// Animacion
				spawn.getLastSpawn().broadcastPacket(new MagicSkillUse(spawn.getLastSpawn(), spawn.getLastSpawn(), 1034, 1, 1, 1));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
		// agregamos nuestro npc a la lista
		_eventNpc.put(npc.getId(), npc);
	}
	
	/**
	 * Borramos todos los npc generados dentro de nuestro evento.
	 */
	public void removeAllEventNpc()
	{
		// TODO no se si es realmente necesario
		for (L2Npc npc : _eventNpc.values())
		{
			if (npc == null)
			{
				continue;
			}
			
			// Paramos el respawn del npc
			npc.getSpawn().stopRespawn();
			// Borramos al npc
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
		
		// limpiamos la lista, ya no la necesitaremos
		EventEngineManager.getAllRegisterPlayers().clear();
	}
	
	/**
	 * Verificamos si un player esta participando de algun evento.
	 * @param player
	 * @return
	 */
	public boolean isPlayerInEvent(L2PcInstance player)
	{
		return _eventPlayers.containsKey(player.getObjectId());
	}

	/**
	 * Verificamos si un player esta participando de algun evento.
	 * @param player
	 * @return
	 */
	public PlayerHolder getEventPlayer(L2PcInstance player)
	{
		if (!_eventPlayers.containsKey(player.getObjectId()))
		{
			return null;
		}

		return _eventPlayers.get(player.getObjectId());
	}

	// LISTENERS ------------------------------------------------------------------------------------ //
	// Obs -> solo definiremos aqui algunas pequeñas acciones generales.

	/**
	 * @param player
	 * @param npc
	 */
	public abstract void onInteract(PlayerHolder player, L2Npc npc);

	/**
	 * @param player
	 * @param target
	 */
	public abstract void onKill(PlayerHolder player, PlayerHolder target);

	/**
	 * @param player
	 */
	public abstract void onDeath(PlayerHolder player);

	/**
	 * @param player
	 * @param target
	 * @return true -> solo en el caso de que no queremos q un ataque continue su progeso normal.
	 */
	public abstract boolean onAttack(PlayerHolder player, PlayerHolder target);

	/**
	 * @param player
	 * @param target
	 * @param skill
	 * @return true -> solo en el caso de no queremos de una habilidad no continue su progrso normal.
	 */
	public abstract boolean onUseSkill(PlayerHolder player, PlayerHolder target, Skill skill);

	// METODOS VARIOS -------------------------------------------------------------------------------- //

	/**
	 * Teletransportamos a los players de cada team a su respectivos puntos de inicio.<br>
	 */
	public void teleportAllPlayers()
	{
		for (PlayerHolder player : getAllEventPlayers())
		{
			teleportPlayer(player, player.getDinamicInstanceId());
		}
	}

	/**
	 * Teletransportamos a un player especifico a su localizacion inicial dentro del evento.
	 * @param player
	 */
	public void teleportPlayer(PlayerHolder player, int instanceId)
	{
		switch (getEventType())
		{
			case TVT:
			{
				switch (player.getPcInstance().getTeam())
				{
					case BLUE:
						Location locBlue = Configs.TVT_LOC_TEAM_BLUE;
						player.getPcInstance().teleToLocation(locBlue.getX(), locBlue.getY(), locBlue.getZ(), locBlue.getHeading(), instanceId);
						break;
					case RED:
						Location locRed = Configs.TVT_LOC_TEAM_RED;
						player.getPcInstance().teleToLocation(locRed.getX(), locRed.getY(), locRed.getZ(), locRed.getHeading(), instanceId);
						break;
				}
				break;
			}
			case CTF:
			{
				// TODO terminar los configs
				switch (player.getPcInstance().getTeam())
				{
					case BLUE:
						Location locBlue = Configs.TVT_LOC_TEAM_BLUE;
						player.getPcInstance().teleToLocation(locBlue.getX(), locBlue.getY(), locBlue.getZ(), locBlue.getHeading(), instanceId);
						break;
					case RED:
						Location locRed = Configs.TVT_LOC_TEAM_RED;
						player.getPcInstance().teleToLocation(locRed.getX(), locRed.getY(), locRed.getZ(), locRed.getHeading(), instanceId);
						break;
				}
				break;
			}
			case AVA:
			{
				Location loc = Configs.AVA_LOC_PLAYER;
				player.getPcInstance().teleToLocation(loc.getX() + Rnd.get(200), loc.getY() + Rnd.get(200), loc.getZ(), 0, instanceId);
				break;
			}
		}
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

			if (player.getPcInstance().hasPet())
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
	 * <li>Cancelamos los effectos de TEAM -> <b>Desarrollar!</b></li><br>
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
	public void giveResurectPlayer(PlayerHolder player, int time)
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
					// lo curamos por completo
					player.getPcInstance().setCurrentCp(player.getPcInstance().getMaxCp());
					player.getPcInstance().setCurrentHp(player.getPcInstance().getMaxHp());
					player.getPcInstance().setCurrentMp(player.getPcInstance().getMaxMp());

					DecayTaskManager.getInstance().cancel(player.getPcInstance());
					player.getPcInstance().doRevive();
					// lo teletransportamos
					EventEngineManager.getCurrentEvent().teleportPlayer(player, player.getDinamicInstanceId());
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
			switch (getEventType())
			{
				case TVT:
					for (SkillHolder sh : Configs.TVT_BUFF_PLAYER_MAGE)
					{
						sh.getSkill().applyEffects(player, player);
					}
					break;
				case CTF:

					break;

				case AVA:
					for (SkillHolder sh : Configs.AVA_BUFF_PLAYER_MAGE)
					{
						sh.getSkill().applyEffects(player, player);
					}
					break;
			}

		}
		else
		{
			switch (getEventType())
			{
				case TVT:
					for (SkillHolder sh : Configs.TVT_BUFF_PLAYER_WARRIOR)
					{
						sh.getSkill().applyEffects(player, player);
					}
					break;
				case CTF:

					break;

				case AVA:
					for (SkillHolder sh : Configs.AVA_BUFF_PLAYER_WARRIOR)
					{
						sh.getSkill().applyEffects(player, player);
					}
					break;
			}
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
