package cc.folia.justtp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

final class FabricConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("justtp.json");

    private FabricConfig() {
    }

    static void load() {
        ConfigData data = read();
        apply(data);
        write(data);
    }

    private static ConfigData read() {
        if (!Files.exists(CONFIG_PATH)) {
            return new ConfigData();
        }
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            ConfigData data = GSON.fromJson(reader, ConfigData.class);
            return data == null ? new ConfigData() : data;
        } catch (IOException | JsonSyntaxException e) {
            return new ConfigData();
        }
    }

    private static void write(ConfigData data) {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException ignored) {
        }
    }

    private static void apply(ConfigData data) {
        JustTPConfig.enableCoordinateTp = data.enableCoordinateTp;
        JustTPConfig.tpMessageMode = parseMode(data.tpMessageMode);
    }

    private static JustTPConfig.TpMessageMode parseMode(String raw) {
        if (raw == null) {
            return JustTPConfig.TpMessageMode.ALL;
        }
        try {
            return JustTPConfig.TpMessageMode.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return JustTPConfig.TpMessageMode.ALL;
        }
    }

    private static final class ConfigData {
        boolean enableCoordinateTp = true;
        String tpMessageMode = "ALL";
    }
}
