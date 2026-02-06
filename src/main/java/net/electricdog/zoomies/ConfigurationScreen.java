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

                        .option(Option.<Float>createBuilder()
                                .name(Text.literal("Transition Speed"))
                                .description(OptionDescription.of(Text.literal(
                                        "Controls how quickly the zoom transitions occur.\n" +
                                                "Higher values = snappier zoom response.\n" +
                                                "Lower values = smoother, more gradual transitions."
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
                                        "Automatically adjusts mouse sensitivity based on zoom level.\n" +
                                                "When enabled, mouse movement feels consistent regardless of zoom.\n" +
                                                "Disable if you prefer raw mouse input."
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
                                                "Provides a stable, steady view while zooming.\n" +
                                                "Your normal view bobbing setting is restored when zoom ends."
                                )))
                                .binding(
                                        defaults.disableViewBobbing,
                                        () -> config.disableViewBobbing,
                                        val -> config.disableViewBobbing = val
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())

                        .build())

                .save(ModConfiguration::persist)
                .build()
                .generateScreen(parent);
    }
}