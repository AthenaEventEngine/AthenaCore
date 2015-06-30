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

import net.sf.eventengine.configs.Configs;
import net.sf.eventengine.enums.EventState;
import net.sf.eventengine.enums.EventType;
import net.sf.eventengine.enums.PlayerClassType;
import net.sf.eventengine.handler.AbstractEvent;
import net.sf.eventengine.holder.PlayerHolder;

import com.l2jserver.gameserver.enums.Team;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.skills.Skill;

/**
 * @author fissban
 */
public class CaptureTheFlag extends AbstractEvent
{
	public CaptureTheFlag()
	{
		super();
		
		// Definimos los spawns de cada team
		setTeamSpawn(Team.RED, Configs.TVT_LOC_TEAM_RED);
		setTeamSpawn(Team.BLUE, Configs.TVT_LOC_TEAM_BLUE);
		// Definimos los buffs de los personajes
		setPlayerBuffs(PlayerClassType.MAGE, Configs.TVT_BUFF_PLAYER_MAGE);
		setPlayerBuffs(PlayerClassType.WARRIOR, Configs.TVT_BUFF_PLAYER_WARRIOR);
	}
	
	@Override
	public EventType getEventType()
	{
		return EventType.CTF;
	}

	@Override
	public void runEventState(EventState state)
	{
		switch (state)
		{
			case START:
				prepareToStart(); // Metodo general
				// createTeam();
				teleportAllPlayers();
				break;

			case FIGHT:
				prepareToFight(); // Metodo general
				break;

			case END:
				// giveRewardsTeams();
				prepareToEnd(); // Metodo general
				break;
		}

	}

	@Override
	public void onInteract(PlayerHolder player, L2Npc npc)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void onKill(PlayerHolder player, L2Character target)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void onDeath(PlayerHolder player)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public boolean onAttack(PlayerHolder player, L2Character target)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onUseSkill(PlayerHolder player, L2Character target, Skill skill)
	{
		// TODO Auto-generated method stub
		return false;
	}
}