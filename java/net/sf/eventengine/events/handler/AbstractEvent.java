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

import net.sf.eventengine.EventEngineManager;
import net.sf.eventengine.builders.TeamsBuilder;
import net.sf.eventengine.datatables.BuffListData;
import net.sf.eventengine.datatables.ConfigData;
import net.sf.eventengine.datatables.MessageData;
import net.sf.eventengine.enums.EventState;
import net.sf.eventengine.enums.TeamType;
import net.sf.eventengine.events.handler.managers.AntiAfkManager;
import net.sf.eventengine.events.handler.managers.InstanceWorldManager;
import net.sf.eventengine.events.handler.managers.PlayersManager;
import net.sf.eventengine.events.handler.managers.ScheduledEventsManager;
import net.sf.eventengine.events.handler.managers.SpawnManager;
import net.sf.eventengine.events.handler.managers.TeamsManagers;
import net.sf.eventengine.events.holders.NpcHolder;
import net.sf.eventengine.events.holders.PlayerHolder;
import net.sf.eventengine.events.listeners.EventEngineListener;
import net.sf.eventengine.events.schedules.AnnounceNearEndEvent;
import net.sf.eventengine.events.schedules.AnnounceTeleportEvent;
import net.sf.eventengine.events.schedules.ChangeToEndEvent;
import net.sf.eventengine.events.schedules.ChangeToFightEvent;
import net.sf.eventengine.events.schedules.ChangeToStartEvent;
import net.sf.eventengine.util.EventUtil;

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

/**
 * @author fissban
 */
public abstract class AbstractEvent
{
	private static final Logger LOGGER = Logger.getLogger(AbstractEvent.class.getName());
	
	public AbstractEvent(String instanceFile)
	{
		// Add every player registered for the event.
		getPlayerEventManager().createEventPlayers();
		
		if (ConfigData.getInstance().ANTI_AFK_ENABLED)
		{
			_antiAfkManager = new AntiAfkManager();
		}
		
		initScheduledEvents();
		// Starts the clock to control the sequence of internal events of the event.
		getScheduledEventsManager().startTaskControlTime();
		
		getInstanceWorldManager().setInstanceFile(instanceFile);
	}
	
