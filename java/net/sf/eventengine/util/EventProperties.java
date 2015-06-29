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
package net.sf.eventengine.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Specialized {@link java.util.Properties} class.<br>
 * Simplifies loading of property files and adds logging if a non existing property is requested.<br>
 * @author Noctarius
 */
public final class EventProperties extends Properties
{
	private static final long serialVersionUID = 1L;
	
	private static final Logger LOG = Logger.getLogger(EventProperties.class.getName());
	
	public EventProperties()
	{
		
	}
	
	public EventProperties(String name) throws IOException
	{
		try (FileInputStream fis = new FileInputStream(name))
		{
			load(fis);
		}
	}
	
	public EventProperties(File file) throws IOException
	{
		try (FileInputStream fis = new FileInputStream(file))
		{
			load(fis);
		}
	}
	
	public EventProperties(InputStream inStream) throws IOException
	{
		load(inStream);
	}
	
	public EventProperties(Reader reader) throws IOException
	{
		load(reader);
	}
	
	public void load(String name) throws IOException
	{
		try (FileInputStream fis = new FileInputStream(name))
		{
			load(fis);
		}
	}
	
	public void load(File file) throws IOException
	{
		try (FileInputStream fis = new FileInputStream(file))
		{
			load(fis);
		}
	}
	
	@Override
	public void load(InputStream inStream) throws IOException
	{
		try (InputStreamReader isr = new InputStreamReader(inStream, Charset.defaultCharset()))
		{
			super.load(isr);
		}
		finally
		{
			inStream.close();
		}
	}
	
	@Override
	public void load(Reader reader) throws IOException
	{
		try
		{
			super.load(reader);
		}
		finally
		{
			reader.close();
		}
	}
	
	@Override
	public String getProperty(String key, String defaultValue)
	{
		String property = super.getProperty(key, defaultValue);
		
		if (property == null)
		{
			LOG.warning("L2Properties: Missing defaultValue for key - " + key);
			
			return defaultValue;
		}
		
		return property.trim();
	}
}
