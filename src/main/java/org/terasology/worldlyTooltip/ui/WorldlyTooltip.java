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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.management.AssetManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.input.InputSystem;
import org.terasology.input.Keyboard;
import org.terasology.input.cameraTarget.CameraTargetSystem;
import org.terasology.input.device.KeyboardDevice;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.ControlWidget;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.layers.hud.CoreHudWidget;
import org.terasology.rendering.nui.layers.ingame.inventory.GetItemTooltip;
import org.terasology.rendering.nui.layers.ingame.inventory.GetTooltipIconEvent;
import org.terasology.rendering.nui.layers.ingame.inventory.ItemIcon;
import org.terasology.rendering.nui.skin.UISkin;
import org.terasology.rendering.nui.widgets.TooltipLine;
import org.terasology.rendering.nui.widgets.TooltipLineRenderer;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UIList;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

import java.util.List;

public class WorldlyTooltip extends CoreHudWidget implements ControlWidget {
    private UILabel blockName;
    private UIList<TooltipLine> tooltip;
    private ItemIcon icon;

    private static final Logger logger = LoggerFactory.getLogger(WorldlyTooltip.class);
    @In
    private CameraTargetSystem cameraTargetSystem;

    @In
    private WorldProvider worldProvider;

    @In
    private BlockEntityRegistry blockEntityRegistry;

    @In
    private InputSystem inputSystem;

    @In
    private AssetManager assetManager;

    @Override
    public void initialise() {
        final KeyboardDevice keyboard = inputSystem.getKeyboard();

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

                    if (!cameraTargetSystem.isBlock()) {
                        EntityRef targetEntity = cameraTargetSystem.getTarget();
                        return targetEntity.getParentPrefab().getName();
                    }

                    Vector3i blockPosition = cameraTargetSystem.getTargetBlockPosition();
                    Block block = worldProvider.getBlock(blockPosition);
                    if (keyboard.isKeyDown(Keyboard.KeyId.LEFT_ALT) || keyboard.isKeyDown(Keyboard.KeyId.LEFT_ALT)) {
                        return block.getURI().toString();
                    } else {
                        EntityRef blockEntity = blockEntityRegistry.getBlockEntityAt(blockPosition);
                        DisplayNameComponent displayNameComponent = blockEntity.getComponent(DisplayNameComponent.class);
                        if (displayNameComponent != null) {
                            return displayNameComponent.name;
                        } else {
                            return block.getDisplayName();
                        }
                    }
                } else {
                    return "";
                }
            }
        });

        tooltip = find("tooltip", UIList.class);
        if (tooltip != null) {
            UISkin defaultSkin = assetManager.getAsset("core:itemTooltip", UISkin.class).get();
            tooltip.setItemRenderer(new TooltipLineRenderer(defaultSkin));
            tooltip.setSkin(defaultSkin);
            tooltip.bindList(
                    new ReadOnlyBinding<List<TooltipLine>>() {
                        @Override
                        public List<TooltipLine> get() {
                            if (cameraTargetSystem.isTargetAvailable()) {
                                EntityRef targetEntity = cameraTargetSystem.getTarget();
                                GetItemTooltip itemTooltip = new GetItemTooltip();
                                try {
                                    targetEntity.send(itemTooltip);
                                    return itemTooltip.getTooltipLines();
                                } catch (Exception ex) {
                                    return Lists.newArrayList(new TooltipLine("Error"));
                                }
                            }
                            return Lists.newArrayList();
                        }
                    });
        }

        icon = find("icon", ItemIcon.class);
        if (icon != null) {
            icon.bindIcon(new ReadOnlyBinding<TextureRegion>() {
                @Override
                public TextureRegion get() {
                    if (cameraTargetSystem.isTargetAvailable()) {
                        if (!cameraTargetSystem.isBlock()) {
                            EntityRef targetEntity = cameraTargetSystem.getTarget();
                            GetTooltipIconEvent getTooltipIconEvent = new GetTooltipIconEvent();
                            targetEntity.send(getTooltipIconEvent);
                            return getTooltipIconEvent.getIcon();
                        } else {
                            return null;
                        }
                    }
                    return null;
                }
            });
            icon.bindMesh(new ReadOnlyBinding<Mesh>() {
                @Override
                public Mesh get() {
                    if (cameraTargetSystem.isTargetAvailable()) {
                        if (!cameraTargetSystem.isBlock()) {
                            return null;
                        } else {
                            Vector3i blockPosition = cameraTargetSystem.getTargetBlockPosition();
                            Block block = worldProvider.getBlock(blockPosition);
                            if (block.getBlockFamily() != null) {
                                return block.getBlockFamily().getArchetypeBlock().getMesh();
                            }
                        }
                        return null;
                    }
                    return null;
                }
            });
            icon.setMeshTexture(assetManager.getAsset("engine:terrain", Texture.class).get());
        }
    }
}
