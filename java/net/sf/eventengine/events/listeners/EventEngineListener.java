package net.sf.eventengine.events.listeners;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.interfaces.IEventListener;

/**
 * @author Zephyr
 */
public class EventEngineListener implements IEventListener
{
	private final L2PcInstance _player;
	
	public EventEngineListener(L2PcInstance player)
	{
		_player = player;
	}
	
	@Override
	public boolean isOnEvent()
	{
		return true;
	}
	
	@Override
	public boolean isBlockingExit()
	{
		return true;
	}
	
	@Override
	public boolean isBlockingDeathPenalty()
	{
		return true;
	}
	
	@Override
	public boolean canRevive()
	{
		return false;
	}
	
	@Override
	public L2PcInstance getPlayer()
	{
		return _player;
	}
	
}
