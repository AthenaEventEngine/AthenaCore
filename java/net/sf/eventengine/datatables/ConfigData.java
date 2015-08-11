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
package net.sf.eventengine.datatables;

import java.util.ArrayList;
import java.util.List;

import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.holders.ItemHolder;

import net.sf.eventengine.util.EventPropertiesParser;

/**
 * Load the config from "properties"
 * @author fissban
 */
public class ConfigData
{
	private static final String EVENT_CONFIG = "./config/EventEngine/EventEngine.properties";
	private static final String TVT_CONFIG = "./config/EventEngine/TeamVsTeam.properties";
	private static final String AVA_CONFIG = "./config/EventEngine/AllVsAll.properties";
	private static final String CTF_CONFIG = "./config/EventEngine/CaptureTheFlag.properties";
	private static final String OVO_CONFIG = "./config/EventEngine/OneVsOne.properties";
	private static final String SURVIVE_CONFIG = "./config/EventEngine/Survive.properties";
	
	// General configs
	public int NPC_MANAGER_ID;
	public boolean EVENT_MESSAGE_GLOBAL;
	public int EVENT_TASK;
	public boolean EVENT_VOTING_ENABLED;
	public int EVENT_VOTING_TIME;
	public int EVENT_REGISTER_TIME;
	public int EVENT_DURATION;
	public int EVENT_TEXT_TIME_FOR_END;
	public boolean EVENT_KILLER_MESSAGE;
	public boolean FRIENDLY_FIRE;
	public int MIN_PLAYERS_IN_EVENT;
	public int MAX_PLAYERS_IN_EVENT;
	public int MIN_LVL_IN_EVENT;
	public int MAX_LVL_IN_EVENT;
	public static int MAX_BUFF_COUNT;
	
	// -------------------------------------------------------------------------------
	// Configs Capture The Flag
	// -------------------------------------------------------------------------------
	public boolean CTF_EVENT_ENABLED;
	public String CTF_INSTANCE_FILE;
	public List<ItemHolder> CTF_REWARD_PLAYER_WIN = new ArrayList<>();
	public boolean CTF_REWARD_KILLER_ENABLED;
	public List<ItemHolder> CTF_REWARD_KILLER = new ArrayList<>();
	public boolean CTF_REWARD_PVP_KILLER_ENABLED;
	public int CTF_REWARD_PVP_KILLER;
	public boolean CTF_REWARD_FAME_KILLER_ENABLED;
	public int CTF_REWARD_FAME_KILLER;
	public Location CTF_COORDINATES_TEAM_RED;
	public Location CTF_COORDINATES_TEAM_BLUE;
	public int CTF_POINTS_CONQUER_FLAG;
	public int CTF_POINTS_KILL;
	
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
	public Location AVA_COORDINATES_PLAYER;
	
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
	public Location OVO_COORDINATES_TEAM_RED;
	public Location OVO_COORDINATES_TEAM_BLUE;
	
	// -------------------------------------------------------------------------------
	// Configs Team Vs Team
	// -------------------------------------------------------------------------------
	public String TVT_INSTANCE_FILE;
	public boolean TVT_EVENT_ENABLED;
	public List<ItemHolder> TVT_REWARD_PLAYER_WIN = new ArrayList<>();
	public boolean TVT_REWARD_KILLER_ENABLED;
	public List<ItemHolder> TVT_REWARD_KILLER = new ArrayList<>();
	public boolean TVT_REWARD_PVP_KILLER_ENABLED;
	public int TVT_REWARD_PVP_KILLER;
	public boolean TVT_REWARD_FAME_KILLER_ENABLED;
	public int TVT_REWARD_FAME_KILLER;
	public Location TVT_COORDINATES_TEAM_RED;
	public Location TVT_COORDINATES_TEAM_BLUE;
	
	// -------------------------------------------------------------------------------
	// Configs Survive
	// -------------------------------------------------------------------------------
	public boolean SURVIVE_EVENT_ENABLED;
	public String SURVIVE_INSTANCE_FILE;
	public List<ItemHolder> SURVIVE_REWARD_PLAYER_WIN = new ArrayList<>();
	public Location SURVIVE_COORDINATES_PLAYER;
	public Location SURVIVE_COORDINATES_MOBS;
	public List<Integer> SURVIVE_MONSTERS_ID = new ArrayList<>();
	public int SURVIVE_MONSTER_SPAWN_FOR_STAGE;
	
	public ConfigData()
	{
		load();
	}
	
