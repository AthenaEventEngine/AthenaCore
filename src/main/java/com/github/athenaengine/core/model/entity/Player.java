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
package com.github.athenaengine.core.model.entity;

import com.github.athenaengine.core.enums.InventoryItemType;
import com.github.athenaengine.core.enums.ScoreType;
import com.github.athenaengine.core.events.listeners.EventEngineListener;
import com.github.athenaengine.core.interfaces.IGamePacket;
import com.github.athenaengine.core.managers.ItemInstanceManager;
import com.github.athenaengine.core.model.holder.LocationHolder;
import com.github.athenaengine.core.model.holder.EItemHolder;
import com.github.athenaengine.core.enums.TeamType;
import com.github.athenaengine.core.interfaces.IParticipant;
import com.github.athenaengine.core.model.instance.ItemInstance;
import com.github.athenaengine.core.model.instance.WorldInstance;
import com.l2jserver.gameserver.instancemanager.InstanceManager;
import com.l2jserver.gameserver.model.L2Party;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.instance.L2CubicInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.holders.SkillHolder;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.network.serverpackets.SkillCoolTime;
import com.l2jserver.gameserver.taskmanager.DecayTaskManager;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * It manages player's info that participates in an event.
 */
public class Player extends Playable implements IParticipant
{
	private EventPlayerStatus mEventPlayerStatus;

	private int mWorldInstanceId = 0;
	
	// Variable used to know the protection time inside events
	private long mProtectionTimeEnd = 0;

	public Player(int objectId) {
		super(objectId);
	}

	@Override
	public boolean isPlayer() {
		return true;
	}

	/**
	 * Get L2PcInstance.
	 * @return
	 */
	public L2PcInstance getPcInstance() {
		return L2World.getInstance().getPlayer(getObjectId());
	}

	@Override
	public String getName() {
		return getPcInstance().getName();
	}

	@Override
	public String getTitle() {
		return getPcInstance().getTitle();
	}

	@Override
	public void setTitle(String title) {
		getPcInstance().setTitle(title);
		getPcInstance().updateAndBroadcastStatus(2);
	}

	public void teleportTo(LocationHolder location) {
		getPcInstance().teleToLocation(location.getLocation());
	}

	public void addToEvent(Team team) {
		if (mEventPlayerStatus == null) mEventPlayerStatus = new EventPlayerStatus();

		mEventPlayerStatus._oriColorTitle = getPcInstance().getAppearance().getTitleColor();
		mEventPlayerStatus._oriTitle = getPcInstance().getTitle();
		mEventPlayerStatus._returnLocation = new LocationHolder(getPcInstance().getLocation());
		setTeam(team);
	}

	public void removeFromEvent() {
		// Recovers player's title and color
		getPcInstance().setTitle(mEventPlayerStatus._oriTitle);
		getPcInstance().getAppearance().setTitleColor(mEventPlayerStatus._oriColorTitle);

		// Remove the player from world instance
		InstanceManager.getInstance().getPlayerWorld(getPcInstance()).removeAllowed(getPcInstance().getObjectId());
		getPcInstance().setInstanceId(0);
		mWorldInstanceId = 0;

		// Remove the player from event listener (it's used to deny the manual res)
		getPcInstance().removeEventListener(EventEngineListener.class);

		mEventPlayerStatus._team = null;
		mEventPlayerStatus._returnLocation = null;
		mEventPlayerStatus = null;
	}

	public double getCP() {
		return getPcInstance().getCurrentCp();
	}

	public double getMaxCP() {
		return getPcInstance().getMaxCp();
	}

	public void setCP(double cp) {
		getPcInstance().setCurrentCp(cp);
	}

	/**
	 * Get the player's team.
	 * @return TeamType
	 */
	public Team getTeam() {
		return mEventPlayerStatus._team;
	}

	/**
	 * Get the player's team type.
	 * @return TeamType
	 */
	public TeamType getTeamType() {
		return mEventPlayerStatus._team.getTeamType();
	}

	public void setTeam(Team team) {
		if (mEventPlayerStatus == null) mEventPlayerStatus = new EventPlayerStatus();

		mEventPlayerStatus._team = team;
		getPcInstance().setTitle(team.getName());
		getPcInstance().getAppearance().setTitleColor(team.getTeamType().getColor());
	}

	public LocationHolder getReturnLocation() {
		return mEventPlayerStatus._returnLocation;
	}
	
