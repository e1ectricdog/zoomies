package net.electricdog.zoomies;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

// You will see some really dodgey code because I can't code very well
// Apologies in advance

public class ZoomController implements ClientModInitializer {

    private static final float MAX_ZOOM_MULTIPLIER = 10.0f;
    private static final float MIN_ZOOM_MULTIPLIER = 1.0f;

    public static KeyBinding activateZoom;

    private static boolean zoomActive = false;
    private static float targetIntensity = 0.0f;

    private static float currentIntensity = 0.0f;
    private static float prevIntensity = 0.0f;

    private static Double savedMouseSensitivity = null;
    private static Boolean savedViewBobbing = null;

    @Override
    public void onInitializeClient() {
        activateZoom = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.zoomies.activate",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                KeyBinding.Category.create(Identifier.of("zoomies", "controls"))
        ));

        ModConfiguration.initialize();

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onClientTick(MinecraftClient client) {
        if (client.player == null) return;

        prevIntensity = currentIntensity;

        boolean wasActive = zoomActive;
        zoomActive = activateZoom.isPressed();

        if (wasActive && !zoomActive) {
            targetIntensity = 0.0f;
        }

        if (zoomActive) {
            if (targetIntensity < 0.1f) targetIntensity = 0.2f;
        }

        ModConfiguration config = ModConfiguration.get();

        float smoothSpeed = (float) config.zoomTransitionSpeed * 0.25f;

        currentIntensity += (targetIntensity - currentIntensity) * smoothSpeed;

        if (Math.abs(currentIntensity - targetIntensity) < 0.001f) {
            currentIntensity = targetIntensity;
        }

        applyMouseSensitivity(client, config);
        applyViewBobbing(client, config);
    }

    private void applyMouseSensitivity(MinecraftClient client, ModConfiguration config) {
        boolean shouldAdjust = isZooming() && config.normalizeMouseSensitivity;

        if (shouldAdjust) {
            if (savedMouseSensitivity == null) {
                savedMouseSensitivity = client.options.getMouseSensitivity().getValue();
            }

            float fovMultiplier = getFovMultiplierNoDelta();

            float adjustedSensitivity = savedMouseSensitivity.floatValue() * fovMultiplier;

            adjustedSensitivity = MathHelper.clamp(adjustedSensitivity, 0.0f, 1.0f);

            client.options.getMouseSensitivity().setValue((double) adjustedSensitivity);
        } else {
            if (savedMouseSensitivity != null) {
                client.options.getMouseSensitivity().setValue(savedMouseSensitivity);
                savedMouseSensitivity = null;
            }
        }
    }

    private static float getAdjustedSensitivity() {
        float fovMultiplier = getFovMultiplierNoDelta();

        float adjustedSensitivity = savedMouseSensitivity.floatValue() * fovMultiplier;

        adjustedSensitivity = MathHelper.clamp(adjustedSensitivity, 0.0f, 1.0f);
        return adjustedSensitivity;
    }

    private void applyViewBobbing(MinecraftClient client, ModConfiguration config) {
        boolean shouldDisable = isZooming() && config.disableViewBobbing;

        if (shouldDisable) {
            if (savedViewBobbing == null && client.options.getBobView().getValue()) {
                savedViewBobbing = true;
                client.options.getBobView().setValue(false);
            }
        } else {
            if (savedViewBobbing != null && savedViewBobbing) {
                client.options.getBobView().setValue(true);
                savedViewBobbing = null;
            }
        }
    }

    public static void processScroll(float scrollDelta) {
        if (!zoomActive) return;

        float baseStep = 0.02f + (targetIntensity * 0.08f);
        float step = baseStep * Math.signum(scrollDelta);

        targetIntensity += step;
        targetIntensity = Math.max(0.0f, Math.min(1.0f, targetIntensity));
    }

    public static float getCameraFov(float baseFov, float tickDelta) {
        float multiplier = getCurrentFOVMultiplier(tickDelta);
        return baseFov * multiplier;
    }

    public static float getCurrentFOVMultiplier(float tickDelta) {
        if (!isZooming()) return 1.0f;

        float t = MathHelper.lerp(tickDelta, prevIntensity, currentIntensity);

        float zoomMultiplier = MIN_ZOOM_MULTIPLIER + (MAX_ZOOM_MULTIPLIER - MIN_ZOOM_MULTIPLIER) * t;
        return 1.0f / zoomMultiplier;
    }

    private static float getFovMultiplierNoDelta() {
        return getCurrentFOVMultiplier(1.0f);
    }

    public static boolean isZooming() {
        return currentIntensity > 0.001f || targetIntensity > 0.001f;
    }
}