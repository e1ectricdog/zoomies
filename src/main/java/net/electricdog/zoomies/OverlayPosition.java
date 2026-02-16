package net.electricdog.zoomies;

public enum OverlayPosition {
    TOP_LEFT("Top Left"),
    TOP_RIGHT("Top Right"),
    CENTER_LEFT("Center Left"),
    CENTER_RIGHT("Center Right"),
    BOTTOM_LEFT("Bottom Left"),
    BOTTOM_RIGHT("Bottom Right");

    private final String displayName;

    OverlayPosition(String displayName) {
        this.displayName = displayName;
    }

    public boolean isBottom() {
        return this == BOTTOM_LEFT || this == BOTTOM_RIGHT;
    }

    public boolean isTop() {
        return this == TOP_LEFT || this == TOP_RIGHT;
    }

    public boolean isLeft() {
        return this == TOP_LEFT || this == CENTER_LEFT || this == BOTTOM_LEFT;
    }

    @Override
    public String toString() {
        return displayName;
    }
}