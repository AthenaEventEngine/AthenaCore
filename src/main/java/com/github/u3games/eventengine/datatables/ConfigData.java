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
package com.github.u3games.eventengine.datatables;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.github.u3games.eventengine.util.EventPropertiesParser;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.holders.ItemHolder;

/**
 * Load the config from "properties"
 * @author fissban
 */
public class ConfigData
{
	private static final Logger LOGGER = Logger.getLogger(ConfigData.class.getName());
	private static final String TVT_CONFIG = "./config/EventEngine/TeamVsTeam.properties";
	private static final String AVA_CONFIG = "./config/EventEngine/AllVsAll.properties";
	private static final String CTF_CONFIG = "./config/EventEngine/CaptureTheFlag.properties";
	private static final String OVO_CONFIG = "./config/EventEngine/OneVsOne.properties";
	private static final String SURVIVE_CONFIG = "./config/EventEngine/Survive.properties";
	
	// General configs
	
	// -------------------------------------------------------------------------------
	// Configs Capture The Flag
	// -------------------------------------------------------------------------------
	public boolean CTF_EVENT_ENABLED;
	public String CTF_INSTANCE_FILE;
	public int CTF_NPC_FLAG_ID;
	public int CTF_NPC_HOLDER_ID;
	public List<ItemHolder> CTF_REWARD_PLAYER_WIN = new ArrayList<>();
	public boolean CTF_REWARD_KILLER_ENABLED;
	public List<ItemHolder> CTF_REWARD_KILLER = new ArrayList<>();
	public boolean CTF_REWARD_PVP_KILLER_ENABLED;
	public int CTF_REWARD_PVP_KILLER;
	public boolean CTF_REWARD_FAME_KILLER_ENABLED;
	public int CTF_REWARD_FAME_KILLER;
	public int CTF_POINTS_CONQUER_FLAG;
	public int CTF_POINTS_KILL;
	public int CTF_COUNT_TEAM;
	public List<Location> CTF_COORDINATES_TEAM;
	
	// -------------------------------------------------------------------------------
	// Configs All Vs All
	// -------------------------------------------------------------------------------
	public boolean AVA_EVENT_ENABLED;
	public String AVA_INSTANCE_FILE;
	public List<ItemHolder> AVA_REWARD_PLAYER_WIN = new ArrayList<>();
	public boolean AVA_REWARD_KILLER_ENABLED;
	public List<ItemHolder> AVA_REWARD_KILLER = new ArrayList<>();
	public boolean AVA_REWARD_PVP_KILLER_ENABLED;
	public int AVA_REWARD_PVP_KILLER;
	public boolean AVA_REWARD_FAME_KILLER_ENABLED;
	public int AVA_REWARD_FAME_KILLER;
	public List<Location> AVA_COORDINATES_TEAM;
	
	// -------------------------------------------------------------------------------
	// Configs One Vs One
	// -------------------------------------------------------------------------------
	public boolean OVO_EVENT_ENABLED;
	public String OVO_INSTANCE_FILE;
	public List<ItemHolder> OVO_REWARD_PLAYER_WIN = new ArrayList<>();
	public boolean OVO_REWARD_KILLER_ENABLED;
	public List<ItemHolder> OVO_REWARD_KILLER = new ArrayList<>();
	public boolean OVO_REWARD_PVP_KILLER_ENABLED;
	public int OVO_REWARD_PVP_KILLER;
	public boolean OVO_REWARD_FAME_KILLER_ENABLED;
	public int OVO_REWARD_FAME_KILLER;
	public int OVO_COUNT_TEAM;
	public List<Location> OVO_COORDINATES_TEAM;
	
	// -------------------------------------------------------------------------------
	// Configs Survive
	// -------------------------------------------------------------------------------
	public boolean SURVIVE_EVENT_ENABLED;
	public String SURVIVE_INSTANCE_FILE;
	public List<ItemHolder> SURVIVE_REWARD_PLAYER_WIN = new ArrayList<>();
	public Location SURVIVE_COORDINATES_MOBS;
	public List<Integer> SURVIVE_MONSTERS_ID = new ArrayList<>();
	public int SURVIVE_MONSTER_SPAWN_FOR_STAGE;
	public int SURVIVE_COUNT_TEAM;
	public List<Location> SURVIVE_COORDINATES_TEAM;
	
