package net.electricdog.zoomies.mixin;

import net.electricdog.zoomies.ZoomController;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class CameraZoomMixin {

    @Inject(
            method = "getFov",
            at = @At("RETURN"),
            cancellable = true
    )
    private void modifyFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
        if (!ZoomController.isZooming()) return;

        double originalFov = cir.getReturnValue().doubleValue();
        double multiplier = (double) ZoomController.getCurrentFOVMultiplier(tickDelta);

        cir.setReturnValue((float) (originalFov * multiplier));
    }
}