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
package net.sf.eventengine.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.eventengine.EventEngineManager;
import net.sf.eventengine.EventEngineWorld;
import net.sf.eventengine.datatables.BuffListData;
import net.sf.eventengine.datatables.ConfigData;
import net.sf.eventengine.datatables.MessageData;
import net.sf.eventengine.enums.EventState;
import net.sf.eventengine.holder.PlayerHolder;
import net.sf.eventengine.task.EventTask;
import net.sf.eventengine.util.EventUtil;

import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.data.xml.impl.NpcData;
import com.l2jserver.gameserver.datatables.SpawnTable;
import com.l2jserver.gameserver.enums.Team;
import com.l2jserver.gameserver.instancemanager.InstanceManager;
import com.l2jserver.gameserver.model.L2Spawn;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.L2Playable;
import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.instance.L2CubicInstance;
import com.l2jserver.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.gameserver.model.holders.SkillHolder;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jserver.gameserver.taskmanager.DecayTaskManager;
import com.l2jserver.util.Rnd;

/**
 * @author fissban
 */
public abstract class AbstractEvent
{
	private static final Logger LOGGER = Logger.getLogger(AbstractEvent.class.getName());
	
	public AbstractEvent()
	{
		// We add every player registered for the event.
		createEventPlayers();
		// We started the clock to control the sequence of internal events of the event.
		controlTimeEvent();
	}
	
	/** Necessary to keep track of the states of the event. */
	public abstract void runEventState(EventState state);
	
	// XXX DINAMIC INSTANCE ------------------------------------------------------------------------------
	private String _instanceFile = "";
	
	public void setInstanceFile(String instanceFile)
	{
		_instanceFile = instanceFile;
	}
	
	private final List<InstanceWorld> _instanceWorlds = new ArrayList<>();
	
	/**
	 * Create dynamic instances and a world for her
	 * @param count
	 * @return InstanceWorld
	 */
	public InstanceWorld createNewInstanceWorld()
	{
		InstanceWorld world = null;
		try
		{
			int instanceId = InstanceManager.getInstance().createDynamicInstance(_instanceFile);
			InstanceManager.getInstance().getInstance(instanceId).setAllowSummon(false);
			InstanceManager.getInstance().getInstance(instanceId).setPvPInstance(true);
			InstanceManager.getInstance().getInstance(instanceId).setEmptyDestroyTime(1000 + 60000L);
			// We closed the doors of the instance if there
			for (L2DoorInstance door : InstanceManager.getInstance().getInstance(instanceId).getDoors())
			{
				door.closeMe();
			}
			
			world = new EventEngineWorld();
			world.setInstanceId(instanceId);
			world.setTemplateId(100); // TODO hardcode
			world.setStatus(0);
			InstanceManager.getInstance().addWorld(world);
			_instanceWorlds.add(world);
			
		}
		catch (Exception e)
		{
			LOGGER.warning(EventEngineManager.class.getSimpleName() + ": -> createDynamicInstances() " + e);
			e.printStackTrace();
		}
		
		return world;
	}
	
	public List<InstanceWorld> getInstancesWorlds()
	{
		return _instanceWorlds;
	}
	
	// NPC IN EVENT --------------------------------------------------------------------------------- //
	
	// List of NPC in the event.
	private final Map<Integer, L2Npc> _eventNpc = new HashMap<>();
	
	/**
	 * We get the complete list of all the NPC during the event.<br>
	 * @return Collection<PlayerHolder>
	 */
	public Collection<L2Npc> getAllEventNpc()
	{
		return _eventNpc.values();
	}
	
