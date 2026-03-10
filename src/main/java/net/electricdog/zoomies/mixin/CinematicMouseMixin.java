package net.electricdog.zoomies.mixin;

import net.electricdog.zoomies.ZoomController;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class CinematicMouseMixin {

    @Shadow private double cursorDeltaX;
    @Shadow private double cursorDeltaY;

    @Unique private double smoothX = 0;
    @Unique private double smoothY = 0;

    @Inject(method = "updateMouse", at = @At("HEAD"))
    private void applyCinematicSmoothing(CallbackInfo ci) {
        if (!ZoomController.isCinematicActive()) {
            smoothX = 0;
            smoothY = 0;
            return;
        }

        smoothX += (cursorDeltaX - smoothX) * 0.08;
        smoothY += (cursorDeltaY - smoothY) * 0.08;

        cursorDeltaX = smoothX;
        cursorDeltaY = smoothY;
    }
}