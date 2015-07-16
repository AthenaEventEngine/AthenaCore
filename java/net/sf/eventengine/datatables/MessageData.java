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

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author swarlog, fissban
 */
public final class MessageData
{
	private static final Logger LOG = Logger.getLogger(MessageData.class.getName());
	
	// Mapa para identificar el lenguaje de cada personaje
	private static Map<L2PcInstance, String> PLAYER_CURRENT_LANG = new HashMap<>();
	
	private static final String DIRECTORY = "config/EventEngine/Language";
	private static Map<String, String> MSG_MAP = new HashMap<>();
	private static Map<String, String> LANGUAGES = new HashMap<>();
	private static final String DEFAULT_LANG = "en";
	
	public static void load()
	{
		try
		{
			File dir = new File(DIRECTORY);
			
			for (File file : dir.listFiles(new FileFilter()
			{
				@Override
				public boolean accept(File pathname)
				{
					if (pathname.getName().endsWith(".xml"))
					{
						return true;
					}
					return false;
				}
			}))
			{
				if (file.getName().startsWith("lang_"))
				{
					loadXml(file, file.getName().substring(5, file.getName().indexOf(".xml")));
				}
			}
			
			LOG.info(MessageData.class.getSimpleName() + ": Loaded " + LANGUAGES.size() + " languages.");
			
		}
		catch (Exception e)
		{
			LOG.warning(MessageData.class.getSimpleName() + ": -> Error while loading language files: " + e);
			e.printStackTrace();
		}
	}
	
	private static void loadXml(File file, String lang)
	{
		int count = 0;
		String langName = "";
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		Document doc = null;
		if (file.exists())
		{
			try
			{
				doc = factory.newDocumentBuilder().parse(file);
			}
			catch (Exception e)
			{
				LOG.warning(MessageData.class.getSimpleName() + ": -> Could not load language (" + lang + ") file for event engine: " + e);
				e.printStackTrace();
			}
			
			Node n = doc.getFirstChild();
			NamedNodeMap docAttr = n.getAttributes();
			if (docAttr.getNamedItem("lang") != null)
			{
				langName = docAttr.getNamedItem("lang").getNodeValue();
			}
			
			if (!LANGUAGES.containsKey(lang))
			{
				LANGUAGES.put(lang, langName);
			}
			
			for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
			{
				if (d.getNodeName().equals("message"))
				{
					NamedNodeMap attrs = d.getAttributes();
					String id = attrs.getNamedItem("id").getNodeValue();
					String text = attrs.getNamedItem("text").getNodeValue();
					
					MSG_MAP.put(lang + "_" + id, text);
					count++;
				}
			}
		}
		
		LOG.info("Loaded language file for language " + lang + " " + count + " messages.");
	}
	
	public static String getTag(L2PcInstance player)
	{
		return getMsgByLang(player, "event_engine_tag") + " ";
	}
	
	/**
	 * Obtenemos un texto segun el lang q este usando el personaje.<br>
	 * @param player
	 * @param text
	 * @return String
	 */
	public static String getMsgByLang(L2PcInstance player, String text)
	{
		// lenguaje definido por el usuario o el default
		String lang = getLanguage(player);
		
		StringBuilder msg = new StringBuilder(50);
		
		StringTokenizer st = new StringTokenizer(text, " ");
		// generamos la traduccion de las diferentes partes del mensaje
		while (st.hasMoreTokens())
		{
			// texto a ser traducido
			String textLang = st.nextToken();
			
			if (MSG_MAP.containsKey(lang + "_" + textLang))
			{
				// buscamos la traduccion del texto en el lenguaje seleccionado por el personaje
				msg.append(MSG_MAP.get(lang + "_" + textLang));
			}
			else if (MSG_MAP.containsKey(lang + "_" + textLang))
			{
				// buscamos la traduccion del texto en el lenguaje default -> "en"
				msg.append(MSG_MAP.get("en_" + textLang));
			}
			else
			{
				// agregamos el texto sin traduccion
				msg.append(textLang);
			}
		}
		
		return msg.toString();
	}
	
	/**
	 * Definimos el idioma que quiere un personaje
	 * @param player
	 * @param lang
	 */
	public static void setLanguage(L2PcInstance player, String lang)
	{
		PLAYER_CURRENT_LANG.put(player, lang);
	}
	
	/**
	 * Obtenemos el idioma de un personaje, en caso de no haberlo definido devolvemos "DEFAULT_LANG".
	 * @param player
	 * @return String
	 */
	public static String getLanguage(L2PcInstance player)
	{
		if (PLAYER_CURRENT_LANG.containsKey(player))
		{
			return PLAYER_CURRENT_LANG.get(player);
			
		}
		return DEFAULT_LANG;
	}
	
	/**
	 * Obtenemos un mapa con todos los lenguajes que fueron cargados.
	 * @return
	 */
	public static Map<String, String> getLanguages()
	{
		return LANGUAGES;
	}
}
