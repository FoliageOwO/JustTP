package cc.folia.justtp;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = JustTP.MODID)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_COORDINATE_TP = BUILDER
            .comment("Whether /tp supports coordinates")
            .define("enableCoordinateTp", true);

    public static final ModConfigSpec.EnumValue<TpMessageMode> TP_MESSAGE_MODE = BUILDER
            .comment("Teleport message mode: OFF, BOTH, ALL")
            .defineEnum("tpMessageMode", TpMessageMode.ALL);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean enableCoordinateTp = true;
    public static TpMessageMode tpMessageMode = TpMessageMode.ALL;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        enableCoordinateTp = ENABLE_COORDINATE_TP.get();
        tpMessageMode = TP_MESSAGE_MODE.get();
    }

    public enum TpMessageMode {
        OFF,
        BOTH,
        ALL
    }
}
