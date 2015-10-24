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
package net.sf.eventengine.events.holders;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

import net.sf.eventengine.enums.TeamType;
import net.sf.eventengine.interfaces.ParticipantHolder;

/**
 * Clase encargada de administrar los datos de los players que participan del evento.
 * @author fissban
 */
public class PlayerHolder implements ParticipantHolder
{
	private final L2PcInstance _player;
	
	// Contador de kills realizados.
	private int _kills = 0;
	// Contador de deaths obtenidas.
	private int _deaths = 0;
	// Color original de un personaje por si en algun evento es cambiado.
	private int _oriColorTitle;
	// Titulo original de un personaje por si en algun evento es cambiado.
	private String _oriTitle;
	// Team al que pertenece el usuario
	private TeamType _team;
	
	private int _dinamicInstanceId = 0;
	
	/**
	 * Constructor
	 * @param player
	 */
	public PlayerHolder(L2PcInstance player)
	{
		_player = player;
	}
	
	// METODOS VARIOS -----------------------------------------------------------
	
	/**
	 * Acceso directo a todos los metodos de L2PcInstance.
	 * @return
	 */
	public L2PcInstance getPcInstance()
	{
		return _player;
	}
	
	/**
	 * <ul>
	 * <b>Acciones:</b>
	 * </ul>
	 * <li>Definimos el team al que pertenece el personaje.</li>
	 * <li>Ajustamos el color del personaje segun su team</li>
	 * @param team
	 */
	public void setTeam(TeamType team)
	{
		// Almacenamos el color del titulo y el titulo original del personaje.
		_oriColorTitle = _player.getAppearance().getTitleColor();
		_oriTitle = _player.getTitle();
		// Team del personaje
		_team = team;
		// Nuevo color del titulo del personaje segun su team
		_player.getAppearance().setTitleColor(team.getColor());
	}
	
	/**
	 * Obtenemos el team del personaje.
	 * @return
	 */
	public TeamType getTeamType()
	{
		return _team;
	}
	
	/**
	 * Obtenemos el id de la instancia en la que participa dentro de los eventos.
	 * @return
	 */
	public int getDinamicInstanceId()
	{
		return _dinamicInstanceId;
	}
	
	/**
	 * Definimos el id de la instancia de la cual participa dentro de los eventos.
	 * @param dinamicInstanceId
	 */
	public void setDinamicInstanceId(int dinamicInstanceId)
	{
		_dinamicInstanceId = dinamicInstanceId;
	}
	
	/**
	 * Incrementamos en uno la cantidad de asesiantos.
	 */
	public void increaseKills()
	{
		_kills++;
	}
	
	/**
	 * Obtenemos lacantidad de asesinatos.
	 * @return
	 */
	public int getKills()
	{
		return _kills;
	}
	
	/**
	 * Incrementamos en uno la cantidad de muertes.
	 */
	public void increaseDeaths()
	{
		_deaths++;
	}
	
	/**
	 * Cantidad de muertes que tiene.
	 * @return
	 */
	public int getDeaths()
	{
		return _deaths;
	}
	
	/**
	 * Obtenemos la cant de puntos de un player.<br>
	 * La formula se obtiene a partir de: (_kills-_deaths)<br>
	 * @return
	 */
	public int getPoints()
	{
		return _kills - _deaths;
	}
	
	/**
	 * Definimos un nuevo titulo para elpersonaje
	 * @param title
	 */
	public void setNewTitle(String title)
	{
		_player.setTitle(title);
	}
	
	/**
	 * Recuperamos el titulo original de un player
	 */
	public void recoverOriginalTitle()
	{
		_player.setTitle(_oriTitle);
	}
	
	/**
	 * Recuperamos el color original del titulo del player
	 */
	public void recoverOriginalColorTitle()
	{
		_player.getAppearance().setTitleColor(_oriColorTitle);
	}
}
