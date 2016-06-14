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

package com.github.u3games.eventengine.adapter;

import com.github.u3games.eventengine.EventEngineManager;
import com.github.u3games.eventengine.ai.NpcManager;
import com.l2jserver.gameserver.model.actor.L2Playable;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.events.EventType;
import com.l2jserver.gameserver.model.events.ListenerRegisterType;
import com.l2jserver.gameserver.model.events.annotations.Priority;
import com.l2jserver.gameserver.model.events.annotations.Range;
import com.l2jserver.gameserver.model.events.annotations.RegisterEvent;
import com.l2jserver.gameserver.model.events.annotations.RegisterType;
import com.l2jserver.gameserver.model.events.impl.character.OnCreatureAttack;
import com.l2jserver.gameserver.model.events.impl.character.OnCreatureKill;
import com.l2jserver.gameserver.model.events.impl.character.OnCreatureSkillUse;
import com.l2jserver.gameserver.model.events.impl.character.npc.OnNpcFirstTalk;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerEquipItem;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerLogin;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerLogout;
import com.l2jserver.gameserver.model.events.returns.TerminateReturn;
import com.l2jserver.gameserver.model.quest.Quest;

/**
 * This is an adapter to communicate the L2J Core with Event Engine
 * @author Zephyr
 */
public class EventEngineAdapter extends Quest
{
	public EventEngineAdapter()
	{
		super(-1, NpcManager.class.getSimpleName(), "EventEngineAdapter");
	}
	
	// When the player logins
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	@Priority(Integer.MAX_VALUE)
	public void onPlayerLogin(OnPlayerLogin event)
	{
		EventEngineManager.getInstance().listenerOnLogin(event.getActiveChar());
	}
	
	// When the player exits
	@RegisterEvent(EventType.ON_PLAYER_LOGOUT)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	@Priority(Integer.MAX_VALUE)
	public void onPlayerLogout(OnPlayerLogout event)
	{
		EventEngineManager.getInstance().listenerOnLogout(event.getActiveChar());
	}
	
	// When a playable uses a skill
	@RegisterEvent(EventType.ON_CREATURE_SKILL_USE)
	@RegisterType(ListenerRegisterType.GLOBAL)
	@Priority(Integer.MAX_VALUE)
	public TerminateReturn onPlayableUseSkill(OnCreatureSkillUse event)
	{
		if (event.getCaster().isPlayable())
		{
			if (EventEngineManager.getInstance().listenerOnUseSkill((L2Playable) event.getCaster(), event.getTarget(), event.getSkill()))
			{
				return new TerminateReturn(true, true, true);
			}
		}
		return null;
	}
	
	// When a playable attack a character
	@RegisterEvent(EventType.ON_CREATURE_ATTACK)
	@RegisterType(ListenerRegisterType.GLOBAL)
	@Priority(Integer.MAX_VALUE)
	public TerminateReturn onPlayableAttack(OnCreatureAttack event)
	{
		if ((event.getAttacker() != null) && event.getAttacker().isPlayable())
		{
			if (EventEngineManager.getInstance().listenerOnAttack((L2Playable) event.getAttacker(), event.getTarget()))
			{
				return new TerminateReturn(true, true, true);
			}
		}
		
		return null;
	}
	
	// When a playable kills a character and a player dies
	@RegisterEvent(EventType.ON_CREATURE_KILL)
	@RegisterType(ListenerRegisterType.GLOBAL)
	@Priority(Integer.MAX_VALUE)
	public TerminateReturn onCharacterKill(OnCreatureKill event)
	{
		if ((event.getAttacker() != null) && event.getAttacker().isPlayable())
		{
			EventEngineManager.getInstance().listenerOnKill((L2Playable) event.getAttacker(), event.getTarget());
		}
		
		if (event.getTarget().isPlayer())
		{
			EventEngineManager.getInstance().listenerOnDeath((L2PcInstance) event.getTarget());
		}
		
		return null;
	}
	
	// TODO: not finished
	// When a player equips an item
	@RegisterEvent(EventType.ON_PLAYER_EQUIP_ITEM)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	@Priority(Integer.MAX_VALUE)
	public void onUseItem(OnPlayerEquipItem event)
	{
		EventEngineManager.getInstance().listenerOnUseItem(event.getActiveChar(), event.getItem().getItem());
	}
	
	// When a player talks with npc
	@RegisterEvent(EventType.ON_NPC_FIRST_TALK)
	@RegisterType(ListenerRegisterType.NPC)
	// The npc with ids from 36600 to 36699 are reserved for engine
	@Range(from = 36600, to = 36699)
	@Priority(Integer.MAX_VALUE)
	public void onNpcInteract(OnNpcFirstTalk event)
	{
		EventEngineManager.getInstance().listenerOnInteract(event.getActiveChar(), event.getNpc());
	}
}