	/** Necessary to handle the event states. */
	public final void runEventState(EventState state)
	{
		switch (state)
		{
			case START:
				prepareToStart();
				initTeleportAllPlayers();
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
	
	// XXX TELEPORT --------------------------------------------------------------
	
	protected int _radius = 50;
	
	/**
	 * Init the scheduled events<br>
	 * <ul>
	 * <b>Actions: </b>
	 * </ul>
	 * <li>-> step 1: Announce participants will be teleported</li><br>
	 * <li>Wait 3 secs</li><br>
	 * <li>-> step 2: Adjust the status of the event -> START</li><br>
	 * <li>We hope 1 sec to actions within each event is executed..</li><br>
	 * <li>-> step 3: Adjust the status of the event -> FIGHT</li><br>
	 * <li>-> step 3: We sent a message that they are ready to fight.</li><br>
	 * <li>We wait until the event ends</li><br>
	 * <li>-> step 4: Adjust the status of the event -> END</li><br>
	 * <li>-> step 4: We sent a message warning that term event</li><br>
	 * <li>Wait for 1 seg</li><br>
	 * <li>-> step 5: Alert the event has ended</li><br>
	 */
	private void initScheduledEvents()
	{
		int time = 1000;
		getScheduledEventsManager().addScheduledEvent(new AnnounceTeleportEvent(time));
		time += 3000;
		getScheduledEventsManager().addScheduledEvent(new ChangeToStartEvent(time));
		time += 1000;
		getScheduledEventsManager().addScheduledEvent(new ChangeToFightEvent(time));
		time += ConfigData.getInstance().EVENT_DURATION * 60 * 1000;
		getScheduledEventsManager().addScheduledEvent(new ChangeToEndEvent(time));
		
		// Announce near end event
		int timeLeftAnnounce = ConfigData.getInstance().EVENT_TEXT_TIME_FOR_END * 1000;
		getScheduledEventsManager().addScheduledEvent(new AnnounceNearEndEvent(time - timeLeftAnnounce, ConfigData.getInstance().EVENT_TEXT_TIME_FOR_END));
		getScheduledEventsManager().addScheduledEvent(new AnnounceNearEndEvent(time - (timeLeftAnnounce / 2), ConfigData.getInstance().EVENT_TEXT_TIME_FOR_END / 2));
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
	 * @param player
	 * @param target
	 */
	public void listenerOnInteract(L2PcInstance player, L2Npc target)
	{
		if (!getPlayerEventManager().isPlayableInEvent(player) || !getSpawnManager().isNpcInEvent(target))
		{
			return;
		}
		
		// Get the player involved in our event.
		PlayerHolder activePlayer = getPlayerEventManager().getEventPlayer(player);
		
		// Exclude the player from the next Anti Afk control
		if (getAntiAfkManager() != null)
		{
			getAntiAfkManager().excludePlayer(activePlayer);
		}
		
		onInteract(activePlayer, getSpawnManager().getEventNpc(target));
	}
	
	/**
	 * @param ph
	 * @param npc
	 */
	public void onInteract(PlayerHolder ph, NpcHolder npc)
	{
		// Nothing
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
		
		// Get the player involved in our event.
		PlayerHolder activePlayer = getPlayerEventManager().getEventPlayer(playable);
		
		// Exclude the player from the next Anti Afk control
		if (getAntiAfkManager() != null)
		{
			getAntiAfkManager().excludePlayer(activePlayer);
		}
		
		onKill(activePlayer, target);
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
		
		// Exclude the player from the next Anti Afk control
		if (getAntiAfkManager() != null)
		{
			getAntiAfkManager().excludePlayer(activePlayer);
		}
		
		// CHECK FRIENDLY_FIRE ----------------------------------------
		if (!ConfigData.getInstance().FRIENDLY_FIRE)
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
		
		// Exclude the player from the next Anti Afk control
		if (getAntiAfkManager() != null)
		{
			getAntiAfkManager().excludePlayer(activePlayer);
		}
		
		// CHECK FRIENDLY_FIRE ----------------------------------------
		if (!ConfigData.getInstance().FRIENDLY_FIRE)
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
		
		PlayerHolder activePlayer = getPlayerEventManager().getEventPlayer(player);
		
		// Exclude the player from the next Anti Afk control
		if (getAntiAfkManager() != null)
		{
			getAntiAfkManager().excludePlayer(activePlayer);
		}
		
		return onUseItem(activePlayer, item);
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
				PlayerHolder activePlayer = getPlayerEventManager().getEventPlayer(player);
				// Listener
				onLogout(activePlayer);
				removePlayerFromEvent(activePlayer, true);
				EventEngineManager.getInstance().addPlayerDisconnected(activePlayer);
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
	
	protected void initTeleportAllPlayers()
	{
		InstanceWorld world = getInstanceWorldManager().getAllInstancesWorlds().get(0);
		
		for (PlayerHolder ph : getPlayerEventManager().getAllEventPlayers())
		{
			// Adjust the instance that owns the character
			ph.setDinamicInstanceId(world.getInstanceId());
			// We add the character to the world and then be teleported
			world.addAllowed(ph.getPcInstance().getObjectId());
			teleportPlayer(ph, _radius);
		}
	}
	
	/**
	 * Teleport to players of each team to their respective starting points<br>
	 */
	protected void teleportAllPlayers(int radius)
	{
		for (PlayerHolder ph : getPlayerEventManager().getAllEventPlayers())
		{
			revivePlayer(ph);
			teleportPlayer(ph, radius);
		}
	}
	
	/**
	 * Teleport to a specific player to its original location within the event.
	 * @param ph
	 */
	protected void teleportPlayer(PlayerHolder ph, int radius)
	{
		// Get the spawn defined at the start of each event
		Location loc = getTeamsManager().getTeamSpawn(ph.getTeamType());
		
		loc.setInstanceId(ph.getDinamicInstanceId());
		loc.setX(loc.getX() + Rnd.get(-radius, radius));
		loc.setY(loc.getY() + Rnd.get(-radius, radius));
		
		// teleport to character
		ph.getPcInstance().teleToLocation(loc, false);
	}
	
	/**
	 * Prepare players, teams and the instance to start<br>
	 * <ul>
	 * <b>Actions: </b>
	 * </ul>
	 * <li>Cancel any player attack in progress</li><br>
	 * <li>Cancel any player skill in progress</li><br>
	 * <li>Paralyzed the player</li><br>
	 * <li>Cancel all character effects</li><br>
	 * <li>Cancel summon pet</li><br>
	 * <li>Cancel all character cubics</li><br>
	 * <li>Save the return player location</li><br>
	 * <li>Create the teams</li><br>
	 * <li>Create the instance world</li><br>
	 */
	public void prepareToStart()
	{
		for (PlayerHolder ph : getPlayerEventManager().getAllEventPlayers())
		{
			cancelAllPlayerActions(ph);
			cancelAllEffects(ph);
			
			Location returnLoc = ph.getPcInstance().getLocation();
			ph.setReturnLoc(new Location(returnLoc.getX(), returnLoc.getY(), returnLoc.getZ()));
		}
		
		_teamsManagers.createTeams(onCreateTeams());
		_instanceWorldManager.createNewInstanceWorld();
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
			cancelAllPlayerActions(ph);
			cancelAllEffects(ph);
			removePlayerFromEvent(ph, false);
			revivePlayer(ph);
		}
		getScheduledEventsManager().cancelTaskControlTime();
		getInstanceWorldManager().destroyWorldInstances();
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
				
			}, time * 1000));
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
	
	/**
	 * <ul>
	 * <b>Actions: </b>
	 * </ul>
	 * <li>Cancel target</li> <li>Cancel cast</li> <li>Cancel attack</li>
	 * @param ph
	 */
	public void cancelAllPlayerActions(PlayerHolder ph)
	{
		// Cancel target
		ph.getPcInstance().setTarget(null);
		// Cancel any attack in progress
		ph.getPcInstance().breakAttack();
		// Cancel any skill in progress
		ph.getPcInstance().breakCast();
	}
	
	/**
	 * <ul>
	 * <b>Actions: </b>
	 * </ul>
	 * <li>Stop all effects from player and summon</li>
	 * @param ph
	 */
	public void cancelAllEffects(PlayerHolder ph)
	{
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
	
	/**
	 * <ul>
	 * <b>Actions: </b>
	 * </ul>
	 * <li>Recover original title</li> <li>Recover original color title</li> <li>Remove from instance and back 0</li>
	 * @param ph
	 * @param forceRemove
	 * @param logout
	 */
	public void removePlayerFromEvent(PlayerHolder ph, boolean forceRemove)
	{
		// Recovers player's title and color
		ph.recoverOriginalColorTitle();
		ph.recoverOriginalTitle();
		
		// Remove the player from world instance
		InstanceManager.getInstance().getPlayerWorld(ph.getPcInstance()).removeAllowed(ph.getPcInstance().getObjectId());
		ph.getPcInstance().setInstanceId(0);
		
		// Remove the player from event listener (it's used to deny the manual ress)
		ph.getPcInstance().removeEventListener(EventEngineListener.class);
		
		if (forceRemove)
		{
			getPlayerEventManager().getAllEventPlayers().remove(ph);
		}
		
		ph.getPcInstance().teleToLocation(ph.getReturnLoc());
	}
}
