// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.worldlyTooltip.ui;

import com.google.common.collect.Lists;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.input.InputSystem;
import org.terasology.engine.input.cameraTarget.CameraTargetSystem;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.assets.texture.TextureRegion;
import org.terasology.engine.rendering.nui.layers.hud.CoreHudWidget;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.inventory.rendering.nui.layers.ingame.GetItemTooltip;
import org.terasology.inventory.rendering.nui.layers.ingame.ItemIcon;
import org.terasology.math.geom.Vector3i;
import org.terasology.nui.ControlWidget;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.input.Keyboard;
import org.terasology.nui.input.device.KeyboardDevice;
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
