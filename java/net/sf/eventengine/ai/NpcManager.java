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

import java.util.Map;
import java.util.StringTokenizer;

import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.holders.SkillHolder;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jserver.util.StringUtil;

import net.sf.eventengine.EventEngineManager;
import net.sf.eventengine.datatables.BuffListData;
import net.sf.eventengine.datatables.ConfigData;
import net.sf.eventengine.datatables.EventData;
import net.sf.eventengine.datatables.MessageData;
import net.sf.eventengine.events.handler.AbstractEvent;

/**
 * @author swarlog, Zephyr, fissban
 */
public class NpcManager extends Quest
{
	private static final int MAX_BUFF_PAGE = 12;
	
	public NpcManager()
	{
		super(-1, NpcManager.class.getSimpleName(), "EventEngine");
		
		addStartNpc(ConfigData.getInstance().NPC_MANAGER_ID);
		addFirstTalkId(ConfigData.getInstance().NPC_MANAGER_ID);
		addTalkId(ConfigData.getInstance().NPC_MANAGER_ID);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		sendHtmlIndex(player);
		return null;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		StringTokenizer st = new StringTokenizer(event, " ");
		
		switch (st.nextToken())
		{
			case "index":
				sendHtmlIndex(player);
				break;
				
			case "engine":
				final NpcHtmlMessage html = new NpcHtmlMessage();
				html.setFile(player.getHtmlPrefix(), "data/html/events/event_engine.htm");
				html.replace("%buttonMain%", MessageData.getInstance().getMsgByLang(player, "button_main", false));
				player.sendPacket(html);
				break;
				
			case "vote":
				// Add vote event
				Class<? extends AbstractEvent> type = EventData.getInstance().getEvent(st.nextToken());
				if (type != null)
				{
					EventEngineManager.getInstance().increaseVote(player, type);
					player.sendMessage(MessageData.getInstance().getMsgByLang(player, "event_vote_done", true));
				}
				sendHtmlIndex(player);
				break;
				
			case "info":
				String eventName = st.nextToken();
				sendHtmlInfo(player, eventName);
				break;
				
			case "register":
				if (!EventEngineManager.getInstance().isRegistered(player))
				{
					// Check for register
					if (player.getLevel() < ConfigData.getInstance().MIN_LVL_IN_EVENT)
					{
						player.sendMessage(MessageData.getInstance().getMsgByLang(player, "registering_lowLevel", true));
					}
					else if (player.getLevel() > ConfigData.getInstance().MAX_LVL_IN_EVENT)
					{
						player.sendMessage(MessageData.getInstance().getMsgByLang(player, "registering_highLevel", true));
					}
					else if (EventEngineManager.getInstance().getAllRegisteredPlayers().size() >= ConfigData.getInstance().MAX_PLAYERS_IN_EVENT)
					{
						player.sendMessage(MessageData.getInstance().getMsgByLang(player, "registering_maxPlayers", true));
					}
					else
					{
						EventEngineManager.getInstance().registerPlayer(player);
						player.sendMessage(MessageData.getInstance().getMsgByLang(player, "registering_registered", true));
					}
				}
				else
				{
					player.sendMessage(MessageData.getInstance().getMsgByLang(player, "registering_already_registered", true));
				}
				
				sendHtmlIndex(player);
				break;
				
			case "unregister":
				if (EventEngineManager.getInstance().isOpenRegister())
				{
					if (EventEngineManager.getInstance().unRegisterPlayer(player))
					{
						player.sendMessage(MessageData.getInstance().getMsgByLang(player, "unregistering_unregistered", true));
					}
					else
					{
						player.sendMessage(MessageData.getInstance().getMsgByLang(player, "unregistering_notRegistered", true));
					}
				}
				else
				{
					player.sendMessage(MessageData.getInstance().getMsgByLang(player, "event_registration_notUnRegState", true));
				}
				
				sendHtmlIndex(player);
				break;
				
			// Multi-Language System menu
			case "menulang":
				sendHtmlLang(player);
				break;
				
			// Multi-Language System set language
			case "setlang":
				String lang = st.nextToken();
				MessageData.getInstance().setLanguage(player, lang);
				player.sendMessage(MessageData.getInstance().getMsgByLang(player, "lang_current_successfully", false) + " " + lang);
				sendHtmlIndex(player);
				break;
				
			case "buffs":
				int page = 1;
				
				if (st.hasMoreTokens())
				{
					page = Integer.parseInt(st.nextToken());
				}
				if (st.hasMoreTokens())
				{
					switch (st.nextToken())
					{
						case "add":
							BuffListData.getInstance().addBuffPlayer(player, new SkillHolder(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken())));
							break;
						case "remove":
							BuffListData.getInstance().deleteBuffPlayer(player, new SkillHolder(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken())));
							break;
					}
				}
				
