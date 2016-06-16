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
package com.github.u3games.eventengine.events.holders;

import com.l2jserver.gameserver.model.actor.L2Npc;

/**
 * Clase encargada de administrar toda la informacion de los Npc de los eventos.
 * @author fissban
 */
public class NpcHolder
{
	private L2Npc _npc;
	private String _customTitle;
	
	public NpcHolder(L2Npc npc)
	{
		_npc = npc;
	}
	
	/**
	 * Acceso directo a todos los metodos de L2Npc.
	 * @return
	 */
	public L2Npc getNpcInstance()
	{
		return _npc;
	}
	
	public String getTitle()
	{
		if (_customTitle != null)
		{
			return _customTitle;
		}
		return _npc.getTitle();
	}
	
	public void setTitle(String customTitle)
	{
		_customTitle = customTitle;
	}
}