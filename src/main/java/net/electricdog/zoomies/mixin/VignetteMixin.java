package net.electricdog.zoomies.mixin;

import net.electricdog.zoomies.ModConfiguration;
import net.electricdog.zoomies.ZoomController;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class VignetteMixin {

    @Shadow
    public float vignetteDarkness;

    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    private void setZoomVignetteDarkness(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        ModConfiguration config = ModConfiguration.get();
        float tickDelta = tickCounter.getFixedDeltaTicks();

        if (!config.enableVignette || !ZoomController.isZooming()) {
            vignetteDarkness = Math.max(0, vignetteDarkness - 0.05f);
            return;
        }

        float zoomIntensity = 1.0f - ZoomController.getCurrentFOVMultiplier(tickDelta);
        float targetVignette = zoomIntensity * (float) config.vignetteIntensity;

        vignetteDarkness += (targetVignette - vignetteDarkness) * 0.15f;
    }
}