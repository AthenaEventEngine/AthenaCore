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
package com.github.athenaengine.core.ai;

import java.util.Map;
import java.util.StringTokenizer;

import com.github.athenaengine.core.managers.CacheManager;
import com.github.athenaengine.core.config.BaseConfigLoader;
import com.github.athenaengine.core.model.entity.Player;
import com.github.athenaengine.core.security.DualBoxProtection;
import com.github.athenaengine.core.EventEngineManager;
import com.github.athenaengine.core.model.config.MainEventConfig;
import com.github.athenaengine.core.datatables.BuffListData;
import com.github.athenaengine.core.datatables.EventLoader;
import com.github.athenaengine.core.datatables.MessageData;
import com.github.athenaengine.core.interfaces.IEventContainer;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.entity.L2Event;
import com.l2jserver.gameserver.model.entity.TvTEvent;
import com.l2jserver.gameserver.model.holders.SkillHolder;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jserver.util.StringUtil;

public class NpcManager extends Quest
{
	private static final int MAX_BUFF_PAGE = 12;
	
	public NpcManager()
	{
		super(-1, NpcManager.class.getSimpleName(), "EventEngine");

		addStartNpc(getConfig().getNpcId());
		addFirstTalkId(getConfig().getNpcId());
		addTalkId(getConfig().getNpcId());
	}

