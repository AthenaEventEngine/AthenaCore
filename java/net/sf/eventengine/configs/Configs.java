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
	/** Definimos cada cuanto se ejecutara algun evento en hs */
	public static int EVENT_TASK;
	/** Definimos el tiempo que durara cada evento en minutos */
	public static int EVENT_DURATION;
	/** Definimos si permitimos o no el daño entre amigos. */
	public static boolean FRIENDLY_FIRE;
	/** Definimos que eventos queremos que se ejecuten */
	public static boolean ENABLE_DM;
	public static boolean ENABLE_CTF;
	public static boolean ENABLE_TVT;
	public static boolean ENABLE_AVA;
	/** Definimos la cant de players maximo/minimo que podran participar */
	public static int MAX_PLAYERS_IN_EVENT;
	public static int MIN_PLAYERS_IN_EVENT;
	/** Definimos el lvl maximo/minimo que podran participar de los eventos. */
	public static int MAX_LVL_IN_EVENT;
	public static int MIN_LVL_IN_EVENT;
	/** Definimos el xml q usaremos para nuestras instancias. */
	public static String INSTANCE_FILE;
	
	// -------------------------------------------------------------------------------
	// Configs Team Vs Team
	// -------------------------------------------------------------------------------
	/** Definimos los rewards */
	public static List<ItemHolder> TVT_REWARD_TEAM_WIN = new ArrayList<>();
	public static List<ItemHolder> TVT_REWARD_TEAM_LOSER = new ArrayList<>();
	/** Definimos los buff de los players */
	public static List<SkillHolder> TVT_BUFF_PLAYER_MAGE = new ArrayList<>();
	public static List<SkillHolder> TVT_BUFF_PLAYER_WARRIOR = new ArrayList<>();
	/** Definimos a donde teletransportaremos a los players al iniciar el evento */
	public static Location TVT_LOC_TEAM_BLUE;
	public static Location TVT_LOC_TEAM_RED;
	// -------------------------------------------------------------------------------
	// Configs All Vs All
	// -------------------------------------------------------------------------------
	/** Definimos los rewards */
	public static List<ItemHolder> AVA_REWARD_PLAYER_WIN = new ArrayList<>();
	public static List<ItemHolder> AVA_REWARD_PLAYER_LOSER = new ArrayList<>();
	/** Definimos los buff de los players */
	public static List<SkillHolder> AVA_BUFF_PLAYER_MAGE = new ArrayList<>();
	public static List<SkillHolder> AVA_BUFF_PLAYER_WARRIOR = new ArrayList<>();
	/** Definimos a donde teletransportaremos a los players al iniciar el evento */
	public static Location AVA_LOC_PLAYER;
	// -------------------------------------------------------------------------------
	// Configs One Vs One
	// -------------------------------------------------------------------------------
	/** Definimos los rewards */
	public static List<ItemHolder> OVO_REWARD_PLAYER_WIN = new ArrayList<>();
	public static List<ItemHolder> OVO_REWARD_PLAYER_LOSER = new ArrayList<>();
	/** Definimos los buff de los players */
	public static List<SkillHolder> OVO_BUFF_PLAYER_MAGE = new ArrayList<>();
	public static List<SkillHolder> OVO_BUFF_PLAYER_WARRIOR = new ArrayList<>();
	/** Definimos a donde teletransportaremos a los players al iniciar el evento */
	public static Location OVO_LOC_TEAM_BLUE;
	public static Location OVO_LOC_TEAM_RED;
	// -------------------------------------------------------------------------------
	// Configs Survive
	// -------------------------------------------------------------------------------
	/** Definimos los rewards */
	public static List<ItemHolder> SURVIVE_REWARD_PLAYER_WIN = new ArrayList<>();
	public static List<ItemHolder> SURVIVE_REWARD_PLAYER_LOSER = new ArrayList<>();
	/** Definimos los buff de los players */
	public static List<SkillHolder> SURVIVE_BUFF_PLAYER_MAGE = new ArrayList<>();
	public static List<SkillHolder> SURVIVE_BUFF_PLAYER_WARRIOR = new ArrayList<>();
	/** Definimos a donde teletransportaremos a los players al iniciar el evento */
	public static Location SURVIVE_LOC_PLAYER;
	/** Definimos los monsters q podran spawnear dentro del evento */
	public static List<Integer> SURVIVE_MOSNTERS_SPAWN = new ArrayList<>();
	/** Definimos la cantidad de monsters que spawnaran por stage */
	public static int SURVIVE_MONSTER_SPAWN_FOR_STAGE;
	
	// Metodo encargado de leer los configs
	public static void load()
	{
		EventPropertiesParser settings;
		// ------------------------------------------------------------------------------------- //
		// EventEngine.properties
		// ------------------------------------------------------------------------------------- //
		settings = new EventPropertiesParser(EVENT_CONFIG);
		NPC_MANAGER_ID = settings.getInt("NpcManagerId", 36600);
		EVENT_TASK = settings.getInt("EventTask", 60);
		EVENT_DURATION = settings.getInt("EventDuration", 20);
		FRIENDLY_FIRE = settings.getBoolean("FriendlyFire", false);
		ENABLE_DM = settings.getBoolean("EnableDM", true);
		ENABLE_CTF = settings.getBoolean("EnableCTF", true);
		ENABLE_TVT = settings.getBoolean("EnableTVT", true);
		ENABLE_AVA = settings.getBoolean("EnableAVA", true);
		MAX_PLAYERS_IN_EVENT = settings.getInt("MaxPlayersInEvent", 20);
		MIN_PLAYERS_IN_EVENT = settings.getInt("MinPlayersInEvent", 2);
		MAX_LVL_IN_EVENT = settings.getInt("MaxLvlInEvent", 78);
		MIN_LVL_IN_EVENT = settings.getInt("MinLvlInEvent", 40);
		INSTANCE_FILE = settings.getString("InstanceFile", "EventEngine.xml");
		
		// ------------------------------------------------------------------------------------- //
		// TeamVsTeam.properties
		// ------------------------------------------------------------------------------------- //
		settings = new EventPropertiesParser(TVT_CONFIG);
		TVT_REWARD_TEAM_WIN = settings.getItemHolderList("RewardTeamWin");
		TVT_REWARD_TEAM_LOSER = settings.getItemHolderList("RewardTeamLoser");
		TVT_BUFF_PLAYER_MAGE = settings.getSkillHolderList("BuffMage");
		TVT_BUFF_PLAYER_WARRIOR = settings.getSkillHolderList("BuffWarrior");
		TVT_LOC_TEAM_BLUE = settings.getLocation("LocTeamBlue");
		TVT_LOC_TEAM_RED = settings.getLocation("LocTeamRed");
		
		// ------------------------------------------------------------------------------------- //
		// AllVsAll.properties
		// ------------------------------------------------------------------------------------- //
		settings = new EventPropertiesParser(AVA_CONFIG);
		AVA_REWARD_PLAYER_WIN = settings.getItemHolderList("RewardPlayerWin");
		AVA_REWARD_PLAYER_LOSER = settings.getItemHolderList("RewardPlayerLoser");
		AVA_BUFF_PLAYER_MAGE = settings.getSkillHolderList("BuffMage");
		AVA_BUFF_PLAYER_WARRIOR = settings.getSkillHolderList("BuffWarrior");
		AVA_LOC_PLAYER = settings.getLocation("LocPlayer");
		
		// ------------------------------------------------------------------------------------- //
		// OneVsOne.properties
		// ------------------------------------------------------------------------------------- //
		settings = new EventPropertiesParser(OVO_CONFIG);
		OVO_REWARD_PLAYER_WIN = settings.getItemHolderList("RewardPlayerWin");
		OVO_REWARD_PLAYER_LOSER = settings.getItemHolderList("RewardPlayerLoser");
		OVO_BUFF_PLAYER_MAGE = settings.getSkillHolderList("BuffMage");
		OVO_BUFF_PLAYER_WARRIOR = settings.getSkillHolderList("BuffWarrior");
		OVO_LOC_TEAM_BLUE = settings.getLocation("LocTeamBlue");
		OVO_LOC_TEAM_RED = settings.getLocation("LocTeamRed");
		
		// ------------------------------------------------------------------------------------- //
		// Survive.properties
		// ------------------------------------------------------------------------------------- //
		settings = new EventPropertiesParser(SURVIVE_CONFIG);
		SURVIVE_REWARD_PLAYER_WIN = settings.getItemHolderList("RewardPlayerWin");
		SURVIVE_REWARD_PLAYER_LOSER = settings.getItemHolderList("RewardPlayerLoser");
		SURVIVE_BUFF_PLAYER_MAGE = settings.getSkillHolderList("BuffMage");
		SURVIVE_BUFF_PLAYER_WARRIOR = settings.getSkillHolderList("BuffWarrior");
		SURVIVE_LOC_PLAYER = settings.getLocation("LocPlayer");
		SURVIVE_MOSNTERS_SPAWN = settings.getListInteger("MonsterSpawn");
		SURVIVE_MONSTER_SPAWN_FOR_STAGE = settings.getInt("MonsterSpawnForStager", 5);
	}
}
