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
                                                        Higher values = quicker zoom response.
                                                        Lower values = smoother, more gradual zooms.
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
                                                        
                                                        • Progress Bar - Horizontal bar with zoom level
                                                        • Window - Animated window frame effect
                                                        • Minimal - Text display only
                                                        • None - No indicator"""
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
                                                        
                                                        Higher values = more pronounced darkening
                                                        Lower values = subtle effect
                                                        
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
                                                "Display coordinates of the block you're looking at when sufficiently zoomed in."
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
                                                """
                                                        The minimum zoom level required to show block coordinates and waypoint prompts.
                                                        
                                                        Lower values = shows earlier when zooming
                                                        Higher values = requires more zoom to activate"""
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
                                                        
                                                        • Normal - Standard waypoint marker
                                                        • Destination - Highlighted navigation target"""
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