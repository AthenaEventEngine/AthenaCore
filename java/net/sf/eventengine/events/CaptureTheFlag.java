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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.eventengine.datatables.ConfigData;
import net.sf.eventengine.datatables.MessageData;
import net.sf.eventengine.enums.CollectionTarget;
import net.sf.eventengine.enums.ScoreType;
import net.sf.eventengine.enums.TeamType;
import net.sf.eventengine.events.handler.AbstractEvent;
import net.sf.eventengine.events.holders.NpcHolder;
import net.sf.eventengine.events.holders.PlayerHolder;
import net.sf.eventengine.events.holders.TeamHolder;
import net.sf.eventengine.util.EventUtil;
import net.sf.eventengine.util.SortUtils;

import com.l2jserver.gameserver.datatables.ItemTable;
import com.l2jserver.gameserver.enums.Team;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;
import com.l2jserver.gameserver.model.itemcontainer.Inventory;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.items.L2Weapon;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.network.clientpackets.Say2;
import com.l2jserver.gameserver.network.serverpackets.MagicSkillUse;

/**
 * @author fissban
 */
public class CaptureTheFlag extends AbstractEvent
{
	// Flag
	private static final int FLAG = ConfigData.getInstance().CTF_NPC_FLAG_ID;
	// Holder
	private static final int HOLDER = ConfigData.getInstance().CTF_NPC_HOLDER_ID;
	// FlagItem
	private static final int FLAG_ITEM = 6718;
	// Points to conquer the flag
	private final int POINTS_CONQUER_FLAG = ConfigData.getInstance().CTF_POINTS_CONQUER_FLAG;
	private final int POINTS_KILL = ConfigData.getInstance().CTF_POINTS_KILL;
	// Radius spawn
	private static final int RADIUS_SPAWN_PLAYER = 100;
	// Time for resurrection
	private static final int TIME_RES_PLAYER = 10;
	
	private final Map<NpcHolder, TeamType> _flagSpawn = new ConcurrentHashMap<>();
	private final Map<NpcHolder, TeamType> _holderSpawn = new ConcurrentHashMap<>();
	private final Map<PlayerHolder, TeamType> _flagHasPlayer = new ConcurrentHashMap<>();
	
	public CaptureTheFlag()
	{
		super(ConfigData.getInstance().CTF_INSTANCE_FILE);
	}
	
	@Override
	protected void onEventStart()
	{
		createTeams(ConfigData.getInstance().CTF_COUNT_TEAM);
		spawnFlagsAndHolders();
		teleportAllPlayers(RADIUS_SPAWN_PLAYER);
	}
	
	@Override
	protected void onEventFight()
	{
		// Nothing
	}
	
	@Override
	protected void onEventEnd()
	{
		clearFlags();
		giveRewardsTeams();
	}
	
	@Override
	public boolean onInteract(PlayerHolder ph, NpcHolder npcHolder)
	{
		if (npcHolder.getNpcInstance().getId() == FLAG)
		{
			if (hasFlag(ph))
			{
				return false;
			}
			
			TeamType flagTeam = _flagSpawn.get(npcHolder);
			
			if (ph.getTeamType() != flagTeam)
			{
				// Animacion
				ph.getPcInstance().broadcastPacket(new MagicSkillUse(ph.getPcInstance(), ph.getPcInstance(), 1034, 1, 1, 1));
				// Borramos del MAP la bandera
				_flagSpawn.remove(npcHolder);
				// Guardamos que personaje lleva determinada bandera
				_flagHasPlayer.put(ph, flagTeam);
				// We remove the flag from his position
				getSpawnManager().removeNpc(npcHolder);
				// We equip flag
				equipFlag(ph, flagTeam);
				// We announced that a flag was taken
				EventUtil.announceTo(Say2.BATTLEFIELD, "ctf_captured_the_flag", "%holder%", flagTeam.name(), CollectionTarget.ALL_PLAYERS_IN_EVENT);
			}
		}
		else if (npcHolder.getNpcInstance().getId() == HOLDER)
		{
			if (ph.getTeamType() == _holderSpawn.get(npcHolder))
			{
				if (hasFlag(ph))
				{
					// Animacion -> Large FireWork
					ph.getPcInstance().broadcastPacket(new MagicSkillUse(ph.getPcInstance(), ph.getPcInstance(), 2025, 1, 1, 1));
					// We increased the points
					getTeamsManager().getPlayerTeam(ph).increasePoints(POINTS_CONQUER_FLAG);
					// Remove the flag character
					unequiFlag(ph);
					
					TeamHolder th = getTeamsManager().getTeam(_flagHasPlayer.remove(ph));
					// We created the flag again
					_flagSpawn.put(getSpawnManager().addEventNpc(FLAG, th.getSpawn().getX(), th.getSpawn().getY(), th.getSpawn().getZ(), 0, Team.NONE, th.getTeamType().name(), false, getInstanceWorldManager().getAllInstancesWorlds().get(0).getInstanceId()), th.getTeamType());
					// We announced that a flag was taken
					EventUtil.announceTo(Say2.BATTLEFIELD, "ctf_conquered_the_flag", "%holder%", th.getTeamType().name(), CollectionTarget.ALL_PLAYERS_IN_EVENT);
					// Show points of each team
					showPoint();
				}
			}
		}
		
		return false;
	}
	
