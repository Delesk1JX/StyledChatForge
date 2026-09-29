package eu.pb4.styledchat;

import java.util.List;
import eu.pb4.loader.Platform;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.playerdata.api.PlayerDataApi;
import eu.pb4.styledchat.config.ConfigManager;
import eu.pb4.styledchat.other.GenericModInfo;
import net.minecraft.network.chat.ChatType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(StyledChatMod.MOD_ID)
public class StyledChatMod {
   public static final Logger LOGGER = LogManager.getLogger("Styled Chat");
   public static final String MOD_ID = "styledchatforge";

   /**
    * Shown as the contributor list in {@code /about} and {@code /server brand}. Forge's
    * {@code IModInfo} does not expose the authors field, so this cannot be read back from the
    * metadata; keep it in step with {@code mod_authors} in gradle.properties.
    */
   public static final List<String> CONTRIBUTORS = List.of("Patbox (original author)", "Delesk1JX (Forge port)");

   public static MinecraftServer server = null;
   public static boolean USE_FABRIC_API = true;
   public static ResourceKey<ChatType> MESSAGE_TYPE_ID = ResourceKey.create(Registries.CHAT_TYPE, new ResourceLocation("styled_chat", "generic_hack"));

   public StyledChatMod() {
      // Server lifecycle and command registration are game events, so they belong on the Forge bus
      // rather than the mod event bus. Forge also hands us the dispatcher directly, which is why
      // the port needs no mixin into the Commands constructor the way the Fabric build did.
      MinecraftForge.EVENT_BUS.addListener(this::onServerStarting);
      MinecraftForge.EVENT_BUS.addListener(this::onServerStopped);
      MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);

      GenericModInfo.build();
      PlayerDataApi.register(StyledChatUtils.PLAYER_DATA);
      Placeholders.registerChangeEvent((id, removed) -> ConfigManager.clearCached());
   }

   public static ChatType getMessageType() {
      return (ChatType)server.registryAccess().registryOrThrow(Registries.CHAT_TYPE).get(MESSAGE_TYPE_ID);
   }

   private void onServerStarting(ServerStartingEvent event) {
      serverStarting(event.getServer());
   }

   private void onServerStopped(ServerStoppedEvent event) {
      serverStopped(event.getServer());
   }

   private void onRegisterCommands(RegisterCommandsEvent event) {
      eu.pb4.styledchat.command.Commands.register(
            event.getDispatcher(), event.getBuildContext(), event.getCommandSelection()
      );
   }

   public static void serverStarting(MinecraftServer s) {
      ConfigManager.loadConfig();
      server = s;
   }

   public static void serverStopped(MinecraftServer s) {
      server = null;
   }
}
