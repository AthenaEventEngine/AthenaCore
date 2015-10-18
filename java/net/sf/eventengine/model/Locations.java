package net.sf.eventengine.model;

import java.util.ArrayList;

import com.l2jserver.gameserver.model.Location;
import com.l2jserver.util.Rnd;

public class Locations
{
	private ArrayList<Location> _locs;
	
	/**
	 * Constructor
	 */
	public Locations()
	{
		_locs = new ArrayList<>();
	}
	
	/**
	 * Constructor
	 * @param locs
	 */
	public Locations(ArrayList<Location> locs)
	{
		_locs = locs;
	}
	
	/**
	 * Add a location
	 * @return
	 */
	public void addLoc(Location loc)
	{
		_locs.add(loc);
	}
	
	/**
	 * Get the locations
	 * @return
	 */
	public ArrayList<Location> getLocs()
	{
		return _locs;
	}
	
	/**
	 * Get a random location
	 * @return
	 */
	public Location getRandomSpawn()
	{
		return _locs.get(Rnd.get(_locs.size() - 1));
	}
}
