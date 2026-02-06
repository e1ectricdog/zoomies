package net.electricdog.zoomies.mixin;

import net.electricdog.zoomies.ZoomController;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class ScrollInterceptMixin {

    @Inject(
            method = "onMouseScroll",
            at = @At("HEAD"),
            cancellable = true
    )
    private void interceptScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (ZoomController.isZooming()) {
            ZoomController.processScroll((float) vertical);
            ci.cancel();
        }
    }
}