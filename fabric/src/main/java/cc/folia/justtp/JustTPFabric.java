package cc.folia.justtp;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class JustTPFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FabricConfig.load();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                JustTPCommands.register(dispatcher));
    }
}
