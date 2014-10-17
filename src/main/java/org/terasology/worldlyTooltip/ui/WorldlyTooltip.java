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
package org.terasology.worldlyTooltip.ui;

import org.terasology.input.cameraTarget.CameraTargetSystem;
import org.terasology.math.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.ControlWidget;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.layers.hud.CoreHudWidget;
import org.terasology.rendering.nui.widgets.UIBox;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

public class WorldlyTooltip extends CoreHudWidget implements ControlWidget {

    private CameraTargetSystem cameraTargetSystem;
    private WorldProvider worldProvider;
    private UIBox tooltipContainer;
    private UILabel blockName;
    private UILabel blockUri;

    @Override
    protected void initialise() {
        cameraTargetSystem = CoreRegistry.get(CameraTargetSystem.class);
        worldProvider = CoreRegistry.get(WorldProvider.class);

        tooltipContainer = find("tooltipContainer", UIBox.class);
        tooltipContainer.bindVisible(new ReadOnlyBinding<Boolean>() {
            @Override
            public Boolean get() {
                return cameraTargetSystem.isTargetAvailable();
            }
        });

        blockName = tooltipContainer.find("blockName", UILabel.class);
        blockName.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                if (cameraTargetSystem.isTargetAvailable()) {
                    Vector3i blockPosition = cameraTargetSystem.getTargetBlockPosition();
                    Block block = worldProvider.getBlock(blockPosition);
                    return block.getDisplayName();
                } else {
                    return "";
                }
            }
        });

        blockUri = tooltipContainer.find("blockUri", UILabel.class);
        blockUri.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                if (cameraTargetSystem.isTargetAvailable()) {
                    Vector3i blockPosition = cameraTargetSystem.getTargetBlockPosition();
                    Block block = worldProvider.getBlock(blockPosition);
                    return block.getURI().toString();
                } else {
                    return "";
                }
            }
        });
    }
}
