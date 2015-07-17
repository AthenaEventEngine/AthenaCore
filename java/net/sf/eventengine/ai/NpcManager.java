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

import net.sf.eventengine.EventEngineManager;
import net.sf.eventengine.datatables.ConfigData;
import net.sf.eventengine.datatables.MessageData;
import net.sf.eventengine.enums.EventType;

import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jserver.util.StringUtil;

/**
 * @author swarlog
 */
public class NpcManager extends Quest
{
	private static int NPC = ConfigData.NPC_MANAGER_ID;
	
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
		StringTokenizer st = new StringTokenizer(event, " ");
		final NpcHtmlMessage html = new NpcHtmlMessage();
		switch (st.nextToken())
		{
			case "index":
				return index(player);
				
			case "vote":
				// Add vote event
				EventEngineManager.increaseVote(player, EventType.valueOf(st.nextToken()));
				player.sendMessage(MessageData.getMsgByLang(player, "event_vote_done", true));
				return index(player);
				
			case "info":
				html.setFile(player.getHtmlPrefix(), "data/html/events/event_info.htm");
				
				// Requirements
				html.replace("%textRequirements%", MessageData.getMsgByLang(player, "text_requirements", false));
				html.replace("%textLevelMax%", MessageData.getMsgByLang(player, "text_level_max", false));
				html.replace("%textLevelMin%", MessageData.getMsgByLang(player, "text_level_min", false));
				// Load Levels
				html.replace("%levelMax%", ConfigData.MAX_LVL_IN_EVENT);
				html.replace("%levelMin%", ConfigData.MIN_LVL_IN_EVENT);
				// Configuration
				html.replace("%textCondiguration%", MessageData.getMsgByLang(player, "text_configuration", false));
				html.replace("%textTimeEvent%", MessageData.getMsgByLang(player, "text_time_event", false));
				html.replace("%timeEvent%", ConfigData.EVENT_DURATION);
				html.replace("%timeMinutes%", MessageData.getMsgByLang(player, "time_minutes", false));
				// Button
				html.replace("%buttonMain%", "bypass -h Quest " + NpcManager.class.getSimpleName() + " index");
				
				// TODO: Move to events
				switch (EventType.valueOf(st.nextToken()))
				{
					case AVA:
						// Info Event
						html.replace("%eventName%", MessageData.getMsgByLang(player, "event_ava_name", false));
						html.replace("%eventDescription%", MessageData.getMsgByLang(player, "event_ava_description", false));
						html.replace("%textDescription%", MessageData.getMsgByLang(player, "text_description", false));
						// Rewards
						html.replace("%textRewards%", MessageData.getMsgByLang(player, "text_rewards", false));
						// TODO: html.replace("%eventRewards%", Configs.TVT_REWARD_LIST);
						break;
					case TVT:
						// Info Event
						html.replace("%eventName%", MessageData.getMsgByLang(player, "event_tvt_name", false));
						html.replace("%eventDescription%", MessageData.getMsgByLang(player, "event_tvt_description", false));
						html.replace("%textDescription%", MessageData.getMsgByLang(player, "text_description", false));
						// Rewards
						html.replace("%textRewards%", MessageData.getMsgByLang(player, "text_rewards", false));
						// TODO: html.replace("%eventRewards%", Configs.TVT_REWARD_LIST);
						break;
					case CTF:
						// Info Event
						html.replace("%eventName%", MessageData.getMsgByLang(player, "event_ctf_name", false));
						html.replace("%eventDescription%", MessageData.getMsgByLang(player, "event_ctf_description", false));
						html.replace("%textDescription%", MessageData.getMsgByLang(player, "text_description", false));
						// Rewards
						html.replace("%textRewards%", MessageData.getMsgByLang(player, "text_rewards", false));
						// TODO: html.replace("%eventRewards%", Configs.CTF_REWARD_LIST);
						break;
					case OVO:
						// Info Event
						html.replace("%eventName%", MessageData.getMsgByLang(player, "event_ovo_name", false));
						html.replace("%eventDescription%", MessageData.getMsgByLang(player, "event_ovo_description", false));
						html.replace("%textDescription%", MessageData.getMsgByLang(player, "text_description", false));
						// Rewards
						html.replace("%textRewards%", MessageData.getMsgByLang(player, "text_rewards", false));
						// TODO: html.replace("%eventRewards%", Configs.OVO_REWARD_LIST);
						break;
					case SURVIVE:
						// Info Event
						html.replace("%eventName%", MessageData.getMsgByLang(player, "event_survive_name", false));
						html.replace("%eventDescription%", MessageData.getMsgByLang(player, "event_survive_description", false));
						html.replace("%textDescription%", MessageData.getMsgByLang(player, "text_description", false));
						// Rewards
						html.replace("%textRewards%", MessageData.getMsgByLang(player, "text_rewards", false));
						// TODO: html.replace("%eventRewards%", Configs.SURVIVE_REWARD_LIST);
						break;
					case PA:
						// Info Event
						html.replace("%eventName%", MessageData.getMsgByLang(player, "event_pa_name", false));
						html.replace("%eventDescription%", MessageData.getMsgByLang(player, "event_pa_description", false));
						html.replace("%textDescription%", MessageData.getMsgByLang(player, "text_description", false));
						// Rewards
						html.replace("%textRewards%", MessageData.getMsgByLang(player, "text_rewards", false));
						// TODO: html.replace("%eventRewards%", Configs.PA_REWARD_LIST);
						break;
				}
				// Send html
				player.sendPacket(html);
				break;
			
			case "register":
				if (EventEngineManager.registerPlayer(player))
				{
					// Check for register
					if (player.getLevel() < ConfigData.MIN_LVL_IN_EVENT)
					{
						player.sendMessage(MessageData.getMsgByLang(player, "registering_lowLevel", true));
					}
					else if (player.getLevel() > ConfigData.MAX_LVL_IN_EVENT)
					{
						player.sendMessage(MessageData.getMsgByLang(player, "registering_highLevel", true));
					}
					else if (EventEngineManager.getAllRegisteredPlayers().size() >= ConfigData.MAX_PLAYERS_IN_EVENT)
					{
						player.sendMessage(MessageData.getMsgByLang(player, "registering_maxPlayers", true));
					}
					else
					{
						player.sendMessage(MessageData.getMsgByLang(player, "registering_registered", true));
					}
					
					return index(player);
				}
				else
				{
					player.sendMessage(MessageData.getMsgByLang(player, "registering_already_registered", true));
					return index(player);
				}
				
			case "unregister":
				if (EventEngineManager.isOpenRegister())
				{
					if (EventEngineManager.unRegisterPlayer(player))
					{
						player.sendMessage(MessageData.getMsgByLang(player, "unregistering_unregistered", true));
						return index(player);
					}
					else
					{
						player.sendMessage(MessageData.getMsgByLang(player, "unregistering_notRegistered", true));
						return index(player);
					}
				}
				else
				{
					player.sendMessage(MessageData.getMsgByLang(player, "event_registration_notUnRegState", true));
					return index(player);
				}
				
				// Multi-Language System menu
			case "menulang":
				html.setFile(player.getHtmlPrefix(), "data/html/events/event_lang.htm");
				
				// Info menu
				html.replace("%settingTitle%", MessageData.getMsgByLang(player, "lang_menu_title", false));
				html.replace("%languageTitle%", MessageData.getMsgByLang(player, "lang_language_title", false));
				html.replace("%languageDescription%", MessageData.getMsgByLang(player, "lang_language_description", false));
				
				// Info lang
				html.replace("%currentLanguage%", MessageData.getMsgByLang(player, "lang_current_language", false));
				html.replace("%getLanguage%", MessageData.getLanguage(player));
				
				// Buttons
				final StringBuilder langList = new StringBuilder(500);
				for (Map.Entry<String, String> e : MessageData.getLanguages().entrySet())
				{
					StringUtil.append(langList, "<tr>");
					StringUtil.append(langList, "<td align=center width=30% height=30><button value=\"" + e.getValue() + "\" action=\"bypass -h Quest " + NpcManager.class.getSimpleName() + " setlang " + e.getKey() + "\" width=70 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
					StringUtil.append(langList, "</tr>");
				}
				html.replace("%buttonLang%", langList.toString());
				
				// Button
				html.replace("%buttonMain%", "bypass -h Quest " + NpcManager.class.getSimpleName() + " index");
				
				// Send
				player.sendPacket(html);
				break;
			
			// Multi-Language System set language
			case "setlang":
				String lang = st.nextToken();
				MessageData.setLanguage(player, lang);
				player.sendMessage(MessageData.getMsgByLang(player, "lang_current_successfully", false) + " " + lang);
				index(player);
				break;
		}
		
		return null;
	}
	
