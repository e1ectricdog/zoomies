package net.electricdog.zoomies;

public enum ZoomUIStyle {
    PROGRESS_BAR("Progress Bar"),
    WINDOW("Window"),
    MINIMAL("Minimal"),
    NONE("None");

    private final String displayName;

    ZoomUIStyle(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}