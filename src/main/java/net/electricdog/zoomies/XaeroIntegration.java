package net.electricdog.zoomies;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.BuiltInHudModules;
import xaero.hud.minimap.module.MinimapSession;
import xaero.hud.minimap.waypoint.set.WaypointSet;
import xaero.hud.minimap.world.MinimapWorld;
import net.electricdog.zoomies.mixin.AccessorWaypointSet;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

// Big credit to rfresh2's XaeroPlus because couldn't do it without https://github.com/rfresh2/XaeroPlus/blob/1.21.11/common/src/main/java/xaeroplus/feature/waypoint/WaypointAPI.java

public class XaeroIntegration {

    private static final Logger LOGGER = LoggerFactory.getLogger("zoomies");
    private static Boolean xaerosMinimapLoaded = null;
    private static final String WAYPOINT_SET_NAME = "gui.xaero_default";
    private static final AtomicInteger waypointCounter = new AtomicInteger(1);

    public static boolean isXaerosMinimapLoaded() {
        if (xaerosMinimapLoaded == null) {
            xaerosMinimapLoaded = FabricLoader.getInstance().isModLoaded("xaerominimap");
            if (xaerosMinimapLoaded) {
                LOGGER.info("Xaero's Minimap detected!");
            }
        }
        return xaerosMinimapLoaded;
    }

    public static boolean createWaypoint(BlockPos pos, String name, WaypointType type) {
        if (!isXaerosMinimapLoaded()) {
            return false;
        }

        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.world == null) {
                LOGGER.warn("Cannot create waypoint: world is null");
                return false;
            }

            RegistryKey<World> dimension = client.world.getRegistryKey();

            MinimapWorld minimapWorld = getMinimapWorld(dimension);
            if (minimapWorld == null) {
                return false;
            }

            WaypointSet waypointSet = getOrCreateWaypointSet(minimapWorld, WAYPOINT_SET_NAME);
            if (waypointSet == null) {
                return false;
            }

            if (waypointExists(waypointSet, pos)) {
                return true;
            }

            Waypoint waypoint = new Waypoint(
                    pos.getX(),
                    pos.getY(),
                    pos.getZ(),
                    name,
                    generateInitials(name),
                    getColorForType(type),
                    getTypeId(type),
                    isDestination(type)
            );

            ((AccessorWaypointSet) (Object) waypointSet).getList().add(waypoint);

            LOGGER.info("Added waypoint '{}' at {}, {}, {}", name, pos.getX(), pos.getY(), pos.getZ());
            return true;

        } catch (Exception e) {
            LOGGER.error("Failed to create waypoint", e);
            return false;
        }
    }

    private static MinimapWorld getMinimapWorld(RegistryKey<World> dim) {
        MinimapSession minimapSession = BuiltInHudModules.MINIMAP.getCurrentSession();
        if (minimapSession == null) return null;

        MinimapWorld currentWorld = minimapSession.getWorldManager().getCurrentWorld();
        if (currentWorld == null) return null;

        if (currentWorld.getDimId() == dim) {
            return currentWorld;
        }

        var rootContainer = minimapSession.getWorldManager().getCurrentRootContainer();
        for (MinimapWorld world : rootContainer.getWorlds()) {
            if (world.getDimId() == dim) {
                return world;
            }
        }

        return null;
    }

    private static WaypointSet getOrCreateWaypointSet(MinimapWorld minimapWorld, String setName) {
        WaypointSet waypointSet = minimapWorld.getWaypointSet(setName);
        if (waypointSet == null) {
            minimapWorld.addWaypointSet(setName);
            waypointSet = minimapWorld.getWaypointSet(setName);
        }
        return waypointSet;
    }

    private static boolean waypointExists(WaypointSet waypointSet, BlockPos pos) {
        List<Waypoint> waypoints = ((AccessorWaypointSet) (Object) waypointSet).getList();
        for (Waypoint waypoint : waypoints) {
            if (waypoint.getX() == pos.getX() &&
                    waypoint.getY() == pos.getY() &&
                    waypoint.getZ() == pos.getZ()) {
                return true;
            }
        }
        return false;
    }

    private static String generateInitials(String name) {
        if (name == null || name.isEmpty()) {
            return "W";
        }

        String[] words = name.trim().split("\\s+");
        if (words.length > 0 && !words[0].isEmpty()) {
            return words[0].substring(0, 1).toUpperCase();
        }
        return "W";
    }

    private static int getColorForType(WaypointType type) {
        return switch (type) {
            case DESTINATION -> 9;
            case NORMAL -> 5;
        };
    }

    private static int getTypeId(WaypointType type) {
        return switch (type) {
            case DESTINATION -> 3;
            case NORMAL -> 0;
        };
    }

    private static boolean isDestination(WaypointType type) {
        return type == WaypointType.DESTINATION;
    }

    public static String generateWaypointName(BlockPos pos) {
        return String.format("Zoom %d", waypointCounter.getAndIncrement());
    }
}