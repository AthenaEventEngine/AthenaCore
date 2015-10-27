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
package net.sf.eventengine.events.handler;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.instancemanager.InstanceManager;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.L2Playable;
import com.l2jserver.gameserver.model.actor.instance.L2CubicInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.gameserver.model.holders.SkillHolder;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.taskmanager.DecayTaskManager;
import com.l2jserver.util.Rnd;

import net.sf.eventengine.EventEngineManager;
import net.sf.eventengine.datatables.BuffListData;
import net.sf.eventengine.datatables.ConfigData;
import net.sf.eventengine.datatables.MessageData;
import net.sf.eventengine.enums.EventState;
import net.sf.eventengine.enums.TeamType;
import net.sf.eventengine.events.handler.managers.InstanceWorldManager;
import net.sf.eventengine.events.handler.managers.PlayersManager;
import net.sf.eventengine.events.handler.managers.ScheduledEventsManager;
import net.sf.eventengine.events.handler.managers.SpawnManager;
import net.sf.eventengine.events.handler.managers.TeamsManagers;
import net.sf.eventengine.events.holders.PlayerHolder;
import net.sf.eventengine.util.EventUtil;

/**
 * @author fissban
 */
public abstract class AbstractEvent
{
	private static final Logger LOGGER = Logger.getLogger(AbstractEvent.class.getName());
	
	public AbstractEvent()
	{
		// We add every player registered for the event.
		getPlayerEventManager().createEventPlayers();
		// We started the clock to control the sequence of internal events of the event.
		getScheduledEventsManager().startScheduledEvents();
		getScheduledEventsManager().startTaskControlTime();
	}
	
	/** Necessary to hadle the event states. */
	public final void runEventState(EventState state)
	{
		switch (state)
		{
			case START:
				prepareToStart();
				onEventStart();
				break;
				
			case FIGHT:
				prepareToFight();
				onEventFight();
				break;
				
			case END:
				onEventEnd();
				prepareToEnd();
				break;
		}
	}
	
	protected abstract void onEventStart();
	
	protected abstract void onEventFight();
	
	protected abstract void onEventEnd();
	
	// XXX TEAMS -----------------------------------------------------------------------------------------
	private TeamsManagers _teamsManagers = new TeamsManagers();
	
	public TeamsManagers getTeamsManager()
	{
		return _teamsManagers;
	}
	
	// XXX DINAMIC INSTANCE ------------------------------------------------------------------------------
	private InstanceWorldManager _instanceWorldManager = new InstanceWorldManager();
	
	public InstanceWorldManager getInstanceWorldManager()
	{
		return _instanceWorldManager;
	}
	
	// XXX SCHEDULED AND UNSCHEDULED EVENTS --------------------------------------------------------------
	
	private ScheduledEventsManager _scheduledEventsManager = new ScheduledEventsManager();
	
	public ScheduledEventsManager getScheduledEventsManager()
	{
		return _scheduledEventsManager;
	}
	
	// REVIVE --------------------------------------------------------------------------------------- //
	private final List<ScheduledFuture<?>> _revivePending = new CopyOnWriteArrayList<>();
	
	private void stopAllPendingRevive()
	{
		Iterator<ScheduledFuture<?>> iterator = _revivePending.iterator();
		while (iterator.hasNext())
		{
			iterator.next().cancel(true);
		}
		
		_revivePending.clear();
	}
	
	// NPC IN EVENT --------------------------------------------------------------------------------- //
	private SpawnManager _spawnManager = new SpawnManager();
	
	public SpawnManager getSpawnManager()
	{
		return _spawnManager;
	}
	
	// PLAYERS IN EVENT ----------------------------------------------------------------------------- //
	private PlayersManager _playerEventManager = new PlayersManager();
	
	public PlayersManager getPlayerEventManager()
	{
		return _playerEventManager;
	}
	
	// LISTENERS ------------------------------------------------------------------------------------ //
	
	/**
	 * Si se retorna "false" no se mostrara ningun html
	 * @param player
	 * @param target
	 */
	public boolean listenerOnInteract(L2PcInstance player, L2Npc target)
	{
		if (!getPlayerEventManager().isPlayableInEvent(player) && !getSpawnManager().isNpcInEvent(target))
		{
			return true;
		}
		
		return onInteract(getPlayerEventManager().getEventPlayer(player), target);
	}
	
	/**
	 * @param ph
	 * @param npc
	 */
	public boolean onInteract(PlayerHolder ph, L2Npc npc)
	{
		return true;
	}
	
