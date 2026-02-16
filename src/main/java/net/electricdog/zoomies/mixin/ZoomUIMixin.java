package net.electricdog.zoomies.mixin;

import com.terraformersmc.modmenu.util.mod.Mod;
import net.electricdog.zoomies.*;
import net.electricdog.zoomies.util.EnchantRow;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Mixin(InGameHud.class)
public class ZoomUIMixin {

    @Unique private static final int OVERLAY_MARGIN = 8;

    @Unique private float animationProgress = 0.0f;

    @Unique private float coordsAnimationProgress = 0.0f;
    @Unique private BlockPos lastBlockRenderPos = null;

    @Unique private float entityAnimationProgress = 0.0f;
    @Unique private Entity lastTargetedEntity = null;

    @Inject(method = "render", at = @At("TAIL"))
    private void renderOverlays(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        ModConfiguration config = ModConfiguration.get();
        float tickDelta = tickCounter.getFixedDeltaTicks();

        OverlayStackManager.reset();

        renderZoomIndicator(context, tickDelta, config);

        OverlayPosition entityPos = OverlayPosition.valueOf(config.entityOverlayPosition);
        OverlayPosition blockPos = OverlayPosition.valueOf(config.blockCoordsPosition);

        boolean sameBottomCorner = (entityPos == blockPos) && entityPos.isBottom();

        if (sameBottomCorner) {
            maybeRenderBlockCoords(context, tickDelta, config, blockPos);
            maybeRenderEntityOverlay(context, tickDelta, config, entityPos);
        } else {
            maybeRenderEntityOverlay(context, tickDelta, config, entityPos);
            maybeRenderBlockCoords(context, tickDelta, config, blockPos);
        }
    }

