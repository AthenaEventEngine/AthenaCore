/*
 * Copyright (C) 2014-2015 L2jAdmins
 *
 * This file is part of L2jAdmins.
 *
 * L2jAdmins is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2jAdmins is distributed in the hope that it will be useful,
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
import net.sf.eventengine.handler.AbstractEvent;
import net.sf.eventengine.holder.PlayerHolder;

import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.skills.Skill;

/**
 * @author fissban
 */
public class CaptureTheFlag extends AbstractEvent
{

	@Override
	public void runEventState(EventState state)
	{
		switch (state)
		{
			case START:
				prepareToStart();
				// createTeam();
				teleportAllPlayers();
				break;

			case FIGHT:
				prepareToFight();
				break;

			case END:
				prepareToEnd();
				// giveRewardsTeams();
				break;
		}

	}

	@Override
	public EventType getEventType()
	{
		return EventType.CTF;
	}

	@Override
	public void onKill(PlayerHolder player, PlayerHolder target)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onDeath(PlayerHolder player)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onAttack(PlayerHolder player, PlayerHolder target)
	{
		return false;
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onUseSkill(PlayerHolder player, PlayerHolder target, Skill skill)
	{
		return false;
		// TODO Auto-generated method stub

	}

	@Override
	public void onInteract(PlayerHolder player, L2Npc npc)
	{
		// TODO Auto-generated method stub

	}

	/**
	 * Teletransportamos a un player especifico a su localizacion inicial dentro del evento.
	 * @param player
	 */
	@Override
	public void teleportPlayer(PlayerHolder player)
	{
		// TODO terminar los configs
		switch (player.getPcInstance().getTeam())
		{
			case BLUE:
				Location locBlue = Configs.TVT_LOC_TEAM_BLUE;
				player.getPcInstance().teleToLocation(locBlue.getX(), locBlue.getY(), locBlue.getZ(), locBlue.getHeading(), player.getDinamicInstanceId());
				break;
			case RED:
				Location locRed = Configs.TVT_LOC_TEAM_RED;
				player.getPcInstance().teleToLocation(locRed.getX(), locRed.getY(), locRed.getZ(), locRed.getHeading(), player.getDinamicInstanceId());
				break;
		}
	}
}
