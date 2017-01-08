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
package com.github.u3games.eventengine.model.base;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import com.github.u3games.eventengine.EventEngineManager;
import com.github.u3games.eventengine.builders.TeamsBuilder;
import com.github.u3games.eventengine.config.BaseConfigLoader;
import com.github.u3games.eventengine.config.interfaces.EventConfig;
import com.github.u3games.eventengine.config.model.MainEventConfig;
import com.github.u3games.eventengine.datatables.BuffListData;
import com.github.u3games.eventengine.datatables.MessageData;
import com.github.u3games.eventengine.dispatcher.ListenerDispatcher;
import com.github.u3games.eventengine.dispatcher.events.*;
import com.github.u3games.eventengine.enums.EventState;
import com.github.u3games.eventengine.enums.ListenerType;
import com.github.u3games.eventengine.enums.TeamType;
import com.github.u3games.eventengine.events.handler.managers.AntiAfkManager;
import com.github.u3games.eventengine.events.handler.managers.InstanceWorldManager;
import com.github.u3games.eventengine.events.handler.managers.PlayersManager;
import com.github.u3games.eventengine.events.handler.managers.ScheduledEventsManager;
import com.github.u3games.eventengine.events.handler.managers.SpawnManager;
import com.github.u3games.eventengine.events.handler.managers.TeamsManagers;
import com.github.u3games.eventengine.events.schedules.AnnounceNearEndEvent;
import com.github.u3games.eventengine.events.schedules.AnnounceTeleportEvent;
import com.github.u3games.eventengine.events.schedules.ChangeToEndEvent;
import com.github.u3games.eventengine.events.schedules.ChangeToFightEvent;
import com.github.u3games.eventengine.events.schedules.ChangeToStartEvent;
import com.github.u3games.eventengine.interfaces.IListenerSuscriber;
import com.github.u3games.eventengine.model.entities.Player;
import com.github.u3games.eventengine.util.EventUtil;
import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.L2Playable;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.util.Rnd;

/**
 * @author fissban
 */
public abstract class BaseEvent<T extends EventConfig> implements IListenerSuscriber
{
	// Logger
	private static final Logger LOGGER = Logger.getLogger(BaseEvent.class.getName());
	// Max delay time for reuse skill
	private static final int MAX_DELAY_TIME_SKILL = 900000;

	private T _config;

	protected abstract String getInstanceFile();

	public void initialize()
	{
		// Add every player registered for the event
		getPlayerEventManager().createEventPlayers();

		if (getMainConfig().isAntiAfkEnabled())
		{
			_antiAfkManager = new AntiAfkManager();
		}
		initScheduledEvents();
		// Starts the clock to control the sequence of internal events of the event
		getScheduledEventsManager().startTaskControlTime();
		getInstanceWorldManager().setInstanceFile(getInstanceFile());
	}

	public void setConfig(T config)
	{
		_config = config;
	}

	protected T getConfig()
	{
		return _config;
	}

	private MainEventConfig getMainConfig()
	{
		return BaseConfigLoader.getInstance().getMainConfig();
	}
	
