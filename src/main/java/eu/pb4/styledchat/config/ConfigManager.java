package eu.pb4.styledchat.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.pb4.loader.Platform;
import eu.pb4.predicate.api.GsonPredicateSerializer;
import eu.pb4.predicate.api.MinecraftPredicate;
import eu.pb4.styledchat.StyledChatMod;
import eu.pb4.styledchat.config.data.ConfigData;
import eu.pb4.styledchat.config.data.VersionConfigData;
import eu.pb4.styledchat.config.data.old.ConfigDataV2;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
   public static final Gson GSON = new GsonBuilder()
      .setPrettyPrinting()
      .disableHtmlEscaping()
      .setLenient()
      .registerTypeHierarchyAdapter(MinecraftPredicate.class, GsonPredicateSerializer.INSTANCE)
      .create();
   private static Config config = null;
   private static ConfigData configData = null;

   public ConfigManager() {
   }

   public static Config getConfig() {
      if (config == null) {
         if (configData == null) {
            loadConfig();
         }

         config = new Config(configData);
      }

      return config;
   }

   public static void clearCached() {
      config = null;
   }

   public static boolean loadConfig() {
      ConfigManager.config = null;

      try {
         Path configFile = Platform.CONFIG_DIR.resolve("styled-chat.json");
         ConfigData config;
         if (Files.exists(configFile)) {
            String json = Files.readString(configFile, StandardCharsets.UTF_8);
            VersionConfigData versionConfigData = (VersionConfigData)GSON.fromJson(json, VersionConfigData.class);
            if (versionConfigData.version < 3) {
               config = ((ConfigDataV2)GSON.fromJson(json, ConfigDataV2.class)).update();
               Files.writeString(Platform.CONFIG_DIR.resolve("styled-chat.json_old_v2"), json, StandardCharsets.UTF_8);
            } else {
               config = (ConfigData)GSON.fromJson(json, ConfigData.class);
            }

            config.defaultStyle.fillMissing();
         } else {
            config = new ConfigData();
         }

         Files.writeString(configFile, GSON.toJson(config), StandardCharsets.UTF_8);
         configData = config;
         return true;
      } catch (Exception var4) {
         StyledChatMod.LOGGER.error("Something went wrong while reading config! Make sure format is correct!");
         var4.printStackTrace();
         if (configData == null) {
            configData = new ConfigData();
         }

         return false;
      }
   }

   public static JsonObject loadJson(String key) {
      Path path = Platform.CONFIG_DIR.resolve(key);
      if (Files.exists(path)) {
         try {
            return JsonParser.parseReader(Files.newBufferedReader(path)).getAsJsonObject();
         } catch (Throwable var3) {
            var3.printStackTrace();
         }
      }

      return new JsonObject();
   }

   /**
    * Loads a JSON file shipped inside this jar. Forge mods are ordinary jars, so the file is read
    * off the classpath rather than through a loader-provided resource path.
    */
   public static JsonObject loadJsonBuiltin(String baseValue) {
      return Platform.openModResource(StyledChatMod.MOD_ID, "emoji/" + baseValue + ".json").map(stream -> {
         try (InputStream is = stream; Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
         } catch (Exception var3) {
            var3.printStackTrace();
            return new JsonObject();
         }
      }).orElseGet(JsonObject::new);
   }
}
