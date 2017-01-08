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
package com.github.u3games.eventengine.model.entities;

import com.github.u3games.eventengine.enums.ScoreType;
import com.github.u3games.eventengine.enums.TeamType;
import com.github.u3games.eventengine.events.holders.TeamHolder;
import com.github.u3games.eventengine.events.listeners.EventEngineListener;
import com.github.u3games.eventengine.interfaces.ParticipantHolder;
import com.github.u3games.eventengine.model.ELocation;
import com.l2jserver.gameserver.instancemanager.InstanceManager;
import com.l2jserver.gameserver.model.L2Party;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.instance.L2CubicInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.gameserver.model.holders.SkillHolder;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.network.serverpackets.SkillCoolTime;
import com.l2jserver.gameserver.taskmanager.DecayTaskManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * It manages player's info that participates in an event.
 */
public class Player extends Character implements ParticipantHolder
{
	private EventPlayerStatus mEventPlayerStatus;

	private int mWorldInstance = 0;
	
	// Variable used to know the protection time inside events
	private long mProtectionTimeEnd = 0;

	public Player(int objectId) {
		super(objectId);
	}

	/**
	 * Get L2PcInstance.
	 * @return
	 */
	public L2PcInstance getPcInstance() {
		return L2World.getInstance().getPlayer(getObjectId());
	}

	public void teleportTo(ELocation location) {
		getPcInstance().teleToLocation(location.getLocation());
	}

	public void addToEvent() {
		mEventPlayerStatus = new EventPlayerStatus();
		mEventPlayerStatus._oriColorTitle = getPcInstance().getAppearance().getTitleColor();
		mEventPlayerStatus._oriTitle = getPcInstance().getTitle();
		mEventPlayerStatus._returnLocation = new ELocation(getPcInstance().getLocation());
	}

	public void removeFromEvent() {
		// Recovers player's title and color
		getPcInstance().setTitle(mEventPlayerStatus._oriTitle);
		getPcInstance().getAppearance().setTitleColor(mEventPlayerStatus._oriColorTitle);

		// Remove the player from world instance
		InstanceManager.getInstance().getPlayerWorld(getPcInstance()).removeAllowed(getPcInstance().getObjectId());
		getPcInstance().setInstanceId(0);
		mWorldInstance = 0;

		// Remove the player from event listener (it's used to deny the manual res)
		getPcInstance().removeEventListener(EventEngineListener.class);

		mEventPlayerStatus._team = null;
		mEventPlayerStatus._returnLocation = null;
		mEventPlayerStatus = null;
	}

	/**
	 * Get the player's team.
	 * @return
	 */
	public TeamType getTeamType() {
		return mEventPlayerStatus._team.getTeamType();
	}

	public void setTeam(TeamHolder team) {
		mEventPlayerStatus._team = team;

		getPcInstance().setTitle(team.getName());
		getPcInstance().getAppearance().setTitleColor(team.getTeamType().getColor());
	}

	public ELocation getReturnLocation() {
		return mEventPlayerStatus._returnLocation;
	}
	
	/**
	 * Get the event instance id.
	 * @return
	 */
	public int getWorldInstanceId() {
		return mWorldInstance;
	}

	public void setInstanceWorld(InstanceWorld instanceWorld) {
		instanceWorld.addAllowed(getObjectId());
		mWorldInstance = instanceWorld.getInstanceId();
	}
	
	/**
	 * Get the player's points.
	 * @return Amount of points
	 */
	@Override
	public int getPoints(ScoreType type) {
		return mEventPlayerStatus._points.get(type);
	}

	/**
	 * Add an amount of points.
	 */
	@Override
	public void increasePoints(ScoreType type, int points) {
		if (!mEventPlayerStatus._points.containsKey(type)) mEventPlayerStatus._points.put(type, 0);
		mEventPlayerStatus._points.put(type, getPoints(type) + points);
	}

	/**
	 * Send a message to the player.
	 */
	public void sendMessage(String message) {
		getPcInstance().sendMessage(message);
	}

