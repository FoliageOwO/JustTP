package cc.folia.justtp;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Locale;

@EventBusSubscriber
@Mod(JustTP.MODID)
public class JustTP {
    public static final String MODID = "justtp";

    public JustTP(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();
        dispatcher.getRoot().getChildren().removeIf(node ->
                node.getName().equals("tp")
        );
        var tp = Commands.literal("tp")
                .requires(source -> true)
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> teleportToPlayer(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"))));

        if (Config.ENABLE_COORDINATE_TP.get()) {
            tp = tp.then(Commands.argument("pos", Vec3Argument.vec3())
                    .executes(ctx -> teleportToCoordinates(ctx.getSource(), Vec3Argument.getVec3(ctx, "pos"))));
        }

        dispatcher.register(tp);
    }

    private static int teleportToPlayer(CommandSourceStack source, ServerPlayer target) throws CommandSyntaxException {
        if (!(source.getEntity() instanceof ServerPlayer)) {
            source.sendFailure(Component.translatable("justtp.error.only_player"));
            return 0;
        } else {
            ServerPlayer self = source.getPlayerOrException();
            ServerLevel targetLevel = target.serverLevel();
            Vec3 pos = target.position();
            self.teleportTo(targetLevel, pos.x, pos.y, pos.z, self.getYRot(), self.getXRot());
            sendTpMessages(self, target);
            return 1;
        }
    }

    private static int teleportToCoordinates(CommandSourceStack source, Vec3 pos) throws CommandSyntaxException {
        if (!Config.enableCoordinateTp) {
            source.sendFailure(Component.translatable("justtp.error.coordinates_disabled"));
            return 0;
        }
        if (!(source.getEntity() instanceof ServerPlayer)) {
            source.sendFailure(Component.translatable("justtp.error.only_player"));
            return 0;
        }
        ServerPlayer self = source.getPlayerOrException();
        ServerLevel level = self.serverLevel();
        self.teleportTo(level, pos.x, pos.y, pos.z, self.getYRot(), self.getXRot());
        sendTpCoordinatesMessages(self, pos);
        return 1;
    }

    private static void sendTpMessages(ServerPlayer source, ServerPlayer target) {
        Component sourceMessage = Component.translatable("justtp.message.tp.source", source.getDisplayName(), target.getDisplayName());
        Component targetMessage = Component.translatable("justtp.message.tp.target", source.getDisplayName(), target.getDisplayName());

        switch (Config.tpMessageMode) {
            case OFF -> {
            }
            case BOTH -> {
                source.sendSystemMessage(sourceMessage, false);
                if (!source.getUUID().equals(target.getUUID())) {
                    target.sendSystemMessage(targetMessage);
                }
            }
            case ALL -> source.server.getPlayerList().broadcastSystemMessage(sourceMessage, false);
        }
    }

    private static void sendTpCoordinatesMessages(ServerPlayer source, Vec3 pos) {
        String x = String.format(Locale.ROOT, "%.2f", pos.x);
        String y = String.format(Locale.ROOT, "%.2f", pos.y);
        String z = String.format(Locale.ROOT, "%.2f", pos.z);
        Component message = Component.translatable("justtp.message.tp.coordinates", source.getDisplayName(), x, y, z);

        switch (Config.tpMessageMode) {
            case OFF -> {
            }
            case BOTH -> source.sendSystemMessage(message, false);
            case ALL -> source.server.getPlayerList().broadcastSystemMessage(message, false);
        }
    }
}
