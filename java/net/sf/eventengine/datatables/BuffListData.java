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
package net.sf.eventengine.datatables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.holders.SkillHolder;
import com.l2jserver.util.data.xml.IXmlReader;

/**
 * Esta clase se encarga de:<br>
 * Cargar la lista de buffs que se podran usar en los eventos.<br>
 * Administrar la lista de buffs que fueron seleccionados por los personakes
 * @author fissban
 */
public class BuffListData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(BuffListData.class.getName());
	
	// BuffList
	private final List<SkillHolder> _buffList = new ArrayList<>();
	// Lista de buffs de los personajes
	private final Map<Integer, Set<SkillHolder>> _buffsPlayers = new ConcurrentHashMap<>();
	
	public BuffListData()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseDatapackFile("config/EventEngine/xml/buff_list.xml");
		LOGGER.info("Loaded Buffs: " + _buffList.size());
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		for (Node n = doc.getFirstChild().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("buff".equalsIgnoreCase(n.getNodeName()))
			{
				int buffId = Integer.parseInt(n.getAttributes().item(0).getNodeValue());
				int buffLvl = Integer.parseInt(n.getAttributes().item(1).getNodeValue());
				
				_buffList.add(new SkillHolder(buffId, buffLvl));
			}
		}
	}
	
	/**
	 * List all possible buffs for events
	 * @return List<SkillHolder>
	 */
	public List<SkillHolder> getAllBuffs()
	{
		return _buffList;
	}
	
	/**
	 * Obtenemos el listado de buffs seleccionado por un personaje
	 * @param player
	 * @return List<SkillHolder>
	 */
	public Set<SkillHolder> getBuffsPlayer(L2PcInstance player)
	{
		if (_buffsPlayers.containsKey(player.getObjectId()))
		{
			return _buffsPlayers.get(player.getObjectId());
		}
		
		return Collections.emptySet();
	}
	
	/**
	 * Verificamos si un personaje tiene un determinado skill
	 * @param player
	 * @return List<SkillHolder>
	 */
	public boolean getBuffPlayer(L2PcInstance player, SkillHolder sh)
	{
		if (_buffsPlayers.containsKey(player.getObjectId()))
		{
			for (SkillHolder aux : _buffsPlayers.get(player.getObjectId()))
			{
				if (aux.getSkill() == sh.getSkill())
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Eliminamos un buff del listado de un player
	 * @param player
	 * @param sh
	 */
	public void deleteBuffPlayer(L2PcInstance player, SkillHolder sh)
	{
		for (SkillHolder aux : _buffsPlayers.get(player.getObjectId()))
		{
			if (aux.getSkill() == sh.getSkill())
			{
				_buffsPlayers.get(player.getObjectId()).remove(aux);
				break;
			}
		}
	}
	
	/**
	 * Agregamos un buff al listado de un player
	 * @param player
	 * @param sh
	 */
	public void addBuffPlayer(L2PcInstance player, SkillHolder sh)
	{
		if (_buffsPlayers.containsKey(player.getObjectId()))
		{
			_buffsPlayers.get(player.getObjectId()).add(sh);
		}
		else
		{
			Set<SkillHolder> aux = new HashSet<>(1);
			aux.add(sh);
			
			_buffsPlayers.put(player.getObjectId(), aux);
		}
	}
	
	/**
	 * Limpiamos la lista de buff de un personaje sin eliminar al personaje del listado.
	 * @param player
	 */
	public void clearBuffsPlayer(L2PcInstance player)
	{
		if (_buffsPlayers.containsKey(player.getObjectId()))
		{
			_buffsPlayers.get(player.getObjectId()).clear();
		}
	}
	
	public static BuffListData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final BuffListData _instance = new BuffListData();
	}
}
