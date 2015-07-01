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
package net.sf.eventengine.ai;

import java.util.List;
import java.util.StringTokenizer;

import net.sf.eventengine.EventEngineManager;
import net.sf.eventengine.configs.Configs;
import net.sf.eventengine.enums.EventType;

import com.l2jserver.gameserver.datatables.ItemTable;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.quest.Quest;

/**
 * @author fissban
 */
public class NpcManager extends Quest
{
	private static int NPC = Configs.NPC_MANAGER_ID;
	
	public NpcManager()
	{
		super(-1, NpcManager.class.getSimpleName(), "EventEngine");
		
		addStartNpc(NPC);
		addFirstTalkId(NPC);
		addTalkId(NPC);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return index(player);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("<html><body>");
		sb.append("<center>");
		
		StringTokenizer st = new StringTokenizer(event, " ");
		
		switch (st.nextToken())
		{
			case "index":
				return index(player);
				
			case "vote":
				// Incrementamos en uno el voto del evento seleccionado por el personaje
				EventEngineManager.increaseVote(player, EventType.valueOf(st.nextToken()));
				return index(player);
				
			case "info":
				List<ItemHolder> holderWin = null;
				List<ItemHolder> holderLose = null;
				
				switch (EventType.valueOf(st.nextToken()))
				{
					case AVA:
						sb.append("<font name=hs12 color=LEVEL>All Vs All</font><br><br>");
						holderWin = Configs.AVA_REWARD_PLAYER_WIN;
						holderLose = Configs.AVA_REWARD_PLAYER_LOSER;
						break;
					case TVT:
						sb.append("<font name=hs12 color=LEVEL>Team Vs Team</font><br><br>");
						holderWin = Configs.TVT_REWARD_TEAM_WIN;
						holderLose = Configs.TVT_REWARD_TEAM_LOSER;
						break;
					case CTF:
						sb.append("<font name=hs12 color=LEVEL>Capture The Flag</font><br><br>");
						// TODO ajustar los configs una ves creados
						holderWin = Configs.TVT_REWARD_TEAM_WIN;
						holderLose = Configs.TVT_REWARD_TEAM_LOSER;
						break;
					case OVO:
						sb.append("<font name=hs12 color=LEVEL>One Vs One</font><br><br>");
						holderWin = Configs.OVO_REWARD_PLAYER_WIN;
						holderLose = Configs.OVO_REWARD_PLAYER_LOSER;
						break;
					case SURVIVE:
						sb.append("<font name=hs12 color=LEVEL>Survive</font><br<br>>");
						holderWin = Configs.SURVIVE_REWARD_PLAYER_WIN;
						holderLose = Configs.SURVIVE_REWARD_PLAYER_LOSER;
						break;
				}
				
				sb.append("<font name=hs12 color=00FF3C>WINNER</font><br1>");
				sb.append("<table bgcolor=E9E9E9>");
				sb.append("<tr>");
				sb.append("<td align=center width=32></td>");
				sb.append("<td align=center width=120>name</td>");
				sb.append("<td align=center width=50>count</td>");
				sb.append("</tr>");
				sb.append("</table>");
				
				int color = 0;
				for (ItemHolder holder : holderWin)
				{
					sb.append("<table bgcolor=" + colorTable(color) + ">");
					L2Item item = ItemTable.getInstance().getTemplate(holder.getId());
					sb.append("<tr>");
					sb.append("<td align=center valign=center width=32 height=32><img src=" + item.getIcon() + " width=32 height=32></td>");
					sb.append("<td align=center width=120 height=32><font name=hs12>" + item.getName() + "</font></td>");
					sb.append("<td align=center width=50 height=32><font name=hs12>" + holder.getCount() + "</font></td>");
					sb.append("</tr>");
					sb.append("</table>");
					color++;
				}
				
				sb.append("<br>");
				
				sb.append("<font name=hs12 color=FF0000>LOSER</font><br1>");
				sb.append("<table bgcolor=E9E9E9>");
				sb.append("<tr>");
				sb.append("<td align=center width=32></td>");
				sb.append("<td align=center width=120>name</td>");
				sb.append("<td align=center width=50>count</td>");
				sb.append("</tr>");
				sb.append("</table>");
				color = 0;
				for (ItemHolder holder : holderLose)
				{
					
					L2Item item = ItemTable.getInstance().getTemplate(holder.getId());
					sb.append("<table bgcolor=" + colorTable(color) + ">");
					sb.append("<tr>");
					sb.append("<td align=center valign=center width=32 height=32><img src=" + item.getIcon() + " width=32 height=32></td>");
					sb.append("<td align=center width=120 height=32><font name=hs12>" + item.getName() + "</font></td>");
					sb.append("<td align=center width=50 height=32><font name=hs12>" + holder.getCount() + "</font></td>");
					sb.append("</tr>");
					sb.append("</table>");
					color++;
				}
				
				break;
			case "register":
				if (EventEngineManager.registerPlayer(player))
				{
					sb.append("<br><br><br><br><font color=LEVEL>Registro exitoso!</font>");
					sb.append("<button value=Volver action=\"bypass -h Quest " + NpcManager.class.getSimpleName() + " index\" width=90 height=21 back=L2UI_CT1.Button_DF_Down fore=L2UI_CT1.Button_DF><br>");
				}
				else
				{
					sb.append("<br><br><br><br><font color=LEVEL>Ya estabas registrado!</font>");
					sb.append("<button value=Volver action=\"bypass -h Quest " + NpcManager.class.getSimpleName() + " index\" width=90 height=21 back=L2UI_CT1.Button_DF_Down fore=L2UI_CT1.Button_DF><br>");
				}
				break;
			
			case "unregister":
				if (EventEngineManager.unRegisterPlayer(player))
				{
					sb.append("<br><br><br><br><font color=LEVEL>No estabas registrado!</font>");
				}
				else
				{
					sb.append("<br><br><br><br><font color=LEVEL>Tu registro fue borrado!</font>");
				}
				break;
		}
		
		sb.append("</center>");
		sb.append("</body></html>");
		
		return sb.toString();
	}
	
