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
package net.sf.eventengine.events.handler.managers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

import net.sf.eventengine.datatables.ConfigData;

/**
 * @author swarlog, fissban
 */
public class DualBoxManager
{
	// Map IP/TRACE Address Player
	public Map<String, Integer> _addressManager = new ConcurrentHashMap<>();
	
	/**
	 * Check IP/TRACE Address
	 * @param address
	 * @return
	 */
	public boolean checkMultiBox(L2PcInstance activeChar)
	{
		if (ConfigData.DUAL_BOX_PROTECTION_ENABLED)
		{
			// Check Limits Participants
			if (ConfigData.DUAL_BOX_CHECK_MAX_PARTICIPANTS_PER_PC == 0)
			{
				return false;
			}
			
			// Check Participants Counts
			String address = getAddress(activeChar);
			if (getAddressCount(address) >= ConfigData.DUAL_BOX_CHECK_MAX_PARTICIPANTS_PER_PC)
			{
				return true;
			}
			else
			{
				addAddress(address);
			}
		}
		
		return false;
	}
	
	/**
	 * Get IP/TRACE Address Count
	 * @param address
	 * @return
	 */
	private int getAddressCount(String address)
	{
		if (_addressManager.containsKey(address))
		{
			return _addressManager.get(address);
		}
		
		return 0;
	}
	
	/**
	 * Increasing repeated or add new IP/TRACE addresses
	 * @param address
	 */
	private void addAddress(String address)
	{
		if (!_addressManager.containsKey(address))
		{
			_addressManager.put(address, 1);
		}
		else
		{
			int boxCount = _addressManager.get(address);
			_addressManager.put(address, boxCount + 1);
		}
	}
	
	/**
	 * Decreases repeated or delete IP/TRACE addresses
	 * @param L2PcInstance activeChar
	 */
	public void removeAddress(L2PcInstance activeChar)
	{
		String address = getAddress(activeChar);
		if (_addressManager.containsKey(address))
		{
			int boxCount = _addressManager.get(address);
			if (boxCount > 1)
			{
				_addressManager.put(address, boxCount - 1);
			}
			else
			{
				_addressManager.remove(address);
			}
		}
	}
	
	/**
	 * Add IP/TRACE addresses of players
	 * @param activeChar
	 * @return
	 */
	public String getAddress(L2PcInstance activeChar)
	{
		StringBuilder ip = new StringBuilder();
		
		// Add IP Player
		ip.append(activeChar.getClient().getConnection().getInetAddress().getHostAddress());
		ip.append("-");
		
		// Add Trace Player
		int[][] trace = activeChar.getClient().getTrace();
		for (int i = 0; i < 5; i++)
		{
			ip.append(trace[i][0]);
			ip.append(".");
			ip.append(trace[i][1]);
			ip.append(".");
			ip.append(trace[i][2]);
			ip.append(".");
			ip.append(trace[i][3]);
			ip.append("|");
		}
		
		return ip.toString();
	}
	
	/**
	 * Clear all IP/TRACE register.
	 */
	public void clearAddressManager()
	{
		_addressManager.clear();
	}
	
	public static DualBoxManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final DualBoxManager _instance = new DualBoxManager();
	}
}
