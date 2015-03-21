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

import com.google.common.collect.Lists;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.input.Keyboard;
import org.terasology.input.cameraTarget.CameraTargetSystem;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.nui.ControlWidget;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.layers.hud.CoreHudWidget;
import org.terasology.rendering.nui.layers.ingame.inventory.GetItemTooltip;
import org.terasology.rendering.nui.layers.ingame.inventory.ItemIcon;
import org.terasology.rendering.nui.skin.UISkin;
import org.terasology.rendering.nui.widgets.TooltipLine;
import org.terasology.rendering.nui.widgets.TooltipLineRenderer;
import org.terasology.rendering.nui.widgets.UIBox;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UIList;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

import java.util.List;

public class WorldlyTooltip extends CoreHudWidget implements ControlWidget {

    private CameraTargetSystem cameraTargetSystem;
    private WorldProvider worldProvider;
    private BlockEntityRegistry blockEntityRegistry;
    private UIBox tooltipContainer;
    private UILabel blockName;
    private UILabel blockUri;
    private UIList<TooltipLine> tooltip;
    private ItemIcon icon;

    @Override
    protected void initialise() {
        cameraTargetSystem = CoreRegistry.get(CameraTargetSystem.class);
        worldProvider = CoreRegistry.get(WorldProvider.class);
        blockEntityRegistry = CoreRegistry.get(BlockEntityRegistry.class);

        this.bindVisible(new ReadOnlyBinding<Boolean>() {
            @Override
            public Boolean get() {
                return cameraTargetSystem.isTargetAvailable();
            }
        });

        blockName = find("blockName", UILabel.class);
        blockName.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                if (cameraTargetSystem.isTargetAvailable()) {
                    Vector3i blockPosition = cameraTargetSystem.getTargetBlockPosition();
                    Block block = worldProvider.getBlock(blockPosition);
                    if (Keyboard.isKeyDown(Keyboard.KeyId.LEFT_ALT) || Keyboard.isKeyDown(Keyboard.KeyId.LEFT_ALT)) {
                        return block.getURI().toString();
                    } else {
                        return block.getDisplayName();
                    }
                } else {
                    return "";
                }
            }
        });

        tooltip = find("tooltip", UIList.class);
        if (tooltip != null) {
            UISkin defaultSkin = Assets.getSkin("Engine:itemTooltip");
            tooltip.setItemRenderer(new TooltipLineRenderer(defaultSkin));
            tooltip.setSkin(defaultSkin);
            tooltip.bindList(
                    new ReadOnlyBinding<List<TooltipLine>>() {
                        @Override
                        public List<TooltipLine> get() {
                            if (cameraTargetSystem.isTargetAvailable()) {
                                EntityRef targetEntity = blockEntityRegistry.getEntityAt(cameraTargetSystem.getTargetBlockPosition());

                                GetItemTooltip itemTooltip = new GetItemTooltip();
                                targetEntity.send(itemTooltip);
                                return itemTooltip.getTooltipLines();
                            }
                            return Lists.newArrayList();
                        }
                    });
        }

        icon = find("icon", ItemIcon.class);
        if (icon != null) {
            icon.bindMesh(new ReadOnlyBinding<Mesh>() {
                @Override
                public Mesh get() {
                    if (cameraTargetSystem.isTargetAvailable()) {
                        Vector3i blockPosition = cameraTargetSystem.getTargetBlockPosition();
                        Block block = worldProvider.getBlock(blockPosition);
                        if (block.getBlockFamily() != null) {
                            return block.getBlockFamily().getArchetypeBlock().getMesh();
                        }
                    }
                    return null;

                }
            });
            icon.setMeshTexture(Assets.getTexture("engine:terrain"));
        }
    }
}
