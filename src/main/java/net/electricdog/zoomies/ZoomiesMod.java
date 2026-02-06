package net.electricdog.zoomies;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZoomiesMod implements ModInitializer {

    public static final String MOD_ID = "zoomies";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Zoomies mod initializing...");
    }
}