	@Override
	public void onKill(PlayerHolder ph, L2Character target)
	{
		PlayerHolder targetEvent = getPlayerEventManager().getEventPlayer(target);
		
		if (hasFlag(targetEvent))
		{
			// Remove the flag character
			unequiFlag(targetEvent);
			// Drop flag.
			dropFlag(targetEvent);
		}
		
		// We increased the team's points.
		getTeamsManager().getPlayerTeam(ph).increasePoints(POINTS_KILL);
		
		// Reward for kills
		if (ConfigData.getInstance().CTF_REWARD_KILLER_ENABLED)
		{
			giveItems(ph, ConfigData.getInstance().CTF_REWARD_KILLER);
		}
		
		// Reward pvp for kills
		if (ConfigData.getInstance().CTF_REWARD_PVP_KILLER_ENABLED)
		{
			ph.getPcInstance().setPvpKills(ph.getPcInstance().getPvpKills() + ConfigData.getInstance().CTF_REWARD_PVP_KILLER);
			EventUtil.sendEventMessage(ph, MessageData.getInstance().getMsgByLang(ph.getPcInstance(), "reward_text_pvp", true).replace("%count%", ConfigData.getInstance().CTF_REWARD_PVP_KILLER + ""));
		}
		
		// Reward fame for kills
		if (ConfigData.getInstance().CTF_REWARD_FAME_KILLER_ENABLED)
		{
			ph.getPcInstance().setFame(ph.getPcInstance().getFame() + ConfigData.getInstance().CTF_REWARD_FAME_KILLER);
			EventUtil.sendEventMessage(ph, MessageData.getInstance().getMsgByLang(ph.getPcInstance(), "reward_text_fame", true).replace("%count%", ConfigData.getInstance().CTF_REWARD_FAME_KILLER + ""));
		}
		
		// Message Kill
		if (ConfigData.getInstance().EVENT_KILLER_MESSAGE)
		{
			EventUtil.messageKill(ph, target);
		}
		
		showPoint();
	}
	
	@Override
	public void onDeath(PlayerHolder ph)
	{
		giveResurrectPlayer(ph, TIME_RES_PLAYER, RADIUS_SPAWN_PLAYER);
	}
	
	@Override
	public boolean onUseItem(PlayerHolder ph, L2Item item)
	{
		if (item.getId() == FLAG_ITEM)
		{
			return true;
		}
		
		if (hasFlag(ph) && item instanceof L2Weapon)
		{
			return true;
		}
		
		return false;
	}
	
	@Override
	public void onLogout(PlayerHolder ph)
	{
		if (hasFlag(ph))
		{
			// Remove the flag character
			unequiFlag(ph);
			// Drop flag.
			dropFlag(ph);
		}
	}
	
	// VARIOUS METHODS -------------------------------------------------
	
	/**
	 * Spawn flags and holders
	 */
	private void spawnFlagsAndHolders()
	{
		int instanceId = getInstanceWorldManager().getAllInstancesWorlds().get(0).getInstanceId();
		
		for (TeamHolder th : getTeamsManager().getAllTeams())
		{
			int x = th.getSpawn().getX();
			int y = th.getSpawn().getY();
			int z = th.getSpawn().getZ();
			TeamType tt = th.getTeamType();
			
			_flagSpawn.put(getSpawnManager().addEventNpc(FLAG, x, y, z, 0, Team.NONE, th.getTeamType().name(), false, instanceId), tt);
			_holderSpawn.put(getSpawnManager().addEventNpc(HOLDER, x - 100, y, z, 0, Team.NONE, th.getTeamType().name(), false, instanceId), tt);
		}
	}
	
