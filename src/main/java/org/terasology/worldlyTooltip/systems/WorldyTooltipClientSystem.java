/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.worldlyTooltip.systems;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.logic.health.HealthComponent;
import org.terasology.engine.logic.nameTags.NameTagComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.module.inventory.ui.GetItemTooltip;
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
