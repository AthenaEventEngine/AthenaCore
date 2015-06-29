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
import net.sf.eventengine.network.serverpackets.EventParticipantStatus;
import net.sf.eventengine.util.EventUtil;

import com.l2jserver.gameserver.enums.Team;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;
import com.l2jserver.gameserver.model.skills.Skill;

/**
 * @author fissban
 */
public class TeamVsTeam extends AbstractEvent
{
	// Puntos que tiene cada team.
	private int _pointsRed = 0;
	private int _pointsBlue = 0;

	public TeamVsTeam()
	{
		super();
		
		// Definimos los spawns de cada team
		setTeamSpawn(Team.RED, Configs.TVT_LOC_TEAM_RED);
		setTeamSpawn(Team.BLUE, Configs.TVT_LOC_TEAM_BLUE);
		// Definimos los buffs de los personajes
		setPlayerBuffs(PlayerClassType.MAGE, Configs.TVT_BUFF_PLAYER_MAGE);
		setPlayerBuffs(PlayerClassType.WARRIOR, Configs.TVT_BUFF_PLAYER_WARRIOR);
	}

	@Override
	public EventType getEventType()
	{
		return EventType.TVT;
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
				showPoint();
				break;

			case END:
				// showResult();
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
		// Incrementamos los puntos del team.
		switch (player.getPcInstance().getTeam())
		{
			case RED:
				_pointsRed++;
				break;
			case BLUE:
				_pointsBlue++;
				break;
		}

		showPoint();
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
		return;
	}

	// METODOS VARIOS -------------------------------------------------
	
	/**
	 * Tomamos todos los players que estan registrados en el evento y generamos los teams
	 */
	private void createTeams()
	{
		// Creamos la instancia y el mundo
		InstanceWorld world = EventEngineManager.createNewInstanceWorld();

		int aux = 0;

		for (PlayerHolder player : getAllEventPlayers())
		{
			if ((aux % 2) == 0)
			{
				// Ajustamos el color del titulo y el titulo del personaje.
				player.getPcInstance().setTeam(Team.BLUE);
				player.setNewColorTitle(PlayerColorType.BLUE);
				player.setNewTitle("[ BLUE ]");
			}
			else
			{
				// Ajustamos el color del titulo y el titulo del personaje.
				player.getPcInstance().setTeam(Team.RED);
				player.setNewColorTitle(PlayerColorType.RED);
				player.setNewTitle("[ RED ]");
			}

			// Agregamos el personaje al mundo para luego ser teletransportado
			world.addAllowed(player.getPcInstance().getObjectId());
			// Actualizamos al personaje frente a lo de su alrededor y a si mismo
			player.getPcInstance().updateAndBroadcastStatus(2);
			// Ajustamos la instancia a al que perteneceran los personaje
			player.setDinamicInstanceId(world.getInstanceId());

			aux++;
		}
	}

	/**
	 * Entregamos los rewards, por el momento solo tenemos soporte para 1 o 2 teams.
	 */
	private void giveRewardsTeams()
	{
		if (EventEngineManager.getAllRegisterPlayers().isEmpty())
		{
			return;
		}

		Team ganador = getWinTeam();

		for (PlayerHolder player : getAllEventPlayers())
		{
			if (ganador == Team.NONE)
			{
				// Anunciamos el resultado del evento
				EventUtil.sendEventScreenMessage(player, "El evento resulto en un empate entre ambos teams!");

				// Ambos equipos empataron asique le entregamos a ambos el premio de los perdedores xD
				for (ItemHolder reward : Configs.TVT_REWARD_TEAM_LOSE)
				{
					player.getPcInstance().addItem("eventReward", reward.getId(), reward.getCount(), null, true);
				}
			}
			else
			{
				// Anunciamos el resultado del evento
				EventUtil.sendEventScreenMessage(player, "Equipo ganador -> " + ganador.getClass().getCanonicalName() + "!");

				// Entregamo los premios segun el equipo q gano
				if (player.getPcInstance().getTeam() == ganador)
				{
					for (ItemHolder reward : Configs.TVT_REWARD_TEAM_WIN)
					{
						player.getPcInstance().addItem("eventReward", reward.getId(), reward.getCount(), null, true);
					}
				}
				else
				{
					for (ItemHolder reward : Configs.TVT_REWARD_TEAM_LOSE)
					{
						player.getPcInstance().addItem("eventReward", reward.getId(), reward.getCount(), null, true);
					}
				}
			}
		}

	}

	/**
	 * Pequeño codigo para obtener al team ganador<br>
	 * Solo es usado para ahorrar codigo.
	 */
	private Team getWinTeam()
	{
		// Si ambos equipos tienen la misma cant de puntos creo q lo justo es q ambos son perdedores :P
		Team ganador;

		if (_pointsRed == _pointsBlue)
		{
			ganador = Team.NONE;
		}
		else if (_pointsRed > _pointsBlue)
		{
			ganador = Team.RED;
		}
		else
		{
			ganador = Team.BLUE;
		}

		return ganador;
	}

	/**
	 * Mostramos por pantalla la canidad de puntos q tiene cada team
	 */
	private void showPoint()
	{
		// Enviamos por pantalla los puntajes a todos en el evento
		
		for (PlayerHolder ph : getAllEventPlayers())
		{
			EventUtil.sendEventScreenMessage(ph, "RED " + _pointsRed + " | " + _pointsBlue + " BLUE", 10000);
			// ph.getPcInstance().sendPacket(new EventParticipantStatus(_pointsRed, _pointsBlue));
		}
	}

	/**
	 * Creamos listados con todos los participantes y le enviamos a cada uno los resultados finales del evento
	 */
	private void showResult()
	{
		List<PlayerHolder> teamBlue = new ArrayList<>();
		List<PlayerHolder> teamRed = new ArrayList<>();

		// Creamos listamos con los jugadores de cada team
		for (PlayerHolder ph : getAllEventPlayers())
		{
			if (ph.getPcInstance().getTeam() == Team.RED)
			{
				teamBlue.add(ph);
			}
			else
			{
				teamRed.add(ph);
			}
		}

		// Enviamos a todos los resultados finales del evento
		for (PlayerHolder ph : getAllEventPlayers())
		{
			ph.getPcInstance().sendPacket(new EventParticipantStatus(_pointsRed, teamBlue, _pointsBlue, teamRed));
		}
	}
}
