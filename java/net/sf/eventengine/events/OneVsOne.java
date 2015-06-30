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
package net.sf.eventengine.events;

import java.util.ArrayList;
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
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;
import com.l2jserver.gameserver.model.skills.Skill;

/**
 * One Vs One<br>
 * Al crear los equipos<br>
 * Conformaremos tantos equipos como participantes / 2.<br>
 * Cada equipo contara con 1 participante Blue y otro Red<br>
 * Cada equipo contara con su propia instancia dinamica<br>
 * <br>
 * @author fissban
 */
public class OneVsOne extends AbstractEvent
{
	private static List<InstancesTeams> _instancesTeams = new ArrayList<>();

	public OneVsOne()
	{
		super();

		// Definimos los spawns de cada team
		setTeamSpawn(Team.RED, Configs.OVO_LOC_TEAM_RED);
		setTeamSpawn(Team.BLUE, Configs.OVO_LOC_TEAM_BLUE);
		// Definimos los buffs de los personajes
		setPlayerBuffs(PlayerClassType.MAGE, Configs.OVO_BUFF_PLAYER_MAGE);
		setPlayerBuffs(PlayerClassType.WARRIOR, Configs.OVO_BUFF_PLAYER_WARRIOR);
	}

	@Override
	public EventType getEventType()
	{
		return EventType.OVO;
	}

