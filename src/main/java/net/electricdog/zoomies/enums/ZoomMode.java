package net.electricdog.zoomies.enums;

public enum ZoomMode {
    HOLD("Hold"),
    TOGGLE("Toggle");

    private final String displayName;

    ZoomMode(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}