    @Unique
    private void renderZoomIndicator(DrawContext context, float tickDelta, ModConfiguration config) {
        ZoomUIStyle style = ZoomUIStyle.valueOf(config.zoomUIStyle);
        if (style == ZoomUIStyle.NONE) return;
        if (!ZoomController.isZooming() && animationProgress <= 0.0f) return;

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
            case PROGRESS_BAR -> renderProgressBar(context, zoomLevel, animationProgress, topOffset, config);
            case WINDOW -> renderWindow(context, zoomLevel, animationProgress, topOffset, config);
            case MINIMAL -> renderMinimal(context, zoomLevel, animationProgress, topOffset);
        }
    }

    @Unique
    private void maybeRenderBlockCoords(DrawContext context, float tickDelta, ModConfiguration config, OverlayPosition pos) {
        if (!config.showBlockCoordinates) return;

        MinecraftClient client = MinecraftClient.getInstance();

        float fovMultiplier = ZoomController.getCurrentFOVMultiplier(tickDelta);
        float zoomLevel = 1.0f / fovMultiplier;

        boolean shouldBeActive = ZoomController.isZooming()
                && zoomLevel >= config.minZoomForDecorations
                && client.player != null && client.world != null;

        if (!shouldBeActive) {
            coordsAnimationProgress = Math.max(0.0f, coordsAnimationProgress - 0.1f);
            ZoomController.setLastTargetedBlock(null);
            if (coordsAnimationProgress <= 0.0f || lastBlockRenderPos == null) return;
        } else {
            Vec3d cameraPos = client.player.getCameraPosVec(tickDelta);
            Vec3d lookVec = client.player.getRotationVec(tickDelta);
            double maxDist = 512.0;
            Vec3d endVec = cameraPos.add(lookVec.x * maxDist, lookVec.y * maxDist, lookVec.z * maxDist);

            BlockHitResult hitResult = client.world.raycast(new net.minecraft.world.RaycastContext(
                    cameraPos, endVec,
                    net.minecraft.world.RaycastContext.ShapeType.OUTLINE,
                    net.minecraft.world.RaycastContext.FluidHandling.NONE,
                    client.player
            ));

            if (hitResult.getType() != HitResult.Type.BLOCK) {
                coordsAnimationProgress = Math.max(0.0f, coordsAnimationProgress - 0.1f);
                ZoomController.setLastTargetedBlock(null);
                if (coordsAnimationProgress <= 0.0f || lastBlockRenderPos == null) return;
            } else {
                coordsAnimationProgress = Math.min(1.0f, coordsAnimationProgress + 0.15f);
                lastBlockRenderPos = hitResult.getBlockPos();
                ZoomController.setLastTargetedBlock(lastBlockRenderPos);
            }
        }

        BlockPos blockHitPos = lastBlockRenderPos;
        TextRenderer tr = client.textRenderer;

        String coordText = String.format("X: %d  Y: %d  Z: %d",
                blockHitPos.getX(), blockHitPos.getY(), blockHitPos.getZ());

        boolean showWaypoint = config.enableWaypointIntegration
                && XaeroIntegration.isXaerosMinimapLoaded();
        String waypointText = "";
        if (showWaypoint) {
            String keyName = ZoomController.createWaypoint.getBoundKeyLocalizedText().getString();
            waypointText = String.format("Press %s to waypoint", keyName);
        }

        int bgPad = 3;

        int contentW = tr.getWidth(coordText);
        if (showWaypoint) contentW = Math.max(contentW, tr.getWidth(waypointText));
        int contentH = tr.fontHeight + (showWaypoint ? tr.fontHeight + 2 : 0);

        int panelW = contentW + bgPad * 2;
        int panelH = contentH + bgPad * 2;

        int screenW = context.getScaledWindowWidth();
        int screenH = context.getScaledWindowHeight();

        int[] xy = OverlayStackManager.computePosition(pos, panelW, panelH, screenW, screenH, OVERLAY_MARGIN);
        int x = xy[0];
        int y = xy[1];

        float ease = (float)(1.0 - Math.pow(1.0 - coordsAnimationProgress, 3));
        int bgColor = (int)(ease * config.blockCoordsOpacity * 255) << 24;
        int borderColor = (int)(ease * config.blockCoordsOpacity * 150) << 24 | config.getAccentRGB();
        int textColor = (int)(ease * 255) << 24 | 0xFFFFFF;
        int shadowColor = (int)(ease * 100) << 24;

        context.fill(x - 1, y - 1, x + panelW + 1, y + panelH + 1, borderColor);
        context.fill(x, y, x + panelW, y + panelH, bgColor);

        int tx = x + bgPad;
        int ty = y + bgPad;
        drawShadowedText(context, tr, coordText, tx, ty, textColor, shadowColor);

        if (showWaypoint) {
            int wy = ty + tr.fontHeight + 2;
            int waypointColor = (int)(ease * 255) << 24 | 0xFFFF00;
            drawShadowedText(context, tr, waypointText, tx, wy, waypointColor, shadowColor);
        }

        OverlayStackManager.registerHeight(pos, panelH);
    }

    @Unique
    private void maybeRenderEntityOverlay(DrawContext context, float tickDelta, ModConfiguration config, OverlayPosition pos) {
        if (!config.showEntityOverlay) return;
        if (!config.showEntityNames && !config.showEntityHeldItems) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        float fovMultiplier = ZoomController.getCurrentFOVMultiplier(tickDelta);
        float zoomLevel = 1.0f / fovMultiplier;
        boolean aboveThreshold = zoomLevel >= (float) config.minZoomForEntityDecorations;

        Entity target = aboveThreshold ? getTargetedEntity(client, tickDelta) : null;

        if (target != null) {
            entityAnimationProgress = Math.min(1.0f, entityAnimationProgress + 0.15f);
            lastTargetedEntity = target;
        } else {
            entityAnimationProgress = Math.max(0.0f, entityAnimationProgress - 0.1f);
        }

        if (entityAnimationProgress <= 0.0f) return;

        Entity renderTarget = (target != null) ? target : lastTargetedEntity;
        if (renderTarget == null) return;

        renderEntityPanel(context, renderTarget, entityAnimationProgress, config, client, pos);
    }

    @Unique
    private Entity getTargetedEntity(MinecraftClient client, float tickDelta) {
        assert client.player != null;
        Vec3d camera = client.player.getCameraPosVec(tickDelta);
        Vec3d look = client.player.getRotationVec(tickDelta);
        Vec3d end = camera.add(look.multiply(256.0));
        Box searchBox = new Box(camera, end).expand(2.0);

        assert client.world != null;
        List<Entity> candidates = client.world.getOtherEntities(
                client.player, searchBox,
                e -> e instanceof LivingEntity && !e.isSpectator()
        );

        Entity closest = null;
        double closestDist = Double.MAX_VALUE;

        for (Entity entity : candidates) {
            Box hitbox = entity.getBoundingBox().expand(0.3);
            Optional<Vec3d> hit = hitbox.raycast(camera, end);
            if (hit.isEmpty()) continue;

            // we don't want ESP hacks in a zoom mod!!
            // i'd use normal methods like BlockHitResult, but it should ignore transparent blocks
            boolean blocked = false;
            Vec3d direction = hit.get().subtract(camera).normalize();
            double distance = camera.distanceTo(hit.get());

            for (double d = 0; d < distance; d += 0.1) {
                Vec3d pos2 = camera.add(direction.multiply(d));
                BlockPos bp = BlockPos.ofFloored(pos2);
                BlockState state = client.world.getBlockState(bp);
                if (!state.isAir() && state.isOpaque()) { blocked = true; break; }
            }
            if (blocked) continue;

            double dist = camera.squaredDistanceTo(hit.get());
            if (dist < closestDist) { closestDist = dist; closest = entity; }
        }

        return closest;
    }

    @Unique
    private void renderEntityPanel(DrawContext context, Entity entity, float animProgress, ModConfiguration config, MinecraftClient client, OverlayPosition pos) {
        TextRenderer tr = client.textRenderer;
        float ease = (float)(1.0 - Math.pow(1.0 - animProgress, 3));

        String entityName = config.showEntityNames
                ? Objects.requireNonNull(entity.getDisplayName()).getString()
                : null;

        String healthText = null;
        if (config.showEntityHealth && entityName != null && entity instanceof LivingEntity lh) {
            healthText = "♥" + (int) Math.ceil(lh.getHealth());
        }

        String mobTypeLabel = (config.showMobTypes && entity instanceof LivingEntity)
                ? getMobTypeLabel(entity) : null;

        ItemStack heldItem = ItemStack.EMPTY;
        List<EnchantRow> enchants = new ArrayList<>();

        if (config.showEntityHeldItems && entity instanceof LivingEntity living) {
            ItemStack main = living.getMainHandStack();
            if (!main.isEmpty()) {
                heldItem = main;
                var enc = main.getEnchantments();
                for (RegistryEntry<Enchantment> entry : enc.getEnchantments()) {
                    enchants.add(new EnchantRow(
                            Enchantment.getName(entry, enc.getLevel(entry)).getString(),
                            enchantColorFor(entry)
                    ));
                }
            }
        }

        if (entityName == null && mobTypeLabel == null && heldItem.isEmpty()) return;

        final int PADDING = 7;
        final int LINE_H = tr.fontHeight + 3;
        final int ITEM_ROW_H = 18;
        final int ITEM_ICON_W = 16;
        final int ITEM_GAP = 4;

        int contentW = 90;
        if (entityName != null) {
            int nameLineW = tr.getWidth(entityName);
            if (healthText != null) nameLineW += 4 + tr.getWidth(healthText);
            contentW = Math.max(contentW, nameLineW);
        }
        if (mobTypeLabel != null) contentW = Math.max(contentW, tr.getWidth(mobTypeLabel));
        if (!heldItem.isEmpty()) {
            contentW = Math.max(contentW, ITEM_ICON_W + ITEM_GAP + tr.getWidth(heldItem.getName().getString()));
            for (EnchantRow e : enchants)
                contentW = Math.max(contentW, ITEM_ICON_W + ITEM_GAP + tr.getWidth(e.label()));
        }

        boolean hasItem = !heldItem.isEmpty();
        boolean hasEnchants = !enchants.isEmpty();
        int lineGap = LINE_H - tr.fontHeight;

        int panelH = PADDING * 2;
        if (entityName != null) panelH += LINE_H;
        if (mobTypeLabel != null) panelH += LINE_H;
        if (hasItem || hasEnchants) {
            if (entityName != null || mobTypeLabel != null) panelH += 4;
            if (hasItem) panelH += ITEM_ROW_H;
            if (hasEnchants) panelH += enchants.size() * LINE_H;
        }
        if (hasEnchants) panelH -= lineGap;
        else if (!hasItem && (mobTypeLabel != null || entityName != null)) panelH -= lineGap;

        int panelW = contentW + PADDING * 2;

        int screenW = context.getScaledWindowWidth();
        int screenH = context.getScaledWindowHeight();

        int[] xy = OverlayStackManager.computePosition(pos, panelW, panelH, screenW, screenH, OVERLAY_MARGIN);
        int panelX = xy[0];
        int panelY = xy[1];

        int bgColor = (int)(ease * config.entityOverlayOpacity * 255) << 24;
        int borderColor = (int)(ease * config.entityOverlayOpacity * 180) << 24 | config.getAccentRGB();
        int shadowColor = (int)(ease * 100) << 24;

        context.fill(panelX - 1, panelY - 1, panelX + panelW + 1, panelY + panelH + 1, borderColor);
        context.fill(panelX, panelY, panelX + panelW, panelY + panelH, bgColor);

        int cx = panelX + PADDING;
        int cy = panelY + PADDING;

        if (entityName != null) {
            int nameColor = (int)(ease * 255) << 24 | 0xFFFFFF;
            drawShadowedText(context, tr, entityName, cx, cy, nameColor, shadowColor);
            if (healthText != null) {
                int healthColor = (int)(ease * 255) << 24 | 0xFF5555;
                drawShadowedText(context, tr, healthText, cx + tr.getWidth(entityName) + 4, cy, healthColor, shadowColor);
            }
            cy += LINE_H;
        }

        if (mobTypeLabel != null) {
            int typeColor = (int)(ease * 255) << 24 | 0xAAAAAA;
            drawShadowedText(context, tr, mobTypeLabel, cx, cy, typeColor, shadowColor);
            cy += LINE_H;
        }

        if ((hasItem || hasEnchants) && (entityName != null || mobTypeLabel != null)) {
            int sepColor = (int)(ease * 100) << 24 | config.getAccentRGB();
            context.fill(cx, cy, cx + contentW, cy + 1, sepColor);
            cy += 4;
        }

        if (hasItem) {
            context.drawItem(heldItem, cx, cy + 1);
            int rarityHex = getRarityColor(heldItem);
            int itemColor = (int)(ease * 255) << 24 | rarityHex;
            int itemTextX = cx + ITEM_ICON_W + ITEM_GAP;
            int itemTextY = cy + (ITEM_ROW_H / 2) - (tr.fontHeight / 2);
            drawShadowedText(context, tr, heldItem.getName().getString(), itemTextX, itemTextY, itemColor, shadowColor);
            cy += ITEM_ROW_H;
        }

        for (EnchantRow enchant : enchants) {
            int enchColor = (int)(ease * 255) << 24 | enchant.color();
            drawShadowedText(context, tr, enchant.label(), cx + ITEM_ICON_W + ITEM_GAP, cy, enchColor, shadowColor);
            cy += LINE_H;
        }

        OverlayStackManager.registerHeight(pos, panelH);
    }

    @Unique
    private void renderProgressBar(DrawContext context, float zoomLevel, float animProgress, int topOffset, ModConfiguration config) {
        int screenWidth = context.getScaledWindowWidth();
        int barWidth = 120, barHeight = 3;
        int barX = (screenWidth - barWidth) / 2;
        int barY = 15 + topOffset;

        float fillPercent = MathHelper.clamp((zoomLevel - 1.0f) / 99.0f, 0.0f, 1.0f);
        float easeProgress = (float)(1.0 - Math.pow(1.0 - animProgress, 3));

        int bgColor = (int)(easeProgress * 100) << 24;
        int fillColor = (int)(easeProgress * 200) << 24 | config.getAccentRGB();
        int borderColor = (int)(easeProgress * 180) << 24 | config.getSecondaryRGB();

        context.fill(barX - 1, barY - 1, barX + barWidth + 1, barY + barHeight + 1, borderColor);
        context.fill(barX, barY, barX + barWidth, barY + barHeight, bgColor);
        context.fill(barX, barY, barX + (int)(barWidth * fillPercent), barY + barHeight, fillColor);

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer tr = client.textRenderer;
        String zoomText = String.format("%.0fx", zoomLevel);
        int textWidth = tr.getWidth(zoomText);
        int textX = (screenWidth - textWidth) / 2;
        int textY = barY + barHeight + 4;
        int textColor = (int)(easeProgress * 255) << 24 | 0xFFFFFF;
        int shadowColor = (int)(easeProgress * 120) << 24;

        context.drawText(tr, zoomText, textX + 1, textY + 1, shadowColor, false);
        context.drawText(tr, zoomText, textX, textY, textColor, false);
    }

    @Unique
    private void renderWindow(DrawContext context, float zoomLevel, float animProgress, int topOffset, ModConfiguration config) {
        int screenWidth = context.getScaledWindowWidth();
        float easeProgress = (float)(1.0 - Math.pow(1.0 - animProgress, 3));

        int outerWidth = 140, outerHeight = 100;
        int centerX = screenWidth / 2;
        int topY = 10 + topOffset;

        int outerLeftX = centerX - outerWidth / 2;
        int outerRightX = centerX + outerWidth / 2;
        int outerBottomY = topY + outerHeight;

        float zoomPercent = MathHelper.clamp((zoomLevel - 1.0f) / 99.0f, 0.0f, 1.0f);
        int minInnerWidth = 20, minInnerHeight = 14;
        int innerWidth = (int)(outerWidth - (outerWidth - minInnerWidth) * zoomPercent);
        int innerHeight = (int)(outerHeight - (outerHeight - minInnerHeight) * zoomPercent);
        int innerLeftX = centerX - innerWidth / 2;
        int innerRightX = centerX + innerWidth / 2;
        int innerTopY = topY + (outerHeight - innerHeight) / 2;
        int innerBottomY = innerTopY + innerHeight;

        int outerColor = (int)(easeProgress * 150) << 24 | 0xFFFFFF;
        int innerColor = (int)(easeProgress * 200) << 24 | config.getAccentRGB();
        int ft = 2;

        context.fill(outerLeftX - ft, topY - ft, outerRightX + ft, topY, outerColor);
        context.fill(outerLeftX - ft, outerBottomY, outerRightX + ft, outerBottomY + ft, outerColor);
        context.fill(outerLeftX - ft, topY, outerLeftX, outerBottomY, outerColor);
        context.fill(outerRightX, topY, outerRightX + ft, outerBottomY, outerColor);

        context.fill(innerLeftX - 1, innerTopY - 1, innerRightX + 1, innerTopY, innerColor);
        context.fill(innerLeftX - 1, innerBottomY, innerRightX + 1, innerBottomY + 1, innerColor);
        context.fill(innerLeftX - 1, innerTopY, innerLeftX, innerBottomY, innerColor);
        context.fill(innerRightX, innerTopY, innerRightX + 1, innerBottomY, innerColor);

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer tr = client.textRenderer;
        String zoomText = String.format("%.0fx", zoomLevel);
        int textWidth = tr.getWidth(zoomText);
        int textX = centerX - textWidth / 2;
        int textY = outerBottomY + ft + 3;
        int textColor = (int)(easeProgress * 255) << 24 | 0xFFFFFF;
        int shadowColor = (int)(easeProgress * 120) << 24;

        context.drawText(tr, zoomText, textX + 1, textY + 1, shadowColor, false);
        context.drawText(tr, zoomText, textX, textY, textColor, false);
    }

    @Unique
    private void renderMinimal(DrawContext context, float zoomLevel, float animProgress, int topOffset) {
        int screenWidth = context.getScaledWindowWidth();
        float easeProgress = (float)(1.0 - Math.pow(1.0 - animProgress, 3));

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer tr = client.textRenderer;
        String zoomText = String.format("%.0fx", zoomLevel);
        int textWidth = tr.getWidth(zoomText);
        int textX = (screenWidth - textWidth) / 2;
        int textY = 15 + topOffset;
        int textColor = (int)(easeProgress * 255) << 24 | 0xFFFFFF;
        int shadowColor = (int)(easeProgress * 120) << 24;

        context.drawText(tr, zoomText, textX + 1, textY + 1, shadowColor, false);
        context.drawText(tr, zoomText, textX, textY, textColor, false);
    }

    @Unique
    private static void drawShadowedText(DrawContext ctx, TextRenderer tr, String text, int x, int y, int color, int shadowColor) {
        ctx.drawText(tr, text, x + 1, y + 1, shadowColor, false);
        ctx.drawText(tr, text, x, y, color, false);
    }

    @Unique
    private static int getRarityColor(ItemStack stack) {
        return switch (stack.getRarity()) {
            case UNCOMMON -> 0xFFFF55;
            case RARE -> 0x55FFFF;
            case EPIC -> 0xFF55FF;
            default -> 0xFFFFFF;
        };
    }

    // bad enchants get bad colours! attune more to the game
    @Unique
    private static int enchantColorFor(RegistryEntry<Enchantment> entry) {
        return entry.isIn(EnchantmentTags.CURSE) ? 0xFF5555 : 0xBB86FC;
    }

    // this is really useless, but the more content the better.. yay!
    @Unique
    private static String getMobTypeLabel(Entity entity) {
        if (entity instanceof PlayerEntity) return "Player";
        if (entity instanceof Monster) return "Not Friendly";
        if (entity instanceof AnimalEntity) return "Friendly";
        if (entity instanceof MobEntity) return "Neutral";
        return null;
    }

    @Unique
    private static int getBossBarOffset(MinecraftClient client) {
        try {
            BossBarHud bossBarHud = client.inGameHud.getBossBarHud();
            var field = BossBarHud.class.getDeclaredField("bossBars");
            field.setAccessible(true);
            int count = ((java.util.Map<?, ?>) field.get(bossBarHud)).size();
            if (count == 0) return 0;
            int maxHeight = client.getWindow().getScaledHeight() / 3;
            return Math.min(count * 19, maxHeight);
        } catch (Exception e) {
            return 0;
        }
    }
}