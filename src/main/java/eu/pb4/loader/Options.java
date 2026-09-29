package eu.pb4.loader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

/**
 * Per-player option storage, the part of lucko's options API the chat styles actually rely on.
 *
 * Chat styles let a player override an individual formatting property with an option, so the value
 * belongs to the player rather than to the server config. Lucko stored this in its own database;
 * here it is a small JSON file per player under {@code config/styledchat/options}, which keeps the
 * same call sites working without pulling in a permissions mod.
 */
public final class Options {
    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();
    private static final Map<UUID, JsonObject> CACHE = new HashMap<>();

    private Options() {
    }

    public static Optional<String> get(CommandSourceStack source, String option) {
        ServerPlayer player = source.getPlayer();
        return player == null ? Optional.empty() : get(player.getUUID(), option);
    }

public static Optional<String> get(UUID player, String option) {
        JsonObject data = load(player);
        return data.has(option) && !data.get(option).isJsonNull()
              ? Optional.of(data.get(option).getAsString())
              : Optional.empty();
     }

    public static void set(UUID player, String option, String value) {
        JsonObject data = load(player);
        data.addProperty(option, value);
        save(player, data);
    }

public static void clear(CommandSourceStack source, String option) {
        ServerPlayer player = source.getPlayer();

        if (player != null) {
            JsonObject data = load(player.getUUID());
            data.remove(option);
            save(player.getUUID(), data);
        }
     }

    private static synchronized JsonObject load(UUID player) {
        return CACHE.computeIfAbsent(player, id -> {
            Path path = pathFor(id);

            if (!Files.exists(path)) {
                return new JsonObject();
            }

            try {
                return JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
            } catch (Exception e) {
                LOGGER.warn("Could not read options for {}", id, e);
                return new JsonObject();
            }
        });
    }

    private static synchronized void save(UUID player, JsonObject data) {
        Path path = pathFor(player);

        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, data.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.warn("Could not write options for {}", player, e);
        }
    }

    private static Path pathFor(UUID player) {
        return Platform.CONFIG_DIR.resolve("styledchat").resolve("options").resolve(player + ".json");
    }
}
