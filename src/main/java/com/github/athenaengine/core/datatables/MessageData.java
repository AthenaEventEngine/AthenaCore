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
package com.github.athenaengine.core.datatables;

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import com.github.athenaengine.core.model.entity.Player;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author swarlog, fissban
 */
public final class MessageData
{
	private static final Logger LOGGER = Logger.getLogger(MessageData.class.getName());
	private static final String DIRECTORY = "config/EventEngine/language";
	private static final String DEFAULT_LANG = "en";
	// Map to identify the language of each character
	private final Map<Player, String> _playerCurrentLang = new HashMap<>();
	private final Map<String, String> _msgMap = new HashMap<>();
	private final Map<String, String> _languages = new HashMap<>();
	
	public MessageData()
	{
		load();
	}
	
	private void load()
	{
		try
		{
			File dir = new File(DIRECTORY);
			for (File file : dir.listFiles((FileFilter) pathname ->
			{
				if (pathname.getName().endsWith(".xml"))
				{
					return true;
				}
				return false;
			}))
			{
				if (file.getName().startsWith("lang_"))
				{
					loadXml(file, file.getName().substring(5, file.getName().indexOf(".xml")));
				}
			}
			LOGGER.info(MessageData.class.getSimpleName() + ": Loaded " + _languages.size() + " languages.");
		}
		catch (Exception e)
		{
			LOGGER.warning(MessageData.class.getSimpleName() + ": Error while loading language files: " + e);
			e.printStackTrace();
		}
	}
	
	private void loadXml(File file, String lang)
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
				LOGGER.warning(MessageData.class.getSimpleName() + ": Could not load language (" + lang + ") file for event engine: " + e);
				e.printStackTrace();
			}
			
			if (doc != null)
			{
				Node n = doc.getFirstChild();
				NamedNodeMap docAttr = n.getAttributes();
				if (docAttr.getNamedItem("lang") != null)
				{
					langName = docAttr.getNamedItem("lang").getNodeValue();
				}
				if (!_languages.containsKey(lang))
				{
					_languages.put(lang, langName);
				}
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if (d.getNodeName().equals("message"))
					{
						NamedNodeMap attrs = d.getAttributes();
						String id = attrs.getNamedItem("id").getNodeValue();
						String text = attrs.getNamedItem("text").getNodeValue();
						_msgMap.put(lang + "_" + id, text);
						count++;
					}
				}
			}
		}
		LOGGER.info("Loaded language file for language " + lang + " " + count + " messages.");
	}
	
	/**
	 * Returns the text based on player's language selected.
	 * @param player
	 * @param text
	 * @param addTag Used for screen or chat messages.
	 * @return String
	 */
	public String getMsgByLang(Player player, String text, boolean addTag)
	{
		// Get the language by which the user chooses
		String lang = getLanguage(player);
		String tag = "";
		// Generate our own TAG event
		if (addTag)
		{
			if (_msgMap.containsKey(lang + "_" + "event_engine_tag"))
			{
				// Seek the translation of the text in the selected language for the character
				tag = _msgMap.get(lang + "_" + "event_engine_tag") + " ";
			}
			else
			{
				tag = "[Event Engine] ";
			}
		}
		
		StringBuilder msg = new StringBuilder(50);
		StringTokenizer st = new StringTokenizer(text, " ");
		// Generate the translation of the different parts of the message
		while (st.hasMoreTokens())
		{
			// Text to be translated
			String textLang = st.nextToken();
			if (_msgMap.containsKey(lang + "_" + textLang))
			{
				// Seek the translation of the text in the selected language for the character
				msg.append(_msgMap.get(lang + "_" + textLang));
			}
			else if (_msgMap.containsKey(lang + "_" + textLang))
			{
				// Seek the translation of the text in the default language "en"
				msg.append(_msgMap.get("en_" + textLang));
			}
			else
			{
				// Add the text without translation
				msg.append(textLang);
			}
		}
		return tag + msg.toString();
	}
	
	/**
	 * Define the language a character wants.
	 * @param player
	 * @param lang
	 */
	public void setLanguage(Player player, String lang)
	{
		_playerCurrentLang.put(player, lang);
	}
	
	/**
	 * Get the language of a character, should not have defined, return "DEFAULT_LANG".
	 * @param player
	 * @return String
	 */
	public String getLanguage(Player player)
	{
		if (_playerCurrentLang.containsKey(player))
		{
			return _playerCurrentLang.get(player);
			
		}
		return DEFAULT_LANG;
	}
	
	/**
	 * Get a map with all languages were created.
	 * @return
	 */
	public Map<String, String> getLanguages()
	{
		return _languages;
	}
	
	public static MessageData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final MessageData _instance = new MessageData();
	}
}