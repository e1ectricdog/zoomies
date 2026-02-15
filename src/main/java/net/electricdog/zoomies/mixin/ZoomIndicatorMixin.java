package net.electricdog.zoomies.mixin;

import net.electricdog.zoomies.ModConfiguration;
import net.electricdog.zoomies.XaeroIntegration;
import net.electricdog.zoomies.ZoomController;
import net.electricdog.zoomies.ZoomUIStyle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class ZoomIndicatorMixin {

    @Unique
    private float animationProgress = 0.0f;

    @Unique
    private float coordsAnimationProgress = 0.0f;

    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    private void renderZoomIndicator(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        ModConfiguration config = ModConfiguration.get();
        ZoomUIStyle style = ZoomUIStyle.valueOf(config.zoomUIStyle);

        if (style == ZoomUIStyle.NONE) return;

        if (!ZoomController.isZooming() && animationProgress <= 0.0f) {
            return;
        }

        float tickDelta = tickCounter.getFixedDeltaTicks();

        if (ZoomController.isZooming()) {
            animationProgress = Math.min(1.0f, animationProgress + 0.15f);
        } else {
            animationProgress = Math.max(0.0f, animationProgress - 0.1f);
        }

        if (animationProgress <= 0.0f) return;

        float fovMultiplier = ZoomController.getCurrentFOVMultiplier(tickDelta);
        float zoomLevel = 1.0f / fovMultiplier;

        MinecraftClient client = MinecraftClient.getInstance();
        int topOffset = getBossBarOffset(client);

        switch (style) {
            case PROGRESS_BAR:
                renderProgressBar(context, zoomLevel, animationProgress, topOffset);
                break;
            case WINDOW:
                renderWindow(context, zoomLevel, animationProgress, topOffset);
                break;
            case MINIMAL:
                renderMinimal(context, zoomLevel, animationProgress, topOffset);
                break;
        }

        if (config.showBlockCoordinates && zoomLevel >= config.minZoomForDecorations) {
            renderBlockCoordinates(context, tickDelta, config);
        }
    }

    @Unique
    private void renderProgressBar(DrawContext context, float zoomLevel, float animProgress, int topOffset) {
        int screenWidth = context.getScaledWindowWidth();

        int barWidth = 120;
        int barHeight = 3;
        int barX = (screenWidth - barWidth) / 2;
        int barY = 15 + topOffset;

        float fillPercent = (zoomLevel - 1.0f) / 99.0f;

        fillPercent = MathHelper.clamp(fillPercent, 0.0f, 1.0f);

        float easeProgress = (float) (1.0 - Math.pow(1.0 - animProgress, 3));

        int bgColor = (int) (easeProgress * 100) << 24;
        int fillColor = (int)(easeProgress * 200) << 24 | 0x00FFE6;
        int borderColor = (int)(easeProgress * 180) << 24 | 0xFFFFFF;

        context.fill(barX - 1, barY - 1, barX + barWidth + 1, barY + barHeight + 1, borderColor);
        context.fill(barX, barY, barX + barWidth, barY + barHeight, bgColor);

        int fillWidth = (int)(barWidth * fillPercent);
        context.fill(barX, barY, barX + fillWidth, barY + barHeight, fillColor);

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        String zoomText = String.format("%.0fx", zoomLevel);
        int textWidth = textRenderer.getWidth(zoomText);
        int textX = (screenWidth - textWidth) / 2;
        int textY = barY + barHeight + 4;

        int textColor = (int)(easeProgress * 255) << 24 | 0xFFFFFF;
        int shadowColor = (int) (easeProgress * 120) << 24;

        context.drawText(textRenderer, zoomText, textX + 1, textY + 1, shadowColor, false);
        context.drawText(textRenderer, zoomText, textX, textY, textColor, false);
    }

    @Unique
    private void renderWindow(DrawContext context, float zoomLevel, float animProgress, int topOffset) {
        int screenWidth = context.getScaledWindowWidth();

        float easeProgress = (float) (1.0 - Math.pow(1.0 - animProgress, 3));

        int outerWidth = 140;
        int outerHeight = 100;

        int centerX = screenWidth / 2;
        int topY = 10 + topOffset;

        int outerLeftX = centerX - outerWidth / 2;
        int outerRightX = centerX + outerWidth / 2;
        int outerBottomY = topY + outerHeight;

        float zoomPercent = (zoomLevel - 1.0f) / 99.0f;

        zoomPercent = MathHelper.clamp(zoomPercent, 0.0f, 1.0f);

        int minInnerWidth = 20;
        int minInnerHeight = 14;
        int innerWidth = (int)(outerWidth - (outerWidth - minInnerWidth) * zoomPercent);
        int innerHeight = (int)(outerHeight - (outerHeight - minInnerHeight) * zoomPercent);

        int innerLeftX = centerX - innerWidth / 2;
        int innerRightX = centerX + innerWidth / 2;
        int innerTopY = topY + (outerHeight - innerHeight) / 2;
        int innerBottomY = innerTopY + innerHeight;

        int outerColor = (int)(easeProgress * 150) << 24 | 0xFFFFFF;

        int innerColor = (int)(easeProgress * 200) << 24 | 0x00FFE6;

        int frameThickness = 2;

        context.fill(outerLeftX - frameThickness, topY - frameThickness, outerRightX + frameThickness, topY, outerColor);

        context.fill(outerLeftX - frameThickness, outerBottomY, outerRightX + frameThickness, outerBottomY + frameThickness, outerColor);

        context.fill(outerLeftX - frameThickness, topY, outerLeftX, outerBottomY, outerColor);

        context.fill(outerRightX, topY, outerRightX + frameThickness, outerBottomY, outerColor);

        context.fill(innerLeftX - 1, innerTopY - 1, innerRightX + 1, innerTopY, innerColor);

        context.fill(innerLeftX - 1, innerBottomY, innerRightX + 1, innerBottomY + 1, innerColor);

        context.fill(innerLeftX - 1, innerTopY, innerLeftX, innerBottomY, innerColor);

        context.fill(innerRightX, innerTopY, innerRightX + 1, innerBottomY, innerColor);

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        String zoomText = String.format("%.0fx", zoomLevel);
        int textWidth = textRenderer.getWidth(zoomText);
        int textX = centerX - textWidth / 2;
        int textY = outerBottomY + frameThickness + 3;

        int textColor = (int)(easeProgress * 255) << 24 | 0xFFFFFF;
        int shadowColor = (int) (easeProgress * 120) << 24;

        context.drawText(textRenderer, zoomText, textX + 1, textY + 1, shadowColor, false);
        context.drawText(textRenderer, zoomText, textX, textY, textColor, false);
    }

    @Unique
    private void renderMinimal(DrawContext context, float zoomLevel, float animProgress, int topOffset) {
        int screenWidth = context.getScaledWindowWidth();

        float easeProgress = (float) (1.0 - Math.pow(1.0 - animProgress, 3));

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        String zoomText = String.format("%.0fx", zoomLevel);
        int textWidth = textRenderer.getWidth(zoomText);
        int textX = (screenWidth - textWidth) / 2;
        int textY = 15 + topOffset;

        int textColor = (int)(easeProgress * 255) << 24 | 0xFFFFFF;
        int shadowColor = (int) (easeProgress * 120) << 24;

        context.drawText(textRenderer, zoomText, textX + 1, textY + 1, shadowColor, false);
        context.drawText(textRenderer, zoomText, textX, textY, textColor, false);
    }

    @Unique
    private void renderBlockCoordinates(DrawContext context, float tickDelta, ModConfiguration config) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            coordsAnimationProgress = Math.max(0.0f, coordsAnimationProgress - 0.1f);
            ZoomController.setLastTargetedBlock(null);
            return;
        }

        double maxDistance = 512.0;

        net.minecraft.util.math.Vec3d cameraPos = client.player.getCameraPosVec(tickDelta);
        net.minecraft.util.math.Vec3d lookVec = client.player.getRotationVec(tickDelta);
        net.minecraft.util.math.Vec3d endVec = cameraPos.add(
                lookVec.x * maxDistance,
                lookVec.y * maxDistance,
                lookVec.z * maxDistance
        );

        BlockHitResult hitResult = client.world.raycast(new net.minecraft.world.RaycastContext(
                cameraPos,
                endVec,
                net.minecraft.world.RaycastContext.ShapeType.OUTLINE,
                net.minecraft.world.RaycastContext.FluidHandling.NONE,
                client.player
        ));

        if (hitResult.getType() != HitResult.Type.BLOCK) {
            coordsAnimationProgress = Math.max(0.0f, coordsAnimationProgress - 0.1f);
            ZoomController.setLastTargetedBlock(null);
            if (coordsAnimationProgress <= 0.0f) return;
        } else {
            coordsAnimationProgress = Math.min(1.0f, coordsAnimationProgress + 0.15f);
            ZoomController.setLastTargetedBlock(hitResult.getBlockPos());
        }

        if (coordsAnimationProgress <= 0.0f) return;

        BlockPos pos = hitResult.getBlockPos();

        TextRenderer textRenderer = client.textRenderer;

        String coordText = String.format("X: %d Y: %d Z: %d", pos.getX(), pos.getY(), pos.getZ());

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        int textWidth = textRenderer.getWidth(coordText);
        int textHeight = textRenderer.fontHeight;

        String waypointText = "";
        boolean showWaypointPrompt = config.enableWaypointIntegration &&
                XaeroIntegration.isXaerosMinimapLoaded();

        if (showWaypointPrompt) {
            String keyName = ZoomController.createWaypoint.getBoundKeyLocalizedText().getString();
            waypointText = String.format("Press %s to waypoint", keyName);
            int waypointWidth = textRenderer.getWidth(waypointText);
            if (waypointWidth > textWidth) {
                textWidth = waypointWidth;
            }
            textHeight += textRenderer.fontHeight + 2;
        }

        int padding = 6;
        int bgPadding = 3;
        int x = screenWidth - textWidth - padding - bgPadding * 2;
        int y = screenHeight - textHeight - padding - bgPadding * 2;

        float easeProgress = (float) (1.0 - Math.pow(1.0 - coordsAnimationProgress, 3));

        int bgColor = (int) (easeProgress * 120) << 24;
        int borderColor = (int)(easeProgress * 150) << 24 | 0x00FFE6;

        context.fill(x - bgPadding - 1, y - bgPadding - 1,
                x + textWidth + bgPadding + 1, y + textHeight + bgPadding + 1, borderColor);
        context.fill(x - bgPadding, y - bgPadding,
                x + textWidth + bgPadding, y + textHeight + bgPadding, bgColor);

        int textColor = (int)(easeProgress * 255) << 24 | 0xFFFFFF;
        int shadowColor = (int)(easeProgress * 100) << 24;

        context.drawText(textRenderer, coordText, x + 1, y + 1, shadowColor, false);
        context.drawText(textRenderer, coordText, x, y, textColor, false);

        if (showWaypointPrompt) {
            int waypointY = y + textRenderer.fontHeight + 2;
            int waypointColor = (int)(easeProgress * 255) << 24 | 0xFFFF00; // Yellow
            context.drawText(textRenderer, waypointText, x + 1, waypointY + 1, shadowColor, false);
            context.drawText(textRenderer, waypointText, x, waypointY, waypointColor, false);
        }
    }

    @Unique
    private static int getBossBarOffset(MinecraftClient client) {
        try {
            BossBarHud bossBarHud = client.inGameHud.getBossBarHud();
            var field = BossBarHud.class.getDeclaredField("bossBars");
            field.setAccessible(true);
            int count = ((java.util.Map<?, ?>) field.get(bossBarHud)).size();
            if (count == 0) return 0;
            // vanilla renders bars from y=12, 19px each, stopping at windowHeight/3
            int maxHeight = client.getWindow().getScaledHeight() / 3;
            return Math.min(count * 19, maxHeight);
        } catch (Exception e) {
            return 0;
        }
    }
}