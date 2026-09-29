package eu.pb4.styledchat.mixin;

import eu.pb4.styledchat.ducks.ExtSignedMessage;
import java.util.function.Consumer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.PlayerChatMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin({MessageArgument.class})
public class MessageArgumentTypeMixin {
   public MessageArgumentTypeMixin() {
   }

   @ModifyArg(
      method = {"resolveChatMessage"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/commands/arguments/MessageArgument;resolveSignedMessage(Ljava/util/function/Consumer;Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/network/chat/PlayerChatMessage;)V"
      ),
      index = 2
   )
   private static PlayerChatMessage styledChat_attachSource(Consumer<PlayerChatMessage> callback, CommandSourceStack source, PlayerChatMessage message) {
      ExtSignedMessage.of(message).styledChat_setSource(source);
      return message;
   }

   @ModifyArg(
      method = {"resolveChatMessage"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/commands/arguments/MessageArgument;resolveDisguisedMessage(Ljava/util/function/Consumer;Lnet/minecraft/commands/CommandSourceStack;Lnet/minecraft/network/chat/PlayerChatMessage;)V"
      ),
      index = 2
   )
   private static PlayerChatMessage styledChat_attachSource2(Consumer<PlayerChatMessage> callback, CommandSourceStack source, PlayerChatMessage message) {
      ExtSignedMessage.of(message).styledChat_setSource(source);
      return message;
   }
}
