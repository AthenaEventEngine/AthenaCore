package com.github.athenaengine.core.handlers;

import com.github.athenaengine.core.EventEngineManager;
import com.github.athenaengine.core.enums.EventEngineState;
import com.github.athenaengine.core.interfaces.IEventContainer;
import com.github.athenaengine.core.managers.AutoSchedulerManager;
import com.l2jserver.gameserver.handler.IAdminCommandHandler;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;

public class AdminPanelHandler implements IAdminCommandHandler {

    private static final String[] ADMIN_COMMANDS = {
            "admin_athenaevents"
    };

    @Override
    public boolean useAdminCommand(String command, L2PcInstance activeChar) {
        if (command.startsWith("admin_athenaevents")) {
            showHtml(activeChar);
            return true;
        }

        return false;
    }

    private void showHtml(L2PcInstance player) {
        final NpcHtmlMessage html = new NpcHtmlMessage();
        html.setFile(player.getHtmlPrefix(), "data/html/events/event_engine.htm");
        player.sendPacket(html);
    }

    private void showMainMenu() {

    }

    private void startEvent(String eventName) {
        if (EventEngineManager.getInstance().getState() == EventEngineState.REGISTER) {
            IEventContainer nextEvent = EventEngineManager.getInstance().getNextEvent();

            if (nextEvent.getSimpleEventName().equalsIgnoreCase(eventName)) {
                // Start event
            }
        }
    }

    private void stopEvent(String eventName) {
        if (EventEngineManager.getInstance().getState() == EventEngineState.RUNNING_EVENT) {
            IEventContainer event = EventEngineManager.getInstance().getCurrentEventContainer();

            if (event.getSimpleEventName().equalsIgnoreCase(eventName)) {
                // Stop event
            }
        }
    }

    private void cancelEvent(String eventName) {
        if (EventEngineManager.getInstance().getState() == EventEngineState.RUNNING_EVENT) {
            IEventContainer event = EventEngineManager.getInstance().getCurrentEventContainer();

            if (event.getSimpleEventName().equalsIgnoreCase(eventName)) {
                // Stop event
            }
        }
    }

    private void startScheduler() {
        AutoSchedulerManager.getInstance().start();
    }

    private void stopScheduler() {
        AutoSchedulerManager.getInstance().stop();
    }

    @Override
    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