	/**
	 * We all players who are at the event and generate the teams
	 * @param countTeams
	 */
	private void createTeams(int countTeams)
	{
		// Definimos la cantidad de teams que se requieren
		getTeamsManager().setCountTeams(countTeams);
		// We define each team spawns
		getTeamsManager().setSpawnTeams(ConfigData.getInstance().CTF_COORDINATES_TEAM);
		
		// We create the instance and the world
		InstanceWorld world = getInstanceWorldManager().createNewInstanceWorld();
		
		int aux = 1;
		
		for (PlayerHolder ph : getPlayerEventManager().getAllEventPlayers())
		{
			// Obtenemos el team
			TeamType team = getTeamsManager().getEnabledTeams()[aux - 1];
			// Definimos el team del jugador
			ph.setTeam(team);
			// Ajustamos el titulo del personaje segun su team
			ph.setNewTitle("[ " + team.name() + " ]");// [ BLUE ], [ RED ] ....
			// Adjust the instance that owns the character
			ph.setDinamicInstanceId(world.getInstanceId());
			// We add the character to the world and then be teleported
			world.addAllowed(ph.getPcInstance().getObjectId());
			
			if (aux % countTeams == 0)
			{
				aux = 1;
			}
			else
			{
				aux++;
			}
		}
	}
	
	/**
	 * Give rewards.<br>
	 */
	private void giveRewardsTeams()
	{
		if (getPlayerEventManager().getAllEventPlayers().isEmpty())
		{
			return;
		}
		
		List<TeamHolder> teamWinners = SortUtils.getOrdered(getTeamsManager().getAllTeams(), ScoreType.POINT).get(0);
		
		for (PlayerHolder ph : getPlayerEventManager().getAllEventPlayers())
		{
			TeamHolder phTeam = getTeamsManager().getPlayerTeam(ph);
			// We deliver rewards
			if (teamWinners.contains(phTeam))
			{
				// We deliver rewards
				giveItems(ph, ConfigData.getInstance().CTF_REWARD_PLAYER_WIN);
			}
		}
		
		for (TeamHolder team : getTeamsManager().getAllTeams())
		{
			if (teamWinners.contains(team))
			{
				EventUtil.announceTo(Say2.BATTLEFIELD, "team_winner", "%holder%", team.getTeamType().name(), CollectionTarget.ALL_PLAYERS_IN_EVENT);
			}
		}
	}
	
	/**
	 * Show on screen the number of points that each team
	 */
	private void showPoint()
	{
		StringBuilder sb = new StringBuilder();
		
		for (TeamHolder team : getTeamsManager().getAllTeams())
		{
			sb.append(" | ");
			sb.append(team.getTeamType().name());
			sb.append(" ");
			sb.append(team.getPoints());
		}
		sb.append(" | ");
		
		for (PlayerHolder ph : getPlayerEventManager().getAllEventPlayers())
		{
			EventUtil.sendEventScreenMessage(ph, sb.toString(), 10000);
		}
	}
	
	/**
	 * Check if a character has a flag
	 * @param ph
	 * @return
	 */
	private boolean hasFlag(PlayerHolder ph)
	{
		return _flagHasPlayer.containsKey(ph);
	}
	
	/**
	 * We equip a character with a flag
	 * @param ph
	 * @param flagTeam
	 */
	private void equipFlag(PlayerHolder ph, TeamType flagTeam)
	{
		_flagHasPlayer.put(ph, flagTeam);
		
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
	
	private void dropFlag(PlayerHolder ph)
	{
		TeamHolder th = getTeamsManager().getTeam(_flagHasPlayer.remove(ph));
		_flagSpawn.put(getSpawnManager().addEventNpc(FLAG, ph.getPcInstance().getX(), ph.getPcInstance().getY(), ph.getPcInstance().getZ(), 0, Team.NONE, th.getTeamType().name(), false, ph.getDinamicInstanceId()), th.getTeamType());
		
		Map<String, String> map = new HashMap<>();
		
		// We announced that a flag was taken
		map.put("%holder%", ph.getPcInstance().getName());
		map.put("%flag%", th.getTeamType().name());
		EventUtil.announceTo(Say2.BATTLEFIELD, "player_dropped_flag", map, CollectionTarget.ALL_PLAYERS_IN_EVENT);
	}
	
	/**
	 * Remove all the flags equipped
	 */
	private void clearFlags()
	{
		for (PlayerHolder ph : _flagHasPlayer.keySet())
		{
			unequiFlag(ph);
		}
		_flagHasPlayer.clear();
	}
}