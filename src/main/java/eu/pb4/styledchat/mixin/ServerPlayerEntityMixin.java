package eu.pb4.styledchat.mixin;

import eu.pb4.styledchat.StyledChatStyles;
import eu.pb4.styledchat.StyledChatUtils;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ServerPlayer.class})
public abstract class ServerPlayerEntityMixin {
   @Shadow
   @Final
   public MinecraftServer server;

   public ServerPlayerEntityMixin() {
   }

   @Redirect(
      method = {"die"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/damagesource/CombatTracker;getDeathMessage()Lnet/minecraft/network/chat/Component;"
      )
   )
   private Component styledChat$replaceDeathMessage(CombatTracker instance) {
      return StyledChatStyles.getDeath((ServerPlayer)(Object) this, instance.getDeathMessage());
   }

   @Inject(
      method = {"sendSystemMessage(Lnet/minecraft/network/chat/Component;Z)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void styledChat$excludeSendingOfHiddenMessages(Component message, boolean ignore, CallbackInfo ci) {
      if (message == StyledChatUtils.IGNORED_TEXT) {
         ci.cancel();
      }
   }
}
