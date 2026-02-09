package cc.folia.justtp;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = JustTPConstants.MODID)
public class NeoForgeConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_COORDINATE_TP = BUILDER
            .comment("Whether /tp supports coordinates")
            .define("enableCoordinateTp", true);

    public static final ModConfigSpec.EnumValue<JustTPConfig.TpMessageMode> TP_MESSAGE_MODE = BUILDER
            .comment("Teleport message mode: OFF, BOTH, ALL")
            .defineEnum("tpMessageMode", JustTPConfig.TpMessageMode.ALL);

    static final ModConfigSpec SPEC = BUILDER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        JustTPConfig.enableCoordinateTp = ENABLE_COORDINATE_TP.get();
        JustTPConfig.tpMessageMode = TP_MESSAGE_MODE.get();
    }
}
