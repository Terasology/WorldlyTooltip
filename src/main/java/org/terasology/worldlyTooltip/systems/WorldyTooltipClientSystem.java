/*
 * Copyright 2014 MovingBlocks
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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.health.HealthComponent;
import org.terasology.registry.In;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.layers.ingame.inventory.GetItemTooltip;
import org.terasology.rendering.nui.widgets.TooltipLine;

@RegisterSystem(RegisterMode.CLIENT)
public class WorldyTooltipClientSystem extends BaseComponentSystem {
    @In
    private NUIManager nuiManager;

    @Override
    public void preBegin() {
        nuiManager.getHUD().addHUDElement("WorldlyTooltip:WorldlyTooltip");
    }

    @ReceiveEvent
    public void getDurabilityItemTooltip(GetItemTooltip event, EntityRef entity, HealthComponent healthComponent) {
        event.getTooltipLines().add(new TooltipLine("Health: " + healthComponent.currentHealth + "/" + healthComponent.maxHealth));
    }

    @ReceiveEvent
    public void getDisplayNameEntityTooltip(GetItemTooltip event, EntityRef entity, DisplayNameComponent displayNameComponent) {
        event.getTooltipLines().add(new TooltipLine("Name: " + displayNameComponent.name));
    }

}
