package net.electricdog.zoomies;

import dev.isxander.yacl3.api.*;
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

                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("Zoom Behavior"))

                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Smooth Zooming"))
                                .description(OptionDescription.of(Text.literal(
                                        "Enable or disable smooth zooming.\n"
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
                                        "Controls how quickly the zoom transitions occur.\n" +
                                                "Higher values = quicker zoom response.\n" +
                                                "Lower values = smoother, more gradual zooms.\n" +
                                                "Requires smooth zooming to be enabled to take effect."
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

                        .build())

                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("Camera Control"))

                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Normalize Mouse Sensitivity"))
                                .description(OptionDescription.of(Text.literal(
                                        "Automatically adjusts mouse sensitivity based on zoom level."
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
                                        "Disables camera bobbing when zoomed in.\n" +
                                                "Provides a stable, steady view while zooming and moving around."
                                )))
                                .binding(
                                        defaults.disableViewBobbing,
                                        () -> config.disableViewBobbing,
                                        val -> config.disableViewBobbing = val
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())

                        .build())

                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("Visual Effects"))

                        .option(Option.<Boolean>createBuilder()
                                .name(Text.literal("Enable Vignette"))
                                .description(OptionDescription.of(Text.literal(
                                        "Adds a subtle darkening effect around the edges when zoomed in.\n" +
                                                "Helps focus attention on the center of the screen."
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
                                        "Controls how strong the vignette effect is.\n" +
                                                "Higher values = more pronounced darkening.\n" +
                                                "Lower values = subtle effect.\n" +
                                                "Requires vignette to be enabled to take effect."
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

                .save(ModConfiguration::persist)
                .build()
                .generateScreen(parent);
    }
}