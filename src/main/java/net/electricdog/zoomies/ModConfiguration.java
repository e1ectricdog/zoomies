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

    public double zoomTransitionSpeed = 2.5;
    public boolean normalizeMouseSensitivity = true;
    public boolean disableViewBobbing = false;
    public boolean smoothZooming = true;
    public boolean enableVignette = true;
    public double vignetteIntensity = 0.5;
    public String zoomUIStyle = "PROGRESS_BAR"; // PROGRESS_BAR, WINDOW, MINIMAL, NONE

    public boolean showBlockCoordinates = true;
    public boolean enableWaypointIntegration = true;
    public String waypointType = "NORMAL"; // NORMAL, DESTINATION

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