	@Override
	public void runEventState(EventState state)
	{
		switch (state)
		{
			case START:
				prepareToStart(); // Metodo general
				createTeams();
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

	// LISTENERS ------------------------------------------------------
	@Override
	public boolean onUseSkill(PlayerHolder player, L2Character target, Skill skill)
	{
		return false;
	}

	@Override
	public boolean onAttack(PlayerHolder player, L2Character target)
	{
		return false;
	}

	@Override
	public void onKill(PlayerHolder player, L2Character target)
	{
		// XXX En este caso el control es individual y solo tendremos en cuenta los kills

		// incrementamos en uno la cantidad de kills q tiene el participantes
		player.increaseKills();
	}

	@Override
	public void onDeath(PlayerHolder player)
	{
		giveResurectPlayer(player, 10);
		// incrementamos en uno la cantidad de muertes del personaje
		// solo es usado al final del evento para mostrar los resultados
		player.increaseDeaths();
	}

	@Override
	public void onInteract(PlayerHolder player, L2Npc npc)
	{
		//
	}

	// METODOS VARIOS -------------------------------------------------

	/**
	 * Tomamos todos los players que estan registrados en el evento y generamos los teams
	 */
	private void createTeams()
	{
		if (EventEngineManager.getAllRegisterPlayers().isEmpty())
		{
			return;
		}

		// control para saber en q equipo estara el jugador.
		boolean blueOrRed = true;
		// control para saber la cantidad de jugadores por equipo.
		int countPlayer = 0;

		PlayerHolder playerBlue = null;
		PlayerHolder playerRed = null;

		InstanceWorld world = null;

		for (PlayerHolder player : getAllEventPlayers())
		{
			if (countPlayer < 2)
			{
				// Creamos la instancia y el mundo
				if (countPlayer == 0)
				{
					world = EventEngineManager.createNewInstanceWorld();
				}

				// Repartimos uno para cada team
				if (blueOrRed)
				{
					playerBlue = player;
					// Ajustamos el color del titulo y el titulo del personaje.
					player.getPcInstance().setTeam(Team.BLUE);
					player.setNewColorTitle(PlayerColorType.BLUE);
					player.setNewTitle("[ BLUE ]");
					// Asignamos a la instancia q pertenecera el player.
					player.setDinamicInstanceId(world.getInstanceId());
				}
				else
				{
					playerRed = player;
					// Ajustamos el color del titulo y el titulo del personaje.
					player.getPcInstance().setTeam(Team.RED);
					player.setNewColorTitle(PlayerColorType.RED);
					player.setNewTitle("[ RED ]");
					// Asignamos a la instancia q pertenecera el player.
					player.setDinamicInstanceId(world.getInstanceId());
				}

				// alternamos entre true y false
				blueOrRed = !blueOrRed;
				// incrementamos en uno la cant de players
				countPlayer++;
			}

			if ((playerRed != null) && (playerBlue != null))
			{
				// cargamos los diferentes teams
				_instancesTeams.add(new InstancesTeams(playerRed, playerBlue));
				// inicilizamos la variable
				countPlayer = 0;
			}

			// Actualizamos al personaje frente a lo de su alrededor y a si mismo
			player.getPcInstance().updateAndBroadcastStatus(2);
		}
	}

	/**
	 * Entregamos los rewards, por el momento solo tenemos soporte para 1 o 2 teams.
	 */
	private void giveRewardsTeams()
	{
		for (InstancesTeams team : _instancesTeams)
		{
			// Averiguamos q jugador tiene la mayor cant de kills de este equipo

			int pointsBlue = team._playerBlue.getKills();
			int pointsRed = team._playerRed.getKills();

			if (pointsBlue == pointsRed)// Si ambos tienen la misma cantidad de kills reciviran ambos el premio de perdedor.
			{
				// Anunciamos el resultado del evento
				EventUtil.sendEventScreenMessage(team._playerBlue, "El evento resulto en un empate entre ambos teams!");
				EventUtil.sendEventScreenMessage(team._playerRed, "El evento resulto en un empate entre ambos teams!");

				// Ambos equipos empataron asique le entregamos a ambos el premio de los perdedores xD
				for (ItemHolder reward : Configs.OVO_REWARD_PLAYER_LOSE)
				{
					team._playerRed.getPcInstance().addItem("eventReward", reward.getId(), reward.getCount(), null, true);
					team._playerBlue.getPcInstance().addItem("eventReward", reward.getId(), reward.getCount(), null, true);
				}
			}
			else if (pointsBlue < pointsRed)// ganador red
			{
				// Anunciamos el resultado del evento
				EventUtil.sendEventScreenMessage(team._playerBlue, "El evento fue ganado por el jugador RED!");
				EventUtil.sendEventScreenMessage(team._playerRed, "El evento fue ganado por el jugador BLUE!");
				
				for (ItemHolder reward : Configs.OVO_REWARD_PLAYER_LOSE)
				{
					team._playerBlue.getPcInstance().addItem("eventReward", reward.getId(), reward.getCount(), null, true);
				}
				for (ItemHolder reward : Configs.OVO_REWARD_PLAYER_WIN)
				{
					team._playerRed.getPcInstance().addItem("eventReward", reward.getId(), reward.getCount(), null, true);
				}
			}
			else if (pointsBlue > pointsRed)// ganador blue
			{
				// Anunciamos el resultado del evento
				EventUtil.sendEventScreenMessage(team._playerBlue, "El evento fue ganado por el jugador RED!");
				EventUtil.sendEventScreenMessage(team._playerRed, "El evento fue ganado por el jugador BLUE!");
				
				for (ItemHolder reward : Configs.OVO_REWARD_PLAYER_WIN)
				{
					team._playerBlue.getPcInstance().addItem("eventReward", reward.getId(), reward.getCount(), null, true);
				}
				for (ItemHolder reward : Configs.OVO_REWARD_PLAYER_LOSE)
				{
					team._playerRed.getPcInstance().addItem("eventReward", reward.getId(), reward.getCount(), null, true);
				}
			}
		}
	}

	/**
	 * Clase encargada de administrar los puntos de los teams de cada instancia
	 * @author fissban
	 */
	private class InstancesTeams
	{
		public PlayerHolder _playerRed;
		public PlayerHolder _playerBlue;

		public InstancesTeams(PlayerHolder playerRed, PlayerHolder playerBlue)
		{
			_playerRed = playerRed;
			_playerBlue = playerBlue;
		}
	}
}
