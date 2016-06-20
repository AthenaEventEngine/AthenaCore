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
package com.github.u3games.eventengine.security;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.github.u3games.eventengine.datatables.ConfigData;
import com.l2jserver.gameserver.network.L2GameClient;

/**
 * DualBox Protection based on WAN IP Address and tracert obtained directly from the client.
 * @author Sacrifice
 */
public final class DualBoxProtection
{
	private Map<IpPack, Integer> _address = new HashMap<DualBoxProtection.IpPack, Integer>();
	
	public DualBoxProtection()
	{
		
	}
	
	public synchronized boolean registerConnection(L2GameClient client)
	{
		if (ConfigData.DUALBOX_PROTECTION_ENABLED)
		{
			IpPack pack = new IpPack(client.getConnection().getInetAddress().getHostAddress(), client.getTrace());
			Integer count = _address.get(pack);
			if (count == null)
			{
				_address.put(pack, 1);
			}
			if (count <= ConfigData.DUALBOX_MAX_ALLOWED)
			{
				_address.put(pack, count + 1);
			}
			if (count >= ConfigData.DUALBOX_MAX_ALLOWED)
			{
				_address.put(pack, count =+ 0);
				return false;
			}
		}
		return true;
	}
	
	public synchronized void removeConnection(L2GameClient client)
	{
		if (ConfigData.DUALBOX_PROTECTION_ENABLED)
		{
			IpPack pack = new IpPack(client.getConnection().getInetAddress().getHostAddress(), client.getTrace());
			Integer count = _address.get(pack);
			if ((count != null) && (count >= 1))
			{
				_address.put(pack, count - 1);
				return;
			}
			else
			{
				_address.remove(pack);
			}
		}
	}
	
	public int getConnectionCount(String address)
	{
		if (_address.containsKey(address))
		{
			return _address.get(address);
		}
		return 0;
	}
	
	public void clearAllConnections()
	{
		_address.clear();
	}
	
	public static DualBoxProtection getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final DualBoxProtection _instance = new DualBoxProtection();
	}
	
	public final static class IpPack
	{
		String ip;
		int[][] tracert;
		public IpPack(String ip, int[][] tracert)
		{
			this.ip = ip;
			this.tracert = tracert;
		}
		
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = (prime * result) + ((ip == null) ? 0 : ip.hashCode());
			if (tracert == null)
			{
				return result;
			}
			
			for (int[] array : tracert)
			{
				result = (prime * result) + Arrays.hashCode(array);
			}
			return result;
		}
		
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj == null)
			{
				return false;
			}
			if (getClass() != obj.getClass())
			{
				return false;
			}
			
			IpPack other = (IpPack) obj;
			if (ip == null)
			{
				if (other.ip != null)
				{
					return false;
				}
			}
			else if (!ip.equals(other.ip))
			{
				return false;
			}
			
			for (int i = 0; i < tracert.length; i++)
			{
				for (int o = 0; o < tracert[0].length; o++)
				{
					if (tracert[i][o] != other.tracert[i][o])
					{
						return false;
					}
				}
			}
			return true;
		}
	}
}