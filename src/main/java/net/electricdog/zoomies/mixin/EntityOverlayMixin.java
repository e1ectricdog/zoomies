package net.electricdog.zoomies.mixin;

import net.electricdog.zoomies.ModConfiguration;
import net.electricdog.zoomies.ZoomController;
import net.electricdog.zoomies.util.EnchantRow;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
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
import net.minecraft.util.math.Box;
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
public class EntityOverlayMixin {

    @Unique
    private float entityAnimationProgress = 0.0f;

    @Unique
    private Entity lastTargetedEntity = null;

    @Inject(method = "render", at = @At("TAIL"))
    private void renderEntityOverlay(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        ModConfiguration config = ModConfiguration.get();

        if (!config.showEntityNames && !config.showEntityHeldItems) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        float tickDelta = tickCounter.getFixedDeltaTicks();
        float fovMultiplier = ZoomController.getCurrentFOVMultiplier(tickDelta);
        float zoomLevel = 1.0f / fovMultiplier;

        boolean aboveThreshold = zoomLevel >= (float) config.minZoomForEntityDecorations;

        Entity target = aboveThreshold
                ? getTargetedEntity(client, tickDelta)
                : null;

        boolean shouldShow = target != null;

        if (shouldShow) {
            entityAnimationProgress = Math.min(1.0f, entityAnimationProgress + 0.15f);
            lastTargetedEntity = target;
        } else {
            entityAnimationProgress = Math.max(0.0f, entityAnimationProgress - 0.1f);
        }

        if (entityAnimationProgress <= 0.0f) return;

        Entity renderTarget = shouldShow ? target : lastTargetedEntity;
        if (renderTarget == null) return;

        renderEntityPanel(context, renderTarget, entityAnimationProgress, config, client);
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
                client.player,
                searchBox,
                e -> e instanceof LivingEntity && !e.isSpectator()
        );

        Entity closest = null;
        double closestDist = Double.MAX_VALUE;

        for (Entity entity : candidates) {
            Box hitbox = entity.getBoundingBox().expand(0.3);
            Optional<Vec3d> hit = hitbox.raycast(camera, end);
            if (hit.isPresent()) {
                double dist = camera.squaredDistanceTo(hit.get());
                if (dist < closestDist) {
                    closestDist = dist;
                    closest = entity;
                }
            }
        }

