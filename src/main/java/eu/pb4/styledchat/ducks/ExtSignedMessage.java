package eu.pb4.styledchat.ducks;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.PlayerChatMessage;
import org.jetbrains.annotations.Nullable;

public interface ExtSignedMessage {
   static Component getArg(PlayerChatMessage message, String name) {
      return ((ExtSignedMessage)(Object) message).styledChat_getArg(name);
   }

   static void setArg(PlayerChatMessage message, String name, Component value) {
      ((ExtSignedMessage)(Object) message).styledChat_setArg(name, value);
   }

   static ExtSignedMessage of(PlayerChatMessage message) {
      return (ExtSignedMessage)(Object) message;
   }

   void styledChat_setArg(String var1, Component var2);

   String styledChat_getOriginal();

   Component styledChat_getArg(String var1);

   void styledChat_setType(ResourceKey<ChatType> var1);

   @Nullable
   ResourceKey<ChatType> styledChat_getType();

   void styledChat_setSource(CommandSourceStack var1);

   @Nullable
   CommandSourceStack styledChat_getSource();
}
