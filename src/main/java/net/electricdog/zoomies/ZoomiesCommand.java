package net.electricdog.zoomies;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;

public class ZoomiesCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(
                ClientCommandManager.literal("zoomies")
                        .executes(context -> {
                            MinecraftClient client = context.getSource().getClient();

                            client.execute(() -> client.setScreen(ConfigurationScreen.create(client.currentScreen)));

                            return 1;
                        })
        );
    }
}