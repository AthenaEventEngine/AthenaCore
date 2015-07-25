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

import net.sf.eventengine.datatables.ConfigData;
import net.sf.eventengine.enums.EventState;
import net.sf.eventengine.enums.PlayerColorType;
import net.sf.eventengine.handler.AbstractEvent;
import net.sf.eventengine.holder.PlayerHolder;
import net.sf.eventengine.util.EventUtil;

import com.l2jserver.gameserver.datatables.ItemTable;
import com.l2jserver.gameserver.enums.Team;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;
import com.l2jserver.gameserver.model.itemcontainer.Inventory;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.items.L2Weapon;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.network.clientpackets.Say2;

/**
 * @author fissban
 */
public class CaptureTheFlag extends AbstractEvent
{
	// Flags
	private static final int BLUE_FLAG = 36601;
	private static final int RED_FLAG = 36602;
	// Holder
	private static final int BLUE_HOLDER = 36603;
	private static final int RED_HOLDER = 36604;
	// FlagItem
	private static final int FLAG_ITEM = 6718;
	// Points to conquer the flag
	private final int POINTS_CONQUER_FLAG = ConfigData.getInstance().CTF_POINTS_CONQUER_FLAG;
	private final int POINTS_KILL = ConfigData.getInstance().CTF_POINTS_KILL;
	// Points that each team.
	private int _pointsRed = 0;
	private int _pointsBlue = 0;
	
	public CaptureTheFlag()
	{
		super();
		setInstanceFile(ConfigData.getInstance().CTF_INSTANCE_FILE);
		// We define each team spawns
		setTeamSpawn(Team.RED, ConfigData.getInstance().CTF_COORDINATES_TEAM_RED);
		setTeamSpawn(Team.BLUE, ConfigData.getInstance().CTF_COORDINATES_TEAM_BLUE);
	}
	
