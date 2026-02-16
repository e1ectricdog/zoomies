package net.electricdog.zoomies;

import java.util.EnumMap;
import java.util.Map;

public class OverlayStackManager {

    private static final int STACK_GAP = 6;
    private static final Map<OverlayPosition, Integer> usedHeights =
            new EnumMap<>(OverlayPosition.class);

    public static void reset() {
        usedHeights.clear();
    }

    public static int[] computePosition(OverlayPosition pos, int panelW, int panelH, int screenW, int screenH, int margin) {
        int stack = usedHeights.getOrDefault(pos, 0);
        return switch (pos) {
            case TOP_LEFT     -> new int[]{ margin,
                    margin + stack };
            case TOP_RIGHT    -> new int[]{ screenW - panelW - margin,
                    margin + stack };
            case CENTER_LEFT  -> new int[]{ margin,
                    (screenH - panelH) / 2 + stack };
            case CENTER_RIGHT -> new int[]{ screenW - panelW - margin,
                    (screenH - panelH) / 2 + stack };
            case BOTTOM_LEFT  -> new int[]{ margin,
                    screenH - panelH - margin - stack };
            case BOTTOM_RIGHT -> new int[]{ screenW - panelW - margin,
                    screenH - panelH - margin - stack };
        };
    }

    public static void registerHeight(OverlayPosition pos, int panelH) {
        usedHeights.merge(pos, panelH + STACK_GAP, Integer::sum);
    }
}