	public ConfigData()
	{
		load();
	}
	
	public void load()
	{
		EventPropertiesParser settings;

		// ------------------------------------------------------------------------------------- //
		// CaptureTheFlag.properties
		// ------------------------------------------------------------------------------------- //
		settings = new EventPropertiesParser(CTF_CONFIG);
		CTF_EVENT_ENABLED = settings.getBoolean("EventEnabled", false);
		CTF_INSTANCE_FILE = settings.getString("EventInstanceFile", "EventEngine.xml");
		CTF_NPC_FLAG_ID = settings.getInt("EventFlagNpcId", 36601);
		CTF_NPC_HOLDER_ID = settings.getInt("EventHolderNpcId", 36603);
		CTF_REWARD_PLAYER_WIN = settings.getItemHolderList("EventReward");
		CTF_REWARD_KILLER_ENABLED = settings.getBoolean("EventRewardKillEnabled", false);
		CTF_REWARD_KILLER = settings.getItemHolderList("EventRewardKill");
		CTF_REWARD_PVP_KILLER_ENABLED = settings.getBoolean("EventRewardPvPKillEnabled", false);
		CTF_REWARD_PVP_KILLER = settings.getInt("EventRewardPvPKill", 1);
		CTF_REWARD_FAME_KILLER_ENABLED = settings.getBoolean("EventRewardFameKillEnabled", false);
		CTF_REWARD_FAME_KILLER = settings.getInt("EventRewardFameKill", 10);
		CTF_POINTS_CONQUER_FLAG = settings.getInt("EventPointsConquerFlag", 10);
		CTF_POINTS_KILL = settings.getInt("EventPointsKill", 1);
		CTF_COORDINATES_TEAM = settings.getLocationList("EventTeamCoordinates");
		CTF_COUNT_TEAM = settings.getInt("EventCountTeam", 2);
		checkCountTeamAndSpawn("CaptureTheFlag", CTF_COORDINATES_TEAM, CTF_COUNT_TEAM);
		checkCountTeamdAndMinPlayers("CaptureTheFlag", 2, CTF_COUNT_TEAM);
		
		// ------------------------------------------------------------------------------------- //
		// AllVsAll.properties
		// ------------------------------------------------------------------------------------- //
		settings = new EventPropertiesParser(AVA_CONFIG);
		AVA_INSTANCE_FILE = settings.getString("EventInstanceFile", "EventEngine.xml");
		AVA_EVENT_ENABLED = settings.getBoolean("EventEnabled", false);
		AVA_REWARD_PLAYER_WIN = settings.getItemHolderList("EventReward");
		AVA_REWARD_KILLER_ENABLED = settings.getBoolean("EventRewardKillEnabled", false);
		AVA_REWARD_KILLER = settings.getItemHolderList("EventRewardKill");
		AVA_REWARD_PVP_KILLER_ENABLED = settings.getBoolean("EventRewardPvPKillEnabled", false);
		AVA_REWARD_PVP_KILLER = settings.getInt("EventRewardPvPKill", 1);
		AVA_REWARD_FAME_KILLER_ENABLED = settings.getBoolean("EventRewardFameKillEnabled", false);
		AVA_REWARD_FAME_KILLER = settings.getInt("EventRewardFameKill", 10);
		AVA_COORDINATES_TEAM = settings.getLocationList("EventTeamCoordinates");
		
		// ------------------------------------------------------------------------------------- //
		// OneVsOne.properties
		// ------------------------------------------------------------------------------------- //
		settings = new EventPropertiesParser(OVO_CONFIG);
		OVO_INSTANCE_FILE = settings.getString("EventInstanceFile", "EventEngine.xml");
		OVO_EVENT_ENABLED = settings.getBoolean("EventEnabled", false);
		OVO_REWARD_PLAYER_WIN = settings.getItemHolderList("EventReward");
		OVO_REWARD_KILLER_ENABLED = settings.getBoolean("EventRewardKillEnabled", false);
		OVO_REWARD_KILLER = settings.getItemHolderList("EventRewardKill");
		OVO_REWARD_PVP_KILLER_ENABLED = settings.getBoolean("EventRewardPvPKillEnabled", false);
		OVO_REWARD_PVP_KILLER = settings.getInt("EventRewardPvPKill", 1);
		OVO_REWARD_FAME_KILLER_ENABLED = settings.getBoolean("EventRewardFameKillEnabled", false);
		OVO_REWARD_FAME_KILLER = settings.getInt("EventRewardFameKill", 10);
		OVO_COUNT_TEAM = settings.getInt("EventCountTeam", 2);
		OVO_COORDINATES_TEAM = settings.getLocationList("EventTeamCoordinates");
		checkCountTeamAndSpawn("OneVsOne", OVO_COORDINATES_TEAM, OVO_COUNT_TEAM);
		checkCountTeamdAndMinPlayers("OneVsOne", 2, OVO_COUNT_TEAM);
		
		// ------------------------------------------------------------------------------------- //
		// TeamVsTeam.properties
		// ------------------------------------------------------------------------------------- //
		/*settings = new EventPropertiesParser(TVT_CONFIG);
		TVT_INSTANCE_FILE = settings.getString("EventInstanceFile", "EventEngine.xml");
		TVT_EVENT_ENABLED = settings.getBoolean("EventEnabled", false);
		TVT_REWARD_PLAYER_WIN = settings.getItemHolderList("EventReward");
		TVT_REWARD_KILLER_ENABLED = settings.getBoolean("EventRewardKillEnabled", false);
		TVT_REWARD_KILLER = settings.getItemHolderList("EventRewardKill");
		TVT_REWARD_PVP_KILLER_ENABLED = settings.getBoolean("EventRewardPvPKillEnabled", false);
		TVT_REWARD_PVP_KILLER = settings.getInt("EventRewardPvPKill", 1);
		TVT_REWARD_FAME_KILLER_ENABLED = settings.getBoolean("EventRewardFameKillEnabled", false);
		TVT_REWARD_FAME_KILLER = settings.getInt("EventRewardFameKill", 10);
		TVT_COUNT_TEAM = settings.getInt("EventCountTeam", 2);
		TVT_COORDINATES_TEAM = settings.getLocationList("EventTeamCoordinates");
		checkCountTeamAndSpawn("TeamVsTeam", TVT_COORDINATES_TEAM, TVT_COUNT_TEAM);
		checkCountTeamdAndMinPlayers("TeamVsTeam", 2, TVT_COUNT_TEAM);*/
		
		// ------------------------------------------------------------------------------------- //
		// Survive.properties
		// ------------------------------------------------------------------------------------- //
		settings = new EventPropertiesParser(SURVIVE_CONFIG);
		SURVIVE_INSTANCE_FILE = settings.getString("EventInstanceFile", "EventEngine.xml");
		SURVIVE_EVENT_ENABLED = settings.getBoolean("EventEnabled", false);
		SURVIVE_REWARD_PLAYER_WIN = settings.getItemHolderList("EventReward");
		SURVIVE_COORDINATES_MOBS = settings.getLocation("EventCoordinatesMobs");
		SURVIVE_MONSTERS_ID = settings.getListInteger("EventMobsID");
		SURVIVE_MONSTER_SPAWN_FOR_STAGE = settings.getInt("EventMobsSpawnForStage", 5);
		SURVIVE_COUNT_TEAM = settings.getInt("EventCountTeam", 2);
		SURVIVE_COORDINATES_TEAM = settings.getLocationList("EventTeamCoordinates");
		checkCountTeamdAndMinPlayers("Survive", 2, SURVIVE_COUNT_TEAM);
	}
	
	private void checkCountTeamAndSpawn(String eventName, List<Location> locs, int teams)
	{
		if (locs.size() != teams)
		{
			LOGGER.warning(ConfigData.class.getSimpleName() + ": " + eventName + " The amount of equipment does not match the amount of spawns");
			LOGGER.info("locs count: " + locs.size());
			LOGGER.info("teams: " + teams);
		}
	}
	
	private void checkCountTeamdAndMinPlayers(String eventName, int minPlayers, int teams)
	{
		if (teams > minPlayers)
		{
			LOGGER.warning(ConfigData.class.getSimpleName() + ": " + eventName + " You must have at least one player for each team.");
			LOGGER.info("minPlayers: " + minPlayers);
			LOGGER.info("teams: " + teams);
		}
	}
	
	public static ConfigData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ConfigData _instance = new ConfigData();
	}
}