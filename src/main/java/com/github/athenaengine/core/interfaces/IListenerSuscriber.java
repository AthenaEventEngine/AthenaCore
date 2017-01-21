package com.github.athenaengine.core.interfaces;

import com.github.athenaengine.core.dispatcher.events.*;

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
