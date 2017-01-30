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
package com.github.athenaengine.core.managers;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.github.athenaengine.core.events.listeners.EventEngineListener;
import com.github.athenaengine.core.model.entity.Character;
import com.github.athenaengine.core.model.entity.Playable;
import com.github.athenaengine.core.model.entity.Player;
import com.github.athenaengine.core.EventEngineManager;
import com.github.athenaengine.core.model.entity.Summon;

public class PlayersManager
{
	private final Set<Integer> _eventPlayers = ConcurrentHashMap.newKeySet();
	
	/**
	 * We obtain the full list of all players within an event.
	 * @return Collection<Player>
	 */
	public Collection<Player> getAllEventPlayers() {
		Collection<Player> players = new LinkedList<>();
		for (int playerId : _eventPlayers) {
			players.add(CacheManager.getInstance().getPlayer(playerId));
		}

		return players;
	}
	
	/**
	 * We add all the characters registered to our list of characters in the event.<br>
	 * Check if player in olympiad.<br>
	 * Check if player in duel.<br>
	 * Check if player in observer mode.
	 */
	public void createEventPlayers()
	{
		for (Player player : EventEngineManager.getInstance().getAllRegisteredPlayers())
		{
			// Check if player in olympiad
			if (player.getPcInstance().isInOlympiadMode())
			{
				player.sendMessage("You can not attend the event being in the Olympics.");
				continue;
			}
			// Check if player in duel
			if (player.getPcInstance().isInDuel())
			{
				player.sendMessage("You can not attend the event being in the Duel.");
				continue;
			}
			// Check if player in observer mode
			if (player.getPcInstance().inObserverMode())
			{
				player.sendMessage("You can not attend the event being in the Observer mode.");
				continue;
			}
			_eventPlayers.add(player.getObjectId());
			player.getPcInstance().addEventListener(new EventEngineListener(player.getPcInstance()));
		}
		// We clean the list, no longer we need it
		EventEngineManager.getInstance().clearRegisteredPlayers();
	}
	
	/**
	 * Check if the playable is participating in any event. In the case of a summon, verify that the owner participates. For not participate in an event is returned <u>false.</u>
	 * @param playable
	 * @return boolean
	 */
	public boolean isPlayableInEvent(Playable playable)
	{
		if (playable.isPlayer())
		{
			return _eventPlayers.contains(playable.getObjectId());
		}
		
		if (playable.isSummon())
		{
			return _eventPlayers.contains(((Summon) playable).getOwner().getObjectId());
		}
		return false;
	}
	
	/**
	 * Check if a player is participating in any event. In the case of dealing with a summon, verify the owner. For an event not participated returns <u>null.</u>
	 * @param character
	 * @return Player
	 */
	public Player getEventPlayer(Character character)
	{
		if (character instanceof Playable && isPlayableInEvent((Playable) character)) {
			int objectId = character instanceof Summon ? ((Summon) character).getOwner().getObjectId() : character.getObjectId();
			return CacheManager.getInstance().getPlayer(objectId);
		}

		return null;
	}

	public Player getEventPlayer(int playerId) {
		return _eventPlayers.contains(playerId) ? CacheManager.getInstance().getPlayer(playerId) : null;
	}
}