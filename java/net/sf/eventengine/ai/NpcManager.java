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
				player.sendMessage(MessageData.getTag() + MessageData.getMsg("event_vote_done"));
				return index(player);
				
			case "info":
				html.setFile(player.getHtmlPrefix(), "data/html/events/event_info.htm");
				// TODO: Move to events
				switch (EventType.valueOf(st.nextToken()))
				{
					case AVA:
						// Info Event
						html.replace("%eventName%", MessageData.getMsg("event_ava_name"));
						html.replace("%textDescription%", MessageData.getMsg("text_description"));
						html.replace("%eventDescription%", MessageData.getMsg("event_ava_description"));
						
						// Requirements
						html.replace("%textRequirements%", MessageData.getMsg("text_requirements"));
						html.replace("%textLevelMax%", MessageData.getMsg("text_level_max"));
						html.replace("%textLevelMin%", MessageData.getMsg("text_level_min"));
						
						// Load Levels
						html.replace("%levelMax%", ConfigData.MAX_LVL_IN_EVENT);
						html.replace("%levelMin%", ConfigData.MIN_LVL_IN_EVENT);
						
						// Configuration
						html.replace("%textCondiguration%", MessageData.getMsg("text_configuration"));
						html.replace("%textTimeEvent%", MessageData.getMsg("text_time_event"));
						html.replace("%timeEvent%", ConfigData.EVENT_DURATION);
						html.replace("%timeMinutes%", MessageData.getMsg("time_minutes"));
						
						// Rewards
						html.replace("%textRewards%", MessageData.getMsg("text_rewards"));
						// TODO: html.replace("%eventRewards%", Configs.AVA_REWARD_LIST);
						
						// Button
						html.replace("%buttonMain%", "bypass -h Quest " + NpcManager.class.getSimpleName() + " index");
						
						// Send
						player.sendPacket(html);
						break;
					case TVT:
						// Info Event
						html.replace("%eventName%", MessageData.getMsg("event_tvt_name"));
						html.replace("%textDescription%", MessageData.getMsg("text_description"));
						html.replace("%eventDescription%", MessageData.getMsg("event_tvt_description"));
						
						// Requirements
						html.replace("%textRequirements%", MessageData.getMsg("text_requirements"));
						html.replace("%textLevelMax%", MessageData.getMsg("text_level_max"));
						html.replace("%textLevelMin%", MessageData.getMsg("text_level_min"));
						
						// Load Levels
						html.replace("%levelMax%", ConfigData.MAX_LVL_IN_EVENT);
						html.replace("%levelMin%", ConfigData.MIN_LVL_IN_EVENT);
						
						// Configuration
						html.replace("%textCondiguration%", MessageData.getMsg("text_configuration"));
						html.replace("%textTimeEvent%", MessageData.getMsg("text_time_event"));
						html.replace("%timeEvent%", ConfigData.EVENT_DURATION);
						html.replace("%timeMinutes%", MessageData.getMsg("time_minutes"));
						
						// Rewards
						html.replace("%textRewards%", MessageData.getMsg("text_rewards"));
						// TODO: html.replace("%eventRewards%", Configs.TVT_REWARD_LIST);
						
						// Button
						html.replace("%buttonMain%", "bypass -h Quest " + NpcManager.class.getSimpleName() + " index");
						
						// Send
						player.sendPacket(html);
						break;
					case CTF:
						// Info Event
						html.replace("%eventName%", MessageData.getMsg("event_ctf_name"));
						html.replace("%textDescription%", MessageData.getMsg("text_description"));
						html.replace("%eventDescription%", MessageData.getMsg("event_ctf_description"));
						
						// Requirements
						html.replace("%textRequirements%", MessageData.getMsg("text_requirements"));
						html.replace("%textLevelMax%", MessageData.getMsg("text_level_max"));
						html.replace("%textLevelMin%", MessageData.getMsg("text_level_min"));
						
						// Load Levels
						html.replace("%levelMax%", ConfigData.MAX_LVL_IN_EVENT);
						html.replace("%levelMin%", ConfigData.MIN_LVL_IN_EVENT);
						
						// Configuration
						html.replace("%textCondiguration%", MessageData.getMsg("text_configuration"));
						html.replace("%textTimeEvent%", MessageData.getMsg("text_time_event"));
						html.replace("%timeEvent%", ConfigData.EVENT_DURATION);
						html.replace("%timeMinutes%", MessageData.getMsg("time_minutes"));
						
						// Rewards
						html.replace("%textRewards%", MessageData.getMsg("text_rewards"));
						// TODO: html.replace("%eventRewards%", Configs.CTF_REWARD_LIST);
						
						// Button
						html.replace("%buttonMain%", "bypass -h Quest " + NpcManager.class.getSimpleName() + " index");
						
						// Send
						player.sendPacket(html);
						break;
					case OVO:
						// Info Event
						html.replace("%eventName%", MessageData.getMsg("event_ovo_name"));
						html.replace("%textDescription%", MessageData.getMsg("text_description"));
						html.replace("%eventDescription%", MessageData.getMsg("event_ovo_description"));
						
						// Requirements
						html.replace("%textRequirements%", MessageData.getMsg("text_requirements"));
						html.replace("%textLevelMax%", MessageData.getMsg("text_level_max"));
						html.replace("%textLevelMin%", MessageData.getMsg("text_level_min"));
						
						// Load Levels
						html.replace("%levelMax%", ConfigData.MAX_LVL_IN_EVENT);
						html.replace("%levelMin%", ConfigData.MIN_LVL_IN_EVENT);
						
						// Configuration
						html.replace("%textCondiguration%", MessageData.getMsg("text_configuration"));
						html.replace("%textTimeEvent%", MessageData.getMsg("text_time_event"));
						html.replace("%timeEvent%", ConfigData.EVENT_DURATION);
						html.replace("%timeMinutes%", MessageData.getMsg("time_minutes"));
						
						// Rewards
						html.replace("%textRewards%", MessageData.getMsg("text_rewards"));
						// TODO: html.replace("%eventRewards%", Configs.OVO_REWARD_LIST);
						
						// Button
						html.replace("%buttonMain%", "bypass -h Quest " + NpcManager.class.getSimpleName() + " index");
						
						// Send
						player.sendPacket(html);
						break;
					case SURVIVE:
						// Info Event
						html.replace("%eventName%", MessageData.getMsg("event_survive_name"));
						html.replace("%textDescription%", MessageData.getMsg("text_description"));
						html.replace("%eventDescription%", MessageData.getMsg("event_survive_description"));
						
						// Requirements
						html.replace("%textRequirements%", MessageData.getMsg("text_requirements"));
						html.replace("%textLevelMax%", MessageData.getMsg("text_level_max"));
						html.replace("%textLevelMin%", MessageData.getMsg("text_level_min"));
						
						// Load Levels
						html.replace("%levelMax%", ConfigData.MAX_LVL_IN_EVENT);
						html.replace("%levelMin%", ConfigData.MIN_LVL_IN_EVENT);
						
						// Configuration
						html.replace("%textCondiguration%", MessageData.getMsg("text_configuration"));
						html.replace("%textTimeEvent%", MessageData.getMsg("text_time_event"));
						html.replace("%timeEvent%", ConfigData.EVENT_DURATION);
						html.replace("%timeMinutes%", MessageData.getMsg("time_minutes"));
						
						// Rewards
						html.replace("%textRewards%", MessageData.getMsg("text_rewards"));
						// TODO: html.replace("%eventRewards%", Configs.SURVIVE_REWARD_LIST);
						
						// Button
						html.replace("%buttonMain%", "bypass -h Quest " + NpcManager.class.getSimpleName() + " index");
						
						// Send
						player.sendPacket(html);
						break;
					case PA:
						// Info Event
						html.replace("%eventName%", MessageData.getMsg("event_pa_name"));
						html.replace("%textDescription%", MessageData.getMsg("text_description"));
						html.replace("%eventDescription%", MessageData.getMsg("event_pa_description"));
						
						// Requirements
						html.replace("%textRequirements%", MessageData.getMsg("text_requirements"));
						html.replace("%textLevelMax%", MessageData.getMsg("text_level_max"));
						html.replace("%textLevelMin%", MessageData.getMsg("text_level_min"));
						
						// Load Levels
						html.replace("%levelMax%", ConfigData.MAX_LVL_IN_EVENT);
						html.replace("%levelMin%", ConfigData.MIN_LVL_IN_EVENT);
						
						// Configuration
						html.replace("%textCondiguration%", MessageData.getMsg("text_configuration"));
						html.replace("%textTimeEvent%", MessageData.getMsg("text_time_event"));
						html.replace("%timeEvent%", ConfigData.EVENT_DURATION);
						html.replace("%timeMinutes%", MessageData.getMsg("time_minutes"));
						
						// Rewards
						html.replace("%textRewards%", MessageData.getMsg("text_rewards"));
						// TODO: html.replace("%eventRewards%", Configs.PA_REWARD_LIST);
						
						// Button
						html.replace("%buttonMain%", "bypass -h Quest " + NpcManager.class.getSimpleName() + " index");
						
						// Send
						player.sendPacket(html);
						break;
				}
				break;
			case "register":
				if (EventEngineManager.registerPlayer(player))
				{
					// Check for register
					if (player.getLevel() < ConfigData.MIN_LVL_IN_EVENT)
					{
						player.sendMessage(MessageData.getTag() + MessageData.getMsg("registering_lowLevel"));
					}
					else if (player.getLevel() > ConfigData.MAX_LVL_IN_EVENT)
					{
						player.sendMessage(MessageData.getTag() + MessageData.getMsg("registering_highLevel"));
					}
					else
					{
						player.sendMessage(MessageData.getTag() + MessageData.getMsg("registering_registered"));
					}
					
					return index(player);
				}
				else
				{
					player.sendMessage(MessageData.getTag() + MessageData.getMsg("registering_already_registered"));
					return index(player);
				}
				
			case "unregister":
				if (EventEngineManager.isOpenRegister())
				{
					if (EventEngineManager.unRegisterPlayer(player))
					{
						player.sendMessage(MessageData.getTag() + MessageData.getMsg("unregistering_unregistered"));
						return index(player);
					}
					else
					{
						player.sendMessage(MessageData.getTag() + MessageData.getMsg("unregistering_notRegistered"));
						return index(player);
					}
				}
				else
				{
					player.sendMessage(MessageData.getTag() + MessageData.getMsg("event_registration_notUnRegState"));
					return index(player);
				}
				// Multi-Language System menu
			case "menulang":
				html.setFile(player.getHtmlPrefix(), "data/html/events/event_lang.htm");
				
				// Info menu
				html.replace("%settingTitle%", MessageData.getMsg("lang_menu_title"));
				html.replace("%languageTitle%", MessageData.getMsg("lang_language_title"));
				html.replace("%languageDescription%", MessageData.getMsg("lang_language_description"));
				
				// Info lang
				html.replace("%currentLanguage%", MessageData.getMsg("lang_current_language"));
				html.replace("%getLanguage%", MessageData.getLanguage());
				
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
				MessageData.setLanguage(lang);
				player.sendMessage(MessageData.getTag() + MessageData.getMsg("lang_current_successfully") + " " + lang);
				index(player);
				break;
		}
		
		return event;
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
			html.replace("%menuInfo%", MessageData.getMsg("event_waiting"));
			html.replace("%button%", "");
		}
		else if (EventEngineManager.isOpenVote())
		{
			final StringBuilder eventList = new StringBuilder(500);
			for (EventType event : EventType.values())
			{
				StringUtil.append(eventList, "<tr>");
				StringUtil.append(eventList, "<td align=center width=30% height=30><button value=\"" + event.getEventName() + "\" action=\"bypass -h Quest " + NpcManager.class.getSimpleName() + " vote " + event.toString() + "\" width=110 height=21 back=L2UI_CT1.Button_DF_Down fore=L2UI_CT1.Button_DF></td>");
				StringUtil.append(eventList, "<td width=40%><font color=LEVEL>" + MessageData.getMsg("button_votes") + ": </font>" + EventEngineManager.getCurrentVotesInEvent(event) + "</td>");
				StringUtil.append(eventList, "<td width=30%><font color=7898AF><a action=\"bypass -h Quest " + NpcManager.class.getSimpleName() + " info " + event.toString() + "\">" + MessageData.getMsg("button_info") + "</a></font></td>");
				StringUtil.append(eventList, "</tr>");
			}
			html.replace("%menuInfo%", MessageData.getMsg("event_vote_info"));
			html.replace("%button%", "");
			html.replace("%buttonEventList%", eventList.toString());
		}
		else if (EventEngineManager.isOpenRegister())
		{
			html.replace("%menuInfo%", MessageData.getMsg("event_registration_on"));
			html.replace("%button%", "<button value=\"%buttonActionName%\" action=\"%buttonAction%\" width=150 height=27 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"/>");
			if (EventEngineManager.isRegistered(player))
			{
				html.replace("%buttonActionName%", MessageData.getMsg("button_unregister"));
				html.replace("%buttonAction%", "bypass -h Quest " + NpcManager.class.getSimpleName() + " unregister");
			}
			else
			{
				html.replace("%buttonActionName%", MessageData.getMsg("button_register"));
				html.replace("%buttonAction%", "bypass -h Quest " + NpcManager.class.getSimpleName() + " register");
			}
		}
		else if (EventEngineManager.isRunning())
		{
			html.replace("%menuInfo%", MessageData.getMsg("event_registration_notRegState"));
			html.replace("%button%", "<button value=\"%buttonActionName%\" action=\"%buttonAction%\" width=150 height=27 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"/>");
			html.replace("%buttonActionName%", MessageData.getMsg("button_spectator"));
			// html.replace("%buttonAction%", "bypass -h Quest " + NpcManager.class.getSimpleName() + " spectator");
			html.replace("%menuInfo%", MessageData.getMsg("event_registration_notRegState"));
		}
		else
		{
			html.replace("%menuInfo%", MessageData.getMsg("event_reloading"));
			html.replace("%button%", "");
		}
		
		// Button
		html.replace("%buttonLang%", "bypass -h Quest " + NpcManager.class.getSimpleName() + " menulang");
		
		player.sendPacket(html);
		return null;
	}
}