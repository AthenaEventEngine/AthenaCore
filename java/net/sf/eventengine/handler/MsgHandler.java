/*
 * L2J EventEngine is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * L2J EventEngine is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.eventengine.handler;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author swarlog
 */

public final class MsgHandler
{
	private static final Logger LOG = Logger.getLogger(MsgHandler.class.getName());
	private static final String DIRECTORY = "config/EventEngine/Language";
	private static Map<String, String> MSG_MAP = new HashMap<>();
	private static Map<String, String> LANGUAGES = new HashMap<>();
	private static final String DEFAULT_LANG = "en";
	private static String CURRENT_LANG;
	private static double VERSION = 1.0;
	
	public static void init()
	{
		try
		{
			prepare();
			load();
		}
		catch (Exception e)
		{
			LOG.warning(MsgHandler.class.getSimpleName() + ": -> Error while loading language files " + e);
			e.printStackTrace();
		}
	}
	
	public static void prepare() throws IOException
	{
		File folder = new File(DIRECTORY);
		if ((!folder.exists()) || (folder.isDirectory()))
		{
			folder.mkdir();
		}
	}
	
	public static void load() throws IOException
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
		
		LOG.info("Loaded " + LANGUAGES.size() + " languages.");
	}
	
	private static void loadXml(File file, String lang)
	{
		int count = 0;
		String version = "";
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
				LOG.warning(MsgHandler.class.getSimpleName() + ": -> Could not load language (" + lang + ") file for event engine. " + e);
				e.printStackTrace();
			}
			
			Node n = doc.getFirstChild();
			NamedNodeMap docAttr = n.getAttributes();
			if (docAttr.getNamedItem("version") != null)
			{
				version = docAttr.getNamedItem("version").getNodeValue();
			}
			
			if (docAttr.getNamedItem("lang") != null)
			{
				langName = docAttr.getNamedItem("lang").getNodeValue();
			}
			
			if (version != null)
			{
				LOG.info("Processing language file for language - " + lang + "; Version: " + version);
			}
			
			if (!version.equals(VERSION))
			{
				LOG.info("Language file for language " + lang + " is not up-to-date with latest version of the engine (" + VERSION + "). Some newly added messages might not be translated.");
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
	
	public static String getMsg(String id)
	{
		return getMsgByLang(getLanguage(), id);
	}
	
	public static String getMsg(String id, Object... obs)
	{
		String msg = getMsg(id);
		return fillMsg(msg, obs);
	}
	
	public static String getMsgByLang(String lang, String id)
	{
		String msg = MSG_MAP.get(lang + "_" + id);
		if (msg == null)
		{
			LOG.info("No Msg found: ID " + id + " lang = " + lang);
			msg = MSG_MAP.get("en_" + id);
		}
		
		return msg;
	}
	
	public static String fillMsg(String msg, Object... obs)
	{
		String newMsg = msg;
		for (Object o : obs)
		{
			if (((o instanceof Integer)) || ((o instanceof Long)))
			{
				int first = newMsg.indexOf("%i");
				if (first != -1)
				{
					if ((o instanceof Integer))
					{
						newMsg = newMsg.replaceFirst("%i", ((Integer) o).toString());
					}
					else
					{
						newMsg = newMsg.replaceFirst("%i", ((Long) o).toString());
					}
				}
			}
			else if ((o instanceof Double))
			{
				int first = newMsg.indexOf("%d");
				if (first != -1)
				{
					newMsg = newMsg.replaceFirst("%d", ((Double) o).toString());
				}
			}
			else if ((o instanceof String))
			{
				int first = newMsg.indexOf("%s");
				if (first != -1)
				{
					newMsg = newMsg.replaceFirst("%s", (String) o);
				}
			}
		}
		
		return newMsg;
	}
	
	public static void setLanguage(String lang)
	{
		CURRENT_LANG = lang;
	}
	
	public static String getLanguage()
	{
		if (CURRENT_LANG == null)
		{
			return DEFAULT_LANG;
		}
		
		return CURRENT_LANG;
	}
	
	public static Map<String, String> getLanguages()
	{
		return LANGUAGES;
	}
}
