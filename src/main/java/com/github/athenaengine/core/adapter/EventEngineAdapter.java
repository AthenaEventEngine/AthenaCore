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

package com.github.athenaengine.core.adapter;

import com.github.athenaengine.core.ai.NpcManager;
import com.github.athenaengine.core.managers.CacheManager;
import com.github.athenaengine.core.dispatcher.events.*;
import com.github.athenaengine.core.dispatcher.ListenerDispatcher;
import com.github.athenaengine.core.model.template.ItemTemplate;
import com.github.athenaengine.core.model.template.SkillTemplate;
import com.github.athenaengine.core.model.entity.Character;
import com.github.athenaengine.core.model.entity.Npc;
import com.github.athenaengine.core.model.entity.Playable;
import com.github.athenaengine.core.model.entity.Player;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.actor.L2Character;
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
import com.l2jserver.gameserver.model.skills.Skill;

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
		Player player = CacheManager.getInstance().getPlayer(event.getActiveChar(), true);
		ListenerDispatcher.getInstance().notifyEvent(new OnLogInEvent(player));
	}
	
	// When the player exits
	@RegisterEvent(EventType.ON_PLAYER_LOGOUT)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	@Priority(Integer.MAX_VALUE)
	public void onPlayerLogout(OnPlayerLogout event)
	{
		Player player = CacheManager.getInstance().getPlayer(event.getActiveChar(), true);
		ListenerDispatcher.getInstance().notifyEvent(new OnLogOutEvent(player));
		CacheManager.getInstance().removePlayer(player.getObjectId());
	}
	
	// When a playable uses a skill
	@RegisterEvent(EventType.ON_CREATURE_SKILL_USE)
	@RegisterType(ListenerRegisterType.GLOBAL)
	@Priority(Integer.MAX_VALUE)
	public TerminateReturn onPlayableUseSkill(OnCreatureSkillUse event)
	{
		if (event.getCaster().isPlayable())
		{
			Playable caster1 = CacheManager.getInstance().getPlayable((L2Playable) event.getCaster(), true);
			Character target1 = CacheManager.getInstance().getCharacter(event.getCaster(), true);

			Skill l2Skill = event.getSkill();

			SkillTemplate skill = SkillTemplate.Builder.newInstance()
					.setId(l2Skill.getId())
					.setLevel(l2Skill.getLevel())
					.setIsDebuff(l2Skill.isDebuff())
					.setIsDamage(l2Skill.isDamage())
					.build();

			OnUseSkillEvent mainTargetEvent = new OnUseSkillEvent(caster1, skill, target1);

			ListenerDispatcher.getInstance().notifyEvent(mainTargetEvent);

			if (mainTargetEvent.isCanceled())
			{
				return new TerminateReturn(true, true, true);
			}
			// Note: The skill is canceled if there is at least one target with spawn protection in area skills
			// This is not ok, but for now it's the only way
			for (L2Object target : event.getTargets())
			{
				if (target instanceof L2Character)
				{
					Playable caster2 = CacheManager.getInstance().getPlayable((L2Playable) event.getCaster(), true);
					Character target2 = CacheManager.getInstance().getCharacter(event.getCaster(), true);

					OnUseSkillEvent otherTargetEvent = new OnUseSkillEvent(caster2, skill, target2);

					ListenerDispatcher.getInstance().notifyEvent(otherTargetEvent);

					if (otherTargetEvent.isCanceled())
					{
						return new TerminateReturn(true, true, true);
					}
				}
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
			Playable attacker = CacheManager.getInstance().getPlayable((L2Playable) event.getAttacker(), true);
			Character target = CacheManager.getInstance().getCharacter(event.getAttacker(), true);
			OnAttackEvent newEvent = new OnAttackEvent(attacker, target);
			ListenerDispatcher.getInstance().notifyEvent(newEvent);

			if (newEvent.isCanceled())
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
			Playable attacker = CacheManager.getInstance().getPlayable((L2Playable) event.getAttacker(), true);
			Character targetCharacter = CacheManager.getInstance().getCharacter(event.getTarget(), true);
			ListenerDispatcher.getInstance().notifyEvent(new OnKillEvent(attacker, targetCharacter));
		}
		
		if (event.getTarget().isPlayer())
		{
			Player targetPlayer = CacheManager.getInstance().getPlayer((L2PcInstance) event.getTarget(), true);
			ListenerDispatcher.getInstance().notifyEvent(new OnDeathEvent(targetPlayer));
		}

		// TODO: When we hook L2World instance, this won't be necessary
		CacheManager.getInstance().removeCharacter(event.getTarget().getObjectId());
		return null;
	}
	
	// TODO: not finished
	// When a player equips an item
	@RegisterEvent(EventType.ON_PLAYER_EQUIP_ITEM)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	@Priority(Integer.MAX_VALUE)
	public void onUseItem(OnPlayerEquipItem event)
	{
		Player player = CacheManager.getInstance().getPlayer(event.getActiveChar(), true);
		ItemTemplate item = ItemTemplate.newInstance(event.getItem().getId());
		ListenerDispatcher.getInstance().notifyEvent(new OnUseItemEvent(player, item));
	}
	
	// When a player talks with npc
	@RegisterEvent(EventType.ON_NPC_FIRST_TALK)
	@RegisterType(ListenerRegisterType.NPC)
	// The npc with ids from 36600 to 36699 are reserved for engine
	@Range(from = 36600, to = 36699)
	@Priority(Integer.MAX_VALUE)
	public void onNpcInteract(OnNpcFirstTalk event)
	{
		Player player = CacheManager.getInstance().getPlayer(event.getActiveChar(), true);
		Npc npc = (Npc) CacheManager.getInstance().getCharacter(event.getNpc(), true);
		ListenerDispatcher.getInstance().notifyEvent(new OnInteractEvent(player, npc));
	}


}