	@Override
	public void runEventState(EventState state)
	{
		switch (state)
		{
			case START:
				prepareToStart(); // General Method
				createTeams();
				spawnFlagsAndHolders();
				teleportAllPlayers(100);
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
	
	@Override
	public void onInteract(PlayerHolder player, L2Npc npc)
	{
		switch (npc.getId())
		{
			case BLUE_FLAG:
				if (player.getPcInstance().getTeam() == Team.RED)
				{
					// We equip flag
					equipFlag(player);
					// We remove the flag from his position
					removeNpc(npc);
					// We announced that a flag was taken
					EventUtil.announceToAllPlayersInEvent(Say2.SCREEN_ANNOUNCE, "El equipo rojo acaba de tomar la bandera");
				}
				break;
			
			case RED_FLAG:
				if (player.getPcInstance().getTeam() == Team.BLUE)
				{
					// We equip flag
					equipFlag(player);
					// We remove the flag from his position
					removeNpc(npc);
					// We announced that a flag was taken
					EventUtil.announceToAllPlayersInEvent(Say2.SCREEN_ANNOUNCE, "El equipo azul acaba de tomar la bandera");
				}
				break;
			
			case BLUE_HOLDER:
				if (player.getPcInstance().getTeam() == Team.BLUE)
				{
					if (hasFlag(player))
					{
						// We increased the points
						_pointsBlue += POINTS_CONQUER_FLAG;
						// Remove the flag character
						unequiFlag(player);
						// We created the flag again
						addEventNpc(RED_FLAG, ConfigData.getInstance().CTF_COORDINATES_TEAM_RED, Team.RED, getInstancesWorlds().get(0).getInstanceId());
						// We announced that a flag was taken
						EventUtil.announceToAllPlayersInEvent(Say2.SCREEN_ANNOUNCE, "El equipo azul acaba de conquistar la bandera del equipo enemigo");
						// Show points of each team
						showPoint();
					}
				}
				break;
			
			case RED_HOLDER:
				if (player.getPcInstance().getTeam() == Team.RED)
				{
					if (hasFlag(player))
					{
						// We increased the points
						_pointsRed += POINTS_CONQUER_FLAG;
						// Remove the flag character
						unequiFlag(player);
						// We created the flag again
						addEventNpc(BLUE_FLAG, ConfigData.getInstance().CTF_COORDINATES_TEAM_BLUE, Team.BLUE, getInstancesWorlds().get(0).getInstanceId());
						// We announced that a flag was taken
						EventUtil.announceToAllPlayersInEvent(Say2.SCREEN_ANNOUNCE, "El equipo rojo acaba de conquistar la bandera del equipo enemigo");
						// Show points of each team
						showPoint();
					}
				}
				break;
		}
	}
	
	@Override
	public void onKill(PlayerHolder player, L2Character target)
	{
		PlayerHolder targetEvent = getEventPlayer(target);
		
		if (hasFlag(targetEvent))
		{
			// Remove the flag character
			unequiFlag(targetEvent);
			// Drop flag.
			dropFlag(targetEvent);
		}
		
		// We increased the team's points.
		switch (player.getPcInstance().getTeam())
		{
			case RED:
				_pointsRed += POINTS_KILL;
				break;
			case BLUE:
				_pointsBlue += POINTS_KILL;
				break;
		}
		
		showPoint();
	}
	
	@Override
	public void onDeath(PlayerHolder player)
	{
		giveResurrectPlayer(player, 10, 100);
	}
	
	@Override
	public boolean onAttack(PlayerHolder player, L2Character target)
	{
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
		if (item.getId() == FLAG_ITEM)
		{
			return true;
		}
		
		if (hasFlag(player) && item instanceof L2Weapon)
		{
			return true;
		}
		
		return false;
	}
	
	@Override
	public void onLogout(PlayerHolder player)
	{
		if (hasFlag(player))
		{
			// Remove the flag character
			unequiFlag(player);
			dropFlag(player);
		}
	}
	
	// VARIOUS METHODS -------------------------------------------------
	
	/**
	 * Spawn flags and holders
	 */
	private void spawnFlagsAndHolders()
	{
		// red
		int x, y, z;
		x = ConfigData.getInstance().CTF_COORDINATES_TEAM_RED.getX();
		y = ConfigData.getInstance().CTF_COORDINATES_TEAM_RED.getY();
		z = ConfigData.getInstance().CTF_COORDINATES_TEAM_RED.getZ();
		addEventNpc(RED_FLAG, x - 100, y, z, 0, Team.RED, false, getInstancesWorlds().get(0).getInstanceId());
		addEventNpc(RED_HOLDER, x - 200, y, z, 0, Team.RED, false, getInstancesWorlds().get(0).getInstanceId());
		// blue
		x = ConfigData.getInstance().CTF_COORDINATES_TEAM_BLUE.getX();
		y = ConfigData.getInstance().CTF_COORDINATES_TEAM_BLUE.getY();
		z = ConfigData.getInstance().CTF_COORDINATES_TEAM_BLUE.getZ();
		addEventNpc(BLUE_FLAG, x + 100, y, z, 0, Team.BLUE, false, getInstancesWorlds().get(0).getInstanceId());
		addEventNpc(BLUE_HOLDER, x + 200, y, z, 0, Team.BLUE, false, getInstancesWorlds().get(0).getInstanceId());
	}
	
	/**
	 * We all players who are at the event and generate the teams
	 */
	private void createTeams()
	{
		// We create the instance and the world
		InstanceWorld world = createNewInstanceWorld();
		
		int aux = 0;
		
		for (PlayerHolder player : getAllEventPlayers())
		{
			if ((aux % 2) == 0)
			{
				// Adjust the color of the title and the title character.
				player.getPcInstance().setTeam(Team.BLUE);
				player.setNewColorTitle(PlayerColorType.BLUE);
				player.setNewTitle("[ BLUE ]");
			}
			else
			{
				// Adjust the color of the title and the title character.
				player.getPcInstance().setTeam(Team.RED);
				player.setNewColorTitle(PlayerColorType.RED);
				player.setNewTitle("[ RED ]");
			}
			
			// We add the character to the world and then be teleported
			world.addAllowed(player.getPcInstance().getObjectId());
			// We update the character in front of him around and himself
			player.getPcInstance().updateAndBroadcastStatus(2);
			// Adjust the instance that owns the character
			player.setDinamicInstanceId(world.getInstanceId());
			
			aux++;
		}
	}
	
	/**
	 * We deliver rewards.
	 */
	private void giveRewardsTeams()
	{
		if (getAllEventPlayers().isEmpty())
		{
			return;
		}
		
		Team teamWinner = getWinTeam();
		
		for (PlayerHolder player : getAllEventPlayers())
		{
			// FIXME podriamos crear un metodo especificamente para esto pero aprobechamos el recorrido del for aqui!
			if (hasFlag(player))
			{
				// Remove the flag character
				unequiFlag(player);
			}
			
			if (teamWinner == Team.NONE)
			{
				// Announcing the outcome of the event
				EventUtil.sendEventScreenMessage(player, "El evento resulto en un empate entre ambos teams!");
			}
			else
			{
				// Announcing the outcome of the event
				EventUtil.sendEventScreenMessage(player, "Equipo ganador -> " + teamWinner.getClass().getCanonicalName() + "!");
				
				// We deliver rewards
				if (player.getPcInstance().getTeam() == teamWinner)
				{
					giveItems(player, ConfigData.getInstance().CTF_REWARD_PLAYER_WIN);
				}
			}
		}
	}
	
	/**
	 * We get the winning team<br>
	 */
	private Team getWinTeam()
	{
		Team ganador;
		
		if (_pointsRed == _pointsBlue)
		{
			ganador = Team.NONE;
		}
		else if (_pointsRed > _pointsBlue)
		{
			ganador = Team.RED;
		}
		else
		{
			ganador = Team.BLUE;
		}
		
		return ganador;
	}
	
	/**
	 * Show on screen the number of points that each team
	 */
	private void showPoint()
	{
		for (PlayerHolder ph : getAllEventPlayers())
		{
			EventUtil.sendEventScreenMessage(ph, "RED " + _pointsRed + " | " + _pointsBlue + " BLUE", 10000);
			// ph.getPcInstance().sendPacket(new EventParticipantStatus(_pointsRed, _pointsBlue));
		}
	}
	
	/**
	 * Check if a character has a flag
	 * @param ph
	 * @return
	 */
	private boolean hasFlag(PlayerHolder ph)
	{
		L2ItemInstance item = ph.getPcInstance().getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		
		if (item != null && item.getId() == FLAG_ITEM)
		{
			return true;
		}
		
		return false;
	}
	
	/**
	 * We equip a character with a flag
	 * @param ph
	 */
	private void equipFlag(PlayerHolder ph)
	{
		L2ItemInstance flag = ItemTable.getInstance().createItem("", FLAG_ITEM, 1, ph.getPcInstance(), null);
		if (flag != null)
		{
			ph.getPcInstance().useEquippableItem(flag, true);
		}
	}
	
	/**
	 * We remove the flag of a character
	 * @param ph
	 */
	private void unequiFlag(PlayerHolder ph)
	{
		L2ItemInstance flag = ph.getPcInstance().getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if (flag != null)
		{
			ph.getPcInstance().useEquippableItem(flag, true);
		}
	}
	
	private void dropFlag(PlayerHolder player)
	{
		switch (player.getPcInstance().getTeam())
		{
			case RED:
				// New spawn for blue flag
				addEventNpc(BLUE_FLAG, player.getPcInstance().getLocation(), Team.BLUE, getInstancesWorlds().get(0).getInstanceId());
				// We announced that a flag was taken
				EventUtil.announceToAllPlayersInEvent(Say2.SCREEN_ANNOUNCE, "El personaje " + player.getPcInstance().getName() + "ha dropeado la bandera AZUL");
				break;
			case BLUE:
				// New spawn for the red flag
				addEventNpc(RED_FLAG, player.getPcInstance().getLocation(), Team.RED, getInstancesWorlds().get(0).getInstanceId());
				// We announced that a flag was taken
				EventUtil.announceToAllPlayersInEvent(Say2.SCREEN_ANNOUNCE, "El personaje " + player.getPcInstance().getName() + "ha dropeado la bandera RED");
				break;
		}
	}
}