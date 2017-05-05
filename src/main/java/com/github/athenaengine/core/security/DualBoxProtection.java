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
package com.github.athenaengine.core.security;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.github.athenaengine.core.config.BaseConfigLoader;
import com.github.athenaengine.core.model.config.DualboxEventConfig;
import com.github.athenaengine.core.model.entity.Player;
import com.l2jserver.gameserver.network.L2GameClient;

/**
 * DualBox Protection based on WAN IP Address and tracert obtained directly from the client.
 * @author Sacrifice
 */
public final class DualBoxProtection
{
	private final Map<Integer, IpPack> _playerConnections = new HashMap<>();
	private final Map<IpPack, Integer> _address = new HashMap<>();
	
	private DualBoxProtection() {}

	private static DualboxEventConfig getConfig() {
		return BaseConfigLoader.getInstance().getMainConfig().getDualbox();
	}
	
	public synchronized boolean registerConnection(Player player)
	{
		L2GameClient client = player.getPcInstance().getClient();

		if (getConfig().isEnabled())
		{
			try
			{
				IpPack pack = new IpPack(client.getConnection().getInetAddress().getHostAddress(), client.getTrace());
				Integer count = _address.get(pack) == null ? 0 : _address.get(pack);

				if (count < getConfig().getMaxAllowed())
				{
					_playerConnections.put(player.getObjectId(), pack);
					_address.put(pack, count + 1);
					return true;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return false;
		}
		
		return true;
	}
	
	public synchronized void removeConnection(Player player)
	{
		if (getConfig().isEnabled())
		{
			try
			{
				IpPack pack = _playerConnections.get(player.getObjectId());
				Integer count = _address.get(pack) != null ? _address.get(pack) : 0;
				if (count > 0)
				{
					_address.put(pack, count - 1);
				}
				else
				{
					_playerConnections.remove(player.getObjectId());
					_address.remove(pack);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void clearAllConnections()
	{
		_playerConnections.clear();
		_address.clear();
	}
	
	public static DualBoxProtection getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		private static final DualBoxProtection _instance = new DualBoxProtection();
	}
	
	private final static class IpPack
	{
		private String ip;
		private int[][] tracert;
		
		private IpPack(String ip, int[][] tracert)
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