	/**
	 * @param playable
	 * @param target
	 */
	public void listenerOnKill(L2Playable playable, L2Character target)
	{
		if (!getPlayerEventManager().isPlayableInEvent(playable))
		{
			return;
		}
		
		// ignoramos siempre si matan algun summon.
		// XXX se podria usar en algun evento...analizar!
		if (target.isSummon())
		{
			return;
		}
		
		onKill(getPlayerEventManager().getEventPlayer(playable), target);
	}
	
	/**
	 * @param ph
	 * @param target
	 */
	public void onKill(PlayerHolder ph, L2Character target)
	{
		// Nothing
	}
	
	/**
	 * @param player
	 */
	public void listenerOnDeath(L2PcInstance player)
	{
		if (!getPlayerEventManager().isPlayableInEvent(player))
		{
			return;
		}
		
		onDeath(getPlayerEventManager().getEventPlayer(player));
	}
	
	/**
	 * @param ph
	 */
	public void onDeath(PlayerHolder ph)
	{
		// Nothing
	}
	
	public boolean listenerOnAttack(L2Playable playable, L2Character target)
	{
		if (!getPlayerEventManager().isPlayableInEvent(playable))
		{
			return false;
		}
		
		// We get the player involved in our event.
		PlayerHolder activePlayer = getPlayerEventManager().getEventPlayer(playable);
		
		// CHECK FRIENDLY_FIRE ----------------------------------------
		if (ConfigData.getInstance().FRIENDLY_FIRE)
		{
			// If our target is L2Playable type and we do this in the event control.
			PlayerHolder activeTarget = getPlayerEventManager().getEventPlayer(target);
			
			if (activeTarget != null)
			{
				// AllVsAll style events do not have a defined team players.
				if ((activePlayer.getTeamType() == TeamType.WHITE) || (activeTarget.getTeamType() == TeamType.WHITE))
				{
					// Nothing
				}
				else if (activePlayer.getTeamType() == activeTarget.getTeamType())
				{
					return true;
				}
			}
		}
		// CHECK FRIENDLY_FIRE ----------------------------------------
		return onAttack(activePlayer, target);
	}
	
	/**
	 * @param ph
	 * @param target
	 * @return true -> only in the event that an attack not want q continue its normal progress.
	 */
	public boolean onAttack(PlayerHolder ph, L2Character target)
	{
		return false;
	}
	
	/**
	 * @param playable
	 * @param target
	 * @return true -> only in the event that an skill not want that continue its normal progress.
	 */
	public boolean listenerOnUseSkill(L2Playable playable, L2Character target, Skill skill)
	{
		if (!getPlayerEventManager().isPlayableInEvent(playable))
		{
			return false;
		}
		
		// If the character has no target to finish the listener.
		// XXX quizas en algun evento pueda ser requerido el uso de habilidades sin necesidad de target....revisar.
		if (target == null)
		{
			return false;
		}
		
		// If the character is using a skill on itself end the listener.
		if (playable.equals(target))
		{
			return false;
		}
		
		// We get the player involved in our event.
		PlayerHolder activePlayer = getPlayerEventManager().getEventPlayer(playable);
		
		// CHECK FRIENDLY_FIRE ----------------------------------------
		if (ConfigData.getInstance().FRIENDLY_FIRE)
		{
			// If our target is L2Playable type and we do this in the event control.
			PlayerHolder activeTarget = getPlayerEventManager().getEventPlayer(target);
			
			if ((activeTarget != null) && skill.isDamage())
			{
				// AllVsAll style events do not have a defined team players.
				if ((activePlayer.getTeamType() == TeamType.WHITE) || (activeTarget.getTeamType() == TeamType.WHITE))
				{
					// Nothing
				}
				else if (activePlayer.getTeamType() == activeTarget.getTeamType())
				{
					return true;
				}
			}
		}
		// CHECK FRIENDLY_FIRE ----------------------------------------
		
		return onUseSkill(activePlayer, target, skill);
	}
	
	/**
	 * @param ph
	 * @param target
	 * @param skill
	 * @return true -> only in the event that an item not want that continue its normal progress.
	 */
	public boolean onUseSkill(PlayerHolder ph, L2Character target, Skill skill)
	{
		return false;
	}
	