	/**
	 * Generamos el html index del npc<br>
	 * HARDCODE -> se puede generar un html
	 * @param player
	 * @return
	 */
	private static String index(L2PcInstance player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage();
		html.setFile(player.getHtmlPrefix(), "data/html/events/event_main.htm");
		
		// Info
		html.replace("%namePlayer%", player.getName());
		
		if (EventEngineManager.isWaiting())
		{
			html.replace("%menuInfo%", MessageData.getMsgByLang(player, "event_waiting", false));
			html.replace("%button%", "");
		}
		else if (EventEngineManager.isOpenVote())
		{
			final StringBuilder eventList = new StringBuilder(500);
			for (EventType event : EventType.values())
			{
				StringUtil.append(eventList, "<tr>");
				StringUtil.append(eventList, "<td align=center width=30% height=30><button value=\"" + event.getEventName() + "\" action=\"bypass -h Quest " + NpcManager.class.getSimpleName() + " vote " + event.toString() + "\" width=110 height=21 back=L2UI_CT1.Button_DF_Down fore=L2UI_CT1.Button_DF></td>");
				StringUtil.append(eventList, "<td width=40%><font color=LEVEL>" + MessageData.getMsgByLang(player, "button_votes", false) + ": </font>" + EventEngineManager.getCurrentVotesInEvent(event) + "</td>");
				StringUtil.append(eventList, "<td width=30%><font color=7898AF><a action=\"bypass -h Quest " + NpcManager.class.getSimpleName() + " info " + event.toString() + "\">" + MessageData.getMsgByLang(player, "button_info", false) + "</a></font></td>");
				StringUtil.append(eventList, "</tr>");
			}
			html.replace("%menuInfo%", MessageData.getMsgByLang(player, "event_vote_info", false));
			html.replace("%button%", "");
			html.replace("%buttonEventList%", eventList.toString());
		}
		else if (EventEngineManager.isOpenRegister())
		{
			html.replace("%menuInfo%", MessageData.getMsgByLang(player, "event_registration_on", false));
			html.replace("%button%", "<button value=\"%buttonActionName%\" action=\"%buttonAction%\" width=150 height=27 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"/>");
			if (EventEngineManager.isRegistered(player))
			{
				html.replace("%buttonActionName%", MessageData.getMsgByLang(player, "button_unregister", false));
				html.replace("%buttonAction%", "bypass -h Quest " + NpcManager.class.getSimpleName() + " unregister");
			}
			else
			{
				html.replace("%buttonActionName%", MessageData.getMsgByLang(player, "button_register", false));
				html.replace("%buttonAction%", "bypass -h Quest " + NpcManager.class.getSimpleName() + " register");
			}
		}
		else if (EventEngineManager.isRunning())
		{
			html.replace("%menuInfo%", MessageData.getMsgByLang(player, "event_registration_notRegState", false));
			html.replace("%button%", "<button value=\"%buttonActionName%\" action=\"%buttonAction%\" width=150 height=27 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"/>");
			html.replace("%buttonActionName%", MessageData.getMsgByLang(player, "button_spectator", false));
			// html.replace("%buttonAction%", "bypass -h Quest " + NpcManager.class.getSimpleName() + " spectator");
			html.replace("%menuInfo%", MessageData.getMsgByLang(player, "event_registration_notRegState", false));
		}
		else
		{
			html.replace("%menuInfo%", MessageData.getMsgByLang(player, "event_reloading", false));
			html.replace("%button%", "");
		}
		
		// Button
		html.replace("%buttonLang%", "bypass -h Quest " + NpcManager.class.getSimpleName() + " menulang");
		
		player.sendPacket(html);
		return null;
	}
}