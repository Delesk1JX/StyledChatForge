package eu.pb4.styledchat.mixin;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.styledchat.StyledChatMod;
import eu.pb4.styledchat.StyledChatStyles;
import eu.pb4.styledchat.StyledChatUtils;
import eu.pb4.styledchat.config.ChatStyle;
import eu.pb4.styledchat.config.ConfigManager;
import eu.pb4.styledchat.ducks.ExtPlayNetworkHandler;
import java.util.concurrent.CompletableFuture;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.ChatDecorator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ServerGamePacketListenerImpl.class})
public abstract class ServerPlayNetworkManagerMixin implements ExtPlayNetworkHandler {
   @Shadow
   public ServerPlayer player;
   @Unique
   private ChatStyle styledChat$style;
   @Unique
   private boolean styledChat$chatColors = true;

   public ServerPlayNetworkManagerMixin() {
   }

   @ModifyArg(
      method = {"onDisconnect"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/server/players/PlayerList;broadcastSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"
      )
   )
   private Component styledChat_replaceDisconnectMessage(Component text) {
      return StyledChatStyles.getLeft(this.player);
   }

   @Redirect(
      method = {"lambda$handleChat$10"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/network/chat/ChatDecorator;decorate(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/Component;)Ljava/util/concurrent/CompletableFuture;"
      )
   )
   private CompletableFuture<Component> styledChat_replaceDecorator2(ChatDecorator instance, ServerPlayer player, Component text) {
      return player != null
         ? CompletableFuture.completedFuture(StyledChatUtils.formatFor(PlaceholderContext.of(player), text.getString()))
         : CompletableFuture.completedFuture(StyledChatUtils.formatFor(PlaceholderContext.of(StyledChatMod.server), text.getString()));
   }

   @Inject(
      method = {"broadcastChatMessage"},
      at = {@At("HEAD")}
   )
   private void styledChat_setFormattedMessage(PlayerChatMessage signedMessage, CallbackInfo ci) {
      StyledChatUtils.modifyForSending(signedMessage, this.player.createCommandSourceStack(), ChatType.CHAT);
   }

   @Inject(
      method = {"handleClientInformation"},
      at = {@At("TAIL")}
   )
   private void styledChat_setFormattedMessage(ServerboundClientInformationPacket packet, CallbackInfo ci) {
      this.styledChat$chatColors = packet.chatColors();
   }

   @Override
   public ChatStyle styledChat$getStyle() {
      if (this.styledChat$style == null) {
         this.styledChat$style = StyledChatUtils.createStyleOf(this.player);
      }

      return this.styledChat$style;
   }

   @Override
   public void styledChat$setStyle(ChatStyle style) {
      this.styledChat$style = style;
   }

   @Override
   public boolean styledChat$chatColors() {
      return this.styledChat$chatColors || !ConfigManager.getConfig().configData.formatting.respectColors;
   }
}
