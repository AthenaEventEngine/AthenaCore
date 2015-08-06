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
package net.sf.eventengine.events;

import java.util.List;

import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.enums.Team;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.network.clientpackets.Say2;
import com.l2jserver.util.Rnd;

import net.sf.eventengine.datatables.ConfigData;
import net.sf.eventengine.enums.CollectionTarget;
import net.sf.eventengine.enums.EventState;
import net.sf.eventengine.enums.PlayerColorType;
import net.sf.eventengine.events.schedules.AnnounceNearEndEvent;
import net.sf.eventengine.handler.AbstractEvent;
import net.sf.eventengine.holder.PlayerHolder;
import net.sf.eventengine.util.EventUtil;

/**
 * Event survival<br>
 * One team will be created and will have to survive several waves of mobs.<br>
 * @author fissban
 */
public class Survive extends AbstractEvent
{
	// Variable that controls the level of the stage
	private int _stage = 1;
	// Variable that helps us keep track of the number of dead mobs.
	private int _auxKillMonsters = 0;
	
	// Monsters ids
	private final List<Integer> MONSTERS_ID = ConfigData.getInstance().SURVIVE_MONSTERS_ID;
	
	public Survive()
	{
		super();
		setInstanceFile(ConfigData.getInstance().SURVIVE_INSTANCE_FILE);
		// We define the main spawn of equipment
		setTeamSpawn(Team.BLUE, ConfigData.getInstance().SURVIVE_COORDINATES_PLAYER);
		// Announce near end event
		int timeLeft = (ConfigData.getInstance().EVENT_DURATION * 60 * 1000) - (ConfigData.getInstance().EVENT_TEXT_TIME_FOR_END * 1000);
		addScheduledEvent(new AnnounceNearEndEvent(timeLeft));
	}
	
	@Override
	public void runEventState(EventState state)
	{
		switch (state)
		{
			case START:
				prepareToStart(); // General Method
				createTeam();
				teleportAllPlayers(200);
				break;
				
			case FIGHT:
				prepareToFight(); // General Method
				spawnsMobs();
				break;
				
			case END:
				// showResult();
				prepareToEnd(); // General Method
				break;
		}
	}
	
	@Override
	public void onInteract(PlayerHolder player, L2Npc npc)
	{
		//
	}
	
	@Override
	public void onKill(PlayerHolder player, L2Character target)
	{
		// We serve to keep count of how many mobs killed.
		player.increaseKills();
		// Update title character
		updateTitle(player);
		// One increasing the amount of dead mobs
		_auxKillMonsters++;
		// Verify the number of dead mobs, if any killed all increase by one the stage.
		if (_auxKillMonsters >= (_stage * ConfigData.getInstance().SURVIVE_MONSTER_SPAWN_FOR_STAGE))
		{
			// Increase by one the stage.
			_stage++;
			// We restart our assistant.
			_auxKillMonsters = 0;
			// Spawns Mobs
			spawnsMobs();
			// Give rewards
			giveRewardsTeams();
		}
	}
	
	@Override
	public void onDeath(PlayerHolder player)
	{
		//
	}
	
	@Override
	public boolean onAttack(PlayerHolder player, L2Character target)
	{
		if (target.isPlayable())
		{
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onUseSkill(PlayerHolder player, L2Character target, Skill skill)
	{
		return false;
	}
	
	@Override
	public boolean onUseItem(PlayerHolder player, L2Item item)
	{
		return false;
	}
	
	@Override
	public void onLogout(PlayerHolder player)
	{
		//
	}
	
	// MISC ---------------------------------------------------------------------------------------
	public void giveRewardsTeams()
	{
		if (getAllEventPlayers().isEmpty())
		{
			return;
		}
		
		for (PlayerHolder player : getAllEventPlayers())
		{
			EventUtil.sendEventScreenMessage(player, "Congratulations survivor!");
			giveItems(player, ConfigData.getInstance().SURVIVE_REWARD_PLAYER_WIN);
		}
	}
	
	private void spawnsMobs()
	{
		EventUtil.announceTo(Say2.BATTLEFIELD, "survive_spawns_mobs", CollectionTarget.ALL_PLAYERS_IN_EVENT);
		
		// After 5 secs spawn run.
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				for (int i = 0; i < (_stage * ConfigData.getInstance().SURVIVE_MONSTER_SPAWN_FOR_STAGE); i++)
				{
					addEventNpc(MONSTERS_ID.get(Rnd.get(MONSTERS_ID.size() - 1)), ConfigData.getInstance().SURVIVE_COORDINATES_PLAYER, Team.RED, true, getInstancesWorlds().get(0).getInstanceId());
				}
				
				// We notify the characters in the event that stage they are currently.
				for (PlayerHolder ph : getAllEventPlayers())
				{
					EventUtil.sendEventScreenMessage(ph, "Stage " + _stage, 5000);
				}
			}
		}, 5000L);
		
	}
	
	/**
	 * We create the computer that will play the characters.
	 */
	private void createTeam()
	{
		// We create the instance and the world
		InstanceWorld world = createNewInstanceWorld();
		
		for (PlayerHolder ph : getAllEventPlayers())
		{
			// We add the character to the world and then be teleported
			world.addAllowed(ph.getPcInstance().getObjectId());
			// Adjust the team of character
			ph.getPcInstance().setTeam(Team.BLUE);
			// Adjust the instance that owns the character
			ph.setDinamicInstanceId(world.getInstanceId());
			// Adjust the color of the title
			ph.setNewColorTitle(PlayerColorType.YELLOW_OCHRE);
			// Adjust the title character.
			updateTitle(ph);
		}
	}
	
	/**
	 * We update the title of a character depending on the number of murders that have
	 * @param player
	 */
	private void updateTitle(PlayerHolder player)
	{
		// Adjust the title character.
		player.setNewTitle("Monster Death " + player.getKills());
		// Adjust the status character.
		player.getPcInstance().updateAndBroadcastStatus(2);
	}
}
