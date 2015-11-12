package net.sf.eventengine.adapter;

import net.sf.eventengine.EventEngineManager;
import net.sf.eventengine.ai.NpcManager;

import com.l2jserver.gameserver.model.actor.L2Playable;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.events.EventType;
import com.l2jserver.gameserver.model.events.ListenerRegisterType;
import com.l2jserver.gameserver.model.events.annotations.Priority;
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
	@RegisterType(ListenerRegisterType.GLOBAL_NPCS)
	@Priority(Integer.MAX_VALUE)
	public void onNpcInteract(OnNpcFirstTalk event)
	{
		EventEngineManager.getInstance().listenerOnInteract(event.getActiveChar(), event.getNpc());
	}
}
