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
package net.sf.eventengine.configs;

import java.util.ArrayList;
import java.util.List;

import net.sf.eventengine.util.EventPropertiesParser;

import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.gameserver.model.holders.SkillHolder;

/**
 * Clase encargada de leer todos los configs establecidos en los archivos de tipo "properties"
 * @author fissban
 */
public class Configs
{
	private static final String EVENT_CONFIG = "./config/EventEngine/EventEngine.properties";
	private static final String TVT_CONFIG = "./config/EventEngine/TeamVsTeam.properties";
	private static final String AVA_CONFIG = "./config/EventEngine/AllVsAll.properties";
	// private static final String CTF_CONFIG = "./config/EventEngine/CTF.properties";
	private static final String OVO_CONFIG = "./config/EventEngine/OneVsOne.properties";
	private static final String SURVIVE_CONFIG = "./config/EventEngine/Survive.properties";
	
	// lista de configs generales
	
	/** Definimos ID del npc del engine */
	public static int NPC_MANAGER_ID;
	/** Definimos el xml q usaremos para nuestras instancias. */
	public static String INSTANCE_FILE;
	/** Definimos cada cuanto se ejecutara algun evento en hs */
	public static int EVENT_TASK;
	/** Definimos el tiempo que durara cada evento en minutos */
	public static int EVENT_DURATION;
	/** Definimos si permitimos o no el daño entre amigos. */
	public static boolean FRIENDLY_FIRE;
	/** Definimos la cant de players maximo/minimo que podran participar */
	public static int MIN_PLAYERS_IN_EVENT;
	public static int MAX_PLAYERS_IN_EVENT;
	/** Definimos el lvl maximo/minimo que podran participar de los eventos. */
	public static int MIN_LVL_IN_EVENT;
	public static int MAX_LVL_IN_EVENT;
	
	// -------------------------------------------------------------------------------
	// Configs All Vs All
	// -------------------------------------------------------------------------------
	public static boolean AVA_EVENT_ENABLED;
	public static List<ItemHolder> AVA_REWARD_PLAYER_WIN = new ArrayList<>();
	public static List<ItemHolder> AVA_REWARD_KILL_PLAYER = new ArrayList<>();
	public static List<SkillHolder> AVA_BUFF_PLAYER_WARRIOR = new ArrayList<>();
	public static List<SkillHolder> AVA_BUFF_PLAYER_MAGE = new ArrayList<>();
	public static Location AVA_COORDINATES_PLAYER;
	
	// -------------------------------------------------------------------------------
	// Configs One Vs One
	// -------------------------------------------------------------------------------
	public static boolean OVO_EVENT_ENABLED;
	public static boolean OVO_REWARD_TEAM_TIE;
	public static List<ItemHolder> OVO_REWARD_PLAYER_WIN = new ArrayList<>();
	public static List<ItemHolder> OVO_REWARD_KILL_PLAYER = new ArrayList<>();
	public static List<SkillHolder> OVO_BUFF_PLAYER_WARRIOR = new ArrayList<>();
	public static List<SkillHolder> OVO_BUFF_PLAYER_MAGE = new ArrayList<>();
	public static Location OVO_COORDINATES_TEAM_1;
	public static Location OVO_COORDINATES_TEAM_2;
	
	// -------------------------------------------------------------------------------
	// Configs Team Vs Team
	// -------------------------------------------------------------------------------
	public static boolean TVT_EVENT_ENABLED;
	public static List<ItemHolder> TVT_REWARD_PLAYER_WIN = new ArrayList<>();
	public static List<ItemHolder> TVT_REWARD_KILL_PLAYER = new ArrayList<>();
	public static List<SkillHolder> TVT_BUFF_PLAYER_WARRIOR = new ArrayList<>();
	public static List<SkillHolder> TVT_BUFF_PLAYER_MAGE = new ArrayList<>();
	public static Location TVT_COORDINATES_TEAM_1;
	public static Location TVT_COORDINATES_TEAM_2;
	
	// -------------------------------------------------------------------------------
	// Configs Survive
	// -------------------------------------------------------------------------------
	public static boolean SURVIVE_EVENT_ENABLED;
	public static List<ItemHolder> SURVIVE_REWARD_PLAYER_WIN = new ArrayList<>();
	public static List<SkillHolder> SURVIVE_BUFF_PLAYER_WARRIOR = new ArrayList<>();
	public static List<SkillHolder> SURVIVE_BUFF_PLAYER_MAGE = new ArrayList<>();
	public static Location SURVIVE_COORDINATES_PLAYER;
	public static Location SURVIVE_COORDINATES_MOBS;
	public static List<Integer> SURVIVE_MONSTERS_ID = new ArrayList<>();
	public static int SURVIVE_MONSTER_SPAWN_FOR_STAGE;
	