	/**
	 * Get the event instance id.
	 * @return
	 */
	public int getWorldInstanceId() {
		return mWorldInstanceId;
	}

	public void setInstanceWorld(WorldInstance instanceWorld) {
		instanceWorld.addPlayer(this);
		mWorldInstanceId = instanceWorld.getInstanceId();
	}
	
	/**
	 * Get the player's points.
	 * @return Amount of points
	 */
	@Override
	public int getPoints(ScoreType type) {
		if (!mEventPlayerStatus._points.containsKey(type)) mEventPlayerStatus._points.put(type, 0);
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
	public void revive(int spawnProtectionTime) {
		if (getPcInstance().isDead()) {
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
	public void cancelAllActions() {
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
	public void giveBuffs(Collection<SkillHolder> buffs) {
		for (SkillHolder sh : buffs) {
			sh.getSkill().applyEffects(getPcInstance(), getPcInstance());
		}
	}

	/**
	 * We deliver the items in a list defined as. Created in order to deliver rewards in the events.
	 * @param items
	 */
	public void giveItems(Collection<EItemHolder> items) {
		for (EItemHolder reward : items) {
			getPcInstance().addItem("eventReward", reward.getId(), reward.getAmount(), null, true);
		}
	}

	public void removeItems(Collection<EItemHolder> items) {
		// TODO: implement it
	}

	public boolean containsItem(int itemId) {
		return getPcInstance().getInventory().getItemByItemId(itemId) != null;
	}

	public void equipItem(ItemInstance item) {
		L2ItemInstance l2Item = ItemInstanceManager.getInstance().getL2ItemInstance(item);
		if (l2Item != null) getPcInstance().useEquippableItem(l2Item, true);
	}

	public void unequipItem(InventoryItemType type) {
		L2ItemInstance l2Item = getPcInstance().getInventory().getPaperdollItem(type.getValue());
		if (l2Item != null) getPcInstance().useEquippableItem(l2Item, true);
	}

	/**
	 * <ul>
	 * <b>Actions:</b>
	 * </ul>
	 * <li>Stop all effects from player and summon.</li>
	 */
	public void cancelAllEffects() {
		// Stop all effects
		getPcInstance().stopAllEffects();
		// Check Transform
		if (getPcInstance().isTransformed()) getPcInstance().untransform();
		// Check Summon's and pets
		if (getPcInstance().hasSummon()) {
			final L2Summon summon = getPcInstance().getSummon();
			summon.stopAllEffectsExceptThoseThatLastThroughDeath();
			summon.abortAttack();
			summon.abortCast();
			// Remove
			summon.unSummon(getPcInstance());
		}

		// Cancel all character cubics
		for (L2CubicInstance cubic : getPcInstance().getCubics().values()) {
			cubic.stopAction();
			cubic.cancelDisappear();
		}
		// Stop any cubic that has been given by other player
		getPcInstance().stopCubicsByOthers();

		// Remove player from his party
		final L2Party party = getPcInstance().getParty();
		if (party != null) party.removePartyMember(getPcInstance(), L2Party.messageType.Expelled);

		// Remove Agathion
		if (getPcInstance().getAgathionId() > 0) {
			getPcInstance().setAgathionId(0);
			getPcInstance().broadcastUserInfo();
		}

		// Remove reuse delay skills
		for (Skill skill : getPcInstance().getAllSkills()) {
			getPcInstance().enableSkill(skill);
		}

		// Check Skills
		getPcInstance().sendSkillList();
		getPcInstance().sendPacket(new SkillCoolTime(getPcInstance()));
	}

	public int getPvpKills() {
		return getPcInstance().getPvpKills();
	}

	public void setPvpKills(int value) {
		getPcInstance().setPvpKills(value);
	}

	public int getFame() {
		return getPcInstance().getFame();
	}

	public void setFame(int value) {
		getPcInstance().setFame(value);
	}

	public void sendPacket(IGamePacket packet) {
		getPcInstance().sendPacket(packet.getL2Packet());
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
		private Map<ScoreType, Integer> _points = new ConcurrentHashMap<>();
		// Original title color before teleporting to the event
		private int _oriColorTitle;
		// Original title before teleporting to the event
		private String _oriTitle;
		// Player's team in the event
		private Team _team;
		// Previous location before participating in the event
		private LocationHolder _returnLocation;
	}
}