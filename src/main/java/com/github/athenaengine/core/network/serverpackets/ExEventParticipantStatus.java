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
package com.github.athenaengine.core.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.github.athenaengine.core.enums.ScoreType;
import com.github.athenaengine.core.EventEngineManager;
import com.github.athenaengine.core.model.entity.Player;
import com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * @author Synerge
 */
public class ExEventParticipantStatus extends L2GameServerPacket
{
	private EventState _eventState = null;
	private int _pointsBlue = 0;
	private int _pointsRed = 0;
	private List<Player> _teamBlue = new ArrayList<>();
	private List<Player> _teamRed = new ArrayList<>();
	
	// TODO for the moment only it enabled the state "PVP KILL"
	public enum EventState
	{
		TOTAL,
		TOWER_DESTROY,
		CATEGORY_UPDATE,
		RESULT,
		PVP_KILL
	}
	
	/**
	 * Here we show how many points each team takes and the time remaining to finish the event.
	 * @param pointsRed
	 * @param pointsBlue
	 */
	public ExEventParticipantStatus(int pointsRed, int pointsBlue)
	{
		_pointsRed = pointsRed;
		_pointsBlue = pointsBlue;
		_eventState = EventState.PVP_KILL;
	}
	
	/**
	 * Here we report the final result of the event in a new window.
	 * @param pointsRed
	 * @param teamBlue
	 * @param pointsBlue
	 * @param teamRed
	 */
	public ExEventParticipantStatus(int pointsRed, List<Player> teamBlue, int pointsBlue, List<Player> teamRed)
	{
		_pointsRed = pointsRed;
		_teamRed = teamRed;
		_pointsBlue = pointsBlue;
		_teamBlue = teamBlue;
		_eventState = EventState.TOTAL;
	}
	
	public ExEventParticipantStatus()
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
				for (Player info : _teamBlue)
				{
					writeD(info.getObjectId());
					writeD(info.getPoints(ScoreType.KILL));
					writeD(info.getPoints(ScoreType.DEATH));
					writeD(0x00);// special kills
				}
				
				writeD(_teamRed.size());
				for (Player info : _teamRed)
				{
					writeD(info.getObjectId());
					writeD(info.getPoints(ScoreType.KILL));
					writeD(info.getPoints(ScoreType.DEATH));
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