	/**
	 * We generate a new spawn in our event and added to the list.
	 */
	public L2Npc addEventNpc(int npcId, Location loc, Team team, int instanceId)
	{
		return addEventNpc(npcId, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), team, false, instanceId);
	}
	
	/**
	 * We generate a new spawn in our event and added to the list.
	 */
	public L2Npc addEventNpc(int npcId, Location loc, Team team, boolean randomOffset, int instanceId)
	{
		return addEventNpc(npcId, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), team, randomOffset, instanceId);
	}
	
	/**
	 * We generate a new spawn in our event and added to the list.
	 * @param npcId
	 * @param x
	 * @param y
	 * @param z
	 * @param heading
	 * @param randomOffset -> +/- 1000
	 * @return L2Npc
	 */
	public L2Npc addEventNpc(int npcId, int x, int y, int z, int heading, Team team, boolean randomOffset, int instanceId)
	{
		// We generate our npc spawn
		L2Npc npc = null;
		try
		{
			L2NpcTemplate template = NpcData.getInstance().getTemplate(npcId);
			if (template != null)
			{
				if (randomOffset)
				{
					x += Rnd.get(-1000, 1000);
					y += Rnd.get(-1000, 1000);
				}
				
				L2Spawn spawn = new L2Spawn(template);
				spawn.setHeading(heading);
				spawn.setX(x);
				spawn.setY(y);
				spawn.setZ(z + 20);
				spawn.setAmount(1);
				spawn.setInstanceId(instanceId);
				npc = spawn.doSpawn();// isSummonSpawn.
				npc.setTeam(team);
				
				SpawnTable.getInstance().addNewSpawn(spawn, false);
				spawn.init();
				// animation.
				spawn.getLastSpawn().broadcastPacket(new MagicSkillUse(spawn.getLastSpawn(), spawn.getLastSpawn(), 1034, 1, 1, 1));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		// We add our npc to the list.
		_eventNpc.put(npc.getObjectId(), npc);
		
		return npc;
	}
	
	/**
	 * Clear all npc generated within our event.
	 */
	public void removeAllEventNpc()
	{
		for (L2Npc npc : _eventNpc.values())
		{
			if (npc == null)
			{
				continue;
			}
			
			// We stopped the npc spawn.
			npc.getSpawn().stopRespawn();
			// Delete the npc.
			npc.deleteMe();
		}
		
		_eventNpc.clear();
	}
	
	/**
	 * Check if a NPC belongs to our event.
	 * @param npcId
	 * @return
	 */
	public boolean isNpcInEvent(L2Npc npc)
	{
		return _eventNpc.containsValue(npc.getObjectId());
	}
	
	public void removeNpc(L2Npc npc)
	{
		// We stopped the npc spawn.
		npc.getSpawn().stopRespawn();
		// Delete the npc.
		npc.deleteMe();
		
		_eventNpc.remove(npc.getObjectId());
	}
	
	// SPAWNS TEAMS ---------------------------------------------------------------------------------- //
	private final Map<Team, Location> _teamSapwn = new HashMap<>();
	
	/**
	 * We define a team spawns.
	 * @param team
	 * @param loc
	 */
	public void setTeamSpawn(Team team, Location loc)
	{
		_teamSapwn.put(team, loc);
	}
	
	/**
	 * We get the spawn of a particular team.
	 * @param team
	 * @return Location
	 */
	public Location getTeamSpawn(Team team)
	{
		return _teamSapwn.get(team);
	}
	
	// PLAYERS IN EVENT ----------------------------------------------------------------------------- //
	private final Map<Integer, PlayerHolder> _eventPlayers = new HashMap<>();
	
	/**
	 * We obtain the full list of all players within an event.<br>
	 * @return Collection<PlayerHolder>
	 */
	public Collection<PlayerHolder> getAllEventPlayers()
	{
		return _eventPlayers.values();
	}
	
	/**
	 * We add all the characters registered to our list of characters in the event.<br>
	 * Check if player in olympiad.<br>
	 * Check if player in duel<br>
	 * Check if player in observer mode<br>
	 */
	private void createEventPlayers()
	{
		for (L2PcInstance player : EventEngineManager.getInstance().getAllRegisteredPlayers())
		{
			// Check if player in olympiad.
			if (player.isInOlympiadMode())
			{
				player.sendMessage("You can not attend the event being in the Olympics.");
				continue;
			}
			// Check if player in duel
			if (player.isInDuel())
			{
				player.sendMessage("You can not attend the event being in the Duel.");
				continue;
			}
			// Check if player in observer mode
			if (player.inObserverMode())
			{
				player.sendMessage("You can not attend the event being in the Observer mode.");
				continue;
			}
			
			_eventPlayers.put(player.getObjectId(), new PlayerHolder(player));
		}
		
		// We clean the list, no longer we need it.
		EventEngineManager.getInstance().clearRegisteredPlayers();
	}
	
	/**
	 * Check if the playable is participating in any event. In the case of a summon, verify that the owner participates <br>
	 * For not participate in an event is returned <u> false </u>
	 * @param player
	 * @return boolean
	 */
	public boolean isPlayableInEvent(L2Playable playable)
	{
		if (playable.isPlayer())
		{
			return _eventPlayers.containsKey(playable.getObjectId());
		}
		
		if (playable.isSummon())
		{
			return _eventPlayers.containsKey(((L2Summon) playable).getOwner().getObjectId());
		}
		
		return false;
	}
	
	/**
	 * Check if a player is participating in any event. <br>
	 * In the case of dealing with a summon you verify the owner. <br>
	 * For an event not perticipar returns <u> null </u>
	 * @param character
	 * @return PlayerHolder
	 */
	public PlayerHolder getEventPlayer(L2Character character)
	{
		if (character.isSummon())
		{
			return _eventPlayers.get(((L2Summon) character).getOwner().getObjectId());
		}
		
		if (character.isPlayer())
		{
			return _eventPlayers.get(character.getObjectId());
		}
		
		return null;
	}
	
	// LISTENERS ------------------------------------------------------------------------------------ //
	
	/**
	 * @param player
	 * @param target
	 */
	public void listenerOnInteract(L2PcInstance player, L2Npc target)
	{
		if (!isPlayableInEvent(player) && !isNpcInEvent(target))
		{
			return;
		}
		
		onInteract(getEventPlayer(player), target);
	}
	
	/**
	 * @param player
	 * @param npc
	 */
	public abstract void onInteract(PlayerHolder player, L2Npc npc);
	
	/**
	 * @param playable
	 * @param target
	 */
	public void listenerOnKill(L2Playable playable, L2Character target)
	{
		if (!isPlayableInEvent(playable))
		{
			return;
		}
		
		// ignoramos siempre si matan algun summon.
		// XXX se podria usar en algun evento...analizar!
		if (target.isSummon())
		{
			return;
		}
		
		onKill(getEventPlayer(playable), target);
	}
	
	/**
	 * @param player
	 * @param target
	 */
	public abstract void onKill(PlayerHolder player, L2Character target);
	
	/**
	 * @param player
	 */
	public void listenerOnDeath(L2PcInstance player)
	{
		if (!isPlayableInEvent(player))
		{
			return;
		}
		
		onDeath(getEventPlayer(player));
	}
	
	/**
	 * @param player
	 */
	public abstract void onDeath(PlayerHolder player);
	
	public boolean listenerOnAttack(L2Playable playable, L2Character target)
	{
		if (!isPlayableInEvent(playable))
		{
			return false;
		}
		
		// We get the player involved in our event.
		PlayerHolder activePlayer = getEventPlayer(playable);
		
		// CHECK FRIENDLY_FIRE ----------------------------------------
		if (ConfigData.getInstance().FRIENDLY_FIRE)
		{
			// If our target is L2Playable type and we do this in the event control.
			PlayerHolder activeTarget = getEventPlayer(target);
			
			if (activeTarget != null)
			{
				// AllVsAll style events do not have a defined team players.
				if ((activePlayer.getPcInstance().getTeam() == Team.NONE) || (activeTarget.getPcInstance().getTeam() == Team.NONE))
				{
					// Nothing
				}
				else if (activePlayer.getPcInstance().getTeam() == activeTarget.getPcInstance().getTeam())
				{
					return true;
				}
			}
		}
		// CHECK FRIENDLY_FIRE ----------------------------------------
		return onAttack(activePlayer, target);
	}
	
	/**
	 * @param player
	 * @param target
	 * @return true -> only in the event that an attack not want q continue its normal progress.
	 */
	public abstract boolean onAttack(PlayerHolder player, L2Character target);
	
	/**
	 * @param playable
	 * @param target
	 * @return true -> only in the event that an skill not want that continue its normal progress.
	 */
	public boolean listenerOnUseSkill(L2Playable playable, L2Character target, Skill skill)
	{
		if (!isPlayableInEvent(playable))
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
		PlayerHolder activePlayer = getEventPlayer(playable);
		
		// CHECK FRIENDLY_FIRE ----------------------------------------
		if (ConfigData.getInstance().FRIENDLY_FIRE)
		{
			// If our target is L2Playable type and we do this in the event control.
			PlayerHolder activeTarget = getEventPlayer(target);
			
			if ((activeTarget != null) && skill.isDamage())
			{
				// AllVsAll style events do not have a defined team players.
				if ((activePlayer.getPcInstance().getTeam() == Team.NONE) || (activeTarget.getPcInstance().getTeam() == Team.NONE))
				{
					// Nothing
				}
				else if (activePlayer.getPcInstance().getTeam() == activeTarget.getPcInstance().getTeam())
				{
					return true;
				}
			}
		}
		// CHECK FRIENDLY_FIRE ----------------------------------------
		
		return onUseSkill(activePlayer, target, skill);
	}
	
	/**
	 * @param player
	 * @param target
	 * @param skill
	 * @return true -> only in the event that an item not want that continue its normal progress.
	 */
	public abstract boolean onUseSkill(PlayerHolder player, L2Character target, Skill skill);
	
	/**
	 * @param player
	 * @param item
	 * @return -> only in the event that an skill not want q continue its normal progress.
	 */
	public boolean listenerOnUseItem(L2PcInstance player, L2Item item)
	{
		if (!isPlayableInEvent(player))
		{
			return false;
		}
		
		// We will not allow the use of pots or scroll.
		// XXX se podria setear como un config el tema de las pots
		if (item.isScroll() || item.isPotion())
		{
			return true;
		}
		
		return onUseItem(getEventPlayer(player), item);
	}
	
	/**
	 * @param player
	 * @param item
	 * @return true -> only in the event that an skill not want q continue its normal progress.
	 */
	public abstract boolean onUseItem(PlayerHolder player, L2Item item);
	
	public void listenerOnLogout(L2PcInstance player)
	{
		if (isPlayableInEvent(player))
		{
			try
			{
				PlayerHolder ph = getEventPlayer(player);
				// listener
				onLogout(ph);
				// recover the original color title
				ph.recoverOriginalColorTitle();
				// recover the original title
				ph.recoverOriginalTitle();
				// we remove the character of the created world
				InstanceManager.getInstance().getWorld(ph.getDinamicInstanceId()).removeAllowed(ph.getPcInstance().getObjectId());
				
				getAllEventPlayers().remove(ph);
			}
			catch (Exception e)
			{
				LOGGER.warning(EventEngineManager.class.getSimpleName() + ": -> listenerOnLogout() " + e);
				e.printStackTrace();
			}
		}
	}
	
	public abstract void onLogout(PlayerHolder player);
	
	// VARIOUS METHODS. -------------------------------------------------------------------------------- //
	
	/**
	 * Teleport to players of each team to their respective starting points<br>
	 */
	public void teleportAllPlayers(int radius)
	{
		for (PlayerHolder player : getAllEventPlayers())
		{
			teleportPlayer(player, radius);
		}
	}
	
	/**
	 * Teleport to a specific player to its original location within the event.
	 * @param player
	 */
	public void teleportPlayer(PlayerHolder player, int radius)
	{
		// get the spawn defined at the start of each event
		Location loc = getTeamSpawn(player.getPcInstance().getTeam());
		// verify if character is dead
		if (player.getPcInstance().isDead())
		{
			DecayTaskManager.getInstance().cancel(player.getPcInstance());
			player.getPcInstance().doRevive();
			// heal to max
			player.getPcInstance().setCurrentCp(player.getPcInstance().getMaxCp());
			player.getPcInstance().setCurrentHp(player.getPcInstance().getMaxHp());
			player.getPcInstance().setCurrentMp(player.getPcInstance().getMaxMp());
		}
		// teleport to character
		player.getPcInstance().teleToLocation(loc.getX() + Rnd.get(-radius, radius), loc.getY() + Rnd.get(-radius, radius), loc.getZ(), loc.getHeading(), player.getDinamicInstanceId());
		
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
		for (PlayerHolder player : getAllEventPlayers())
		{
			// Cancel target
			player.getPcInstance().setTarget(null);
			// Cancel any attack in progress
			player.getPcInstance().abortAttack();
			player.getPcInstance().breakAttack();
			// Cancel any skill in progress
			player.getPcInstance().abortCast();
			player.getPcInstance().breakCast();
			// Cancel all character effects
			player.getPcInstance().stopAllEffects();
			
			if (player.getPcInstance().getSummon() != null)
			{
				player.getPcInstance().getSummon().stopAllEffects();
				player.getPcInstance().getSummon().unSummon(player.getPcInstance());
			}
			
			// Cancel all character cubics
			for (L2CubicInstance cubic : player.getPcInstance().getCubics().values())
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
		for (PlayerHolder player : getAllEventPlayers())
		{
			giveBuffPlayer(player.getPcInstance());
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
		for (PlayerHolder player : getAllEventPlayers())
		{
			// Cancel target
			player.getPcInstance().setTarget(null);
			// Cancel any attack in progress
			player.getPcInstance().abortAttack();
			player.getPcInstance().breakAttack();
			// Cancel any skill in progress<
			player.getPcInstance().abortCast();
			player.getPcInstance().breakCast();
			// Cancel all effects
			player.getPcInstance().stopAllEffects();
			// Recover the title and color of the participants.
			player.recoverOriginalColorTitle();
			player.recoverOriginalTitle();
			// We canceled the Team
			player.getPcInstance().setTeam(Team.NONE);
			// It out of the world created for the event
			
			InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player.getPcInstance());
			world.removeAllowed(player.getPcInstance().getObjectId());
			player.getPcInstance().setInstanceId(0);
			
			// FIXME We send a character to their actual instance and turn
			player.getPcInstance().teleToLocation(83437, 148634, -3403, 0, 0);// GIRAN CENTER
		}
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
			
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					DecayTaskManager.getInstance().cancel(player.getPcInstance());
					player.getPcInstance().doRevive();
					// heal to max
					player.getPcInstance().setCurrentCp(player.getPcInstance().getMaxCp());
					player.getPcInstance().setCurrentHp(player.getPcInstance().getMaxHp());
					player.getPcInstance().setCurrentMp(player.getPcInstance().getMaxMp());
					// teleport
					teleportPlayer(player, radiusTeleport);
					// buff
					giveBuffPlayer(player.getPcInstance());
				}
				
			}, time * 1000);
		}
		catch (Exception e)
		{
			LOGGER.warning(AbstractEvent.class.getSimpleName() + ": " + e);
			e.printStackTrace();
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
	 * We control the timing of events<br>
	 * <ul>
	 * <b>Actions: </b>
	 * </ul>
	 * <li>-> step 1: We announced that participants will be teleported</li><br>
	 * <li>Wait 3 secs</li><br>
	 * <li>-> step 2: Adjust the status of the event -> START</li><br>
	 * <li>We hope 1 sec to actions within each event is executed..</li><br>
	 * <li>-> step 3: Adjust the status of the event -> FIGHT</li><br>
	 * <li>-> step 3: We sent a message that they are ready to fight.</li><br>
	 * <li>We wait until the event ends</li><br>
	 * <li>-> step 4: Adjust the status of the event -> END</li><br>
	 * <li>-> step 4: We sent a message warning that term event</li><br>
	 * <li>Esperamos 1 seg</li><br>
	 * <li>-> step 5: We alerted the event ended EventEngineManager</li><br>
	 */
	private void controlTimeEvent()
	{
		int time = 1000;
		ThreadPoolManager.getInstance().scheduleGeneral(new EventTask(1), time);
		time += 3000;
		ThreadPoolManager.getInstance().scheduleGeneral(new EventTask(2), time);
		time += 1000;
		ThreadPoolManager.getInstance().scheduleGeneral(new EventTask(3), time);
		time += ConfigData.getInstance().EVENT_DURATION * 60 * 1000;
		ThreadPoolManager.getInstance().scheduleGeneral(new EventTask(4), time);
		time += 1000;
		ThreadPoolManager.getInstance().scheduleGeneral(new EventTask(5), time);
	}
}
