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

import java.util.HashMap;
import java.util.Map;

import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.enums.Team;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.skills.Skill;

import net.sf.eventengine.EventEngineManager;
import net.sf.eventengine.datatables.ConfigData;
import net.sf.eventengine.datatables.MessageData;
import net.sf.eventengine.enums.EventEngineState;
import net.sf.eventengine.enums.EventState;
import net.sf.eventengine.enums.PlayerColorType;
import net.sf.eventengine.events.schedules.AnnounceNearEndEvent;
import net.sf.eventengine.handler.AbstractEvent;
import net.sf.eventengine.holder.PlayerHolder;
import net.sf.eventengine.util.EventUtil;

/**
 * @author fissban
 */
public class OneVsOne extends AbstractEvent
{
	// Time between fights in sec
	private static final int TIME_BETWEEN_FIGHT = 10;
	private Map<Integer, InstancesTeams> _instancesTeams = new HashMap<>();
	// Radius spawn
	private static final int RADIUS_SPAWN_PLAYER = 0;
	
	public OneVsOne()
	{
		super();
		setInstanceFile(ConfigData.getInstance().OVO_INSTANCE_FILE);
		// We define each team spawns
		setTeamSpawn(Team.RED, ConfigData.getInstance().OVO_COORDINATES_TEAM_RED);
		setTeamSpawn(Team.BLUE, ConfigData.getInstance().OVO_COORDINATES_TEAM_BLUE);
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
				createTeams();
				teleportAllPlayers(RADIUS_SPAWN_PLAYER);
				break;
				
			case FIGHT:
				prepareToFight(); // General Method
				break;
				
			case END:
				giveRewardsTeams();
				prepareToEnd(); // General Method
				break;
		}
	}
	
	// LISTENERS ------------------------------------------------------
	@Override
	public boolean onUseSkill(PlayerHolder player, L2Character target, Skill skill)
	{
		return false;
	}
	
	@Override
	public boolean onAttack(PlayerHolder player, L2Character target)
	{
		return false;
	}
	
	@Override
	public void onKill(PlayerHolder player, L2Character target)
	{
		nextFight(player);
		// One we increased the amount of kills you have the participants.
		player.increaseKills();
		showPoint(_instancesTeams.get(player.getDinamicInstanceId()));
		
		// Reward for kills
		if (ConfigData.getInstance().OVO_REWARD_KILLER_ENABLED)
		{
			giveItems(player, ConfigData.getInstance().OVO_REWARD_KILLER);
		}
		
		// Reward pvp for kills
		if (ConfigData.getInstance().OVO_REWARD_PVP_KILLER_ENABLED)
		{
			player.setPvpKills(player.getPvpKills() + ConfigData.getInstance().OVO_REWARD_PVP_KILLER);
		}
		
		// Reward fame for kills
		if (ConfigData.getInstance().OVO_REWARD_FAME_KILLER_ENABLED)
		{
			player.setFame(player.getFame() + ConfigData.getInstance().OVO_REWARD_FAME_KILLER);
		}
		
		// Message Kill
		if (ConfigData.getInstance().EVENT_KILLER_MESSAGE)
		{
			EventUtil.messageKill(player, target);
		}
	}
	
	@Override
	public void onDeath(PlayerHolder player)
	{
		giveResurrectPlayer(player, TIME_BETWEEN_FIGHT, RADIUS_SPAWN_PLAYER);
		// One we increased the amount of deaths you have the participants.
		player.increaseDeaths();
	}
	
	@Override
	public void onInteract(PlayerHolder player, L2Npc npc)
	{
		//
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
	
	// VARIOUS METHODS -------------------------------------------------
	
	/**
	 * We all players who are registered at the event and generate the teams
	 */
	private void createTeams()
	{
		// control variable to find out which team will be the player.
		boolean blueOrRed = true;
		// control variable for the amount of players per team.
		boolean createWorld = true;
		
		PlayerHolder playerBlue = null;
		PlayerHolder playerRed = null;
		
		InstanceWorld world = null;
		
		// We verified that we have an even number of participants.
		// If not, let out a participant at random.
		boolean leaveOutParticipant = false;
		PlayerHolder removePlayer = null;
		if (getAllEventPlayers().size() % 2 != 0)
		{
			leaveOutParticipant = true;
		}
		
		for (PlayerHolder player : getAllEventPlayers())
		{
			if (leaveOutParticipant)
			{
				removePlayer = player;
				leaveOutParticipant = false;
				continue;
			}
			// We create the instance and the world
			if (createWorld)
			{
				world = createNewInstanceWorld();
				createWorld = false;
			}
			
			// We distributed one for each team
			if (blueOrRed)
			{
				playerBlue = player;
				// Adjust the color of the title and the title character.
				player.getPcInstance().setTeam(Team.BLUE);
				player.setNewColorTitle(PlayerColorType.BLUE);
				player.setNewTitle("[ BLUE ]");
				// Assigned to the instance that owns the player.
				player.setDinamicInstanceId(world.getInstanceId());
			}
			else
			{
				playerRed = player;
				// Adjust the color of the title and the title character.
				player.getPcInstance().setTeam(Team.RED);
				player.setNewColorTitle(PlayerColorType.RED);
				player.setNewTitle("[ RED ]");
				// Assigned to the instance that owns the player.
				player.setDinamicInstanceId(world.getInstanceId());
			}
			
			blueOrRed = !blueOrRed;
			
			if ((playerRed != null) && (playerBlue != null))
			{
				// We charge different teams
				_instancesTeams.put(world.getInstanceId(), new InstancesTeams(playerRed, playerBlue));
				// init variable
				createWorld = true;
				playerRed = null;
				playerBlue = null;
			}
		}
		
		// stir a character of the event.
		if (removePlayer != null)
		{
			// TODO podriamos enviarle un mensaje notificando lo sucedido.
			getAllEventPlayers().remove(removePlayer);
		}
	}
	
	/**
	 * We deliver rewards for the moment we only support for 1 or 2 teams.
	 */
	private void giveRewardsTeams()
	{
		if (getAllEventPlayers().isEmpty())
		{
			return;
		}
		
		for (InstancesTeams team : _instancesTeams.values())
		{
			// If both players are offline any prize will be awarded.
			if (team._playerRed.getPcInstance() == null && team._playerBlue.getPcInstance() == null)
			{
				continue;
			}
			
			/** Verify that the blue player follow in the event. */
			if (team._playerBlue.getPcInstance() == null)
			{
				EventUtil.sendEventScreenMessage(team._playerRed, MessageData.getInstance().getMsgByLang(team._playerBlue.getPcInstance(), "winner_red", false));
				giveItems(team._playerRed, ConfigData.getInstance().OVO_REWARD_PLAYER_WIN);
			}
			/** Verify that the red player follow in the event.. */
			else if (team._playerRed.getPcInstance() == null)
			{
				EventUtil.sendEventScreenMessage(team._playerBlue, MessageData.getInstance().getMsgByLang(team._playerBlue.getPcInstance(), "winner_blue", false));
				giveItems(team._playerBlue, ConfigData.getInstance().OVO_REWARD_PLAYER_WIN);
			}
			/** If both players continue to verify the event points. */
			else
			{
				int pointsBlue = team._playerBlue.getKills();
				int pointsRed = team._playerRed.getKills();
				
				if (pointsBlue == pointsRed) // tie
				{
					
					EventUtil.sendEventScreenMessage(team._playerBlue, MessageData.getInstance().getMsgByLang(team._playerBlue.getPcInstance(), "event_tie", false));
					EventUtil.sendEventScreenMessage(team._playerRed, MessageData.getInstance().getMsgByLang(team._playerRed.getPcInstance(), "event_tie", false));
				}
				else if (pointsBlue < pointsRed) // win red
				{
					EventUtil.sendEventScreenMessage(team._playerRed, MessageData.getInstance().getMsgByLang(team._playerRed.getPcInstance(), "winner_red", false));
					giveItems(team._playerRed, ConfigData.getInstance().OVO_REWARD_PLAYER_WIN);
				}
				else if (pointsBlue > pointsRed) // win blue
				{
					EventUtil.sendEventScreenMessage(team._playerBlue, MessageData.getInstance().getMsgByLang(team._playerBlue.getPcInstance(), "winner_blue", false));
					giveItems(team._playerBlue, ConfigData.getInstance().OVO_REWARD_PLAYER_WIN);
				}
			}
		}
	}
	
	private void nextFight(PlayerHolder ph)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				if (EventEngineManager.getInstance().getEventEngineState() == EventEngineState.RUNNING_EVENT)
				{
					// heal to max
					ph.getPcInstance().setCurrentCp(ph.getPcInstance().getMaxCp());
					ph.getPcInstance().setCurrentHp(ph.getPcInstance().getMaxHp());
					ph.getPcInstance().setCurrentMp(ph.getPcInstance().getMaxMp());
					// teleport
					revivePlayer(ph);
					teleportPlayer(ph, 0);
					// buff
					giveBuffPlayer(ph.getPcInstance());
				}
			}
			
		}, TIME_BETWEEN_FIGHT * 1000);
	}
	
	/**
	 * Show on screen the number of points that each team
	 */
	private void showPoint(InstancesTeams instTeams)
	{
		EventUtil.sendEventScreenMessage(instTeams._playerBlue, "RED " + instTeams._playerRed.getPoints() + " | " + instTeams._playerBlue.getPoints() + " BLUE", 10000);
		EventUtil.sendEventScreenMessage(instTeams._playerRed, "RED " + instTeams._playerRed.getPoints() + " | " + instTeams._playerBlue.getPoints() + " BLUE", 10000);
		// ph.getPcInstance().sendPacket(new EventParticipantStatus(_pointsRed, _pointsBlue));
	}
	
	/**
	 * Class responsible for managing the points of the teams in each instance
	 * @author fissban
	 */
	private class InstancesTeams
	{
		private PlayerHolder _playerRed;
		private PlayerHolder _playerBlue;
		
		public InstancesTeams(PlayerHolder playerRed, PlayerHolder playerBlue)
		{
			_playerRed = playerRed;
			_playerBlue = playerBlue;
		}
	}
}
