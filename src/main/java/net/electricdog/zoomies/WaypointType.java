package net.electricdog.zoomies;

public enum WaypointType {
    NORMAL("Normal"),
    DESTINATION("Destination");

    private final String displayName;

    WaypointType(String displayName) {
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