package cc.folia.justtp;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
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
                .requires(source -> true);

        var targetArg = Commands.argument("target", EntityArgument.player())
                .executes(ctx -> teleportSelfToPlayer(ctx.getSource(), getPlayerArg(ctx, "target")));

        var selfTarget = Commands.literal("@s")
                .executes(ctx -> teleportSelfToPlayer(ctx.getSource(), ctx.getSource().getPlayerOrException()));

        if (Config.ENABLE_COORDINATE_TP.get()) {
            tp = tp.then(Commands.argument("pos", Vec3Argument.vec3())
                    .executes(ctx -> teleportSelfToCoordinates(ctx.getSource(), Vec3Argument.getVec3(ctx, "pos"))));
            targetArg = targetArg.then(Commands.argument("pos", Vec3Argument.vec3())
                    .executes(ctx -> teleportTargetsToCoordinates(ctx.getSource(),
                            getPlayerArg(ctx, "target"),
                            Vec3Argument.getVec3(ctx, "pos"))));
            selfTarget = selfTarget.then(Commands.argument("pos", Vec3Argument.vec3())
                    .executes(ctx -> teleportSelfToCoordinates(ctx.getSource(), Vec3Argument.getVec3(ctx, "pos"))));
        }

        targetArg = targetArg.then(Commands.literal("@s")
                .executes(ctx -> teleportTargetsToPlayer(ctx.getSource(),
                        getPlayerArg(ctx, "target"),
                        ctx.getSource().getPlayerOrException())));
        targetArg = targetArg.then(Commands.argument("destination", EntityArgument.player())
                .executes(ctx -> teleportTargetsToPlayer(ctx.getSource(),
                        getPlayerArg(ctx, "target"),
                        getPlayerArg(ctx, "destination"))));

        selfTarget = selfTarget.then(Commands.argument("destination", EntityArgument.player())
                .executes(ctx -> teleportTargetsToPlayer(ctx.getSource(),
                        ctx.getSource().getPlayerOrException(),
                        getPlayerArg(ctx, "destination"))));
        selfTarget = selfTarget.then(Commands.literal("@s")
                .executes(ctx -> teleportTargetsToPlayer(ctx.getSource(),
                        ctx.getSource().getPlayerOrException(),
                        ctx.getSource().getPlayerOrException())));

        tp = tp.then(targetArg).then(selfTarget);

        dispatcher.register(tp);
    }

    private static int teleportSelfToPlayer(CommandSourceStack source, ServerPlayer destination) throws CommandSyntaxException {
        if (destination == null) {
            return 0;
        }
        if (!(source.getEntity() instanceof ServerPlayer)) {
            source.sendFailure(Component.translatable("justtp.error.only_player"));
            return 0;
        }
        return teleportTargetsToPlayer(source, source.getPlayerOrException(), destination);
    }

    private static int teleportSelfToCoordinates(CommandSourceStack source, Vec3 pos) throws CommandSyntaxException {
        if (!Config.enableCoordinateTp) {
            source.sendFailure(Component.translatable("justtp.error.coordinates_disabled"));
            return 0;
        }
        if (!(source.getEntity() instanceof ServerPlayer)) {
            source.sendFailure(Component.translatable("justtp.error.only_player"));
            return 0;
        }
        ServerPlayer self = source.getPlayerOrException();
        return teleportTargetsToCoordinates(source, self, pos);
    }

    private static int teleportTargetsToPlayer(CommandSourceStack source, ServerPlayer target, ServerPlayer destination) {
        if (target == null || destination == null) {
            return 0;
        }
        ServerLevel targetLevel = destination.serverLevel();
        Vec3 pos = destination.position();
        target.teleportTo(targetLevel, pos.x, pos.y, pos.z, target.getYRot(), target.getXRot());
        sendTpPlayerMessages(source, target, destination);
        return 1;
    }

    private static int teleportTargetsToCoordinates(CommandSourceStack source, ServerPlayer target, Vec3 pos) {
        if (!Config.enableCoordinateTp) {
            source.sendFailure(Component.translatable("justtp.error.coordinates_disabled"));
            return 0;
        }
        if (target == null) {
            return 0;
        }
        ServerLevel level = target.serverLevel();
        target.teleportTo(level, pos.x, pos.y, pos.z, target.getYRot(), target.getXRot());
        sendTpCoordinatesMessages(source, target, pos);
        return 1;
    }

    private static void sendTpPlayerMessages(CommandSourceStack source, ServerPlayer moved, ServerPlayer destination) {
        Component sourceMessage = Component.translatable("justtp.message.tp.source", moved.getDisplayName(), destination.getDisplayName());
        Component targetMessage = Component.translatable("justtp.message.tp.target", moved.getDisplayName());
        sendTpMessage(source, sourceMessage, () -> {
            if (destination != source.getEntity()) {
                destination.sendSystemMessage(targetMessage);
            }
        });
    }

    private static void sendTpCoordinatesMessages(CommandSourceStack source, ServerPlayer moved, Vec3 pos) {
        String x = String.format(Locale.ROOT, "%.2f", pos.x);
        String y = String.format(Locale.ROOT, "%.2f", pos.y);
        String z = String.format(Locale.ROOT, "%.2f", pos.z);
        Component message = Component.translatable("justtp.message.tp.coordinates", moved.getDisplayName(), x, y, z);
        sendTpMessage(source, message, () -> {
            if (moved != source.getEntity()) {
                moved.sendSystemMessage(message);
            }
        });
    }

    private static void sendTpMessage(CommandSourceStack source, Component message, Runnable bothExtra) {
        switch (Config.tpMessageMode) {
            case OFF -> {
            }
            case BOTH -> {
                source.sendSuccess(() -> message, false);
                bothExtra.run();
            }
            case ALL -> source.getServer().getPlayerList().broadcastSystemMessage(message, false);
        }
    }

    private static ServerPlayer getPlayerArg(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
        String raw = getRawArgument(ctx, name);
        if (raw.startsWith("@") && !raw.equals("@s")) {
            ctx.getSource().sendFailure(Component.translatable("argument.entity.notfound.player"));
            return null;
        }
        return EntityArgument.getPlayer(ctx, name);
    }

    private static String getRawArgument(CommandContext<CommandSourceStack> ctx, String name) {
        for (ParsedCommandNode<CommandSourceStack> node : ctx.getNodes()) {
            if (node.getNode() instanceof ArgumentCommandNode<?, ?> arg && arg.getName().equals(name)) {
                var range = node.getRange();
                return ctx.getInput().substring(range.getStart(), range.getEnd());
            }
        }
        return "";
    }
}
