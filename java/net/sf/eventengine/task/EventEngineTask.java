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
package net.sf.eventengine.task;

import net.sf.eventengine.EventEngineManager;
import net.sf.eventengine.configs.Configs;
import net.sf.eventengine.enums.EventEngineState;
import net.sf.eventengine.enums.EventType;
import net.sf.eventengine.events.AllVsAll;
import net.sf.eventengine.events.CaptureTheFlag;
import net.sf.eventengine.events.TeamVsTeam;
import net.sf.eventengine.util.EventUtil;

import com.l2jserver.gameserver.network.clientpackets.Say2;

/**
 * Clase encarfada de correr los diferentes estados q sufre el event engine
 * @author fissban
 */
public class EventEngineTask implements Runnable
{
	@Override
	public void run()
	{
		// Solo registramos aqui los eventos fuera de los eventos, los demas se definiran dentro del mismo evento.
		switch (EventEngineManager.getEventEngineState())
		{
			case REGISTER:
			{
				if (EventEngineManager.getCurrentEvent() != null)
				{
					EventEngineManager.setCurrentEvent(null);

					// Reiniciamos el mapa con los votos
					EventEngineManager.clearVotes();
					// Reiniciamos nuestras instancias
					EventEngineManager.getDinamicInstances().clear();
				}

				announceNextEvent();
				break;
			}
			case RUN_EVENT:
			{
				if (EventEngineManager.getAllRegisterPlayers().isEmpty())
				{
					// Tiempo para el proximo evento en minutos.
					EventEngineManager.setTime(Configs.EVENT_TASK * 60);
					// Volvemos a abrir el registro
					EventEngineManager.setEventEngineState(EventEngineState.REGISTER);

					EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, "Evento cancelado por falta de participantes.");
					EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, "Se vuelve e habilitar el registro");
					break;
				}
				// Averiguamos el evento con mas votos y lo ejecutamos
				EventType event = EventEngineManager.getEventMoreVotes();

				// Iniciamos el evento.
				switch (event)
				{
					case AVA:
						EventEngineManager.setCurrentEvent(new AllVsAll());
						break;

					case CTF:
						EventEngineManager.setCurrentEvent(new CaptureTheFlag());
						break;

					case TVT:
						EventEngineManager.setCurrentEvent(new TeamVsTeam());
						break;
				}

				// Tiempo para el proximo evento en minutos.
				EventEngineManager.setTime(Configs.EVENT_TASK * 60);

				EventEngineManager.setEventEngineState(EventEngineState.RUNNING_EVENT);
				break;
			}
			case RUNNING_EVENT:
				// sin accion
				break;
		}
		EventEngineManager.decreaseTime();
	}

	/**
	 * Anunciamos cuando falta para el proximo Evento.<br>
	 */
	public static void announceNextEvent()
	{
		// Generamos los anuncios dependiendo del tiempo q falta para iniciar
		switch (EventEngineManager.getTime())
		{
			case 1800: // 30 min
				EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, "Event start in 30 minuts!");
				break;
			case 1200: // 20 min
				EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, "Next event start in 20 minuts!");
				break;
			case 600:
				EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, "Next event start in 10 minuts!");
				break;
			case 300:
				EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, "Next event start in 5 minuts!");
				break;
			case 240:
				EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, "Next event start in 4 minuts!");
				break;
			case 180:
				EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, "Next event start in 3 minuts!");
				break;
			case 120:
				EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, "Next event start in 2 minuts!");
				break;
			case 60:
				EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, "Next event start in 1 minuts!");
				break;
			case 10:
				EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, "Event start in 10 seconds!");
				break;
			case 5:
				EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, "Event start in 5 seconds!");
				break;
			case 4:
				EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, "Event start in 4 seconds!");
				break;
			case 3:
				EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, "Event start in 3 seconds!");
				break;
			case 2:
				EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, "Event start in 2 seconds!");
				break;
			case 1:
				EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, "Event start in 1 seconds!");
				break;
			case 0:
				EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, "¡¡¡ Event START !!!");
				// Indicamos q un evento comenzara a correr
				EventEngineManager.setEventEngineState(EventEngineState.RUN_EVENT);
				break;
			case -1:
				// Indicamos q un evento comenzara a correr
				EventEngineManager.setEventEngineState(EventEngineState.RUN_EVENT);// FIXME Solo por si se saltea el paso anterior.
				break;
		}
	}
}
