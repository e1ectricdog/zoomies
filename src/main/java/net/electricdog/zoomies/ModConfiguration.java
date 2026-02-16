package net.electricdog.zoomies;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger("zoomies");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_FILE = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("zoomies-config.json");

    private static ModConfiguration instance;

    // alot of doubles don't need to be doubles

    public double zoomTransitionSpeed = 2.5;
    public double zoomScrollSensitivity = 1;
    public boolean normalizeMouseSensitivity = true;
    public double startingZoomAmount = 3;
    public boolean disableViewBobbing = false;
    public boolean smoothZooming = true;
    public boolean enableVignette = true;
    public double vignetteIntensity = 0.5;
    public String zoomUIStyle = "PROGRESS_BAR"; // PROGRESS_BAR, WINDOW, MINIMAL, NONE
    public float entityOverlayOpacity = 0.50f;

    public boolean showBlockCoordinates = true;
    public double minZoomForDecorations = 40.0;
    public boolean enableWaypointIntegration = true;
    public String waypointType = "NORMAL"; // NORMAL, DESTINATION
    public String blockCoordsPosition = "CENTER_LEFT"; // OverlayPosition enum
    public float blockCoordsOpacity = 0.50f;

    public boolean showEntityOverlay = true;
    public boolean showEntityNames = true;
    public boolean showEntityHealth = true;
    public boolean showEntityHeldItems = true;
    public boolean showMobTypes = false;
    public double minZoomForEntityDecorations = 10.0;
    public String entityOverlayPosition = "CENTER_LEFT"; // OverlayPosition enum

    public String accentColor = "#00FFE6";

    public int getAccentRGB() {
        return parseHexColor(accentColor) & 0x00FFFFFF;
    }

    public int getSecondaryRGB() {
        int rgb = getAccentRGB();
        float[] hsb = java.awt.Color.RGBtoHSB(
                (rgb >> 16) & 0xFF,
                (rgb >> 8)  & 0xFF,
                rgb        & 0xFF,
                null
        );
        float h = hsb[0];
        float s = Math.min(hsb[1] * 1.25f, 1.0f);
        float v = hsb[2] * 0.35f;
        return java.awt.Color.HSBtoRGB(h, s, v) & 0x00FFFFFF;
    }

    // YACL
    public java.awt.Color getAccentAwtColor() {
        return new java.awt.Color(getAccentRGB());
    }

    private static int parseHexColor(String hex) {
        if (hex == null) return 65510;
        String clean = hex.startsWith("#") ? hex.substring(1) : hex;
        try {
            long parsed = Long.parseLong(clean, 16);
            if (clean.length() == 6) return (int) (0xFF000000L | parsed);
            if (clean.length() == 8) return (int) parsed;
        } catch (NumberFormatException ignored) {}
        LOGGER.warn("Invalid colour value '{}', using default.", hex);
        return 65510;
    }

    public static void initialize() {
        if (Files.exists(CONFIG_FILE)) {
            try {
                String jsonContent = Files.readString(CONFIG_FILE);
                instance = GSON.fromJson(jsonContent, ModConfiguration.class);
                LOGGER.info("Configuration loaded successfully");
            } catch (IOException e) {
                LOGGER.error("Failed to load configuration, using defaults", e);
                instance = new ModConfiguration();
                persist();
            }
        } else {
            instance = new ModConfiguration();
            persist();
        }
    }

    public static void persist() {
        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            String jsonContent = GSON.toJson(instance);
            Files.writeString(CONFIG_FILE, jsonContent);
            LOGGER.info("Configuration saved");
        } catch (IOException e) {
            LOGGER.error("Failed to save configuration", e);
        }
    }

    public static ModConfiguration get() {
        return instance;
    }
}