	/**
	 * @param player
	 * @param item
	 * @return -> only in the event that an skill not want q continue its normal progress.
	 */
	public boolean listenerOnUseItem(L2PcInstance player, L2Item item)
	{
		if (!getPlayerEventManager().isPlayableInEvent(player))
		{
			return false;
		}
		
		// We will not allow the use of pots or scroll.
		// XXX se podria setear como un config el tema de las pots
		if (item.isScroll() || item.isPotion())
		{
			return true;
		}
		
		return onUseItem(getPlayerEventManager().getEventPlayer(player), item);
	}
	
	/**
	 * @param player
	 * @param item
	 * @return true -> only in the event that an skill not want q continue its normal progress.
	 */
	public boolean onUseItem(PlayerHolder player, L2Item item)
	{
		return false;
	}
	
	public void listenerOnLogout(L2PcInstance player)
	{
		if (getPlayerEventManager().isPlayableInEvent(player))
		{
			try
			{
				PlayerHolder ph = getPlayerEventManager().getEventPlayer(player);
				// listener
				onLogout(ph);
				// recover the original color title
				ph.recoverOriginalColorTitle();
				// recover the original title
				ph.recoverOriginalTitle();
				// we remove the character of the created world
				InstanceManager.getInstance().getWorld(ph.getDinamicInstanceId()).removeAllowed(ph.getPcInstance().getObjectId());
				
				getPlayerEventManager().getAllEventPlayers().remove(ph);
			}
			catch (Exception e)
			{
				LOGGER.warning(EventEngineManager.class.getSimpleName() + ": -> listenerOnLogout() " + e);
				e.printStackTrace();
			}
		}
	}
	
	public void onLogout(PlayerHolder ph)
	{
		// Nothing
	}
	
	// VARIOUS METHODS. -------------------------------------------------------------------------------- //
	
	/**
	 * Teleport to players of each team to their respective starting points<br>
	 */
	protected void teleportAllPlayers(int radius)
	{
		for (PlayerHolder ph : getPlayerEventManager().getAllEventPlayers())
		{
			revivePlayer(ph);
			InstanceManager.getInstance().getWorld(ph.getDinamicInstanceId()).addAllowed(ph.getPcInstance().getObjectId());
			teleportPlayer(ph, radius);
		}
	}
	
	/**
	 * Teleport to a specific player to its original location within the event.
	 * @param ph
	 */
	protected void teleportPlayer(PlayerHolder ph, int radius)
	{
		// get the spawn defined at the start of each event
		Location loc = getTeamsManager().getTeamSpawn(ph.getTeamType());
		
		loc.setInstanceId(ph.getDinamicInstanceId());
		loc.setX(loc.getX() + Rnd.get(-radius, radius));
		loc.setY(loc.getY() + Rnd.get(-radius, radius));
		
		// teleport to character
		ph.getPcInstance().teleToLocation(loc, false);
	}
	
	/**
	 * We prepare players to start the event<br>
	 * <ul>
	 * <b>Actions: </b>
	 * </ul>
	 * <li>Cancel any attack in progress</li><br>
	 * <li>Cancel any skill in progress</li><br>
	 * <li>We paralyzed the player</li><br>
	 * <li>Cancel all character effects</li><br>
	 * <li>Cancel summon pet</li><br>
	 * <li>Cancel all character cubics</li><br>
	 */
	public void prepareToStart()
	{
		for (PlayerHolder ph : getPlayerEventManager().getAllEventPlayers())
		{
			// Cancel target
			ph.getPcInstance().setTarget(null);
			// Cancel any attack in progress
			ph.getPcInstance().abortAttack();
			ph.getPcInstance().breakAttack();
			// Cancel any skill in progress
			ph.getPcInstance().abortCast();
			ph.getPcInstance().breakCast();
			// Cancel all character effects
			ph.getPcInstance().stopAllEffects();
			
			if (ph.getPcInstance().getSummon() != null)
			{
				ph.getPcInstance().getSummon().stopAllEffects();
				ph.getPcInstance().getSummon().unSummon(ph.getPcInstance());
			}
			
			// Cancel all character cubics
			for (L2CubicInstance cubic : ph.getPcInstance().getCubics().values())
			{
				cubic.cancelDisappear();
			}
		}
	}
	
	/**
	 * We prepare the player for the fight.<br>
	 * <ul>
	 * <b>Actions: </b>
	 * </ul>
	 * <li>We canceled the paralysis made in -> <u>prepareToTeleport()</u></li><br>
	 * <li>We deliver buffs defined in configs</li>
	 */
	public void prepareToFight()
	{
		for (PlayerHolder ph : getPlayerEventManager().getAllEventPlayers())
		{
			giveBuffPlayer(ph.getPcInstance());
		}
	}
	
