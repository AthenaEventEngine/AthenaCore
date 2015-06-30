/*
 * Copyright (C) 2015-2015 L2J EventEngine
 *
 * This file is part of L2J EventEngine.
 *
 * L2jAdmins is free software: you can redistribute it and/or modify
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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import net.sf.eventengine.util.EventProperties;

import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.gameserver.model.holders.SkillHolder;

/**
 * @author fissban
 */
public class Configs
{
	private static final Logger LOG = Logger.getLogger(Configs.class.getName());
	
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
	public static List<ItemHolder> TVT_REWARD_TEAM_LOSE = new ArrayList<>();
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
	public static List<ItemHolder> AVA_REWARD_PLAYER_LOSE = new ArrayList<>();
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
	public static List<ItemHolder> OVO_REWARD_PLAYER_LOSE = new ArrayList<>();
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
	public static List<ItemHolder> SURVIVE_REWARD_PLAYER_LOSE = new ArrayList<>();
	/** Definimos los buff de los players */
	public static List<SkillHolder> SURVIVE_BUFF_PLAYER_MAGE = new ArrayList<>();
	public static List<SkillHolder> SURVIVE_BUFF_PLAYER_WARRIOR = new ArrayList<>();
	/** Definimos a donde teletransportaremos a los players al iniciar el evento */
	public static Location SURVIVE_LOC_PLAYER;
	/** Definimos los monsters q podran spawnear dentro del evento */
	public static List<Integer> SURVIVE_MOSNTERS_SPAWN = new ArrayList<>();
	/** Definimos la cantidad de monsters que spawnaran por stage */
	public static int SURVIVE_MONSTER_SPAWN_FOR_STAGE;
	
	// lista de configs de cada evento.
	
