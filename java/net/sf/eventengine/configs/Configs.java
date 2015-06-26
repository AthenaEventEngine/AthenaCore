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
	
	public static final String EVENT_CONFIG = "./config/EventEngine/EventEngine.properties";
	public static final String TVT_CONFIG = "./config/EventEngine/TeamVsTeam.properties";
	public static final String AVA_CONFIG = "./config/EventEngine/AllVsAll.properties";
	public static final String DM_CONFIG = "./config/EventEngine/DM.properties";
	public static final String CTF_CONFIG = "./config/EventEngine/CTF.properties";
	public static final String OVO_CONFIG = "./config/EventEngine/CTF.properties";
	
	// lista de configs generales
	
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
	
	// ------------------------------------------------------------------------------
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
	// ------------------------------------------------------------------------------
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
	// ------------------------------------------------------------------------------
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

	// public static Location LOC_TEAM_NONE = new Location(0, 0, 0);// a partir de este valor se spawnea a todos los usuarios en un radio de 200
	
	// lista de configs de cada evento.
	
	// metodo encargado de leer los configs
	public static void load()
	{
		// ------------------------------------------------------------------------------------- //
		// XXX EventEngine.properties
		// ------------------------------------------------------------------------------------- //
		final EventProperties settingsEventEngine = new EventProperties();
		try (InputStream is = new FileInputStream(new File(EVENT_CONFIG)))
		{
			settingsEventEngine.load(is);
		}
		catch (final Exception e)
		{
			LOG.warning("Failed to Load " + EVENT_CONFIG + " File.");
		}
		
		EVENT_TASK = Integer.parseInt(settingsEventEngine.getProperty("EventTask", "60"));
		EVENT_DURATION = Integer.parseInt(settingsEventEngine.getProperty("EventDuration", "20"));
		FRIENDLY_FIRE = Boolean.parseBoolean(settingsEventEngine.getProperty("FriendlyFire", "False"));;
		ENABLE_DM = Boolean.parseBoolean(settingsEventEngine.getProperty("EnableDM", "True"));;
		ENABLE_CTF = Boolean.parseBoolean(settingsEventEngine.getProperty("EnableCTF", "True"));
		ENABLE_TVT = Boolean.parseBoolean(settingsEventEngine.getProperty("EnableTVT", "True"));
		ENABLE_AVA = Boolean.parseBoolean(settingsEventEngine.getProperty("EnableAVA", "True"));
		MAX_PLAYERS_IN_EVENT = Integer.parseInt(settingsEventEngine.getProperty("MaxPlayersInEvent", "20"));;
		MIN_PLAYERS_IN_EVENT = Integer.parseInt(settingsEventEngine.getProperty("MinPlayersInEvent", "2"));;
		MAX_LVL_IN_EVENT = Integer.parseInt(settingsEventEngine.getProperty("MaxLvlInEvent", "78"));;
		MIN_LVL_IN_EVENT = Integer.parseInt(settingsEventEngine.getProperty("MinLvlInEvent", "40"));;
		
		StringTokenizer st;
		// ------------------------------------------------------------------------------------- //
		// XXX TeamVsTeam.properties
		// ------------------------------------------------------------------------------------- //
		final EventProperties settingsTvT = new EventProperties();
		try (InputStream is = new FileInputStream(new File(TVT_CONFIG)))
		{
			settingsTvT.load(is);
		}
		catch (final Exception e)
		{
			LOG.warning("Failed to Load " + TVT_CONFIG + " File.");
		}
		
		st = new StringTokenizer(settingsTvT.getProperty("RewardTeamWin", "57,10000"), ";");
		while (st.hasMoreTokens())
		{
			StringTokenizer st1 = new StringTokenizer(st.nextToken(), ",");
			TVT_REWARD_TEAM_WIN.add(new ItemHolder(Integer.parseInt(st1.nextToken()), Integer.parseInt(st1.nextToken())));
		}
		st = new StringTokenizer(settingsTvT.getProperty("RewardTeamLose", "57,10000"), ";");
		while (st.hasMoreTokens())
		{
			StringTokenizer st1 = new StringTokenizer(st.nextToken(), ",");
			TVT_REWARD_TEAM_LOSE.add(new ItemHolder(Integer.parseInt(st1.nextToken()), Integer.parseInt(st1.nextToken())));
		}
		st = new StringTokenizer(settingsTvT.getProperty("BuffMage", "1085,3"), ";");
		while (st.hasMoreTokens())
		{
			StringTokenizer st1 = new StringTokenizer(st.nextToken(), ",");
			TVT_BUFF_PLAYER_MAGE.add(new SkillHolder(Integer.parseInt(st1.nextToken()), Integer.parseInt(st1.nextToken())));
		}
		st = new StringTokenizer(settingsTvT.getProperty("BuffWarrior", "1086,3"), ";");
		while (st.hasMoreTokens())
		{
			StringTokenizer st1 = new StringTokenizer(st.nextToken(), ",");
			TVT_BUFF_PLAYER_WARRIOR.add(new SkillHolder(Integer.parseInt(st1.nextToken()), Integer.parseInt(st1.nextToken())));
		}
		
		st = new StringTokenizer(settingsTvT.getProperty("LocTeamBlue", "0,0,0"), ",");
		TVT_LOC_TEAM_BLUE = new Location(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
		st = new StringTokenizer(settingsTvT.getProperty("LocTeamRed", "0,0,0"), ",");
		TVT_LOC_TEAM_RED = new Location(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
		
		// ------------------------------------------------------------------------------------- //
		// XXX AllVsAll.properties
		// ------------------------------------------------------------------------------------- //
		final EventProperties settingsAvA = new EventProperties();
		try (InputStream is = new FileInputStream(new File(AVA_CONFIG)))
		{
			settingsAvA.load(is);
		}
		catch (final Exception e)
		{
			LOG.warning("Failed to Load " + AVA_CONFIG + " File.");
		}
		
		st = new StringTokenizer(settingsAvA.getProperty("RewardPlayerWin", "57,10000"), ";");
		while (st.hasMoreTokens())
		{
			StringTokenizer st1 = new StringTokenizer(st.nextToken(), ",");
			AVA_REWARD_PLAYER_WIN.add(new ItemHolder(Integer.parseInt(st1.nextToken()), Integer.parseInt(st1.nextToken())));
		}
		st = new StringTokenizer(settingsAvA.getProperty("RewardPlayerLose", "57,10000"), ";");
		while (st.hasMoreTokens())
		{
			StringTokenizer st1 = new StringTokenizer(st.nextToken(), ",");
			AVA_REWARD_PLAYER_LOSE.add(new ItemHolder(Integer.parseInt(st1.nextToken()), Integer.parseInt(st1.nextToken())));
		}
		st = new StringTokenizer(settingsAvA.getProperty("BuffMage", "1085,3"), ";");
		while (st.hasMoreTokens())
		{
			StringTokenizer st1 = new StringTokenizer(st.nextToken(), ",");
			AVA_BUFF_PLAYER_MAGE.add(new SkillHolder(Integer.parseInt(st1.nextToken()), Integer.parseInt(st1.nextToken())));
		}
		st = new StringTokenizer(settingsAvA.getProperty("BuffWarrior", "1086,3"), ";");
		while (st.hasMoreTokens())
		{
			StringTokenizer st1 = new StringTokenizer(st.nextToken(), ",");
			AVA_BUFF_PLAYER_WARRIOR.add(new SkillHolder(Integer.parseInt(st1.nextToken()), Integer.parseInt(st1.nextToken())));
		}
		
		st = new StringTokenizer(settingsAvA.getProperty("LocPlayer", "0,0,0"), ",");
		AVA_LOC_PLAYER = new Location(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
		
		// ------------------------------------------------------------------------------------- //
		// XXX CaptureTheFlag.properties
		// ------------------------------------------------------------------------------------- //

	}
}
