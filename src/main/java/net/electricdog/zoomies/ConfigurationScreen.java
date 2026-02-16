package net.electricdog.zoomies;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ConfigurationScreen {

    public static Screen create(Screen parent) {
        ModConfiguration config = ModConfiguration.get();
        ModConfiguration defaults = new ModConfiguration();

        return YetAnotherConfigLib.createBuilder()
                .title(Text.literal("Zoomies Configuration"))

                // Zoom Behaviour
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("Zoom Behavior"))

                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Zoom Mechanics"))
                                .description(OptionDescription.of(Text.literal(
                                        "Control how zooming behaves and responds"
                                )))

                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Smooth Zooming"))
                                        .description(OptionDescription.of(Text.literal(
                                                "Enable or disable smooth zooming transitions."
                                        )))
                                        .binding(
                                                defaults.smoothZooming,
                                                () -> config.smoothZooming,
                                                val -> config.smoothZooming = val
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())

                                .option(Option.<Float>createBuilder()
                                        .name(Text.literal("Transition Speed"))
                                        .description(OptionDescription.of(Text.literal(
                                                """
                                                        Controls how quickly the zoom transitions occur.

                                                        Requires smooth zooming to be enabled to take effect."""
                                        )))
                                        .binding(
                                                (float) defaults.zoomTransitionSpeed,
                                                () -> (float) config.zoomTransitionSpeed,
                                                val -> config.zoomTransitionSpeed = val
                                        )
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .range(1.0f, 3.0f)
                                                .step(0.5f)
                                                .formatValue(val -> Text.literal(String.format("%.1fx", val))))
                                        .build())

                                .option(Option.<Float>createBuilder()
                                        .name(Text.literal("Scroll Zoom Sensitivity"))
                                        .description(OptionDescription.of(Text.literal(
                                                "Adjust if scroll zooming feels too fast or too slow."
                                        )))
                                        .binding(
                                                (float) defaults.zoomScrollSensitivity,
                                                () -> (float) config.zoomScrollSensitivity,
                                                val -> config.zoomScrollSensitivity = val
                                        )
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .range(0.5f, 2.0f)
                                                .step(0.5f)
                                                .formatValue(val -> Text.literal(String.format("%.1fx", val))))
                                        .build())

                                .build())

                        .build())

                // Camera Control
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("Camera Control"))

                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Mouse & View"))
                                .description(OptionDescription.of(Text.literal(
                                        "Adjust camera behavior while zoomed"
                                )))

                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Normalize Mouse Sensitivity"))
                                        .description(OptionDescription.of(Text.literal(
                                                "Automatically adjusts mouse sensitivity based on zoom level for consistent feel."
                                        )))
                                        .binding(
                                                defaults.normalizeMouseSensitivity,
                                                () -> config.normalizeMouseSensitivity,
                                                val -> config.normalizeMouseSensitivity = val
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())

                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Disable View Bobbing"))
                                        .description(OptionDescription.of(Text.literal(
                                                "Disables camera bobbing when zoomed in for a stable view while moving."
                                        )))
                                        .binding(
                                                defaults.disableViewBobbing,
                                                () -> config.disableViewBobbing,
                                                val -> config.disableViewBobbing = val
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())

                                .build())

                        .build())

                // Visuals
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("Visuals"))

                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Zoom Indicator"))
                                .description(OptionDescription.of(Text.literal(
                                        "Customize the on-screen zoom level display"
                                )))

                                .option(Option.<ZoomUIStyle>createBuilder()
                                        .name(Text.literal("UI Style"))
                                        .description(OptionDescription.of(Text.literal(
                                                """
                                                        Choose how the zoom level is displayed:
                                                        
                                                        • Progress Bar
                                                        • Window
                                                        • Minimal (Text display)
                                                        • None"""
                                        )))
                                        .binding(
                                                ZoomUIStyle.valueOf(defaults.zoomUIStyle),
                                                () -> ZoomUIStyle.valueOf(config.zoomUIStyle),
                                                val -> config.zoomUIStyle = val.name()
                                        )
                                        .controller(opt -> EnumControllerBuilder.create(opt)
                                                .enumClass(ZoomUIStyle.class))
                                        .build())

                                .build())

                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Vignette Effect"))
                                .description(OptionDescription.of(Text.literal(
                                        "Edge darkening effect while zoomed"
                                )))

                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Enable Vignette"))
                                        .description(OptionDescription.of(Text.literal(
                                                "Adds a darkening effect around the edges when zoomed in."
                                        )))
                                        .binding(
                                                defaults.enableVignette,
                                                () -> config.enableVignette,
                                                val -> config.enableVignette = val
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())

                                .option(Option.<Float>createBuilder()
                                        .name(Text.literal("Vignette Intensity"))
                                        .description(OptionDescription.of(Text.literal(
                                                """
                                                        Controls how strong the vignette effect is.
                                                        
                                                        Requires vignette to be enabled."""
                                        )))
                                        .binding(
                                                (float) defaults.vignetteIntensity,
                                                () -> (float) config.vignetteIntensity,
                                                val -> config.vignetteIntensity = val
                                        )
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .range(0.0f, 1.0f)
                                                .step(0.1f)
                                                .formatValue(val -> Text.literal(String.format("%.1f", val))))
                                        .build())

                                .build())

                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Block Information"))
                                .description(OptionDescription.of(Text.literal(
                                        "Display coordinates of targeted blocks"
                                )))

                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Show Block Coordinates"))
                                        .description(OptionDescription.of(Text.literal(
                                                "Display coordinates of the block you're looking at when zoomed in enough."
                                        )))
                                        .binding(
                                                defaults.showBlockCoordinates,
                                                () -> config.showBlockCoordinates,
                                                val -> config.showBlockCoordinates = val
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())

                                .option(Option.<Float>createBuilder()
                                        .name(Text.literal("Minimum Zoom Level"))
                                        .description(OptionDescription.of(Text.literal(
                                                "The minimum zoom level required to show block coordinates prompts."
                                        )))
                                        .binding(
                                                (float) defaults.minZoomForDecorations,
                                                () -> (float) config.minZoomForDecorations,
                                                val -> config.minZoomForDecorations = val
                                        )
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .range(10.0f, 100.0f)
                                                .step(5.0f)
                                                .formatValue(val -> Text.literal(String.format("%.0fx", val))))
                                        .build())

                                .option(Option.<OverlayPosition>createBuilder()
                                        .name(Text.literal("Panel Position"))
                                        .description(OptionDescription.of(Text.literal(
                                                "Choose where the block coordinates panel appears on screen."
                                        )))
                                        .binding(
                                                OverlayPosition.valueOf(defaults.blockCoordsPosition),
                                                () -> OverlayPosition.valueOf(config.blockCoordsPosition),
                                                val -> config.blockCoordsPosition = val.name()
                                        )
                                        .controller(opt -> EnumControllerBuilder.create(opt)
                                                .enumClass(OverlayPosition.class))
                                        .build())

                                .build())

                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Entity Information"))
                                .description(OptionDescription.of(Text.literal(
                                        "Display information about distant players and mobs while zoomed in"
                                )))

                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Enable Entity Overlay"))
                                        .description(OptionDescription.of(Text.literal(
                                                "Toggle the entity information overlay shown when zoomed in."
                                        )))
                                        .binding(
                                                defaults.showEntityOverlay,
                                                () -> config.showEntityOverlay,
                                                val -> config.showEntityOverlay = val
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())

                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Show Entity Names"))
                                        .description(OptionDescription.of(Text.literal(
                                                "Display player names and mob names (when nametagged) when zoomed in."
                                        )))
                                        .binding(
                                                defaults.showEntityNames,
                                                () -> config.showEntityNames,
                                                val -> config.showEntityNames = val
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())

                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Show Entity Health"))
                                        .description(OptionDescription.of(Text.literal(
                                                "Display the entity's current health next to their name."
                                        )))
                                        .binding(
                                                defaults.showEntityHealth,
                                                () -> config.showEntityHealth,
                                                val -> config.showEntityHealth = val
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())

                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Show Held Items"))
                                        .description(OptionDescription.of(Text.literal(
                                                "Display the item entities are holding in their main hand."
                                        )))
                                        .binding(
                                                defaults.showEntityHeldItems,
                                                () -> config.showEntityHeldItems,
                                                val -> config.showEntityHeldItems = val
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())

                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Show Entity Description"))
                                        .description(OptionDescription.of(Text.literal(
                                                "Shows a description of the entity."
                                        )))
                                        .binding(
                                                defaults.showMobTypes,
                                                () -> config.showMobTypes,
                                                val -> config.showMobTypes = val
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())

                                .option(Option.<Float>createBuilder()
                                        .name(Text.literal("Min Entity Zoom Level"))
                                        .description(OptionDescription.of(Text.literal(
                                                "The minimum zoom level required to show entity information."
                                        )))
                                        .binding(
                                                (float) defaults.minZoomForEntityDecorations,
                                                () -> (float) config.minZoomForEntityDecorations,
                                                val -> config.minZoomForEntityDecorations = val
                                        )
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .range(5.0f, 50.0f)
                                                .step(5.0f)
                                                .formatValue(val -> Text.literal(String.format("%.0fx", val))))
                                        .build())

                                .option(Option.<OverlayPosition>createBuilder()
                                        .name(Text.literal("Panel Position"))
                                        .description(OptionDescription.of(Text.literal(
                                                "Choose where the entity information panel appears on screen."
                                        )))
                                        .binding(
                                                OverlayPosition.valueOf(defaults.entityOverlayPosition),
                                                () -> OverlayPosition.valueOf(config.entityOverlayPosition),
                                                val -> config.entityOverlayPosition = val.name()
                                        )
                                        .controller(opt -> EnumControllerBuilder.create(opt)
                                                .enumClass(OverlayPosition.class))
                                        .build())

                                .build())

                        .build())

                // Waypoint Integration
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("Waypoint Integration"))
                        .tooltip(Text.literal("Requires Xaero's Minimap to be installed"))

                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Xaero's Minimap"))
                                .description(OptionDescription.of(Text.literal(
                                        "Create waypoints while zoomed in on distant blocks"
                                )))

                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Enable Waypoint Integration"))
                                        .description(OptionDescription.of(Text.literal(
                                                """
                                                        Enable integration with Xaero's Minimap.
                                                        When zoomed in and looking at a block,
                                                        you can press a key to create a waypoint.
                                                        
                                                        Requires Xaero's Minimap to be installed."""
                                        )))
                                        .binding(
                                                defaults.enableWaypointIntegration,
                                                () -> config.enableWaypointIntegration,
                                                val -> config.enableWaypointIntegration = val
                                        )
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())

                                .option(Option.<WaypointType>createBuilder()
                                        .name(Text.literal("Waypoint Type"))
                                        .description(OptionDescription.of(Text.literal(
                                                """
                                                        Choose what type of waypoint to create:
                                                        
                                                        • Normal - Standard waypoint
                                                        • Destination - Deletes when you get to it"""
                                        )))
                                        .binding(
                                                WaypointType.valueOf(defaults.waypointType),
                                                () -> WaypointType.valueOf(config.waypointType),
                                                val -> config.waypointType = val.name()
                                        )
                                        .controller(opt -> EnumControllerBuilder.create(opt)
                                                .enumClass(WaypointType.class))
                                        .build())

                                .build())

                        .build())

                .save(ModConfiguration::persist)
                .build()
                .generateScreen(parent);
    }
}