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
package net.sf.eventengine.util;

import net.sf.eventengine.holder.PlayerHolder;

import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.clientpackets.Say2;
import com.l2jserver.gameserver.network.serverpackets.CreatureSay;
import com.l2jserver.gameserver.network.serverpackets.ExEventMatchMessage;
import com.l2jserver.gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * @author fissban
 */
public class EventUtil
{
	public static final String ENGINE_TAG = "[EventEngine] ";
	
	/**
	 * Enviamos un mensaje a un personaje dentro del evento
	 * @param player
	 * @param text
	 */
	public static void sendEventMessage(PlayerHolder player, String text)
	{
		player.getPcInstance().sendPacket(new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "", ENGINE_TAG + text));
	}
	
	/**
	 * Create an event match message. type
	 * @param type -> 0 - gm, 1 - finish, 2 - start, 3 - game over, 4 - 1, 5 - 2, 6 - 3, 7 - 4, 8 - 5
	 * @param msg ->
	 */
	public static void sendEventSpecialMessage(PlayerHolder player, int type, String msg)
	{
		player.getPcInstance().sendPacket(new ExEventMatchMessage(type, msg));
	}
	
	/**
	 * Enviamos un mensaje por pantalla a un personaje dentro del evento
	 * @param player
	 * @param text
	 */
	public static void sendEventScreenMessage(PlayerHolder player, String text)
	{
		player.getPcInstance().sendPacket(new ExShowScreenMessage(text, 2000));
	}
	
	/**
	 * Enviamos un mensaje por pantalla a un personaje dentro del evento
	 * @param player
	 * @param text
	 * @param time
	 */
	public static void sendEventScreenMessage(PlayerHolder player, String text, int time)
	{
		player.getPcInstance().sendPacket(new ExShowScreenMessage(text, time));
	}
	
	/**
	 * Enviamos un mensaje a todos los usuarios que participan de los eventos
	 * @param say2
	 * @param text
	 */
	public static void announceToAllPlayersInEvent(int say2, String text)
	{
		for (L2PcInstance player : L2World.getInstance().getPlayers())
		{
			player.sendPacket(new CreatureSay(0, say2, "", ENGINE_TAG + text));
		}
	}
	
	/**
	 * Enviamos un mensaje a todos los usuarios del servidor
	 * @param say2
	 * @param text
	 */
	public static void announceToAllPlayers(int say2, String text)
	{
		for (L2PcInstance player : L2World.getInstance().getPlayers())
		{
			player.sendPacket(new CreatureSay(0, say2, "", ENGINE_TAG + text));
		}
	}
}
