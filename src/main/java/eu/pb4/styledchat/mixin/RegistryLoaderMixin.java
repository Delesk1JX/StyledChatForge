package eu.pb4.styledchat.mixin;

import com.mojang.datafixers.util.Pair;
import eu.pb4.styledchat.StyledChatMod;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.network.chat.ChatType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.ChatTypeDecoration;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.RegistryAccess.Frozen;
import net.minecraft.resources.RegistryDataLoader.RegistryData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin({RegistryDataLoader.class})
public class RegistryLoaderMixin {
   public RegistryLoaderMixin() {
   }

   @Inject(
      method = {"load"},
      at = {@At(
         value = "INVOKE",
         target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V",
         ordinal = 0,
         shift = Shift.AFTER
      )},
      locals = LocalCapture.CAPTURE_FAILEXCEPTION
   )
   private static void styledChat$injectMessageTypes(
      ResourceManager resourceManager,
      RegistryAccess baseRegistryManager,
      List<RegistryData<?>> entries,
      CallbackInfoReturnable<Frozen> cir,
      Map _unused,
      List<Pair<WritableRegistry<?>, Object>> list
   ) {
      for (Pair<WritableRegistry<?>, Object> pair : list) {
         WritableRegistry<?> reg = (WritableRegistry<?>)pair.getFirst();
         if (reg.key().equals(Registries.CHAT_TYPE)) {
            registerChatType(reg);
         }
      }
   }

   // Erasure makes the raw register call equivalent to the typed one; the registry is already
   // known to be the chat type registry by the caller.
   @SuppressWarnings({"unchecked", "rawtypes"})
   private static void registerChatType(WritableRegistry<?> registry) {
      Registry.register((WritableRegistry)registry, StyledChatMod.MESSAGE_TYPE_ID,
            new ChatType(ChatTypeDecoration.withSender("%s"), ChatTypeDecoration.withSender("%s")));
   }
}