	/**
	 * Revive the player.
	 * <ul>
	 * <b>Actions:</b>
	 * </ul>
	 * <li>Cancel the DecayTask.</li>
	 * <li>Revive the character.</li>
	 * <li>Set max cp, hp and mp.</li>
	 * @param spawnProtectionTime
	 */
	public void revive(int spawnProtectionTime)
	{
		if (getPcInstance().isDead())
		{
			DecayTaskManager.getInstance().cancel(getPcInstance());
			getPcInstance().doRevive();
			// heal to max
			getPcInstance().setCurrentCp(getPcInstance().getMaxCp());
			getPcInstance().setCurrentHp(getPcInstance().getMaxHp());
			getPcInstance().setCurrentMp(getPcInstance().getMaxMp());
			setProtectionTimeEnd(System.currentTimeMillis() + (spawnProtectionTime * 1000)); // Milliseconds
		}
	}

	/**
	 * <ul>
	 * <b>Actions:</b>
	 * </ul>
	 * <li>Cancel target.</li>
	 * <li>Cancel cast.</li>
	 * <li>Cancel attack.</li>
	 */
	public void cancelAllActions()
	{
		// Cancel target
		getPcInstance().setTarget(null);
		// Cancel any attack in progress
		getPcInstance().breakAttack();
		// Cancel any skill in progress
		getPcInstance().breakCast();
	}

	/**
	 * We give you the buff to a player set within configs.
	 * @param buffs
	 */
	public void giveBuffs(Collection<SkillHolder> buffs)
	{
		for (SkillHolder sh : buffs)
		{
			sh.getSkill().applyEffects(getPcInstance(), getPcInstance());
		}
	}

	/**
	 * We deliver the items in a list defined as. Created in order to deliver rewards in the events.
	 * @param items
	 */
	public void giveItems(Collection<ItemHolder> items)
	{
		for (ItemHolder reward : items)
		{
			getPcInstance().addItem("eventReward", reward.getId(), reward.getCount(), null, true);
		}
	}

	/**
	 * <ul>
	 * <b>Actions:</b>
	 * </ul>
	 * <li>Stop all effects from player and summon.</li>
	 */
	public void cancelAllEffects()
	{
		// Stop all effects
		getPcInstance().stopAllEffects();
		// Check Transform
		if (getPcInstance().isTransformed())
		{
			getPcInstance().untransform();
		}
		// Check Summon's and pets
		if (getPcInstance().hasSummon())
		{
			final L2Summon summon = getPcInstance().getSummon();
			summon.stopAllEffectsExceptThoseThatLastThroughDeath();
			summon.abortAttack();
			summon.abortCast();
			// Remove
			summon.unSummon(getPcInstance());
		}

		// Cancel all character cubics
		for (L2CubicInstance cubic : getPcInstance().getCubics().values())
		{
			cubic.stopAction();
			cubic.cancelDisappear();
		}
		// Stop any cubic that has been given by other player
		getPcInstance().stopCubicsByOthers();

		// Remove player from his party
		final L2Party party = getPcInstance().getParty();
		if (party != null)
		{
			party.removePartyMember(getPcInstance(), L2Party.messageType.Expelled);
		}

		// Remove Agathion
		if (getPcInstance().getAgathionId() > 0)
		{
			getPcInstance().setAgathionId(0);
			getPcInstance().broadcastUserInfo();
		}

		// Remove reuse delay skills
		for (Skill skill : getPcInstance().getAllSkills())
		{
			getPcInstance().enableSkill(skill);
		}

		// Check Skills
		getPcInstance().sendSkillList();
		getPcInstance().sendPacket(new SkillCoolTime(getPcInstance()));
	}

	public long getProtectionTimeEnd() {
		return mProtectionTimeEnd;
	}

	public boolean isProtected() {
		return mProtectionTimeEnd > System.currentTimeMillis();
	}

	public void setProtectionTimeEnd(long time)	{
		mProtectionTimeEnd = time;
	}

	private static class EventPlayerStatus {

		// Stores the different kinds of points
		private Map<ScoreType, Integer> _points = new HashMap<>();
		// Original title color before teleporting to the event
		private int _oriColorTitle;
		// Original title before teleporting to the event
		private String _oriTitle;
		// Player's team in the event
		private TeamHolder _team;
		// Previous location before participating in the event
		private ELocation _returnLocation;
	}
}