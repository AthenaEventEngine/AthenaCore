/*
 * Copyright (C) 2004-2015 L2J Server
 *
 * This file is part of L2J Server.
 *
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.sf.eventengine.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import net.sf.eventengine.model.Locations;

import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.holders.ItemHolder;

/**
 * Simplifies loading of property files and adds logging if a non existing property is requested.
 * @author NosBit
 */
public final class EventPropertiesParser
{
	private static final Logger LOGGER = Logger.getLogger(EventPropertiesParser.class.getName());
	
	private final Properties _properties = new Properties();
	private final File _file;
	
	public EventPropertiesParser(String name)
	{
		this(new File(name));
	}
	
	public EventPropertiesParser(File file)
	{
		_file = file;
		try (FileInputStream fileInputStream = new FileInputStream(file))
		{
			try (InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, Charset.defaultCharset()))
			{
				_properties.load(inputStreamReader);
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("[" + _file.getName() + "] There was an error loading config reason: " + e.getMessage());
		}
	}
	
	public boolean containskey(String key)
	{
		return _properties.containsKey(key);
	}
	
	private String getValue(String key)
	{
		String value = _properties.getProperty(key);
		return value != null ? value.trim() : null;
	}
	
	public boolean getBoolean(String key, boolean defaultValue)
	{
		String value = getValue(key);
		if (value == null)
		{
			LOGGER.warning("[" + _file.getName() + "] missing property for key: " + key + " using default value: " + defaultValue);
			return defaultValue;
		}
		
		if (value.equalsIgnoreCase("true"))
		{
			return true;
		}
		else if (value.equalsIgnoreCase("false"))
		{
			return false;
		}
		else
		{
			LOGGER.warning("[" + _file.getName() + "] Invalid value specified for key: " + key + " specified value: " + value + " should be \"boolean\" using default value: " + defaultValue);
			return defaultValue;
		}
	}
	
	public byte getByte(String key, byte defaultValue)
	{
		String value = getValue(key);
		if (value == null)
		{
			LOGGER.warning("[" + _file.getName() + "] missing property for key: " + key + " using default value: " + defaultValue);
			return defaultValue;
		}
		
		try
		{
			return Byte.parseByte(value);
		}
		catch (NumberFormatException e)
		{
			LOGGER.warning("[" + _file.getName() + "] Invalid value specified for key: " + key + " specified value: " + value + " should be \"byte\" using default value: " + defaultValue);
			return defaultValue;
		}
	}
	
	public short getShort(String key, short defaultValue)
	{
		String value = getValue(key);
		if (value == null)
		{
			LOGGER.warning("[" + _file.getName() + "] missing property for key: " + key + " using default value: " + defaultValue);
			return defaultValue;
		}
		
		try
		{
			return Short.parseShort(value);
		}
		catch (NumberFormatException e)
		{
			LOGGER.warning("[" + _file.getName() + "] Invalid value specified for key: " + key + " specified value: " + value + " should be \"short\" using default value: " + defaultValue);
			return defaultValue;
		}
	}
	
	public int getInt(String key, int defaultValue)
	{
		String value = getValue(key);
		if (value == null)
		{
			LOGGER.warning("[" + _file.getName() + "] missing property for key: " + key + " using default value: " + defaultValue);
			return defaultValue;
		}
		
		try
		{
			return Integer.parseInt(value);
		}
		catch (NumberFormatException e)
		{
			LOGGER.warning("[" + _file.getName() + "] Invalid value specified for key: " + key + " specified value: " + value + " should be \"int\" using default value: " + defaultValue);
			return defaultValue;
		}
	}
	
	public long getLong(String key, long defaultValue)
	{
		String value = getValue(key);
		if (value == null)
		{
			LOGGER.warning("[" + _file.getName() + "] missing property for key: " + key + " using default value: " + defaultValue);
			return defaultValue;
		}
		
		try
		{
			return Long.parseLong(value);
		}
		catch (NumberFormatException e)
		{
			LOGGER.warning("[" + _file.getName() + "] Invalid value specified for key: " + key + " specified value: " + value + " should be \"long\" using default value: " + defaultValue);
			return defaultValue;
		}
	}
	
	public float getFloat(String key, float defaultValue)
	{
		String value = getValue(key);
		if (value == null)
		{
			LOGGER.warning("[" + _file.getName() + "] missing property for key: " + key + " using default value: " + defaultValue);
			return defaultValue;
		}
		
		try
		{
			return Float.parseFloat(value);
		}
		catch (NumberFormatException e)
		{
			LOGGER.warning("[" + _file.getName() + "] Invalid value specified for key: " + key + " specified value: " + value + " should be \"float\" using default value: " + defaultValue);
			return defaultValue;
		}
	}
	
