package com.github.u3games.eventengine.interfaces;

import com.github.u3games.eventengine.dispatcher.events.*;

public interface IListenerSuscriber {

    void listenerOnLogin(OnLogInEvent event);

    void listenerOnLogout(OnLogOutEvent event);

    void listenerOnInteract(OnInteractEvent event);

    void listenerOnKill(OnKillEvent event);

    void listenerOnDeath(OnDeathEvent event);

    void listenerOnAttack(OnAttackEvent event);

    void listenerOnUseSkill(OnUseSkillEvent event);

    void listenerOnUseItem(OnUseItemEvent event);

}
