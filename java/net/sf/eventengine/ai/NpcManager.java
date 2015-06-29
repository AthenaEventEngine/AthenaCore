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
package net.sf.eventengine.ai;

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
		StringBuilder sb = new StringBuilder();
		
		sb.append("<html><body>");
		sb.append("<center>");
		
		sb.append("<font color=\"LEVEL\">Event Manager</font><br>");
		
		sb.append("Bienvenido <font color=\"LEVEL\">" + player.getName() + "</font><br>");
		
		if (EventEngineManager.isOpenRegister())
		{
			sb.append("Registrate en nuestro proximo evento.<br>");
			sb.append("<button value=Register action=\"bypass -h Quest NpcManager register\" width=120 height=30 back=L2UI_CH3.bigbutton2_down fore=L2UI_CH3.bigbutton2><br>");
			sb.append("<button value=Unregister action=\"bypass -h Quest NpcManager unregister\" width=120 height=30 back=L2UI_CH3.bigbutton2_down fore=L2UI_CH3.bigbutton2><br>");
		}
		else
		{
			sb.append("Aun no esta habilitado<br>");
			sb.append("el registro para los eventos<br>");
		}
		
		sb.append("Cual es el proximo evento?<br>");
		sb.append("<button value=NextEvent action=\"bypass -h Quest NpcManager nextevent\" width=120 height=30 back=L2UI_CH3.bigbutton2_down fore=L2UI_CH3.bigbutton2><br>");
		
		if (EventEngineManager.isOpenVote())
		{
			sb.append("Vota por el proximo evento.<br>");
			sb.append("<button value=AVA action=\"bypass -h Quest NpcManager vote AVA\" width=120 height=30 back=L2UI_CH3.bigbutton2_down fore=L2UI_CH3.bigbutton2><br>");
			sb.append("<button value=TVT action=\"bypass -h Quest NpcManager vote TVT\" width=120 height=30 back=L2UI_CH3.bigbutton2_down fore=L2UI_CH3.bigbutton2><br>");
			sb.append("<button value=CTF action=\"bypass -h Quest NpcManager vote CTF\" width=120 height=30 back=L2UI_CH3.bigbutton2_down fore=L2UI_CH3.bigbutton2><br>");
		}
		else
		{
			sb.append("<br><br><br><br>Aun no esta habilitado el sistema de votos<br>");
		}
		
		sb.append("Averigua cuales son los rewards de nuestros eventos.<br>");
		sb.append("<button value=Rewards action=\"bypass -h Quest NpcManager rewards\" width=120 height=30 back=L2UI_CH3.bigbutton2_down fore=L2UI_CH3.bigbutton2><br>");
		
		sb.append("</center>");
		sb.append("</body></html>");
		
		return sb.toString();
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append("<html><body>");
		sb.append("<center><font color=\"LEVEL\">Event Manager</font></center><br><br>");
		sb.append("<center>");
		
		switch (event)
		{
			case "vote TVT":
				EventEngineManager.increaseVote(player, EventType.TVT);
				sb.append("-- Estadisticas --<br>");
				sb.append("<font color=\"LEVEL\">Total de votos por Evento.</font><br>");
				synchronized (EventEngineManager.getAllCurrentVotesInEvents())
				{
					sb.append("<font color=\"LEVEL\">TVT: </font>" + EventEngineManager.getCurrentVotesInEvent(EventType.TVT) + "<br>");
					sb.append("<font color=\"LEVEL\">AVA: </font>" + EventEngineManager.getCurrentVotesInEvent(EventType.AVA) + "<br>");
					sb.append("<font color=\"LEVEL\">CTF: </font>" + EventEngineManager.getCurrentVotesInEvent(EventType.CTF) + "<br>");
				}
				break;
			
			case "vote AVA":
				EventEngineManager.increaseVote(player, EventType.AVA);
				sb.append("-- Estadisticas --<br>");
				sb.append("<font color=\"LEVEL\">Total de votos por Evento.</font><br>");
				synchronized (EventEngineManager.getAllCurrentVotesInEvents())
				{
					sb.append("<font color=\"LEVEL\">TVT: </font>" + EventEngineManager.getCurrentVotesInEvent(EventType.TVT) + "<br>");
					sb.append("<font color=\"LEVEL\">AVA: </font>" + EventEngineManager.getCurrentVotesInEvent(EventType.AVA) + "<br>");
					sb.append("<font color=\"LEVEL\">CTF: </font>" + EventEngineManager.getCurrentVotesInEvent(EventType.CTF) + "<br>");
				}
				break;
			
			case "vote CTF":
				EventEngineManager.increaseVote(player, EventType.CTF);
				sb.append("-- Estadisticas --<br>");
				synchronized (EventEngineManager.getAllCurrentVotesInEvents())
				{
					sb.append("<font color=\"LEVEL\">TVT: </font>" + EventEngineManager.getCurrentVotesInEvent(EventType.TVT) + "<br>");
					sb.append("<font color=\"LEVEL\">AVA: </font>" + EventEngineManager.getCurrentVotesInEvent(EventType.AVA) + "<br>");
					sb.append("<font color=\"LEVEL\">CTF: </font>" + EventEngineManager.getCurrentVotesInEvent(EventType.CTF) + "<br>");
				}
				break;
			
			case "register":
				if (EventEngineManager.registerPlayer(player))
				{
					sb.append("<br><br><br><br><font color=\"LEVEL\">Registro exitoso!</font>");
				}
				else
				{
					sb.append("<br><br><br><br><font color=\"LEVEL\">Ya estabas registrado!</font>");
				}
				break;
			
			case "unregister":
				if (EventEngineManager.unRegisterPlayer(player))
				{
					sb.append("<br><br><br><br><font color=\"LEVEL\">No estabas registrado!</font>");
				}
				else
				{
					sb.append("<br><br><br><br><font color=\"LEVEL\">Tu registro fue borrado!</font>");
				}
				break;
			
			case "rewards":
				sb.append("<br><font color=\"LEVEL\">-=[ TVT WIN ]=-</font><br>");
				for (ItemHolder holder : Configs.TVT_REWARD_TEAM_WIN)
				{
					L2Item item = ItemTable.getInstance().getTemplate(holder.getId());
					sb.append("<font color=\"LEVEL\">Reward -> </font>" + item.getName() + " Count -> " + holder.getCount() + "<br1>");
				}
				
				sb.append("<br><br><font color=\"LEVEL\">-=[ TVT LOSE ]=-</font><br>");
				for (ItemHolder holder : Configs.TVT_REWARD_TEAM_LOSE)
				{
					L2Item item = ItemTable.getInstance().getTemplate(holder.getId());
					sb.append("<font color=\"LEVEL\">Reward -> </font>" + item.getName() + " Count -> " + holder.getCount() + "<br1>");
				}
				
				sb.append("<br><font color=\"LEVEL\">-=[ AVA WIN ]=-</font><br>");
				for (ItemHolder holder : Configs.AVA_REWARD_PLAYER_WIN)
				{
					L2Item item = ItemTable.getInstance().getTemplate(holder.getId());
					sb.append("<font color=\"LEVEL\">Reward -> </font>" + item.getName() + " Count -> " + holder.getCount() + "<br1>");
				}
				
				sb.append("<br><br><font color=\"LEVEL\">-=[ AVA LOSE ]=-</font><br>");
				for (ItemHolder holder : Configs.AVA_REWARD_PLAYER_LOSE)
				{
					L2Item item = ItemTable.getInstance().getTemplate(holder.getId());
					sb.append("<font color=\"LEVEL\">Reward -> </font>" + item.getName() + " Count -> " + holder.getCount() + "<br1>");
				}
				
				sb.append("<br><font color=\"LEVEL\">-=[ CTF WIN ]=-</font><br>");
				// for (ItemHolder holder : Configs.CTF_REWARD_PLAYER_WIN)
				// {
				// L2Item item = ItemTable.getInstance().getTemplate(holder.getId());
				// sb.append("<font color=\"LEVEL\">Reward -> </font>" + item.getName() + " Count -> " + holder.getCount() + "<br>");
				// }
				//
				// sb.append("<br><br><font color=\"LEVEL\">-=[ CTF LOSE ]=-</font>");
				// for (ItemHolder holder : Configs.CTF_REWARD_PLAYER_LOSE)
				// {
				// L2Item item = ItemTable.getInstance().getTemplate(holder.getId());
				// sb.append("<font color=\"LEVEL\">Reward -> </font>" + item.getName() + " Count -> " + holder.getCount() + "<br>");
				// }
				
			case "nextevent":
				// TODO sin desarrollar
				break;
		}
		
		sb.append("</center>");
		sb.append("</body></html>");
		
		return sb.toString();
	}
}
