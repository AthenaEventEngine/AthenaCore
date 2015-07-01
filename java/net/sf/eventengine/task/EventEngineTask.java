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
package net.sf.eventengine.task;

import java.util.ArrayList;

import net.sf.eventengine.EventEngineManager;
import net.sf.eventengine.configs.Configs;
import net.sf.eventengine.enums.EventEngineState;
import net.sf.eventengine.enums.EventType;
import net.sf.eventengine.events.AllVsAll;
import net.sf.eventengine.events.CaptureTheFlag;
import net.sf.eventengine.events.OneVsOne;
import net.sf.eventengine.events.Survive;
import net.sf.eventengine.events.TeamVsTeam;
import net.sf.eventengine.handler.MsgHandler;
import net.sf.eventengine.util.EventUtil;

import com.l2jserver.gameserver.network.clientpackets.Say2;

/**
 * Clase encarfada de correr los diferentes estados q sufre el event engine
 * @author fissban
 */
public class EventEngineTask implements Runnable
{
	/**
	 * Multi-Language System Contiene los tiempos para anunciar lo que falta para iniciar el evento
	 */
	private static final ArrayList<Integer> TIME_LEFT_TO_ANNOUNCE = new ArrayList<>();
	{
		TIME_LEFT_TO_ANNOUNCE.add(1800);
		TIME_LEFT_TO_ANNOUNCE.add(1200);
		TIME_LEFT_TO_ANNOUNCE.add(600);
		TIME_LEFT_TO_ANNOUNCE.add(300);
		TIME_LEFT_TO_ANNOUNCE.add(240);
		TIME_LEFT_TO_ANNOUNCE.add(120);
		TIME_LEFT_TO_ANNOUNCE.add(60);
		TIME_LEFT_TO_ANNOUNCE.add(10);
		TIME_LEFT_TO_ANNOUNCE.add(5);
		TIME_LEFT_TO_ANNOUNCE.add(4);
		TIME_LEFT_TO_ANNOUNCE.add(3);
		TIME_LEFT_TO_ANNOUNCE.add(2);
		TIME_LEFT_TO_ANNOUNCE.add(1);
	}
	
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
					EventEngineManager.getInstancesWorlds().clear();
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
					
					// Messages
					EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, MsgHandler.getMsg("event_aborted"));
					EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, MsgHandler.getMsg("event_registration_on"));
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
					
					case OVO:
						EventEngineManager.setCurrentEvent(new OneVsOne());
						break;
					
					case SURVIVE:
						EventEngineManager.setCurrentEvent(new Survive());
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
		int time = EventEngineManager.getTime();
		if (TIME_LEFT_TO_ANNOUNCE.contains(EventEngineManager.getTime()))
		{
			if (time > 60)
			{
				EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, MsgHandler.getMsg("event_start_time") + " " + (time / 60) + " " + MsgHandler.getMsg("time_minutes"));
			}
			else
			{
				EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, MsgHandler.getMsg("event_start_time") + " " + time + " " + MsgHandler.getMsg("time_seconds"));
			}
		}
		else if (time == 0)
		{
			EventUtil.announceToAllPlayers(Say2.CRITICAL_ANNOUNCE, MsgHandler.getMsg("event_started"));
			// Indicamos q un evento comenzara a correr
			EventEngineManager.setEventEngineState(EventEngineState.RUN_EVENT);
		}
		else if (time == -1)
		{
			// FIXME: Solo por si se saltea el paso anterior.
			EventEngineManager.setEventEngineState(EventEngineState.RUN_EVENT);
		}
	}
}
