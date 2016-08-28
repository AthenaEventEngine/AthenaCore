package com.github.u3games.eventengine.dispatcher;

import com.github.u3games.eventengine.EventEngineManager;
import com.github.u3games.eventengine.dispatcher.events.*;
import com.github.u3games.eventengine.enums.ListenerType;
import com.github.u3games.eventengine.dispatcher.events.ListenerEvent;
import com.github.u3games.eventengine.interfaces.IListenerSuscriber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListenerDispatcher {

    private static ListenerDispatcher sInstance;

    private final Map<ListenerType, List<IListenerSuscriber>> mSuscribers = new HashMap<>();

    private ListenerDispatcher() {
        for (ListenerType type : ListenerType.values()) {
            mSuscribers.put(type, new ArrayList<>());
        }
    }

    public synchronized void addSuscription(ListenerType type, IListenerSuscriber suscriber) {
        mSuscribers.get(type).add(suscriber);
    }

    public synchronized void removeSuscription(ListenerType type, IListenerSuscriber suscriber) {
        mSuscribers.get(type).remove(suscriber);
    }

    public synchronized void removeSuscriber(IListenerSuscriber suscriber) {
        for (List<IListenerSuscriber> suscribers : mSuscribers.values()) {
            suscribers.remove(suscriber);
        }
    }

    public boolean notifyEvent(ListenerEvent event) {
        publishManager(event);

        for (IListenerSuscriber suscriber : mSuscribers.get(event.getType())) {
            if (event.isCanceled()) return true;
            publishEvents(suscriber, event);
        }

        return false;
    }

    private void publishEvents(IListenerSuscriber suscriber, ListenerEvent event) {
        switch (event.getType()) {
            case ON_LOG_IN:
                suscriber.listenerOnLogin((OnLogInEvent) event);
                break;
            case ON_LOG_OUT:
                suscriber.listenerOnLogout((OnLogOutEvent) event);
                break;
            case ON_INTERACT:
                suscriber.listenerOnInteract((OnInteractEvent) event);
                break;
            case ON_KILL:
                suscriber.listenerOnKill((OnKillEvent) event);
                break;
            case ON_DEATH:
                suscriber.listenerOnDeath((OnDeathEvent) event);
                break;
            case ON_ATTACK:
                suscriber.listenerOnAttack((OnAttackEvent) event);
                break;
            case ON_USE_SKILL:
                suscriber.listenerOnUseSkill((OnUseSkillEvent) event);
                break;
            case ON_USE_ITEM:
                suscriber.listenerOnUseItem((OnUseItemEvent) event);
                break;
        }
    }

    private void publishManager(ListenerEvent event) {
        switch (event.getType()) {
            case ON_LOG_IN:
                EventEngineManager.getInstance().listenerOnLogin((OnLogInEvent) event);
                break;
            case ON_LOG_OUT:
                EventEngineManager.getInstance().listenerOnLogout((OnLogOutEvent) event);
                break;
        }
    }

    public static ListenerDispatcher getInstance() {
        if (sInstance == null) sInstance = new ListenerDispatcher();
        return sInstance;
    }
}