	/**
	 * Generamos el html index del npc<br>
	 * HARDCODE -> se puede generar un html
	 * @param player
	 * @return
	 */
	private static String index(L2PcInstance player)
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("<html><body>");
		sb.append("<center>");
		sb.append("Bienvenido <font color=LEVEL>" + player.getName() + "</font><br>");
		
		if (EventEngineManager.isOpenRegister())
		{
			if (EventEngineManager.getAllRegisterPlayers().contains(player.getObjectId()))
			{
				sb.append("Cancela tu registro del proximo evento.<br>");
				sb.append("<button value=Unregister action=\"bypass -h Quest " + NpcManager.class.getSimpleName() + " unregister\" width=65 height=21 back=L2UI_CT1.Button_DF_Down fore=L2UI_CT1.Button_DF>");
			}
			else
			{
				sb.append("Registrate en nuestro proximo evento.<br>");
				sb.append("<button value=Register action=\"bypass -h Quest " + NpcManager.class.getSimpleName() + " register\" width=65 height=21 back=L2UI_CT1.Button_DF_Down fore=L2UI_CT1.Button_DF>");
			}
			sb.append("<br>");
		}
		else
		{
			sb.append("Aun no esta habilitado<br1>");
			sb.append("el registro para los eventos<br>");
		}
		
		if (EventEngineManager.isOpenVote())
		{
			sb.append("<font color=LEVEL>Vota por el proximo evento!.</font><br>");
			sb.append("El evento que consiga<br1>");
			sb.append("mayor cantidad de votos<br1>");
			sb.append("sera el proximo en ejecutarse.<br>");
			sb.append("<table width=100% cellspacing=1 cellpadding=2 bgcolor=111111>");
			
			// Generamos una tabla con:
			// -> un boton para votar por el evento.
			// -> la cantidad de votos q tiene dicho evento.
			// -> un link para poder ver mas info del mismo.
			for (EventType event : EventType.values())
			{
				sb.append("<tr>");
				sb.append("<td align=center width=30% height=30><button value=\"" + event.getEventName() + "\" action=\"bypass -h Quest " + NpcManager.class.getSimpleName() + " vote " + event.toString() + "\" width=110 height=21 back=L2UI_CT1.Button_DF_Down fore=L2UI_CT1.Button_DF></td>");
				sb.append("<td width=40%><font color=LEVEL>votos: </font>" + EventEngineManager.getCurrentVotesInEvent(event) + "</td>");
				sb.append("<td width=30%><font color=7898AF><a action=\"bypass -h Quest " + NpcManager.class.getSimpleName() + " info " + event.toString() + "\">info</a></font></td>");
				sb.append("</tr>");
			}
			
			sb.append("</table>");
		}
		else
		{
			sb.append("<br><br>");
			sb.append("No puedes registrarte mientra tenemos<br1>");
			sb.append("un evento en curso.");
		}
		
		sb.append("</center>");
		sb.append("</body></html>");
		
		return sb.toString();
	}
	
	/**
	 * Agradecemos al player por votar
	 * @return
	 */
	public String votes()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Gracias por votar!<br>");
		sb.append("<button value=Volver action=\"bypass -h Quest " + NpcManager.class.getSimpleName() + " index\" width=90 height=21 back=L2UI_CT1.Button_DF_Down fore=L2UI_CT1.Button_DF><br>");
		return sb.toString();
	}
	
	/**
	 * Podemos asignar un color a la tabla dependiendo si es par o impar
	 * @param color
	 * @return
	 */
	private String colorTable(int color)
	{
		if ((color % 2) == 0)
		{
			return "8B4513";
		}
		return "291405";
	}
}