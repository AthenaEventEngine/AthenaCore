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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.l2jserver.gameserver.ThreadPoolManager;
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
import net.sf.eventengine.enums.TeamType;
import net.sf.eventengine.events.handler.AbstractEvent;
import net.sf.eventengine.events.holders.PlayerHolder;
import net.sf.eventengine.events.schedules.AnnounceNearEndEvent;
import net.sf.eventengine.util.EventUtil;
import net.sf.eventengine.util.SortUtil;

/**
 * @author fissban
 */
public class OneVsOne extends AbstractEvent
{
	// Time between fights in sec
	private static final int TIME_BETWEEN_FIGHT = 10;
	private Map<Integer, Map<TeamType, PlayerHolder>> _instancesTeams = new HashMap<>();
	// Radius spawn
	private static final int RADIUS_SPAWN_PLAYER = 0;
	
	public OneVsOne()
	{
		super();
		// Definimos la instancia en que transcurria el evento
		setInstanceFile(ConfigData.getInstance().OVO_INSTANCE_FILE);
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
				createTeams(ConfigData.getInstance().OVO_COUNT_TEAM);
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
	public boolean onUseSkill(PlayerHolder ph, L2Character target, Skill skill)
	{
		return false;
	}
	
	@Override
	public boolean onAttack(PlayerHolder ph, L2Character target)
	{
		return false;
	}
	
	@Override
	public void onKill(PlayerHolder ph, L2Character target)
	{
		// Obtenemos el id de la instancia del personaje en cuestion
		int instanceId = ph.getDinamicInstanceId();
		// One we increased the amount of kills you have the participants.
		ph.increaseKills();
		
		int auxDeath = 0;
		// Verificamos si los otros participantes de la instancia estan muertos.
		for (PlayerHolder playerHolder : _instancesTeams.get(instanceId).values())
		{
			if (playerHolder.equals(ph))
			{
				continue;
			}
			auxDeath++;
		}
		
		// Si todos estan muertos generamos la proxima pelea.
		if (auxDeath == ConfigData.getInstance().OVO_COUNT_TEAM - 1)
		{
			nextFight(instanceId);
		}
		
		showPoint(instanceId);
		
		// Reward for kills
		if (ConfigData.getInstance().OVO_REWARD_KILLER_ENABLED)
		{
			giveItems(ph, ConfigData.getInstance().OVO_REWARD_KILLER);
		}
		// Reward pvp for kills
		if (ConfigData.getInstance().OVO_REWARD_PVP_KILLER_ENABLED)
		{
			ph.getPcInstance().setPvpKills(ph.getPcInstance().getPvpKills() + ConfigData.getInstance().OVO_REWARD_PVP_KILLER);
			EventUtil.sendEventMessage(ph, MessageData.getInstance().getMsgByLang(ph.getPcInstance(), "reward_text_pvp", true).replace("%count%", ConfigData.getInstance().OVO_REWARD_PVP_KILLER + ""));
		}
		// Reward fame for kills
		if (ConfigData.getInstance().OVO_REWARD_FAME_KILLER_ENABLED)
		{
			ph.getPcInstance().setFame(ph.getPcInstance().getFame() + ConfigData.getInstance().OVO_REWARD_FAME_KILLER);
			EventUtil.sendEventMessage(ph, MessageData.getInstance().getMsgByLang(ph.getPcInstance(), "reward_text_fame", true).replace("%count%", ConfigData.getInstance().OVO_REWARD_FAME_KILLER + ""));
		}
		// Message Kill
		if (ConfigData.getInstance().EVENT_KILLER_MESSAGE)
		{
			EventUtil.messageKill(ph, target);
		}
	}
	
	@Override
	public void onDeath(PlayerHolder ph)
	{
		//
	}
	
	@Override
	public boolean onInteract(PlayerHolder ph, L2Npc npc)
	{
		return true;
	}
	
	@Override
	public boolean onUseItem(PlayerHolder ph, L2Item item)
	{
		return false;
	}
	
	@Override
	public void onLogout(PlayerHolder ph)
	{
		//
	}
	
	// VARIOUS METHODS -------------------------------------------------
	
	/**
	 * We all players who are registered at the event and generate the teams
	 * @param countTeams
	 */
	private void createTeams(int countTeams)
	{
		// Definimos la cantidad de teams que se requieren
		setCountTeams(countTeams);
		// We define each team spawns
		setSpawnTeams(ConfigData.getInstance().OVO_COORDINATES_TEAM);
		
		// We verified that we have an even number of participants.
		// If not, let out a participant at random.
		int leaveOutParticipant = 0;
		
		if ((getAllEventPlayers().size() % countTeams) != 0)
		{
			leaveOutParticipant = countTeams - 1;
		}
		
		Iterator<PlayerHolder> players = getAllEventPlayers().iterator();
		
		List<PlayerHolder> phRemove = new ArrayList<>();
		
		while (players.hasNext())
		{
			// removemos personajes hasta obtener la cantidad justa para realizar eventos 1vs1,1vs1vs1
			if (leaveOutParticipant > 0)
			{
				leaveOutParticipant--;
				// removemos al personaje impar del evento.
				phRemove.add(players.next());
				// TODO falta agregar un mensaje avisando de la accion.
			}
			else
			{
				// We create the instance and the world
				InstanceWorld world = createNewInstanceWorld();
				// auxiliar para llevar la personajes de cada isntancia del evento
				Map<TeamType, PlayerHolder> teams = new HashMap<>();
				
				for (int i = 0; i < countTeams; i++)
				{
					PlayerHolder ph = players.next();
					// Obtenemos el team
					TeamType team = getEnabledTeams()[i];
					// Definimos algunas caracteristicas generales de cada poersonaje.
					ph.setTeam(team);
					ph.setNewTitle("[ " + team.name() + " ]");
					ph.setDinamicInstanceId(world.getInstanceId());
					teams.put(team, ph);
				}
				
				_instancesTeams.put(world.getInstanceId(), teams);
			}
		}
		
		for (PlayerHolder ph : phRemove)
		{
			getAllEventPlayers().remove(ph);
		}
	}
	
	/**
	 * We deliver rewards for the moment we only support for 1 or 2 teams.
	 */
	private void giveRewardsTeams()
	{
		// Recorremos nuestra variable instancia a instancia
		// y vamos entregando los premios a los ganadores.
		for (Map<TeamType, PlayerHolder> instances : _instancesTeams.values())
		{
			List<PlayerHolder> winners = SortUtil.getOrderedByKills(instances.values(), 0).get(0);
			
			// Entregamos los rewards
			for (PlayerHolder ph : winners)
			{
				if (ph.getPcInstance() != null)
				{
					// FIXME agregar al sistema de lang
					EventUtil.sendEventScreenMessage(ph, "WINNER " + ph.getPcInstance().getName());
					giveItems(ph, ConfigData.getInstance().OVO_REWARD_PLAYER_WIN);
				}
			}
		}
	}
	
	private void nextFight(int instanceId)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(() ->
		{
			if (EventEngineManager.getInstance().getEventEngineState() == EventEngineState.RUNNING_EVENT)
			{
				Map<TeamType, PlayerHolder> teams = _instancesTeams.get(instanceId);
				
				for (PlayerHolder ph : teams.values())
				{
					if (ph.getPcInstance().isDead())
					{
						// revive
						revivePlayer(ph);
					}
					// teleport
					teleportPlayer(ph, 0);
					// buff
					giveBuffPlayer(ph.getPcInstance());
				}
			}
			
		} , TIME_BETWEEN_FIGHT * 1000);
	}
	
	/**
	 * Show on screen the number of points that each team
	 */
	private void showPoint(int instanceId)
	{
		StringBuilder sb = new StringBuilder();
		
		for (PlayerHolder ph : _instancesTeams.get(instanceId).values())
		{
			sb.append(" | ");
			sb.append(ph.getTeamType().name());
			sb.append(" ");
			sb.append(ph.getKills());
		}
		sb.append(" | ");
		
		for (PlayerHolder ph : _instancesTeams.get(instanceId).values())
		{
			EventUtil.sendEventScreenMessage(ph, sb.toString(), 10000);
		}
	}
}
