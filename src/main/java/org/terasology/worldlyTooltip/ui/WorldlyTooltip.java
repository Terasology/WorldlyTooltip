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
package org.terasology.worldlyTooltip.ui;

import com.google.common.collect.Lists;
import org.joml.Vector3i;
import org.terasology.assets.management.AssetManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.input.InputSystem;
import org.terasology.engine.input.cameraTarget.CameraTargetSystem;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.assets.texture.TextureRegion;
import org.terasology.engine.rendering.nui.layers.hud.CoreHudWidget;
import org.terasology.rendering.nui.layers.ingame.inventory.GetItemTooltip;
import org.terasology.rendering.nui.layers.ingame.inventory.ItemIcon;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.input.Keyboard;
import org.terasology.input.device.KeyboardDevice;
import org.terasology.nui.ControlWidget;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.skin.UISkin;
import org.terasology.nui.widgets.TooltipLine;
import org.terasology.nui.widgets.TooltipLineRenderer;
import org.terasology.nui.widgets.UILabel;
import org.terasology.nui.widgets.UIList;
import org.terasology.worldlyTooltipAPI.events.GetTooltipIconEvent;
import org.terasology.worldlyTooltipAPI.events.GetTooltipNameEvent;

import java.util.List;

public class WorldlyTooltip extends CoreHudWidget implements ControlWidget {
    private UILabel name;
    private UIList<TooltipLine> tooltip;
    private ItemIcon icon;
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

        initialiseName(keyboard);
        initialiseTooltip();
        initialiseIcon();
    }

    /**
     * Initialise the label for the name of the targeted entity.
     *
     * If the user presses the ALT key the technical URI will be displayed for blocks instead of the display name.
     *
     * @param keyboard the keyboard device to adjust the shown label on user input
     */
    private void initialiseName(KeyboardDevice keyboard) {
        name = find("name", UILabel.class);
        name.bindText(new ReadOnlyBinding<String>() {
            @Override
            public String get() {
                if (cameraTargetSystem.isTargetAvailable()) {

                    if (!cameraTargetSystem.isBlock()) {
                        EntityRef targetEntity = cameraTargetSystem.getTarget();
                        GetTooltipNameEvent getTooltipNameEvent = new GetTooltipNameEvent();
                        targetEntity.send(getTooltipNameEvent);
                        return getTooltipNameEvent.getName();
                    }

                    Vector3i blockPosition = cameraTargetSystem.getTargetBlockPosition();
                    Block block = worldProvider.getBlock(blockPosition);
                    if (keyboard.isKeyDown(Keyboard.KeyId.LEFT_ALT) || keyboard.isKeyDown(Keyboard.KeyId.RIGHT_ALT)) {
                        return block.getURI().toString();
                    } else {
                        EntityRef blockEntity = blockEntityRegistry.getBlockEntityAt(blockPosition);
                        DisplayNameComponent displayNameComponent =
                                blockEntity.getComponent(DisplayNameComponent.class);
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
    }

    /**
     * Initialise the bindings for the tooltip lines.
     */
    private void initialiseTooltip() {
        tooltip = find("tooltip", UIList.class);
        if (tooltip != null) {
            UISkin defaultSkin = assetManager.getAsset("inventory:itemTooltip", UISkin.class).get();
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
    }

    /**
     * Initialise the bindings for the tooltip icon.
     */
    private void initialiseIcon() {
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
                        }
                    }
                    return null;
                }
            });
            icon.bindMesh(new ReadOnlyBinding<Mesh>() {
                @Override
                public Mesh get() {
                    if (cameraTargetSystem.isTargetAvailable()) {
                        if (cameraTargetSystem.isBlock()) {
                            Vector3i blockPosition = cameraTargetSystem.getTargetBlockPosition();
                            Block block = worldProvider.getBlock(blockPosition);
                            if (block.getBlockFamily() != null) {
                                return block.getBlockFamily().getArchetypeBlock().getMesh();
                            }
                        }
                    }
                    return null;
                }
            });
            icon.setMeshTexture(assetManager.getAsset("engine:terrain", Texture.class).get());
        }
    }
}
