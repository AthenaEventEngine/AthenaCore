/*
 * Copyright (C) 2015-2016 L2J EventEngine
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
package com.github.u3games.eventengine.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.github.u3games.eventengine.EventEngineManager;
import com.github.u3games.eventengine.events.holders.PlayerHolder;
import com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * @author Synerge
 */
public class EventParticipantStatus extends L2GameServerPacket
{
	private EventState _eventState = null;
	private int _pointsBlue = 0;
	private int _pointsRed = 0;
	private List<PlayerHolder> _teamBlue = new ArrayList<>();
	private List<PlayerHolder> _teamRed = new ArrayList<>();
	
	// TODO por el momento solo esta habilitado el estado "PVP_KILL"
	public enum EventState
	{
		TOTAL, //
		TOWER_DESTROY,
		CATEGORY_UPDATE,
		RESULT,
		PVP_KILL
	}
	
	/**
	 * Aqui mostramos cuantos puntos lleva cada team y el tiempo q falta para finalizar el evento
	 * @param pointsRed
	 * @param pointsBlue
	 * @param eventState
	 */
	public EventParticipantStatus(int pointsRed, int pointsBlue)
	{
		_pointsRed = pointsRed;
		_pointsBlue = pointsBlue;
		_eventState = EventState.PVP_KILL;
	}
	
	/**
	 * Aqui mostramos el resultado final del evento en una nueva ventana
	 * @param pointsRed
	 * @param pointsBlue
	 * @param eventState
	 */
	public EventParticipantStatus(int pointsRed, List<PlayerHolder> teamBlue, int pointsBlue, List<PlayerHolder> teamRed)
	{
		_pointsRed = pointsRed;
		_teamRed = teamRed;
		_pointsBlue = pointsBlue;
		_teamBlue = teamBlue;
		_eventState = EventState.TOTAL;
	}
	
	public EventParticipantStatus()
	{
		_eventState = EventState.TOTAL;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0x95);
		writeD(_eventState.ordinal());
		
		switch (_eventState)
		{
			case TOTAL:
				writeD(EventEngineManager.getInstance().getTime());
				writeD(_pointsBlue);
				writeD(_pointsRed);
				writeD(1); // Equipo 1
				writeD(2); // Equipo 2
				writeS("Blue Team");
				writeS("Red Team");
				
				writeD(_teamBlue.size());
				for (PlayerHolder info : _teamBlue)
				{
					writeD(info.getPcInstance().getObjectId());
					writeD(info.getKills());
					writeD(info.getDeaths());
					writeD(0x00);// special kills
				}
				
				writeD(_teamRed.size());
				for (PlayerHolder info : _teamRed)
				{
					writeD(info.getPcInstance().getObjectId());
					writeD(info.getKills());
					writeD(info.getDeaths());
					writeD(0x00);// special kills
				}
				break;
			case TOWER_DESTROY:
				break;
			case CATEGORY_UPDATE:
				break;
			case RESULT:
				break;
			case PVP_KILL:
				writeD(EventEngineManager.getInstance().getTime());
				writeD(_pointsBlue);
				writeD(_pointsRed);
				
				writeD(1);// teamId
				writeD(0x00);// playerObjectId
				writeD(0x00);// kills
				writeD(0x00);// deaths
				writeD(0x00);// special kills
				
				writeD(2);// teamId
				writeD(0x00);// playerObjectId
				writeD(0x00);// kills
				writeD(0x00);// deaths
				writeD(0x00);// special kills
				break;
		}
	}
}