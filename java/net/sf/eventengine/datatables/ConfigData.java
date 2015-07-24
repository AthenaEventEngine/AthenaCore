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

import net.sf.eventengine.util.EventPropertiesParser;

import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.holders.ItemHolder;

/**
 * Clase encargada de leer todos los configs establecidos en los archivos de tipo "properties"
 * @author fissban
 */
public class ConfigData
{
	private static final String EVENT_CONFIG = "./config/EventEngine/EventEngine.properties";
	private static final String TVT_CONFIG = "./config/EventEngine/TeamVsTeam.properties";
	private static final String AVA_CONFIG = "./config/EventEngine/AllVsAll.properties";
	// private static final String CTF_CONFIG = "./config/EventEngine/CTF.properties";
	private static final String OVO_CONFIG = "./config/EventEngine/OneVsOne.properties";
	private static final String SURVIVE_CONFIG = "./config/EventEngine/Survive.properties";
	
	// lista de configs generales
	
	/** Definimos ID del npc del engine */
	public int NPC_MANAGER_ID;
	/** Definimos el xml q usaremos para nuestras instancias. */
	public String INSTANCE_FILE;
	/** Definimos cada cuanto se ejecutara algun evento en hs */
	public int EVENT_TASK;
	/** Definimos si va a haber tiempo de votacion */
	public boolean EVENT_VOTING_ENABLED;
	/** Definimos el tiempo que durara el periodo de votacion */
	public int EVENT_VOTING_TIME;
	/** Definimos el tiempo que durara el periodo de registro */
	public int EVENT_REGISTER_TIME;
	/** Definimos el tiempo que durara cada evento en minutos */
	public int EVENT_DURATION;
	/** Definimos si permitimos o no el daño entre amigos. */
	public boolean FRIENDLY_FIRE;
	/** Definimos la cant de players maximo/minimo que podran participar */
	public int MIN_PLAYERS_IN_EVENT;
	public int MAX_PLAYERS_IN_EVENT;
	/** Definimos el lvl maximo/minimo que podran participar de los eventos. */
	public int MIN_LVL_IN_EVENT;
	public int MAX_LVL_IN_EVENT;
	/** Definimos la cantidad maxima de buffs q se podran usar */
	public static int MAX_BUFF_COUNT;
	
	// -------------------------------------------------------------------------------
	// Configs All Vs All
	// -------------------------------------------------------------------------------
	public boolean AVA_EVENT_ENABLED;
	public List<ItemHolder> AVA_REWARD_PLAYER_WIN = new ArrayList<>();
	public List<ItemHolder> AVA_REWARD_KILL_PLAYER = new ArrayList<>();
	public Location AVA_COORDINATES_PLAYER;
	
	// -------------------------------------------------------------------------------
	// Configs One Vs One
	// -------------------------------------------------------------------------------
	public boolean OVO_EVENT_ENABLED;
	public boolean OVO_REWARD_TEAM_TIE;
	public List<ItemHolder> OVO_REWARD_PLAYER_WIN = new ArrayList<>();
	public List<ItemHolder> OVO_REWARD_KILL_PLAYER = new ArrayList<>();
	public Location OVO_COORDINATES_TEAM_1;
	public Location OVO_COORDINATES_TEAM_2;
	
	// -------------------------------------------------------------------------------
	// Configs Team Vs Team
	// -------------------------------------------------------------------------------
	public boolean TVT_EVENT_ENABLED;
	public List<ItemHolder> TVT_REWARD_PLAYER_WIN = new ArrayList<>();
	public List<ItemHolder> TVT_REWARD_KILL_PLAYER = new ArrayList<>();
	public Location TVT_COORDINATES_TEAM_1;
	public Location TVT_COORDINATES_TEAM_2;
	
	// -------------------------------------------------------------------------------
	// Configs Survive
	// -------------------------------------------------------------------------------
	public boolean SURVIVE_EVENT_ENABLED;
	public List<ItemHolder> SURVIVE_REWARD_PLAYER_WIN = new ArrayList<>();
	public Location SURVIVE_COORDINATES_PLAYER;
	public Location SURVIVE_COORDINATES_MOBS;
	public List<Integer> SURVIVE_MONSTERS_ID = new ArrayList<>();
	public int SURVIVE_MONSTER_SPAWN_FOR_STAGE;
	
	public ConfigData()
	{
		load();
	}
	