	// Metodo encargado de leer los configs
	public static void load()
	{
		EventPropertiesParser settings;
		// ------------------------------------------------------------------------------------- //
		// EventEngine.properties
		// ------------------------------------------------------------------------------------- //
		settings = new EventPropertiesParser(EVENT_CONFIG);
		NPC_MANAGER_ID = settings.getInt("EventParticipationNpcId", 36600);
		EVENT_TASK = settings.getInt("EventInterval", 10);
		EVENT_DURATION = settings.getInt("EventRunningTime", 10);
		FRIENDLY_FIRE = settings.getBoolean("EventFriendlyFire", false);
		MIN_PLAYERS_IN_EVENT = settings.getInt("EventMinPlayers", 2);
		MAX_PLAYERS_IN_EVENT = settings.getInt("EventMaxPlayers", 20);
		MIN_LVL_IN_EVENT = settings.getInt("EventMinPlayerLevel", 40);
		MAX_LVL_IN_EVENT = settings.getInt("EventMaxPlayerLevel", 78);
		INSTANCE_FILE = settings.getString("EventInstanceFile", "EventEngine.xml");
		
		// ------------------------------------------------------------------------------------- //
		// AllVsAll.properties
		// ------------------------------------------------------------------------------------- //
		settings = new EventPropertiesParser(AVA_CONFIG);
		AVA_EVENT_ENABLED = settings.getBoolean("AvAEventEnabled", false);
		AVA_REWARD_PLAYER_WIN = settings.getItemHolderList("AvAEventReward");
		AVA_REWARD_KILL_PLAYER = settings.getItemHolderList("AvAEventRewardKill");
		AVA_BUFF_PLAYER_WARRIOR = settings.getSkillHolderList("AvAEventFighterBuffs");
		AVA_BUFF_PLAYER_MAGE = settings.getSkillHolderList("AvAEventMageBuffs");
		AVA_COORDINATES_PLAYER = settings.getLocation("AvAEventCoordinates");
		
		// ------------------------------------------------------------------------------------- //
		// OneVsOne.properties
		// ------------------------------------------------------------------------------------- //
		settings = new EventPropertiesParser(OVO_CONFIG);
		OVO_EVENT_ENABLED = settings.getBoolean("OVOEventEnabled", false);
		OVO_REWARD_TEAM_TIE = settings.getBoolean("OvORewardTeamTie", false);
		OVO_REWARD_PLAYER_WIN = settings.getItemHolderList("OvOEventReward");
		OVO_REWARD_KILL_PLAYER = settings.getItemHolderList("OvOEventRewardKill");
		OVO_REWARD_KILL_PLAYER = settings.getItemHolderList("OvOEventRewardTried");
		OVO_BUFF_PLAYER_WARRIOR = settings.getSkillHolderList("OvOAvAEventFighterBuffs");
		OVO_BUFF_PLAYER_MAGE = settings.getSkillHolderList("OvOEventMageBuffs");
		OVO_COORDINATES_TEAM_1 = settings.getLocation("OvOEventTeam1Coordinates");
		OVO_COORDINATES_TEAM_2 = settings.getLocation("OvOEventTeam2Coordinates");
		
		// ------------------------------------------------------------------------------------- //
		// TeamVsTeam.properties
		// ------------------------------------------------------------------------------------- //
		settings = new EventPropertiesParser(TVT_CONFIG);
		TVT_EVENT_ENABLED = settings.getBoolean("TvTEventEnabled", false);
		TVT_REWARD_PLAYER_WIN = settings.getItemHolderList("TVTEventReward");
		TVT_REWARD_KILL_PLAYER = settings.getItemHolderList("TVTEventRewardKill");
		TVT_BUFF_PLAYER_WARRIOR = settings.getSkillHolderList("TVTAvAEventFighterBuffs");
		TVT_BUFF_PLAYER_MAGE = settings.getSkillHolderList("TVTEventMageBuffs");
		TVT_COORDINATES_TEAM_1 = settings.getLocation("TVTEventTeam1Coordinates");
		TVT_COORDINATES_TEAM_2 = settings.getLocation("TVTEventTeam2Coordinates");
		
		// ------------------------------------------------------------------------------------- //
		// Survive.properties
		// ------------------------------------------------------------------------------------- //
		settings = new EventPropertiesParser(SURVIVE_CONFIG);
		SURVIVE_EVENT_ENABLED = settings.getBoolean("SVEventEnabled", false);
		SURVIVE_REWARD_PLAYER_WIN = settings.getItemHolderList("SVEventReward");
		SURVIVE_BUFF_PLAYER_WARRIOR = settings.getSkillHolderList("SVEventFighterBuffs");
		SURVIVE_BUFF_PLAYER_MAGE = settings.getSkillHolderList("SVEventMageBuffs");
		SURVIVE_COORDINATES_PLAYER = settings.getLocation("SVEventCoordinatesPlayer");
		SURVIVE_COORDINATES_MOBS = settings.getLocation("SVEventCoordinatesMobs");
		SURVIVE_MONSTERS_ID = settings.getListInteger("SVEventMobsID");
		SURVIVE_MONSTER_SPAWN_FOR_STAGE = settings.getInt("SVEventMobsSpawnForStage", 5);
	}
}