	/**
	 * We prepare the player for the end of the event<br>
	 * <ul>
	 * <b>Actions: </b>
	 * </ul>
	 * <li>Cancel any attack in progress</li><br>
	 * <li>Cancel any skill in progress</li><br>
	 * <li>Cancel all effects</li><br>
	 * <li>Recover the title and color of the participants.</li><br>
	 * <li>We canceled the Team</li><br>
	 * <li>It out of the world we created for the event</li>
	 */
	public void prepareToEnd()
	{
		stopAllPendingRevive();
		
		for (PlayerHolder ph : getPlayerEventManager().getAllEventPlayers())
		{
			// Cancel target
			ph.getPcInstance().setTarget(null);
			// Cancel any attack in progress
			ph.getPcInstance().abortAttack();
			ph.getPcInstance().breakAttack();
			// Cancel any skill in progress<
			ph.getPcInstance().abortCast();
			ph.getPcInstance().breakCast();
			// Cancel all effects
			ph.getPcInstance().stopAllEffects();
			// Recover the title and color of the participants.
			ph.recoverOriginalColorTitle();
			ph.recoverOriginalTitle();
			// It out of the world created for the event
			
			InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(ph.getPcInstance());
			world.removeAllowed(ph.getPcInstance().getObjectId());
			ph.getPcInstance().setInstanceId(0);
			
			revivePlayer(ph);
			
			// FIXME We send a character to their actual instance and turn
			ph.getPcInstance().teleToLocation(83437, 148634, -3403, 0, 0);// GIRAN CENTER
		}
		getScheduledEventsManager().cancelTaskControlTime();
	}
	
	/**
	 * We generated a task to revive a character.<br>
	 * <ul>
	 * <b>Actions: </b>
	 * </ul>
	 * <li>Generate a pause before executing any action.</li><br>
	 * <li>Revive the character.</li><br>
	 * <li>We give you the buff depending on the event in which this.</li><br>
	 * <li>Teleport the character depending on the event in this.</li><br>
	 * <li>We do invulnerable for 5 seconds and not allow it to move.</li><br>
	 * <li>We canceled the invul and let you move</li><br>
	 * @param player
	 * @param time
	 * @param radiusTeleport
	 */
	public void giveResurrectPlayer(final PlayerHolder player, int time, int radiusTeleport)
	{
		try
		{
			EventUtil.sendEventMessage(player, MessageData.getInstance().getMsgByLang(player.getPcInstance(), "revive_in", true).replace("%time%", time + ""));
			
			_revivePending.add(ThreadPoolManager.getInstance().scheduleGeneral(() ->
			{
				revivePlayer(player);
				giveBuffPlayer(player.getPcInstance());
				teleportPlayer(player, radiusTeleport);
				
			} , time * 1000));
		}
		catch (Exception e)
		{
			LOGGER.warning(AbstractEvent.class.getSimpleName() + ": " + e);
			e.printStackTrace();
		}
	}
	
	/**
	 * Revive the player.<br>
	 * <ul>
	 * <b>Actions: </b>
	 * </ul>
	 * <li>Cancel the DecayTask.</li><br>
	 * <li>Revive the character.</li><br>
	 * <li>Set max cp, hp and mp.</li><br>
	 * @param ph
	 */
	protected void revivePlayer(PlayerHolder ph)
	{
		if (ph.getPcInstance().isDead())
		{
			DecayTaskManager.getInstance().cancel(ph.getPcInstance());
			ph.getPcInstance().doRevive();
			// heal to max
			ph.getPcInstance().setCurrentCp(ph.getPcInstance().getMaxCp());
			ph.getPcInstance().setCurrentHp(ph.getPcInstance().getMaxHp());
			ph.getPcInstance().setCurrentMp(ph.getPcInstance().getMaxMp());
		}
	}
	
	/**
	 * We give you the buff to a player seteados within configs
	 * @param player
	 */
	public void giveBuffPlayer(L2PcInstance player)
	{
		for (SkillHolder sh : BuffListData.getInstance().getBuffsPlayer(player))
		{
			sh.getSkill().applyEffects(player, player);
		}
	}
	
	/**
	 * We deliver the items in a list defined as<br>
	 * Created in order to deliver rewards in the events
	 * @param ph
	 * @param items
	 */
	public void giveItems(PlayerHolder ph, List<ItemHolder> items)
	{
		for (ItemHolder reward : items)
		{
			ph.getPcInstance().addItem("eventReward", reward.getId(), reward.getCount(), null, true);
		}
	}
}