	public void load()
	{
		EventPropertiesParser settings;
		// ------------------------------------------------------------------------------------- //
		// EventEngine.properties
		// ------------------------------------------------------------------------------------- //
		settings = new EventPropertiesParser(EVENT_CONFIG);
		NPC_MANAGER_ID = settings.getInt("EventParticipationNpcId", 36600);
		EVENT_MESSAGE_GLOBAL = settings.getBoolean("EventGlobalMessage", true);
		EVENT_TASK = settings.getInt("EventInterval", 10);
		EVENT_VOTING_ENABLED = settings.getBoolean("EventVotingEnabled", true);
		EVENT_VOTING_TIME = settings.getInt("EventVotingTime", 10);
		EVENT_REGISTER_TIME = settings.getInt("EventRegisterTime", 10);
		EVENT_DURATION = settings.getInt("EventRunningTime", 10);
		EVENT_TEXT_TIME_FOR_END = settings.getInt("EventTextTimeForEnd", 10);
		EVENT_KILLER_MESSAGE = settings.getBoolean("EventKillerMessage", true);
		FRIENDLY_FIRE = settings.getBoolean("EventFriendlyFire", false);
		MIN_PLAYERS_IN_EVENT = settings.getInt("EventMinPlayers", 2);
		MAX_PLAYERS_IN_EVENT = settings.getInt("EventMaxPlayers", 20);
		MIN_LVL_IN_EVENT = settings.getInt("EventMinPlayerLevel", 40);
		MAX_LVL_IN_EVENT = settings.getInt("EventMaxPlayerLevel", 78);
		MAX_BUFF_COUNT = settings.getInt("EventMaxBuffCount", 5);
		
		// ------------------------------------------------------------------------------------- //
		// CaptureTheFlag.properties
		// ------------------------------------------------------------------------------------- //
		settings = new EventPropertiesParser(CTF_CONFIG);
		CTF_INSTANCE_FILE = settings.getString("EventInstanceFile", "EventEngine.xml");
		CTF_EVENT_ENABLED = settings.getBoolean("EventEnabled", false);
		CTF_REWARD_PLAYER_WIN = settings.getItemHolderList("EventReward");
		CTF_REWARD_KILLER_ENABLED = settings.getBoolean("EventRewardKillEnabled", false);
		CTF_REWARD_KILLER = settings.getItemHolderList("EventRewardKill");
		CTF_REWARD_PVP_KILLER_ENABLED = settings.getBoolean("EventRewardPvPKillEnabled", false);
		CTF_REWARD_PVP_KILLER = settings.getInt("EventRewardPvPKill", 1);
		CTF_REWARD_FAME_KILLER_ENABLED = settings.getBoolean("EventRewardFameKillEnabled", false);
		CTF_REWARD_FAME_KILLER = settings.getInt("EventRewardFameKill", 10);
		CTF_COORDINATES_TEAM_RED = settings.getLocation("EventTeam1Coordinates");
		CTF_COORDINATES_TEAM_BLUE = settings.getLocation("EventTeam2Coordinates");
		CTF_POINTS_CONQUER_FLAG = settings.getInt("EventPointsConquerFlag", 10);
		CTF_POINTS_KILL = settings.getInt("EventPointsKill", 1);
		
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
		AVA_COORDINATES_PLAYER = settings.getLocation("EventCoordinates");
		
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
		OVO_COORDINATES_TEAM_RED = settings.getLocation("EventTeam1Coordinates");
		OVO_COORDINATES_TEAM_BLUE = settings.getLocation("EventTeam2Coordinates");
		
		// ------------------------------------------------------------------------------------- //
		// TeamVsTeam.properties
		// ------------------------------------------------------------------------------------- //
		settings = new EventPropertiesParser(TVT_CONFIG);
		TVT_INSTANCE_FILE = settings.getString("EventInstanceFile", "EventEngine.xml");
		TVT_EVENT_ENABLED = settings.getBoolean("EventEnabled", false);
		TVT_REWARD_PLAYER_WIN = settings.getItemHolderList("EventReward");
		TVT_REWARD_KILLER_ENABLED = settings.getBoolean("EventRewardKillEnabled", false);
		TVT_REWARD_KILLER = settings.getItemHolderList("EventRewardKill");
		TVT_REWARD_PVP_KILLER_ENABLED = settings.getBoolean("EventRewardPvPKillEnabled", false);
		TVT_REWARD_PVP_KILLER = settings.getInt("EventRewardPvPKill", 1);
		TVT_REWARD_FAME_KILLER_ENABLED = settings.getBoolean("EventRewardFameKillEnabled", false);
		TVT_REWARD_FAME_KILLER = settings.getInt("EventRewardFameKill", 10);
		TVT_COORDINATES_TEAM_RED = settings.getLocation("EventTeam1Coordinates");
		TVT_COORDINATES_TEAM_BLUE = settings.getLocation("EventTeam2Coordinates");
		
		// ------------------------------------------------------------------------------------- //
		// Survive.properties
		// ------------------------------------------------------------------------------------- //
		settings = new EventPropertiesParser(SURVIVE_CONFIG);
		SURVIVE_INSTANCE_FILE = settings.getString("EventInstanceFile", "EventEngine.xml");
		SURVIVE_EVENT_ENABLED = settings.getBoolean("EventEnabled", false);
		SURVIVE_REWARD_PLAYER_WIN = settings.getItemHolderList("EventReward");
		SURVIVE_COORDINATES_PLAYER = settings.getLocation("EventCoordinatesPlayer");
		SURVIVE_COORDINATES_MOBS = settings.getLocation("EventCoordinatesMobs");
		SURVIVE_MONSTERS_ID = settings.getListInteger("EventMobsID");
		SURVIVE_MONSTER_SPAWN_FOR_STAGE = settings.getInt("EventMobsSpawnForStage", 5);
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
