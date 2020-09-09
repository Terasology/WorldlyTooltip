// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.worldlyTooltip.systems;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.nameTags.NameTagComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.health.logic.HealthComponent;
import org.terasology.inventory.rendering.nui.layers.ingame.GetItemTooltip;
import org.terasology.nui.widgets.TooltipLine;
import org.terasology.worldlyTooltipAPI.events.GetTooltipNameEvent;

@RegisterSystem(RegisterMode.CLIENT)
public class WorldyTooltipClientSystem extends BaseComponentSystem {
    @In
    private NUIManager nuiManager;

    @Override
    public void preBegin() {
        nuiManager.getHUD().addHUDElement("WorldlyTooltip:WorldlyTooltip");
    }

    /*
     * Sets the Name at the top of the WorldlyTooltip to show the player's name
     */
    @ReceiveEvent
    public void getTooltipName(GetTooltipNameEvent event, EntityRef entity, NameTagComponent nameTagComponent) {
        event.setName(nameTagComponent.text);
    }

    /*
     * Adds Health inside the WorldlyTooltip to show health of any entity having a HealthComponent
     */
    @ReceiveEvent
    public void addHealthToTooltip(GetItemTooltip event, EntityRef entity, HealthComponent healthComponent) {
        event.getTooltipLines().add(new TooltipLine("Health: " + healthComponent.currentHealth + "/" + healthComponent.maxHealth));
    }

}
