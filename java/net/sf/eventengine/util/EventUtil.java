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
	/**
	 * Enviamos un mensaje a un personaje dentro del evento
	 * @param player
	 * @param text
	 */
	public static void sendEventMessage(PlayerHolder player, String text)
	{
		player.getPcInstance().sendPacket(new CreatureSay(0, Say2.PARTYROOM_COMMANDER, "", "[EventEngine]" + text));
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
			player.sendPacket(new CreatureSay(0, say2, "", "[EventEngine]" + text));
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
			player.sendPacket(new CreatureSay(0, say2, "", "[EventEngine]" + text));
		}
	}

	/**
	 * Generamos el ID de la imagen de un skill a partir de su ID
	 * @param id
	 * @return
	 */
	private static String getSkillIcon(int id)
	{
		String formato;
		if (id == 4400)
		{
			formato = "1035";
		}
		else if (id == 4401)
		{
			formato = "1048";
		}
		else if (id == 4402)
		{
			formato = "1062";
		}
		else if (id == 4403)
		{
			formato = "1204";
		}
		else if (id == 4404)
		{
			formato = "1045";
		}
		else if (id == 4405)
		{
			formato = "1036";
		}
		else if (id == 4406)
		{
			formato = "1040";
		}
		else if (id == 4407)
		{
			formato = "1048";
		}
		else if (id == 4408)
		{
			formato = "1303";
		}
		else if (id == 4409)
		{
			formato = "1059";
		}
		else if (id == 4410)
		{
			formato = "1085";
		}
		else if (id == 4411)
		{
			formato = "1240";
		}
		else if (id == 4412)
		{
			formato = "1242";
		}
		else if (id == 4413)
		{
			formato = "1068";
		}
		else if (id == 4414)
		{
			formato = "1077";
		}
		else if (id == 4415)
		{
			formato = "1086";
		}
		else if (id == 4416)
		{
			formato = "1268";
		}
		else if (id == 4417)
		{
			formato = "1243";
		}
		else if (id == 4418)
		{
			formato = "1304";
		}
		else if (id == 4419)
		{
			formato = "1259";
		}
		else if (id == 4420)
		{
			formato = "1087";
		}
		else if (id == 4421)
		{
			formato = "0269";
		}
		else if (id == 4422)
		{
			formato = "0304";
		}
		else if (id == 4423)
		{
			formato = "0264";
		}
		else if (id == 4424)
		{
			formato = "0267";
		}
		else if (id == 4425)
		{
			formato = "0268";
		}
		else if (id == 4426)
		{
			formato = "0273";
		}
		else if (id == 4427)
		{
			formato = "0276";
		}
		else if (id == 4428)
		{
			formato = "0274";
		}
		else if (id == 4429)
		{
			formato = "0275";
		}
		else if (id == 4430)
		{
			formato = "0271";
		}
		else if (id == 4431)
		{
			formato = "0310";
		}
		else if (id == 4436)
		{
			formato = "0364";
		}
		else if (id == 4437)
		{
			formato = "0349";
		}
		else if (id == 4438)
		{
			formato = "0365";
		}
		else if (id == 4440)
		{
			formato = "1355";
		}
		else if (id == 4441)
		{
			formato = "1356";
		}
		else if (id == 4442)
		{
			formato = "1357";
		}
		else if (id == 4443)
		{
			formato = "1363";
		}
		else
		{
			formato = String.valueOf(id);
		}
		return formato;
	}
}
