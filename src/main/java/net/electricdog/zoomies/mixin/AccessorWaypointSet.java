package net.electricdog.zoomies.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.hud.minimap.waypoint.set.WaypointSet;

import java.util.List;

@Mixin(WaypointSet.class)
public interface AccessorWaypointSet {
    @Accessor("list")
    List<Waypoint> getList();
}