	/**
	 * Necessary to handle the event states.
	 * @param state
	 */
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
				ListenerDispatcher.getInstance().removeSuscriber(this);
				onEventEnd();
				prepareToEnd();
				break;
		}
	}
	
	protected abstract TeamsBuilder onCreateTeams();
	
	protected abstract void onEventStart();
	
	protected abstract void onEventFight();
	
	protected abstract void onEventEnd();
	
	// XXX ANTI AFK SYSTEM -------------------------------------------------------------------------------
	private AntiAfkManager _antiAfkManager;
	
	public AntiAfkManager getAntiAfkManager()
	{
		return _antiAfkManager;
	}
	
	// XXX TEAMS -----------------------------------------------------------------------------------------
	private final TeamsManagers _teamsManagers = new TeamsManagers();
	
	public TeamsManagers getTeamsManager()
	{
		return _teamsManagers;
	}
	
	// XXX DINAMIC INSTANCE ------------------------------------------------------------------------------
	private final InstanceWorldManager _instanceWorldManager = new InstanceWorldManager();
	
	public InstanceWorldManager getInstanceWorldManager()
	{
		return _instanceWorldManager;
	}
	
	// XXX SCHEDULED AND UNSCHEDULED EVENTS --------------------------------------------------------------
	private final ScheduledEventsManager _scheduledEventsManager = new ScheduledEventsManager();
	
	public ScheduledEventsManager getScheduledEventsManager()
	{
		return _scheduledEventsManager;
	}
	
	// XXX TELEPORT --------------------------------------------------------------
	protected int _radius = 50;
	
	/**
	 * Init the scheduled events.
	 * <ul>
	 * <b>Actions:</b>
	 * </ul>
	 * <li>Step 1: Announce participants will be teleported.</li>
	 * <li>Wait 3 secs.</li>
	 * <li>Step 2: Adjust the status of the event -> START.</li>
	 * <li>We hope 1 sec to actions within each event is executed.</li>
	 * <li>Step 3: Adjust the status of the event -> FIGHT.</li>
	 * <li>Step 4: We sent a message that they are ready to fight.</li>
	 * <li>We wait until the event ends.</li>
	 * <li>Step 5: Adjust the status of the event -> END.</li>
	 * <li>Step 6: We sent a message warning that term event.</li>
	 * <li>Wait for 1 sec.</li>
	 * <li>Step 7: Alert the event has ended.</li>
	 */
	private void initScheduledEvents()
	{
		int time = 1000;
		getScheduledEventsManager().addScheduledEvent(new AnnounceTeleportEvent(time));
		time += 3000;
		getScheduledEventsManager().addScheduledEvent(new ChangeToStartEvent(time));
		time += 1000;
		getScheduledEventsManager().addScheduledEvent(new ChangeToFightEvent(time));
		time += getMainConfig().getRunningTime() * 60 * 1000;
		getScheduledEventsManager().addScheduledEvent(new ChangeToEndEvent(time));
		// Announce near end event
		int timeLeftAnnounce = getMainConfig().getTextTimeForEnd() * 1000;
		getScheduledEventsManager().addScheduledEvent(new AnnounceNearEndEvent(time - timeLeftAnnounce, getMainConfig().getTextTimeForEnd()));
		getScheduledEventsManager().addScheduledEvent(new AnnounceNearEndEvent(time - (timeLeftAnnounce / 2), getMainConfig().getTextTimeForEnd() / 2));
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
	private final SpawnManager _spawnManager = new SpawnManager();
	
	public SpawnManager getSpawnManager()
	{
		return _spawnManager;
	}
	
	// PLAYERS IN EVENT ----------------------------------------------------------------------------- //
	private final PlayersManager _playerEventManager = new PlayersManager();
	
	public PlayersManager getPlayerEventManager()
	{
		return _playerEventManager;
	}
	
	// LISTENERS ------------------------------------------------------------------------------------ //
	/**
	 * @param event
	 */
	@Override
	public final void listenerOnInteract(OnInteractEvent event)
	{
		L2PcInstance player = event.getPlayer();
		L2Npc target = event.getNpc();

		if (!getPlayerEventManager().isPlayableInEvent(player) || !getSpawnManager().isNpcInEvent(target))
		{
			return;
		}
		// Get the player involved in our event
		Player activePlayer = getPlayerEventManager().getEventPlayer(player);
		// Exclude the player from the next Anti Afk control
		if (getAntiAfkManager() != null)
		{
			getAntiAfkManager().excludePlayer(activePlayer);
		}
		onInteract(event);
	}

	/**
	 * @param event
	 */
	protected void onInteract(OnInteractEvent event) {}
	
	/**
	 * @param event
	 */
	@Override
	public final void listenerOnKill(OnKillEvent event)
	{
		L2Playable playable = event.getAttacker();
		L2Character target = event.getTarget();

		if (!getPlayerEventManager().isPlayableInEvent(playable))
		{
			return;
		}
		// We ignore if they kill any summon
		// XXX It could be used in some event...analyze!
		if (target.isSummon())
		{
			return;
		}
		// Get the player involved in our event
		Player activePlayer = getPlayerEventManager().getEventPlayer(playable);
		// Exclude the player from the next Anti Afk control
		if (getAntiAfkManager() != null)
		{
			getAntiAfkManager().excludePlayer(activePlayer);
		}
		onKill(event);
	}

	/**
	 * @param event
	 */
	protected void onKill(OnKillEvent event) {}
	
	/**
	 * @param event
	 */
	@Override
	public final void listenerOnDeath(OnDeathEvent event)
	{
		L2PcInstance player = event.getTarget();

		if (!getPlayerEventManager().isPlayableInEvent(player))
		{
			return;
		}
		onDeath(event);
	}

	/**
	 * @param event
	 */
	protected void onDeath(OnDeathEvent event) {}

	@Override
	public final void listenerOnAttack(OnAttackEvent event)
	{
		L2Playable playable = event.getAttacker();
		L2Character target = event.getTarget();

		if (!getPlayerEventManager().isPlayableInEvent(playable))
		{
			event.setCancel(true);
			return;
		}
		// We get the player involved in our event
		Player activePlayer = getPlayerEventManager().getEventPlayer(playable);
		
		// Remove the spawn protection time
		if (activePlayer.getProtectionTimeEnd() > 0)
		{
			activePlayer.setProtectionTimeEnd(0);
			activePlayer.sendMessage(MessageData.getInstance().getMsgByLang(activePlayer, "spawnprotection_ended", false));
		}
		
		// Exclude the player from the next Anti Afk control
		if (getAntiAfkManager() != null)
		{
			getAntiAfkManager().excludePlayer(activePlayer);
		}
		
		// If our target is L2Playable type and we do this in the event control
		Player activeTarget = getPlayerEventManager().getEventPlayer(target);
		
		if (activeTarget != null)
		{
			if (activeTarget.isProtected())
			{
				activePlayer.sendMessage(MessageData.getInstance().getMsgByLang(activePlayer, "spawnprotection_protected", false));
				return;
			}
			// Check Friendly Fire
			if (!getMainConfig().isFriendlyFireEnabled())
			{
				if (activePlayer.getTeamType() == activeTarget.getTeamType())
				{
					if ((activePlayer.getTeamType() != TeamType.WHITE) || (activeTarget.getTeamType() != TeamType.WHITE))
					{
						return;
					}
				}
			}
		}
		onAttack(event);
	}

	/**
	 * @param event
	 */
	protected void onAttack(OnAttackEvent event) {}
	
	/**
	 * @param event
	 * @return true only in the event that an skill not want that continue its normal progress.
	 */
	@Override
	public final void listenerOnUseSkill(OnUseSkillEvent event)
	{
		L2Playable playable = event.getCaster();
		L2Character target = event.getTarget();
		Skill skill = event.getSkill();

		if (!getPlayerEventManager().isPlayableInEvent(playable))
		{
			event.setCancel(true);
			return;
		}
		// If the character has no target to finish the listener.
		// XXX Perhaps in any event it is required to use skills without target... check!
		if (target == null)
		{
			event.setCancel(true);
			return;
		}
		// If the character is using a skill on itself end the listener
		if (playable.equals(target))
		{
			event.setCancel(true);
			return;
		}
		// We get the player involved in our event
		Player activePlayer = getPlayerEventManager().getEventPlayer(playable);
		// Exclude the player from the next Anti Afk control
		if (getAntiAfkManager() != null)
		{
			getAntiAfkManager().excludePlayer(activePlayer);
		}
		// If our target is L2Playable type and we do this in the event control
		Player activeTarget = getPlayerEventManager().getEventPlayer(target);
		if (activeTarget != null)
		{
			if (activeTarget.isProtected())
			{
				activePlayer.sendMessage(MessageData.getInstance().getMsgByLang(activePlayer, "spawnprotection_protected", false));
				return;
			}
			
			if ((skill.isDamage() || skill.isDebuff()))
			{
				// Remove the spawn protection time
				if (activePlayer.getProtectionTimeEnd() > 0)
				{
					activePlayer.setProtectionTimeEnd(0);
					activePlayer.sendMessage(MessageData.getInstance().getMsgByLang(activePlayer, "spawnprotection_ended", false));
				}
				
				// Check Friendly Fire
				if (!getMainConfig().isFriendlyFireEnabled() && (activePlayer.getTeamType() == activeTarget.getTeamType()))
				{
					if ((activePlayer.getTeamType() != TeamType.WHITE) || (activeTarget.getTeamType() != TeamType.WHITE))
					{
						return;
					}
				}
			}
		}
		onUseSkill(event);
	}

	/**
	 * @param event
	 */
	protected void onUseSkill(OnUseSkillEvent event) {}
	
	/**
	 * @param event
	 * @return Only in the event that an skill not want that continue its normal progress.
	 */
	@Override
	public final void listenerOnUseItem(OnUseItemEvent event)
	{
		L2PcInstance player = event.getPlayer();
		L2Item item = event.getItem();

		if (!getPlayerEventManager().isPlayableInEvent(player))
		{
			event.setCancel(true);
			return;
		}
		// We will not allow the use of pots or scroll
		// XXX It could be set as a theme config pots
		if (item.isScroll() || item.isPotion())
		{
			return;
		}
		Player activePlayer = getPlayerEventManager().getEventPlayer(player);
		// Exclude the player from the next Anti Afk control
		if (getAntiAfkManager() != null)
		{
			getAntiAfkManager().excludePlayer(activePlayer);
		}
		onUseItem(event);
	}

	/**
	 * @param event
	 */
	protected void onUseItem(OnUseItemEvent event) {}

	@Override
	public void listenerOnLogin(OnLogInEvent event) {
		L2PcInstance player = event.getPlayer();

		if (getPlayerEventManager().isPlayableInEvent(player))
		{
			onLogin(event);
		}
	}

	protected void onLogin(OnLogInEvent event) {}

	@Override
	public final void listenerOnLogout(OnLogOutEvent event)
	{
		L2PcInstance player = event.getPlayer();

		if (getPlayerEventManager().isPlayableInEvent(player))
		{
			try
			{
				Player activePlayer = getPlayerEventManager().getEventPlayer(player);
				// Listener
				onLogout(event);
				removePlayerFromEvent(activePlayer, true);
				EventEngineManager.getInstance().addPlayerDisconnected(activePlayer);
			}
			catch (Exception e)
			{
				LOGGER.warning(EventEngineManager.class.getSimpleName() + ": listenerOnLogout() " + e);
				e.printStackTrace();
			}
		}
	}

	protected void onLogout(OnLogOutEvent event) {}
	
	// VARIOUS METHODS. -------------------------------------------------------------------------------- //
	
	/**
	 * Teleport to players of each team to their respective starting points.
	 * @param radius
	 */
	protected void teleportAllPlayers(int radius)
	{
		for (Player ph : getPlayerEventManager().getAllEventPlayers())
		{
			ph.revive(getMainConfig().getSpawnProtectionTime());
			teleportPlayer(ph, radius);
		}
	}
	
	/**
	 * Teleport to a specific player to its original location within the event.
	 * @param ph
	 * @param radius
	 */
	protected void teleportPlayer(Player ph, int radius)
	{
		// Get the spawn defined at the start of each event
		Location loc = getTeamsManager().getTeamSpawn(ph.getTeamType());
		loc.setInstanceId(ph.getWorldInstanceId());
		loc.setX(loc.getX() + Rnd.get(-radius, radius));
		loc.setY(loc.getY() + Rnd.get(-radius, radius));
		// teleport to character
		ph.getPcInstance().teleToLocation(loc, false);
	}

	protected void teleportPlayer(Player ph)
	{
		teleportPlayer(ph, 0);
	}
	
	/**
	 * Prepare players, teams and the instance to start.
	 * <ul>
	 * <b>Actions:</b>
	 * </ul>
	 * <li>Cancel any player attack in progress.</li>
	 * <li>Cancel any player skill in progress.</li>
	 * <li>Paralyzed the player.</li>
	 * <li>Cancel all character effects.</li>
	 * <li>Cancel summon pet.</li>
	 * <li>Cancel all character cubics.</li>
	 * <li>Save the return player location.</li>
	 * <li>Create the teams.</li>
	 * <li>Create the instance world.</li>
	 */
	public void prepareToStart()
	{
		_teamsManagers.createTeams(onCreateTeams());
		InstanceWorld world = _instanceWorldManager.createNewInstanceWorld();

		for (Player player : getPlayerEventManager().getAllEventPlayers())
		{
			player.cancelAllActions();
			player.cancelAllEffects();
			player.addToEvent();
			player.setInstanceWorld(world);
			teleportPlayer(player, _radius);
			player.setProtectionTimeEnd(System.currentTimeMillis() + (getMainConfig().getSpawnProtectionTime() * 1000)); // Milliseconds
		}
	}
	
	/**
	 * We prepare the player for the fight.
	 * <ul>
	 * <b>Actions:</b>
	 * </ul>
	 * <li>We canceled the paralysis made in <u>prepareToTeleport().</u></li>
	 * <li>We deliver buffs defined in configs.</li>
	 */
	public void prepareToFight()
	{
		for (Player ph : getPlayerEventManager().getAllEventPlayers())
		{
			ph.giveBuffs(BuffListData.getInstance().getBuffsPlayer(ph));
		}
	}
	
	/**
	 * We prepare the player for the end of the event.
	 * <ul>
	 * <b>Actions:</b>
	 * </ul>
	 * <li>Cancel any attack in progress.</li>
	 * <li>Cancel any skill in progress.</li>
	 * <li>Cancel all effects.</li>
	 * <li>Recover the title and color of the participants.</li>
	 * <li>We canceled the Team.</li>
	 * <li>It out of the world we created for the event.</li>
	 */
	public void prepareToEnd()
	{
		stopAllPendingRevive();
		for (Player ph : getPlayerEventManager().getAllEventPlayers())
		{
			ph.revive(getMainConfig().getSpawnProtectionTime());
			ph.cancelAllActions();
			ph.cancelAllEffects();
			removePlayerFromEvent(ph, false);
		}
		getScheduledEventsManager().cancelTaskControlTime();
		getInstanceWorldManager().destroyWorldInstances();
	}
	
	/**
	 * We generated a task to revive a character.
	 * <ul>
	 * <b>Actions:</b>
	 * </ul>
	 * <li>Generate a pause before executing any action.</li>
	 * <li>Revive the character.</li>
	 * <li>We give you the buff depending on the event in which this.</li>
	 * <li>Teleport the character depending on the event in this.</li>
	 * <li>We do invulnerable for 5 seconds and not allow it to move.</li>
	 * <li>We canceled the invul and let you move.</li>
	 * @param player
	 * @param time
	 * @param radiusTeleport
	 */
	public void scheduleRevivePlayer(final Player player, int time, int radiusTeleport)
	{
		try
		{
			EventUtil.sendEventMessage(player, MessageData.getInstance().getMsgByLang(player.getPcInstance(), "revive_in", true).replace("%time%", time + ""));
			_revivePending.add(ThreadPoolManager.getInstance().scheduleGeneral(() ->
			{
				player.revive(getMainConfig().getSpawnProtectionTime());
				player.giveBuffs(BuffListData.getInstance().getBuffsPlayer(player));
				teleportPlayer(player);
			}, time * 1000));
		}
		catch (Exception e)
		{
			LOGGER.warning(BaseEvent.class.getSimpleName() + ": " + e);
			e.printStackTrace();
		}
	}

	public void scheduleRevivePlayer(final Player player, int time)
	{
		scheduleRevivePlayer(player, time, 0);
	}
	
	/**
	 * <ul>
	 * <b>Actions:</b>
	 * </ul>
	 * <li>Recover original title.</li>
	 * <li>Recover original color title.</li>
	 * <li>Remove from instance and back 0</li>
	 * @param ph
	 * @param forceRemove
	 */
	public void removePlayerFromEvent(Player ph, boolean forceRemove)
	{
		if (forceRemove) getPlayerEventManager().getAllEventPlayers().remove(ph);
		ph.teleportTo(ph.getReturnLocation());
		ph.removeFromEvent();
	}

	/**
	 * <ul>
	 * <b>Actions:</b>
	 * </ul>
	 * <li>Add a suscription to one type of events</li>
	 * @param type: ListenerType
	 */
	protected final void addSuscription(ListenerType type)
	{
		ListenerDispatcher.getInstance().addSuscription(type, this);
	}

	/**
	 * <ul>
	 * <b>Actions:</b>
	 * </ul>
	 * <li>Remove the suscription to one type of events</li>
	 * @param type: ListenerType
     */
	protected final void removeSuscription(ListenerType type)
	{
		ListenerDispatcher.getInstance().removeSuscription(type, this);
	}
}