	public double getDouble(String key, double defaultValue)
	{
		String value = getValue(key);
		if (value == null)
		{
			LOGGER.warning("[" + _file.getName() + "] missing property for key: " + key + " using default value: " + defaultValue);
			return defaultValue;
		}
		
		try
		{
			return Double.parseDouble(value);
		}
		catch (NumberFormatException e)
		{
			LOGGER.warning("[" + _file.getName() + "] Invalid value specified for key: " + key + " specified value: " + value + " should be \"double\" using default value: " + defaultValue);
			return defaultValue;
		}
	}
	
	public String getString(String key, String defaultValue)
	{
		String value = getValue(key);
		if (value == null)
		{
			LOGGER.warning("[" + _file.getName() + "] missing property for key: " + key + " using default value: " + defaultValue);
			return defaultValue;
		}
		return value;
	}
	
	public <T extends Enum<T>> T getEnum(String key, Class<T> clazz, T defaultValue)
	{
		String value = getValue(key);
		if (value == null)
		{
			LOGGER.warning("[" + _file.getName() + "] missing property for key: " + key + " using default value: " + defaultValue);
			return defaultValue;
		}
		
		try
		{
			return Enum.valueOf(clazz, value);
		}
		catch (IllegalArgumentException e)
		{
			LOGGER.warning("[" + _file.getName() + "] Invalid value specified for key: " + key + " specified value: " + value + " should be enum value of \"" + clazz.getSimpleName() + "\" using default value: " + defaultValue);
			return defaultValue;
		}
	}
	
	/**
	 * Parseamos un config usando "," para diferenciar entre cada cordenada<br>
	 * Ejemplo -> "xx,xx,xx;xx,xx,xx ..."
	 * @param key
	 * @return List<ItemHolder>
	 */
	public List<ItemHolder> getItemHolderList(String key)
	{
		List<ItemHolder> itemList = new ArrayList<>();
		try
		{
			StringTokenizer st = new StringTokenizer(getValue(key), ";");
			while (st.hasMoreTokens())
			{
				StringTokenizer st1 = new StringTokenizer(st.nextToken(), ",");
				itemList.add(new ItemHolder(Integer.parseInt(st1.nextToken()), Integer.parseInt(st1.nextToken())));
			}
		}
		catch (Exception e)
		{
			LOGGER.warning(getClass().getSimpleName() + ": fail to read config -> " + key);
		}
		
		return itemList;
	}
	
	/**
	 * Parseamos un config usando "," para diferenciar entre cada coordenada<br>
	 * Ejemplo -> "xx,xx,xx"
	 * @param key
	 * @return Location
	 */
	public Location getLocation(String key)
	{
		try
		{
			StringTokenizer st = new StringTokenizer(getValue(key), ",");
			return new Location(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
		}
		catch (Exception e)
		{
			LOGGER.warning(getClass().getSimpleName() + ": fail to read config -> " + key);
		}
		
		return new Location(167616, 20315, -3332);
	}
	
	/**
	 * Transform a string with the following format 'XX,XX,XX;XX,XX,XX#XX,XX,XX' to list of Locations.<br>
	 * @param key
	 * @return List<Location>
	 */
	public ArrayList<Locations> getMultipleLocationList(String key)
	{
		try
		{
			ArrayList<Locations> locList = new ArrayList<>();
			
			StringTokenizer st = new StringTokenizer(getValue(key), "#");
			while (st.hasMoreTokens())
			{
				locList.add(getLocationList(st.nextToken().toString()));
			}
			
			return locList;
		}
		catch (Exception e)
		{
			LOGGER.warning(getClass().getSimpleName() + ": fail to read config -> " + key);
		}
		
		return new ArrayList<>();
	}
	
	/**
	 * Transform a string with the following format 'XX,XX,XX;XX,XX,XX' to Locations object.<br>
	 * @param key
	 * @return Locations
	 */
	public Locations getLocationList(String key)
	{
		Locations locList = new Locations();
		
		try
		{
			StringTokenizer st = new StringTokenizer(key, ";");
			while (st.hasMoreTokens())
			{
				StringTokenizer stLoc = new StringTokenizer(st.nextToken(), ",");
				locList.addLoc(new Location(Integer.parseInt(stLoc.nextToken()), Integer.parseInt(stLoc.nextToken()), Integer.parseInt(stLoc.nextToken())));
			}
			
			return locList;
		}
		catch (Exception e)
		{
			LOGGER.warning(getClass().getSimpleName() + ": fail to read config -> " + key);
		}
		
		return locList;
	}
	
	/**
	 * Parseamos un config usando "," para diferenciar entre un valor y otro.<br>
	 * Ejemplo -> xx,xx,xx,xx ....
	 * @param key
	 * @return List<Integer>
	 */
	public List<Integer> getListInteger(String key)
	{
		List<Integer> integerList = new ArrayList<>();
		try
		{
			StringTokenizer st = new StringTokenizer(getValue(key), ",");
			while (st.hasMoreTokens())
			{
				integerList.add(Integer.parseInt(st.nextToken()));
			}
		}
		catch (Exception e)
		{
			LOGGER.warning(getClass().getSimpleName() + ": fail to read config -> " + key);
		}
		
		return integerList;
	}
}