	// metodo encargado de leer los configs
	public static void load()
	{
		// ------------------------------------------------------------------------------------- //
		// XXX EventEngine.properties
		// ------------------------------------------------------------------------------------- //
		EventProperties settings = new EventProperties();

		try (InputStream is = new FileInputStream(new File(EVENT_CONFIG)))
		{
			settings.load(is);
		}
		catch (final Exception e)
		{
			LOG.warning("Failed to Load " + EVENT_CONFIG + " File.");
		}
		
		NPC_MANAGER_ID = Integer.parseInt(settings.getProperty("NpcManagerId", "36600"));
		EVENT_TASK = Integer.parseInt(settings.getProperty("EventTask", "60"));
		EVENT_DURATION = Integer.parseInt(settings.getProperty("EventDuration", "20"));
		FRIENDLY_FIRE = Boolean.parseBoolean(settings.getProperty("FriendlyFire", "False"));;
		ENABLE_DM = Boolean.parseBoolean(settings.getProperty("EnableDM", "True"));;
		ENABLE_CTF = Boolean.parseBoolean(settings.getProperty("EnableCTF", "True"));
		ENABLE_TVT = Boolean.parseBoolean(settings.getProperty("EnableTVT", "True"));
		ENABLE_AVA = Boolean.parseBoolean(settings.getProperty("EnableAVA", "True"));
		MAX_PLAYERS_IN_EVENT = Integer.parseInt(settings.getProperty("MaxPlayersInEvent", "20"));;
		MIN_PLAYERS_IN_EVENT = Integer.parseInt(settings.getProperty("MinPlayersInEvent", "2"));;
		MAX_LVL_IN_EVENT = Integer.parseInt(settings.getProperty("MaxLvlInEvent", "78"));;
		MIN_LVL_IN_EVENT = Integer.parseInt(settings.getProperty("MinLvlInEvent", "40"));;
		INSTANCE_FILE = settings.getProperty("InstanceFile", "EventEngine.xml");
		
		StringTokenizer st;
		// ------------------------------------------------------------------------------------- //
		// XXX TeamVsTeam.properties
		// ------------------------------------------------------------------------------------- //
		settings = new EventProperties();
		try (InputStream is = new FileInputStream(new File(TVT_CONFIG)))
		{
			settings.load(is);
		}
		catch (final Exception e)
		{
			LOG.warning("Failed to Load " + TVT_CONFIG + " File.");
		}
		
		st = new StringTokenizer(settings.getProperty("RewardTeamWin", "57,10000"), ";");
		while (st.hasMoreTokens())
		{
			StringTokenizer st1 = new StringTokenizer(st.nextToken(), ",");
			TVT_REWARD_TEAM_WIN.add(new ItemHolder(Integer.parseInt(st1.nextToken()), Integer.parseInt(st1.nextToken())));
		}
		st = new StringTokenizer(settings.getProperty("RewardTeamLose", "57,10000"), ";");
		while (st.hasMoreTokens())
		{
			StringTokenizer st1 = new StringTokenizer(st.nextToken(), ",");
			TVT_REWARD_TEAM_LOSE.add(new ItemHolder(Integer.parseInt(st1.nextToken()), Integer.parseInt(st1.nextToken())));
		}
		st = new StringTokenizer(settings.getProperty("BuffMage", "1085,3"), ";");
		while (st.hasMoreTokens())
		{
			StringTokenizer st1 = new StringTokenizer(st.nextToken(), ",");
			TVT_BUFF_PLAYER_MAGE.add(new SkillHolder(Integer.parseInt(st1.nextToken()), Integer.parseInt(st1.nextToken())));
		}
		st = new StringTokenizer(settings.getProperty("BuffWarrior", "1086,3"), ";");
		while (st.hasMoreTokens())
		{
			StringTokenizer st1 = new StringTokenizer(st.nextToken(), ",");
			TVT_BUFF_PLAYER_WARRIOR.add(new SkillHolder(Integer.parseInt(st1.nextToken()), Integer.parseInt(st1.nextToken())));
		}
		
		st = new StringTokenizer(settings.getProperty("LocTeamBlue", "0,0,0"), ",");
		TVT_LOC_TEAM_BLUE = new Location(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
		st = new StringTokenizer(settings.getProperty("LocTeamRed", "0,0,0"), ",");
		TVT_LOC_TEAM_RED = new Location(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
		
		// ------------------------------------------------------------------------------------- //
		// XXX AllVsAll.properties
		// ------------------------------------------------------------------------------------- //
		settings = new EventProperties();
		try (InputStream is = new FileInputStream(new File(AVA_CONFIG)))
		{
			settings.load(is);
		}
		catch (final Exception e)
		{
			LOG.warning("Failed to Load " + AVA_CONFIG + " File.");
		}
		
		st = new StringTokenizer(settings.getProperty("RewardPlayerWin", "57,10000"), ";");
		while (st.hasMoreTokens())
		{
			StringTokenizer st1 = new StringTokenizer(st.nextToken(), ",");
			AVA_REWARD_PLAYER_WIN.add(new ItemHolder(Integer.parseInt(st1.nextToken()), Integer.parseInt(st1.nextToken())));
		}
		st = new StringTokenizer(settings.getProperty("RewardPlayerLose", "57,10000"), ";");
		while (st.hasMoreTokens())
		{
			StringTokenizer st1 = new StringTokenizer(st.nextToken(), ",");
			AVA_REWARD_PLAYER_LOSE.add(new ItemHolder(Integer.parseInt(st1.nextToken()), Integer.parseInt(st1.nextToken())));
		}
		st = new StringTokenizer(settings.getProperty("BuffMage", "1085,3"), ";");
		while (st.hasMoreTokens())
		{
			StringTokenizer st1 = new StringTokenizer(st.nextToken(), ",");
			AVA_BUFF_PLAYER_MAGE.add(new SkillHolder(Integer.parseInt(st1.nextToken()), Integer.parseInt(st1.nextToken())));
		}
		st = new StringTokenizer(settings.getProperty("BuffWarrior", "1086,3"), ";");
		while (st.hasMoreTokens())
		{
			StringTokenizer st1 = new StringTokenizer(st.nextToken(), ",");
			AVA_BUFF_PLAYER_WARRIOR.add(new SkillHolder(Integer.parseInt(st1.nextToken()), Integer.parseInt(st1.nextToken())));
		}
		
		st = new StringTokenizer(settings.getProperty("LocPlayer", "0,0,0"), ",");
		AVA_LOC_PLAYER = new Location(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
		
		// ------------------------------------------------------------------------------------- //
		// XXX OneVsOne.properties
		// ------------------------------------------------------------------------------------- //
		
		settings = new EventProperties();
		try (InputStream is = new FileInputStream(new File(OVO_CONFIG)))
		{
			settings.load(is);
		}
		catch (final Exception e)
		{
			LOG.warning("Failed to Load " + OVO_CONFIG + " File.");
		}
		
		st = new StringTokenizer(settings.getProperty("RewardPlayerWin", "57,10000"), ";");
		while (st.hasMoreTokens())
		{
			StringTokenizer st1 = new StringTokenizer(st.nextToken(), ",");
			OVO_REWARD_PLAYER_WIN.add(new ItemHolder(Integer.parseInt(st1.nextToken()), Integer.parseInt(st1.nextToken())));
		}
		st = new StringTokenizer(settings.getProperty("RewardPlayerLose", "57,10000"), ";");
		while (st.hasMoreTokens())
		{
			StringTokenizer st1 = new StringTokenizer(st.nextToken(), ",");
			OVO_REWARD_PLAYER_LOSE.add(new ItemHolder(Integer.parseInt(st1.nextToken()), Integer.parseInt(st1.nextToken())));
		}
		st = new StringTokenizer(settings.getProperty("BuffMage", "1085,3"), ";");
		while (st.hasMoreTokens())
		{
			StringTokenizer st1 = new StringTokenizer(st.nextToken(), ",");
			OVO_BUFF_PLAYER_MAGE.add(new SkillHolder(Integer.parseInt(st1.nextToken()), Integer.parseInt(st1.nextToken())));
		}
		st = new StringTokenizer(settings.getProperty("BuffWarrior", "1086,3"), ";");
		while (st.hasMoreTokens())
		{
			StringTokenizer st1 = new StringTokenizer(st.nextToken(), ",");
			OVO_BUFF_PLAYER_WARRIOR.add(new SkillHolder(Integer.parseInt(st1.nextToken()), Integer.parseInt(st1.nextToken())));
		}
		
		st = new StringTokenizer(settings.getProperty("LocTeamBlue", "0,0,0"), ",");
		OVO_LOC_TEAM_BLUE = new Location(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
		st = new StringTokenizer(settings.getProperty("LocTeamRed", "0,0,0"), ",");
		OVO_LOC_TEAM_RED = new Location(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
		
		// ------------------------------------------------------------------------------------- //
		// XXX Survive.properties
		// ------------------------------------------------------------------------------------- //

		settings = new EventProperties();
		try (InputStream is = new FileInputStream(new File(SURVIVE_CONFIG)))
		{
			settings.load(is);
		}
		catch (final Exception e)
		{
			LOG.warning("Failed to Load " + SURVIVE_CONFIG + " File.");
		}
		
		st = new StringTokenizer(settings.getProperty("RewardPlayerWin", "57,10000"), ";");
		while (st.hasMoreTokens())
		{
			StringTokenizer st1 = new StringTokenizer(st.nextToken(), ",");
			SURVIVE_REWARD_PLAYER_WIN.add(new ItemHolder(Integer.parseInt(st1.nextToken()), Integer.parseInt(st1.nextToken())));
		}
		st = new StringTokenizer(settings.getProperty("RewardPlayerLose", "57,10000"), ";");
		while (st.hasMoreTokens())
		{
			StringTokenizer st1 = new StringTokenizer(st.nextToken(), ",");
			SURVIVE_REWARD_PLAYER_LOSE.add(new ItemHolder(Integer.parseInt(st1.nextToken()), Integer.parseInt(st1.nextToken())));
		}
		st = new StringTokenizer(settings.getProperty("BuffMage", "1085,3"), ";");
		while (st.hasMoreTokens())
		{
			StringTokenizer st1 = new StringTokenizer(st.nextToken(), ",");
			SURVIVE_BUFF_PLAYER_MAGE.add(new SkillHolder(Integer.parseInt(st1.nextToken()), Integer.parseInt(st1.nextToken())));
		}
		st = new StringTokenizer(settings.getProperty("BuffWarrior", "1086,3"), ";");
		while (st.hasMoreTokens())
		{
			StringTokenizer st1 = new StringTokenizer(st.nextToken(), ",");
			SURVIVE_BUFF_PLAYER_WARRIOR.add(new SkillHolder(Integer.parseInt(st1.nextToken()), Integer.parseInt(st1.nextToken())));
		}
		
		st = new StringTokenizer(settings.getProperty("LocPlayer", "0,0,0"), ",");
		SURVIVE_LOC_PLAYER = new Location(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));

		st = new StringTokenizer(settings.getProperty("MonsterSpawn", "22839,22840,22843"), ",");
		while (st.hasMoreTokens())
		{
			SURVIVE_MOSNTERS_SPAWN.add(Integer.parseInt(st.nextToken()));
		}
		
		SURVIVE_MONSTER_SPAWN_FOR_STAGE = Integer.parseInt(settings.getProperty("MonsterSpawnForStager", "5"));
	}
}