	private static MainEventConfig getConfig() {
		return BaseConfigLoader.getInstance().getMainConfig();
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		sendHtmlIndex(CacheManager.getInstance().getPlayer(player, true));
		return null;
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance l2PcInstance)
	{
		Player player = CacheManager.getInstance().getPlayer(l2PcInstance, true);
		StringTokenizer st = new StringTokenizer(event, " ");
		switch (st.nextToken())
		{
			case "index":
				sendHtmlIndex(player);
				break;
			case "engine":
				final NpcHtmlMessage html = new NpcHtmlMessage();
				html.setFile(l2PcInstance.getHtmlPrefix(), "data/html/events/event_engine.htm");
				html.replace("%buttonMain%", MessageData.getInstance().getMsgByLang(player, "button_main", false));
				l2PcInstance.sendPacket(html);
				break;
			case "vote":
				// Check for vote
				if (checkPlayerCondition(l2PcInstance, null))
				{
					// Add vote event
					IEventContainer container = EventLoader.getInstance().getEvent(st.nextToken());
					if (container != null)
					{
						EventEngineManager.getInstance().increaseVote(player, container.getSimpleEventName());
						l2PcInstance.sendMessage(MessageData.getInstance().getMsgByLang(player, "event_vote_done", true));
					}
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
					if (checkPlayerCondition(l2PcInstance, EventEngineManager.getInstance().getNextEvent()))
					{
						boolean dualboxProtection = DualBoxProtection.getInstance().registerConnection(player);

						if (!dualboxProtection)
						{
							l2PcInstance.sendMessage(MessageData.getInstance().getMsgByLang(player, "registering_dualbox_limit", true));
						}
						else if (EventEngineManager.getInstance().getAllRegisteredPlayers().size() >= getConfig().getMaxPlayers())
						{
							l2PcInstance.sendMessage(MessageData.getInstance().getMsgByLang(player, "registering_maxPlayers", true));
						}
						else
						{
							EventEngineManager.getInstance().registerPlayer(player);
							l2PcInstance.sendMessage(MessageData.getInstance().getMsgByLang(player, "registering_registered", true));
						}
					}
				}
				else
				{
					l2PcInstance.sendMessage(MessageData.getInstance().getMsgByLang(player, "registering_already_registered", true));
				}
				sendHtmlIndex(player);
				break;
			case "unregister":
				if (EventEngineManager.getInstance().isOpenRegister())
				{
					DualBoxProtection.getInstance().removeConnection(player);
					
					if (EventEngineManager.getInstance().unRegisterPlayer(player))
					{
						l2PcInstance.sendMessage(MessageData.getInstance().getMsgByLang(player, "unregistering_unregistered", true));
					}
					else
					{
						l2PcInstance.sendMessage(MessageData.getInstance().getMsgByLang(player, "unregistering_notRegistered", true));
					}
				}
				else
				{
					l2PcInstance.sendMessage(MessageData.getInstance().getMsgByLang(player, "event_registration_notUnRegState", true));
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
				l2PcInstance.sendMessage(MessageData.getInstance().getMsgByLang(player, "lang_current_successfully", false) + " " + lang);
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
	
	private static void sendHtmlInfo(Player player, String eventName)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage();
		html.setFile(player.getPcInstance().getHtmlPrefix(), "data/html/events/event_info.htm");
		IEventContainer container = EventLoader.getInstance().getEvent(eventName);
		// Avoid a vulnerability
		if (container != null)
		{
			// Info event
			html.replace("%eventName%", container.getEventName());
			html.replace("%textDescription%", MessageData.getInstance().getMsgByLang(player, "text_description", false));
			html.replace("%eventDescription%", container.getDescription());
			// Requirements
			html.replace("%textRequirements%", MessageData.getInstance().getMsgByLang(player, "text_requirements", false));
			html.replace("%textLevelMax%", MessageData.getInstance().getMsgByLang(player, "text_level_max", false));
			html.replace("%textLevelMin%", MessageData.getInstance().getMsgByLang(player, "text_level_min", false));
			html.replace("%levelMax%", container.getMaxLevel());
			html.replace("%levelMin%", container.getMinLevel());
			// Configuration
			html.replace("%textConfiguration%", MessageData.getInstance().getMsgByLang(player, "text_configuration", false));
			html.replace("%textTimeEvent%", MessageData.getInstance().getMsgByLang(player, "text_time_event", false));
			html.replace("%timeEvent%", container.getRunningTime());
			html.replace("%timeMinutes%", MessageData.getInstance().getMsgByLang(player, "time_minutes", false));
			// Rewards rewardList
			html.replace("%textRewards%", MessageData.getInstance().getMsgByLang(player, "text_rewards", false));
			html.replace("%rewardList%", container.getRewards());
			// Button
			html.replace("%buttonMain%", MessageData.getInstance().getMsgByLang(player, "button_main", false));

			player.getPcInstance().sendPacket(html);
		}
	}
	
	private static void sendHtmlLang(Player player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage();
		html.setFile(player.getPcInstance().getHtmlPrefix(), "data/html/events/event_lang.htm");
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
		player.getPcInstance().sendPacket(html);
	}
	
	private static void sendHtmlBuffList(Player player, int page)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage();
		html.setFile(player.getPcInstance().getHtmlPrefix(), "data/html/events/event_buffs.htm");
		html.replace("%buffTitle%", MessageData.getInstance().getMsgByLang(player, "buff_title", false));
		html.replace("%buffDescription%", MessageData.getInstance().getMsgByLang(player, "buff_description", false));
		html.replace("%buffTextCount%", MessageData.getInstance().getMsgByLang(player, "buff_text_count", false));
		html.replace("%buffTextMax%", MessageData.getInstance().getMsgByLang(player, "buff_text_max", false));
		html.replace("%buffCount%", " <font color=LEVEL>" + BuffListData.getInstance().getBuffsPlayer(player).size() + "</font>");
		html.replace("%buffMax%", " <font color=LEVEL>" + getConfig().getMaxBuffCount() + "</font>");
		html.replace("%buttonMain%", MessageData.getInstance().getMsgByLang(player, "button_main", false));
		StringBuilder sb = new StringBuilder();
		sb.append("<table>");
		for (int cont = (page - 1) * MAX_BUFF_PAGE; cont < (page * MAX_BUFF_PAGE); cont++)
		{
			SkillHolder sh = BuffListData.getInstance().getAllBuffs().get(cont);
			sb.append("<tr>");
			sb.append("<td width=32 height=32><img src=" + sh.getSkill().getIcon() + " width=32 height=32></td>");
			sb.append("<td width=130><font color=LEVEL>" + sh.getSkill().getName() + "</font><br></td>");
			if (!BuffListData.getInstance().getBuffPlayer(player, sh))
			{
				if (BuffListData.getInstance().getBuffsPlayer(player).size() >= getConfig().getMaxBuffCount())
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
		for (int cont = 0; cont < (BuffListData.getInstance().getAllBuffs().size() / MAX_BUFF_PAGE); cont++)
		{
			sb.append("<td width=32 height=32><button value=" + (cont + 1) + " action=\"bypass -h Quest " + NpcManager.class.getSimpleName() + " buffs " + (cont + 1) + "\" back=L2UI_CT1.Button_DF_Down fore=L2UI_CT1.Button_DF width=32 height=32/></td>");
		}
		sb.append("</tr>");
		sb.append("</table>");
		sb.append("<center><img src=\"l2ui.squaregray\" width=210 height=1></center>");
		html.replace("%buffList%", sb.toString());
		// Send html
		player.getPcInstance().sendPacket(html);
	}

	private static void sendHtmlIndex(Player player)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage();
		html.setFile(player.getPcInstance().getHtmlPrefix(), "data/html/events/event_main.htm");
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
			for (IEventContainer container : EventLoader.getInstance().getEnabledEvents())
			{
				StringUtil.append(eventList, "<tr>");
				StringUtil.append(eventList, "<td align=center width=30% height=30><button value=\"" + container.getEventName() + "\" action=\"bypass -h Quest " + NpcManager.class.getSimpleName() + " vote "
					+ container.getSimpleEventName() + "\" width=110 height=21 back=L2UI_CT1.Button_DF_Down fore=L2UI_CT1.Button_DF></td>");
				StringUtil.append(eventList, "<td width=40%><font color=LEVEL>" + MessageData.getInstance().getMsgByLang(player, "button_votes", false) + ": </font>" + EventEngineManager.getInstance().getCurrentVotesInEvent(container.getSimpleEventName()) + "</td>");
				StringUtil.append(eventList, "<td width=30%><font color=7898AF><a action=\"bypass -h Quest " + NpcManager.class.getSimpleName() + " info " + container.getSimpleEventName() + "\">" + MessageData.getInstance().getMsgByLang(player, "button_info", false) + "</a></font></td>");
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
		player.getPcInstance().sendPacket(html);
	}
	
	private static boolean checkPlayerCondition(L2PcInstance l2PcInstance, IEventContainer container)
	{
		Player player = CacheManager.getInstance().getPlayer(l2PcInstance, true);

		if (EventEngineManager.getInstance().isOpenRegister())
		{
			if (container != null)
			{
				// Check level min
				if (l2PcInstance.getLevel() < container.getMinLevel())
				{
					l2PcInstance.sendMessage(MessageData.getInstance().getMsgByLang(player, "lowLevel", true));
					return false;
				}
				// Check level max
				else if (l2PcInstance.getLevel() > container.getMaxLevel())
				{
					l2PcInstance.sendMessage(MessageData.getInstance().getMsgByLang(player, "highLevel", true));
					return false;
				}
			}
			else
			{
				return false;
			}
		}

		// Check dead mode player
		if (l2PcInstance.isDead() || l2PcInstance.isAlikeDead())
		{
			l2PcInstance.sendMessage(MessageData.getInstance().getMsgByLang(player, "deadMode", true));
			return false;
		}
		// Check Olympiad Mode
		else if (l2PcInstance.isInOlympiadMode())
		{
			l2PcInstance.sendMessage(MessageData.getInstance().getMsgByLang(player, "olympiadMode", true));
			return false;
		}
		// Check Observer Mode
		else if (l2PcInstance.inObserverMode())
		{
			l2PcInstance.sendMessage(MessageData.getInstance().getMsgByLang(player, "observerMode", true));
			return false;
		}
		// Check in festival
		else if (l2PcInstance.isFestivalParticipant())
		{
			l2PcInstance.sendMessage(MessageData.getInstance().getMsgByLang(player, "festivalMode", true));
			return false;
		}
		// Check in Events
		else if (L2Event.isParticipant(l2PcInstance))
		{
			l2PcInstance.sendMessage(MessageData.getInstance().getMsgByLang(player, "eventMode", true));
			return false;
		}
		// Check in TvT Event
		else if (TvTEvent.isPlayerParticipant(l2PcInstance.getObjectId()))
		{
			l2PcInstance.sendMessage(MessageData.getInstance().getMsgByLang(player, "tvtEvent", true));
			return false;
		}
		// Check player state
		else if ((l2PcInstance.getPvpFlag() > 0) || (l2PcInstance.isInCombat()) || (l2PcInstance.isInDuel()) || (l2PcInstance.getKarma() > 0) || (l2PcInstance.isCursedWeaponEquipped()))
		{
			// Check properties
			if (!getConfig().isChaoticPlayerRegisterAllowed())
			{
				l2PcInstance.sendMessage(MessageData.getInstance().getMsgByLang(player, "chaoticPlayer", true));
				return false;
			}
		}
		return true;
	}
}