        return closest;
    }

    @Unique
    private void renderEntityPanel(DrawContext context, Entity entity, float animProgress, ModConfiguration config, MinecraftClient client) {

        TextRenderer textRenderer = client.textRenderer;
        float ease = (float) (1.0 - Math.pow(1.0 - animProgress, 3));


        String entityName = null;
        String mobTypeLabel = null;
        ItemStack heldItem = ItemStack.EMPTY;
        List<EnchantRow> enchants = new ArrayList<>();

        if (config.showEntityNames) {
            entityName = Objects.requireNonNull(entity.getDisplayName()).getString();
        }

        if (config.showMobTypes && entity instanceof LivingEntity) {
            mobTypeLabel = getMobTypeLabel(entity);
        }

        if (config.showEntityHeldItems && entity instanceof LivingEntity living) {
            ItemStack main = living.getMainHandStack();
            if (!main.isEmpty()) {
                heldItem = main;
                var enchantmentsComp = main.getEnchantments();
                for (RegistryEntry<Enchantment> entry : enchantmentsComp.getEnchantments()) {
                    int level = enchantmentsComp.getLevel(entry);
                    String label = Enchantment.getName(entry, level).getString();
                    enchants.add(new EnchantRow(label, enchantColorFor(entry)));
                }
            }
        }

        final int PADDING = 7;
        final int LINE_H = textRenderer.fontHeight + 3;
        final int ITEM_ROW_H = 18;
        final int ITEM_ICON_W = 16;
        final int ITEM_GAP = 4;

        int contentWidth = 90;

        if (entityName != null) {
            contentWidth = Math.max(contentWidth, textRenderer.getWidth(entityName));
        }
        if (mobTypeLabel != null) {
            contentWidth = Math.max(contentWidth, textRenderer.getWidth(mobTypeLabel));
        }
        if (!heldItem.isEmpty()) {
            int itemNameW = ITEM_ICON_W + ITEM_GAP + textRenderer.getWidth(heldItem.getName().getString());
            contentWidth = Math.max(contentWidth, itemNameW);
            for (EnchantRow e : enchants) {
                int enchW = ITEM_ICON_W + ITEM_GAP + textRenderer.getWidth(e.label());
                contentWidth = Math.max(contentWidth, enchW);
            }
        }

        int panelH = PADDING * 2;

        if (entityName != null)   panelH += LINE_H;
        if (mobTypeLabel != null) panelH += LINE_H;

        boolean hasItem = !heldItem.isEmpty();
        boolean hasEnchants = !enchants.isEmpty();

        if (hasItem || hasEnchants) {
            if (entityName != null || mobTypeLabel != null) panelH += 4;
            if (hasItem)    panelH += ITEM_ROW_H;
            if (hasEnchants) panelH += enchants.size() * LINE_H;
        }

        int panelW = contentWidth + PADDING * 2;

        int screenHeight = context.getScaledWindowHeight();

        int panelX = 10;
        int panelY = screenHeight / 2 - panelH / 2;
        panelY = Math.max(10, Math.min(panelY, screenHeight - panelH - 10));

        int bgColor = (int) (ease * 150) << 24;
        int borderColor = (int) (ease * 180) << 24 | 0x00FFE6;

        context.fill(panelX - 1, panelY - 1,
                panelX + panelW + 1, panelY + panelH + 1, borderColor);
        context.fill(panelX, panelY,
                panelX + panelW, panelY + panelH, bgColor);

        int shadowColor = (int) (ease * 100) << 24;
        int cx = panelX + PADDING;
        int cy = panelY + PADDING;

        if (entityName != null) {
            int nameColor = (int) (ease * 255) << 24 | 0xFFFFFF;
            drawShadowedText(context, textRenderer, entityName, cx, cy, nameColor, shadowColor);
            cy += LINE_H;
        }

        if (mobTypeLabel != null) {
            int typeColor = (int) (ease * 255) << 24 | 0xAAAAAA;
            drawShadowedText(context, textRenderer, mobTypeLabel, cx, cy, typeColor, shadowColor);
            cy += LINE_H;
        }

        if ((hasItem || hasEnchants) && (entityName != null || mobTypeLabel != null)) {
            int sepColor = (int) (ease * 100) << 24 | 0x00FFE6;
            context.fill(cx, cy, cx + contentWidth, cy + 1, sepColor);
            cy += 4;
        }

        if (hasItem) {
            context.drawItem(heldItem, cx, cy + 1);

            String itemName = heldItem.getName().getString();
            int    rarityHex = getRarityColor(heldItem);
            int    itemColor = (int) (ease * 255) << 24 | rarityHex;
            int    itemTextX = cx + ITEM_ICON_W + ITEM_GAP;
            int    itemTextY = cy + (ITEM_ROW_H / 2) - (textRenderer.fontHeight / 2);

            drawShadowedText(context, textRenderer, itemName, itemTextX, itemTextY, itemColor, shadowColor);
            cy += ITEM_ROW_H;
        }

        for (EnchantRow enchant : enchants) {
            int enchColor = (int) (ease * 255) << 24 | enchant.color();
            int enchTextX = cx + ITEM_ICON_W + ITEM_GAP;
            drawShadowedText(context, textRenderer, enchant.label(), enchTextX, cy, enchColor, shadowColor);
            cy += LINE_H;
        }
    }

    @Unique
    private static void drawShadowedText(DrawContext ctx, TextRenderer tr, String text, int x, int y, int color, int shadowColor) {
        ctx.drawText(tr, text, x + 1, y + 1, shadowColor, false);
        ctx.drawText(tr, text, x,     y,     color,       false);
    }

    @Unique
    private static int getRarityColor(ItemStack stack) {
        return switch (stack.getRarity()) {
            case UNCOMMON -> 0xFFFF55; // yellow
            case RARE     -> 0x55FFFF; // aqua
            case EPIC     -> 0xFF55FF; // light purple
            default       -> 0xFFFFFF; // white
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
}