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

import com.l2jserver.gameserver.network.clientpackets.Say2;

import net.sf.eventengine.EventEngineManager;
import net.sf.eventengine.enums.CollectionTarget;
import net.sf.eventengine.enums.EventEngineState;
import net.sf.eventengine.enums.EventState;
import net.sf.eventengine.holder.PlayerHolder;
import net.sf.eventengine.util.EventUtil;

/**
 * Clase encargada de correr los diferentes estados q sufren los diferentes eventos
 * @author fissban
 */
public class EventTask implements Runnable
{
	private int _step = 0;
	
	public EventTask(int step)
	{
		_step = step;
	}
	
	@Override
	public void run()
	{
		switch (_step)
		{
			case 1:
				// Anunciamos a los players q pronto seran teletransportados
				EventUtil.announceTo(Say2.CRITICAL_ANNOUNCE, "teleport_seconds", CollectionTarget.ALL_PLAYERS_IN_EVENT);
				break;
				
			case 2:
				/** Se ejecutan acciones dentro de cada evento */
				EventEngineManager.getInstance().getCurrentEvent().runEventState(EventState.START);
				break;
				
			case 3:
				/** Se ejecutan acciones dentro de cada evento */
				EventEngineManager.getInstance().getCurrentEvent().runEventState(EventState.FIGHT);
				
				// Enviamos un mensaje especial para los participantes
				for (PlayerHolder player : EventEngineManager.getInstance().getCurrentEvent().getAllEventPlayers())
				{
					EventUtil.sendEventSpecialMessage(player, 2, "status_started");
				}
				break;
				
			case 4:
				/** Se ejecutan acciones dentro de cada evento */
				EventEngineManager.getInstance().getCurrentEvent().runEventState(EventState.END);
				
				// Borramos todos los spawns de npc
				EventEngineManager.getInstance().getCurrentEvent().removeAllEventNpc();
				
				// Enviamos un mensaje especial para los participantes
				for (PlayerHolder player : EventEngineManager.getInstance().getCurrentEvent().getAllEventPlayers())
				{
					EventUtil.sendEventSpecialMessage(player, 1, "status_finished");
				}
				break;
				
			case 5:
				// Volvemos a habilitar el registro
				EventEngineManager.getInstance().setEventEngineState(EventEngineState.EVENT_ENDED);
				break;
		}
	}
}