				sendHtmlBuffList(player, page);
				break;
		}
		
		return "";
	}
	
	private static void sendHtmlInfo(L2PcInstance player, String eventName)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage();
		html.setFile(player.getHtmlPrefix(), "data/html/events/event_info.htm");
		
		// Avoid a vulnerability
		if (EventData.getInstance().getEvent(eventName) != null)
		{
			// Info event
			html.replace("%eventName%", MessageData.getInstance().getMsgByLang(player, "event_" + eventName.toLowerCase() + "_name", false));
			html.replace("%textDescription%", MessageData.getInstance().getMsgByLang(player, "text_description", false));
			html.replace("%eventDescription%", MessageData.getInstance().getMsgByLang(player, "event_" + eventName.toLowerCase() + "_description", false));
			// Requirements
			html.replace("%textRequirements%", MessageData.getInstance().getMsgByLang(player, "text_requirements", false));
			html.replace("%textLevelMax%", MessageData.getInstance().getMsgByLang(player, "text_level_max", false));
			html.replace("%textLevelMin%", MessageData.getInstance().getMsgByLang(player, "text_level_min", false));
			// TODO: replace for max and min from event
			html.replace("%levelMax%", ConfigData.getInstance().MAX_LVL_IN_EVENT);
			html.replace("%levelMin%", ConfigData.getInstance().MIN_LVL_IN_EVENT);
			// Configuration
			html.replace("%textConfiguration%", MessageData.getInstance().getMsgByLang(player, "text_configuration", false));
			html.replace("%textTimeEvent%", MessageData.getInstance().getMsgByLang(player, "text_time_event", false));
			// TODO: replace for duration from event
			html.replace("%timeEvent%", ConfigData.getInstance().EVENT_DURATION);
			html.replace("%timeMinutes%", MessageData.getInstance().getMsgByLang(player, "time_minutes", false));
			// Rewards
			html.replace("%textRewards%", MessageData.getInstance().getMsgByLang(player, "text_rewards", false));
			// Button
			html.replace("%buttonMain%", MessageData.getInstance().getMsgByLang(player, "button_main", false));
		}
		// Send html
		player.sendPacket(html);
	}
	
	private static void sendHtmlLang(L2PcInstance player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage();
		html.setFile(player.getHtmlPrefix(), "data/html/events/event_lang.htm");
		
		// Info menu
		html.replace("%settingTitle%", MessageData.getInstance().getMsgByLang(player, "lang_menu_title", false));
		html.replace("%languageTitle%", MessageData.getInstance().getMsgByLang(player, "lang_language_title", false));
		html.replace("%languageDescription%", MessageData.getInstance().getMsgByLang(player, "lang_language_description", false));
		// Info lang
		html.replace("%currentLanguage%", MessageData.getInstance().getMsgByLang(player, "lang_current_language", false));
		html.replace("%getLanguage%", MessageData.getInstance().getLanguage(player));
		// Buttons
		final StringBuilder langList = new StringBuilder(500);
		for (Map.Entry<String, String> e : MessageData.getInstance().getLanguages().entrySet())
		{
			StringUtil.append(langList, "<tr>");
			StringUtil.append(langList, "<td align=center width=30% height=30><button value=\"" + e.getValue() + "\" action=\"bypass -h Quest " + NpcManager.class.getSimpleName() + " setlang " + e.getKey() + "\" width=70 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			StringUtil.append(langList, "</tr>");
		}
		html.replace("%buttonLang%", langList.toString());
		// Button
		html.replace("%buttonMain%", MessageData.getInstance().getMsgByLang(player, "button_main", false));
		
		// Send html
		player.sendPacket(html);
	}
	
	private static void sendHtmlBuffList(L2PcInstance player, int page)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage();
		html.setFile(player.getHtmlPrefix(), "data/html/events/event_buffs.htm");
		
		html.replace("%buffTitle%", MessageData.getInstance().getMsgByLang(player, "buff_title", false));
		html.replace("%buffDescription%", MessageData.getInstance().getMsgByLang(player, "buff_description", false));
		html.replace("%buffTextCount%", MessageData.getInstance().getMsgByLang(player, "buff_text_count", false));
		html.replace("%buffTextMax%", MessageData.getInstance().getMsgByLang(player, "buff_text_max", false));
		html.replace("%buffCount%", " <font color=LEVEL>" + BuffListData.getInstance().getBuffsPlayer(player).size() + "</font>");
		html.replace("%buffMax%", " <font color=LEVEL>" + ConfigData.MAX_BUFF_COUNT + "</font>");
		html.replace("%buttonMain%", MessageData.getInstance().getMsgByLang(player, "button_main", false));
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("<table>");
		for (int cont = (page - 1) * MAX_BUFF_PAGE; cont < page * MAX_BUFF_PAGE; cont++)
		{
			SkillHolder sh = BuffListData.getInstance().getAllBuffs().get(cont);
			
			sb.append("<tr>");
			sb.append("<td width=32 height=32><img src=" + sh.getSkill().getIcon() + " width=32 height=32></td>");
			sb.append("<td width=130><font color=LEVEL>" + sh.getSkill().getName() + "</font><br></td>");
			
			if (!BuffListData.getInstance().getBuffPlayer(player, sh))
			{
				if (BuffListData.getInstance().getBuffsPlayer(player).size() >= ConfigData.MAX_BUFF_COUNT)
				{
					sb.append("<td width=32 height=32></td>");
				}
				else
				{
					sb.append("<td width=32 height=32><button value=\"+\" action=\"bypass -h Quest " + NpcManager.class.getSimpleName() + " buffs " + page + " add " + sh.getSkillId() + " " + sh.getSkillLvl() + "\" back=L2UI_CT1.Button_DF_Down fore=L2UI_CT1.Button_DF width=32 height=32/></td>");
				}
				sb.append("<td width=32 height=32></td>");
			}
			else
			{
				sb.append("<td width=32 height=32></td>");
				sb.append("<td width=32 height=32><button value=\"-\" action=\"bypass -h Quest " + NpcManager.class.getSimpleName() + " buffs " + page + " remove " + sh.getSkillId() + " " + sh.getSkillLvl() + "\" back=L2UI_CT1.Button_DF_Down fore=L2UI_CT1.Button_DF width=32 height=32/></td>");
			}
			
			sb.append("</tr>");
		}
		
		sb.append("</table>");
		sb.append("<br>");
		sb.append("<center><img src=\"l2ui.squaregray\" width=210 height=1></center>");
		sb.append("<table>");
		sb.append("<tr>");
		
		for (int cont = 0; cont < BuffListData.getInstance().getAllBuffs().size() / MAX_BUFF_PAGE; cont++)
		{
			sb.append("<td width=32 height=32><button value=" + (cont + 1) + " action=\"bypass -h Quest " + NpcManager.class.getSimpleName() + " buffs " + (cont + 1) + "\" back=L2UI_CT1.Button_DF_Down fore=L2UI_CT1.Button_DF width=32 height=32/></td>");
		}
		
		sb.append("</tr>");
		sb.append("</table>");
		sb.append("<center><img src=\"l2ui.squaregray\" width=210 height=1></center>");
		
		html.replace("%buffList%", sb.toString());
		// Send html
		player.sendPacket(html);
	}
	
	/**
	 * Generamos el html index del npc<br>
	 * @param player
	 * @return
	 */
	private static void sendHtmlIndex(L2PcInstance player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage();
		html.setFile(player.getHtmlPrefix(), "data/html/events/event_main.htm");
		
		// Info
		html.replace("%namePlayer%", player.getName());
		
		if (EventEngineManager.getInstance().isWaiting())
		{
			html.replace("%menuInfo%", MessageData.getInstance().getMsgByLang(player, "event_waiting", false));
			html.replace("%button%", "");
		}
		else if (EventEngineManager.getInstance().isOpenVote())
		{
			final StringBuilder eventList = new StringBuilder(500);
			for (Class<? extends AbstractEvent> event : EventData.getInstance().getEnabledEvents())
			{
				StringUtil.append(eventList, "<tr>");
				StringUtil.append(eventList, "<td align=center width=30% height=30><button value=\"" + MessageData.getInstance().getMsgByLang(player, "event_" + event.getSimpleName().toLowerCase() + "_name", false) + "\" action=\"bypass -h Quest " + NpcManager.class.getSimpleName() + " vote " + event.getSimpleName() + "\" width=110 height=21 back=L2UI_CT1.Button_DF_Down fore=L2UI_CT1.Button_DF></td>");
				StringUtil.append(eventList, "<td width=40%><font color=LEVEL>" + MessageData.getInstance().getMsgByLang(player, "button_votes", false) + ": </font>" + EventEngineManager.getInstance().getCurrentVotesInEvent(event) + "</td>");
				StringUtil.append(eventList, "<td width=30%><font color=7898AF><a action=\"bypass -h Quest " + NpcManager.class.getSimpleName() + " info " + event.getSimpleName() + "\">" + MessageData.getInstance().getMsgByLang(player, "button_info", false) + "</a></font></td>");
				StringUtil.append(eventList, "</tr>");
			}
			
			html.replace("%menuInfo%", MessageData.getInstance().getMsgByLang(player, "event_vote_info", false));
			html.replace("%button%", "%eventTextVotes% " + "%eventCountVotes%");
			html.replace("%eventTextVotes%", MessageData.getInstance().getMsgByLang(player, "event_text_votes", false));
			html.replace("%eventCountVotes%", " <font color=LEVEL>" + EventEngineManager.getInstance().getAllCurrentVotesInEvents() + "</font><br1>");
			html.replace("%buttonEventList%", eventList.toString());
		}
		else if (EventEngineManager.getInstance().isOpenRegister())
		{
			html.replace("%menuInfo%", MessageData.getInstance().getMsgByLang(player, "event_registration_on", false));
			html.replace("%button%", "%eventTextRecords% " + "%eventCountRecords%" + "<button value=\"%buttonActionName%\" action=\"%buttonAction%\" width=150 height=27 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"/>");
			html.replace("%eventTextRecords%", MessageData.getInstance().getMsgByLang(player, "event_text_records", false));
			html.replace("%eventCountRecords%", " <font color=LEVEL>" + EventEngineManager.getInstance().getAllRegisteredPlayers().size() + "</font><br1>");
			
			if (EventEngineManager.getInstance().isRegistered(player))
			{
				html.replace("%buttonActionName%", MessageData.getInstance().getMsgByLang(player, "button_unregister", false));
				html.replace("%buttonAction%", "bypass -h Quest " + NpcManager.class.getSimpleName() + " unregister");
			}
			else
			{
				html.replace("%buttonActionName%", MessageData.getInstance().getMsgByLang(player, "button_register", false));
				html.replace("%buttonAction%", "bypass -h Quest " + NpcManager.class.getSimpleName() + " register");
			}
		}
		else if (EventEngineManager.getInstance().isRunning())
		{
			html.replace("%menuInfo%", MessageData.getInstance().getMsgByLang(player, "event_registration_notRegState", false));
			html.replace("%button%", "<button value=\"%buttonActionName%\" action=\"%buttonAction%\" width=150 height=27 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"/>");
			html.replace("%buttonActionName%", MessageData.getInstance().getMsgByLang(player, "button_spectator", false));
			// html.replace("%buttonAction%", "bypass -h Quest " + NpcManager.class.getSimpleName() + " spectator");
			html.replace("%menuInfo%", MessageData.getInstance().getMsgByLang(player, "event_registration_notRegState", false));
		}
		else
		{
			html.replace("%menuInfo%", MessageData.getInstance().getMsgByLang(player, "event_reloading", false));
			html.replace("%button%", "");
		}
		// Send html
		player.sendPacket(html);
	}
}