	// Metodo encargado de leer los configs
	public void load()
	{
		EventPropertiesParser settings;
		// ------------------------------------------------------------------------------------- //
		// EventEngine.properties
		// ------------------------------------------------------------------------------------- //
		settings = new EventPropertiesParser(EVENT_CONFIG);
		NPC_MANAGER_ID = settings.getInt("EventParticipationNpcId", 36600);
		EVENT_TASK = settings.getInt("EventInterval", 10);
		EVENT_VOTING_ENABLED = settings.getBoolean("EventVotingEnabled", true);
		EVENT_VOTING_TIME = settings.getInt("EventVotingTime", 10);
		EVENT_REGISTER_TIME = settings.getInt("EventRegisterTime", 10);
		EVENT_DURATION = settings.getInt("EventRunningTime", 10);
		FRIENDLY_FIRE = settings.getBoolean("EventFriendlyFire", false);
		MIN_PLAYERS_IN_EVENT = settings.getInt("EventMinPlayers", 2);
		MAX_PLAYERS_IN_EVENT = settings.getInt("EventMaxPlayers", 20);
		MIN_LVL_IN_EVENT = settings.getInt("EventMinPlayerLevel", 40);
		MAX_LVL_IN_EVENT = settings.getInt("EventMaxPlayerLevel", 78);
		INSTANCE_FILE = settings.getString("EventInstanceFile", "EventEngine.xml");
		MAX_BUFF_COUNT = settings.getInt("EventMaxBuffCount", 5);
		
		// ------------------------------------------------------------------------------------- //
		// AllVsAll.properties
		// ------------------------------------------------------------------------------------- //
		settings = new EventPropertiesParser(AVA_CONFIG);
		AVA_EVENT_ENABLED = settings.getBoolean("AvAEventEnabled", false);
		AVA_REWARD_PLAYER_WIN = settings.getItemHolderList("AvAEventReward");
		AVA_REWARD_KILL_PLAYER = settings.getItemHolderList("AvAEventRewardKill");
		AVA_COORDINATES_PLAYER = settings.getLocation("AvAEventCoordinates");
		
		// ------------------------------------------------------------------------------------- //
		// OneVsOne.properties
		// ------------------------------------------------------------------------------------- //
		settings = new EventPropertiesParser(OVO_CONFIG);
		OVO_EVENT_ENABLED = settings.getBoolean("OvOEventEnabled", false);
		OVO_REWARD_PLAYER_WIN = settings.getItemHolderList("OvOEventReward");
		OVO_REWARD_KILL_PLAYER = settings.getItemHolderList("OvOEventRewardKill");
		OVO_COORDINATES_TEAM_1 = settings.getLocation("OvOEventTeam1Coordinates");
		OVO_COORDINATES_TEAM_2 = settings.getLocation("OvOEventTeam2Coordinates");
		
		// ------------------------------------------------------------------------------------- //
		// TeamVsTeam.properties
		// ------------------------------------------------------------------------------------- //
		settings = new EventPropertiesParser(TVT_CONFIG);
		TVT_EVENT_ENABLED = settings.getBoolean("TvTEventEnabled", false);
		TVT_REWARD_PLAYER_WIN = settings.getItemHolderList("TvTEventReward");
		TVT_REWARD_KILL_PLAYER = settings.getItemHolderList("TvTEventRewardKill");
		TVT_COORDINATES_TEAM_1 = settings.getLocation("TvTEventTeam1Coordinates");
		TVT_COORDINATES_TEAM_2 = settings.getLocation("TvTEventTeam2Coordinates");
		
		// ------------------------------------------------------------------------------------- //
		// Survive.properties
		// ------------------------------------------------------------------------------------- //
		settings = new EventPropertiesParser(SURVIVE_CONFIG);
		SURVIVE_EVENT_ENABLED = settings.getBoolean("SVEventEnabled", false);
		SURVIVE_REWARD_PLAYER_WIN = settings.getItemHolderList("SVEventReward");
		SURVIVE_COORDINATES_PLAYER = settings.getLocation("SVEventCoordinatesPlayer");
		SURVIVE_COORDINATES_MOBS = settings.getLocation("SVEventCoordinatesMobs");
		SURVIVE_MONSTERS_ID = settings.getListInteger("SVEventMobsID");
		SURVIVE_MONSTER_SPAWN_FOR_STAGE = settings.getInt("SVEventMobsSpawnForStage", 5);
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
