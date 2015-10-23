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
package net.sf.eventengine.events.handler.managers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Playable;
import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

import net.sf.eventengine.EventEngineManager;
import net.sf.eventengine.events.holders.PlayerHolder;

/**
 * @author fissban
 */
public class PlayersManager
{
	private final Map<Integer, PlayerHolder> _eventPlayers = new HashMap<>();
	
	public PlayersManager()
	{
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * We obtain the full list of all players within an event.<br>
	 * @return Collection<PlayerHolder>
	 */
	public Collection<PlayerHolder> getAllEventPlayers()
	{
		return _eventPlayers.values();
	}
	
	/**
	 * We add all the characters registered to our list of characters in the event.<br>
	 * Check if player in olympiad.<br>
	 * Check if player in duel<br>
	 * Check if player in observer mode<br>
	 */
	public void createEventPlayers()
	{
		for (L2PcInstance player : EventEngineManager.getInstance().getAllRegisteredPlayers())
		{
			// Check if player in olympiad.
			if (player.isInOlympiadMode())
			{
				player.sendMessage("You can not attend the event being in the Olympics.");
				continue;
			}
			// Check if player in duel
			if (player.isInDuel())
			{
				player.sendMessage("You can not attend the event being in the Duel.");
				continue;
			}
			// Check if player in observer mode
			if (player.inObserverMode())
			{
				player.sendMessage("You can not attend the event being in the Observer mode.");
				continue;
			}
			
			_eventPlayers.put(player.getObjectId(), new PlayerHolder(player));
		}
		
		// We clean the list, no longer we need it.
		EventEngineManager.getInstance().clearRegisteredPlayers();
	}
	
	/**
	 * Check if the playable is participating in any event. In the case of a summon, verify that the owner participates <br>
	 * For not participate in an event is returned <u> false </u>
	 * @param player
	 * @return boolean
	 */
	public boolean isPlayableInEvent(L2Playable playable)
	{
		if (playable.isPlayer())
		{
			return _eventPlayers.containsKey(playable.getObjectId());
		}
		
		if (playable.isSummon())
		{
			return _eventPlayers.containsKey(((L2Summon) playable).getOwner().getObjectId());
		}
		
		return false;
	}
	
	/**
	 * Check if a player is participating in any event. <br>
	 * In the case of dealing with a summon you verify the owner. <br>
	 * For an event not perticipar returns <u> null </u>
	 * @param character
	 * @return PlayerHolder
	 */
	public PlayerHolder getEventPlayer(L2Character character)
	{
		if (character.isSummon())
		{
			return _eventPlayers.get(((L2Summon) character).getOwner().getObjectId());
		}
		
		if (character.isPlayer())
		{
			return _eventPlayers.get(character.getObjectId());
		}
		
		return null;
	}
}
