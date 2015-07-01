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
package net.sf.eventengine.events;

import java.util.List;

import net.sf.eventengine.EventEngineManager;
import net.sf.eventengine.configs.Configs;
import net.sf.eventengine.enums.EventState;
import net.sf.eventengine.enums.EventType;
import net.sf.eventengine.enums.PlayerClassType;
import net.sf.eventengine.enums.PlayerColorType;
import net.sf.eventengine.handler.AbstractEvent;
import net.sf.eventengine.holder.PlayerHolder;
import net.sf.eventengine.util.EventUtil;

import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.enums.Team;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.network.clientpackets.Say2;
import com.l2jserver.util.Rnd;

/**
 * Evento de supervivencia<br>
 * Se creara un unico team y tendran que sobrevivir durante varias oleadas de mobs,<br>
 * @author fissban
 */
public class Survive extends AbstractEvent
{
	// Variable que controlara el nivel del stage.
	private int _stage = 1;
	// Variable que nos ayudara a llevar el control de la cantidad de mobs muertos.
	private int _auxKillMonsters = 0;
	
	// Id de los monsters
	private static final List<Integer> MONSTERS_ID = Configs.SURVIVE_MOSNTERS_SPAWN;
	
	public Survive()
	{
		super();
		// Definimos los spawns de cada team
		setTeamSpawn(Team.BLUE, Configs.SURVIVE_LOC_PLAYER);
		// Definimos los buffs de los personajes
		setPlayerBuffs(PlayerClassType.MAGE, Configs.SURVIVE_BUFF_PLAYER_MAGE);
		setPlayerBuffs(PlayerClassType.WARRIOR, Configs.SURVIVE_BUFF_PLAYER_WARRIOR);
	}
	
	@Override
	public EventType getEventType()
	{
		return EventType.SURVIVE;
	}
	
	@Override
	public void runEventState(EventState state)
	{
		switch (state)
		{
			case START:
				prepareToStart(); // Metodo general
				createTeam();
				teleportAllPlayers();
				break;
			
			case FIGHT:
				prepareToFight(); // Metodo general
				spawnsMobs();
				break;
			
			case END:
				// showResult();
				prepareToEnd(); // Metodo general
				break;
		}
	}
	
	@Override
	public void onInteract(PlayerHolder player, L2Npc npc)
	{
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onKill(PlayerHolder player, L2Character target)
	{
		// La instancia al ser "NO PVP" no hace falta tener en cuenta q maten a un compañero.
		
		// Nos servira para llevar el recuento de cuantos mobs mato.
		player.increaseKills();
		// Actualizamos el titulo de un personaje
		updateTitle(player);
		// Incrementamos en uno la cantidad de mobs muertos
		_auxKillMonsters++;
		// Verificamos la cantidad de mobs muertos, de haberlos matados a todos aumentamos en uno el stage.
		if (_auxKillMonsters >= (_stage * Configs.SURVIVE_MONSTER_SPAWN_FOR_STAGE))
		{
			// aumentamos en uno el stage.
			_stage++;
			// reiniciamos nuestro auxiliar.
			_auxKillMonsters = 0;
			// spawneamos mas mobs
			spawnsMobs();
			// Entregamos los rewards por haber pasado de stage
			giveRewardsTeams();
		}
	}
	
	@Override
	public void onDeath(PlayerHolder player)
	{
		//
	}
	
	@Override
	public boolean onAttack(PlayerHolder player, L2Character target)
	{
		if (target.isPlayable())
		{
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onUseSkill(PlayerHolder player, L2Character target, Skill skill)
	{
		return false;
	}
	
	// MISC ---------------------------------------------------------------------------------------
	public void giveRewardsTeams()
	{
		if (getAllEventPlayers().isEmpty())
		{
			return;
		}
		
		// Entregamos los rewards y anunciamos los ganadores.
		for (PlayerHolder player : getAllEventPlayers())
		{
			// Enviamos un mensaje al ganador
			EventUtil.sendEventScreenMessage(player, "Felicitaciones sobreviviente");
			// Entregamos los rewards
			giveItems(player, Configs.AVA_REWARD_PLAYER_LOSER);
		}
	}
	
	private void spawnsMobs()
	{
		EventUtil.announceToAllPlayersInEvent(Say2.BATTLEFIELD, "Ya llegan, preparate!");
		
		// Transcurridos 5 segs se ejecutara el spawn.
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				// Spawneamos los mobs dependiendo del nivel del stage dentro de la unica instancia creada.
				for (int i = 0; i < (_stage * Configs.SURVIVE_MONSTER_SPAWN_FOR_STAGE); i++)
				{
					L2Npc eventNpc = addEventNpc(MONSTERS_ID.get(Rnd.get(MONSTERS_ID.size() - 1)), 149539, 46712, -3411, 0, true, EventEngineManager.getInstancesWorlds().get(0).getInstanceId());
					// Definimos un team para el monster.
					// Solo usado como un efecto.
					eventNpc.setTeam(Team.RED);
				}
				
				// Avisamos a los personajes del evento en q stage estan actualmente.
				for (PlayerHolder ph : getAllEventPlayers())
				{
					EventUtil.sendEventScreenMessage(ph, "Stage " + _stage, 5000);
				}
			}
			
		}, 5000L);
		
	}
	
	/**
	 * Creamos el equipo donde jugaran los personajes
	 */
	private void createTeam()
	{
		// Creamos la instancia y el mundo
		InstanceWorld world = EventEngineManager.createNewInstanceWorld();
		
		for (PlayerHolder ph : getAllEventPlayers())
		{
			// Agregamos el personaje al mundo para luego ser teletransportado
			world.addAllowed(ph.getPcInstance().getObjectId());
			// Ajustamos el team de los personaje
			ph.getPcInstance().setTeam(Team.BLUE);
			// Ajustamos la instancia a al que perteneceran los personaje
			ph.setDinamicInstanceId(world.getInstanceId());
			// Ajustamos el color del titulo
			ph.setNewColorTitle(PlayerColorType.YELLOW_OCHRE);
			// Ajustamos el titulo del personaje.
			updateTitle(ph);
		}
	}
	
	/**
	 * Actualizamos el titulo de un personaje dependiendo de la cantidad de kills q tenga
	 * @param player
	 */
	private void updateTitle(PlayerHolder player)
	{
		// Ajustamos el titulo del pesonaje
		player.setNewTitle("Monster Death " + player.getKills());
		// Actualizamos el status del personaje
		player.getPcInstance().updateAndBroadcastStatus(2);
	}
}
