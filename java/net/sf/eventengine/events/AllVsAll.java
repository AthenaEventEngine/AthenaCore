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

import java.util.ArrayList;
import java.util.Collections;
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

import com.l2jserver.gameserver.enums.Team;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;
import com.l2jserver.gameserver.model.skills.Skill;

/**
 * @author fissban
 */
public class AllVsAll extends AbstractEvent
{
	public AllVsAll()
	{
		super();
		// Definimos el spawn del team
		setTeamSpawn(Team.NONE, Configs.AVA_LOC_PLAYER);
		// Definimos los buffs de los personajes
		setPlayerBuffs(PlayerClassType.MAGE, Configs.AVA_BUFF_PLAYER_MAGE);
		setPlayerBuffs(PlayerClassType.WARRIOR, Configs.AVA_BUFF_PLAYER_WARRIOR);
	}
	
	@Override
	public EventType getEventType()
	{
		return EventType.AVA;
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
				break;
			
			case END:
				giveRewardsTeams();
				prepareToEnd(); // Metodo general
				break;
		}
		
	}
	
	// LISTENERS -----------------------------------------------------------------------
	@Override
	public void onKill(PlayerHolder player, L2Character target)
	{
		// Incrementamos en uno la cant de kills al player.
		player.increaseKills();
		// Actualizamos el titulo del personaje
		updateTitle(player);
	}
	
	@Override
	public boolean onAttack(PlayerHolder player, L2Character target)
	{
		return false;
	}
	
	@Override
	public boolean onUseSkill(PlayerHolder player, L2Character target, Skill skill)
	{
		return false;
	}
	
	@Override
	public void onDeath(PlayerHolder player)
	{
		// generamos un task para revivir al player
		giveResurectPlayer(player, 10); // TODO -> hardcode
		// Incrementamos en uno la cant de muertes al player.
		player.increaseDeaths();
		// Actualizamos el titulo del personaje
		updateTitle(player);
	}
	
	@Override
	public void onInteract(PlayerHolder player, L2Npc npc)
	{
		return;
	}
	
	// METODOS VARIOS ------------------------------------------------------------------
	
	/**
	 * Creamos el equipo donde jugaran los personajes
	 */
	private void createTeam()
	{
		// Creamos la instancia y el mundo
		InstanceWorld world = EventEngineManager.createNewInstanceWorld();
		
		for (PlayerHolder player : getAllEventPlayers())
		{
			// Agregamos el personaje al mundo para luego ser teletransportado
			world.addAllowed(player.getPcInstance().getObjectId());
			// Ajustamos el team de los personaje
			player.getPcInstance().setTeam(Team.NONE);
			// Ajustamos la instancia a al que perteneceran los personaje
			player.setDinamicInstanceId(world.getInstanceId());
			// Ajustamos el color del titulo
			player.setNewColorTitle(PlayerColorType.YELLOW_OCHRE);
			// Ajustamos el color del titulo y el titulo del personaje.
			updateTitle(player);
		}
	}
	
	/**
	 * Actualizamos el titulo de un personaje dependiendo de la cantidad de muertes o kills q tenga
	 * @param player
	 */
	private void updateTitle(PlayerHolder player)
	{
		// Ajustamos el titulo del pesonaje
		player.setNewTitle("Kills " + player.getKills() + " | " + player.getDeaths() + " Death");
		// Actualizamos el status del personaje
		player.getPcInstance().updateAndBroadcastStatus(2);
	}
	
	/**
	 * Entregamos los rewards.<br>
	 * <u>Acciones:</u> <li>Ordenamos la lista dependiendo de la cant de puntos de cada player</li><br>
	 * <li>Al 50% mejor se le entregan los rewards de ganador</li><br>
	 * <li>Al 50% peor se le entregan los rewards de perdedor</li><br>
	 */
	public void giveRewardsTeams()
	{
		if (getAllEventPlayers().isEmpty())
		{
			return;
		}
		
		// Le daremos por default reward de ganador al 50% de los mejores participantes y a los demas le damos reward de perdedor :P
		
		// Lista auxiliar
		List<PlayerHolder> playersInEvent = new ArrayList<>();
		//
		playersInEvent.addAll(getAllEventPlayers());
		// Ordenamos la lista
		Collections.sort(playersInEvent, PlayerHolder._pointsComparator);
		// Entregamos los rewards y anunciamos los ganadores.
		for (PlayerHolder player : playersInEvent)
		{
			int aux = 0;
			
			if (aux <= (playersInEvent.size() / 2))
			{
				// Enviamos un mensaje al ganador
				EventUtil.sendEventScreenMessage(player, "Ganador " + player.getPcInstance().getName() + " con " + player.getPoints());
				// Entregamos los rewards
				giveItems(player, Configs.AVA_REWARD_PLAYER_WIN);
			}
			else
			{
				// Enviamos un mensaje al ganador
				EventUtil.sendEventScreenMessage(player, "Perdedor " + player.getPcInstance().getName() + " con " + player.getPoints());
				// Entregamos los rewards
				giveItems(player, Configs.AVA_REWARD_PLAYER_LOSER);
			}
			
			aux++;
		}
	}
}
