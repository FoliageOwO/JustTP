package cc.folia.justtp;

public final class JustTPConfig {
    public static boolean enableCoordinateTp = true;
    public static TpMessageMode tpMessageMode = TpMessageMode.ALL;

    private JustTPConfig() {
    }

    public enum TpMessageMode {
        OFF,
        BOTH,
        ALL
    }
}
