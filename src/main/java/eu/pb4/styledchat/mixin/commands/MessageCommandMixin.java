package eu.pb4.styledchat.mixin.commands;

import eu.pb4.styledchat.StyledChatMod;
import eu.pb4.styledchat.StyledChatStyles;
import eu.pb4.styledchat.StyledChatUtils;
import eu.pb4.styledchat.ducks.ExtSignedMessage;
import eu.pb4.styledchat.other.ExtendedSentMessage;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.MsgCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.ChatType.Bound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({MsgCommand.class})
public class MessageCommandMixin {
   public MessageCommandMixin() {
   }

   @Inject(
      method = {"sendMessage"},
      at = {@At("HEAD")}
   )
   private static void styledChat_formatOgText(CommandSourceStack serverCommandSource, Collection<ServerPlayer> collection, PlayerChatMessage signedMessage, CallbackInfo ci) {
      Component input = StyledChatUtils.maybeFormatFor(serverCommandSource, signedMessage.signedContent(), signedMessage.decoratedContent());
      ExtSignedMessage.setArg(signedMessage, "base_input", input);
   }

   @Redirect(
      method = {"sendMessage"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/commands/CommandSourceStack;sendChatMessage(Lnet/minecraft/network/chat/OutgoingChatMessage;ZLnet/minecraft/network/chat/ChatType$Bound;)V"
      )
   )
   private static void styledChat_noopFeedback(CommandSourceStack instance, OutgoingChatMessage message, boolean bl, Bound parameters) {
   }

   @Redirect(
      method = {"sendMessage"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/server/level/ServerPlayer;sendChatMessage(Lnet/minecraft/network/chat/OutgoingChatMessage;ZLnet/minecraft/network/chat/ChatType$Bound;)V"
      )
   )
   private static void styledChat_formatText(ServerPlayer instance, OutgoingChatMessage message, boolean bl, Bound parameters, CommandSourceStack source) {
      if (message instanceof ExtendedSentMessage extSentMessage) {
         try {
            Bound sent = StyledChatMod.getMessageType()
               .bind(
                  StyledChatStyles.getPrivateMessageSent(
                     source.getDisplayName(),
                     instance.getDisplayName(),
                     ExtSignedMessage.getArg(extSentMessage.styledChat$message(), "base_input"),
                     instance.createCommandSourceStack()
                  )
               );
            source.sendChatMessage(message, bl, sent);
            Bound rex = StyledChatMod.getMessageType()
               .bind(
                  StyledChatStyles.getPrivateMessageReceived(
                     source.getDisplayName(), instance.getDisplayName(), ExtSignedMessage.getArg(extSentMessage.styledChat$message(), "base_input"), source
                  )
               );
            instance.sendChatMessage(message, bl, rex);
            return;
         } catch (Exception var8) {
            var8.printStackTrace();
         }
      }

      instance.sendChatMessage(message, bl, parameters);
   }
}
