package eu.pb4.styledchat.mixin;

import eu.pb4.styledchat.StyledChatMod;
import eu.pb4.styledchat.StyledChatUtils;
import eu.pb4.styledchat.ducks.ExtSignedMessage;
import eu.pb4.styledchat.other.ExtendedSentMessage;
import eu.pb4.styledchat.other.StyledChatSentMessage;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies chat styles to a normal (signed) outgoing message.
 *
 * Vanilla builds {@code new OutgoingChatMessage.Player(message)} for anything that is not a system
 * message. The Fabric build replaced that object from the {@code of} factory; Mixin cannot inject
 * into an interface, so the styling is applied here instead, from the message the record already
 * holds.
 */
@Mixin({OutgoingChatMessage.Player.class})
public class OutgoingPlayerChatMessageMixin implements ExtendedSentMessage {
   @Shadow
   @Final
   private PlayerChatMessage message;

   @Unique
   private Component styledChat_override = null;
   @Unique
   private ChatType.Bound styledChat_parameters = null;
   @Unique
   private ResourceKey<ChatType> styledChat_sourceType = null;
   @Unique
   private MutableObject<ChatType.Bound> styledChat_colorless = null;

   public OutgoingPlayerChatMessageMixin() {
   }

   @Inject(
      method = {"<init>"},
      at = {@At("TAIL")}
   )
   private void styledChat$patchStyle(PlayerChatMessage message, CallbackInfo ci) {
      if (this.message == null) {
         this.message = message;
      }

      Component override = ((ExtSignedMessage)(Object)message).styledChat_getArg("override");
      if (override != StyledChatUtils.EMPTY_TEXT && StyledChatMod.server != null) {
         this.styledChat_override = override;
         this.styledChat_parameters = StyledChatUtils.createParameters(override);
         this.styledChat_sourceType = ((ExtSignedMessage)(Object)message).styledChat_getType();
         this.styledChat_colorless = new MutableObject<>();
      }
   }

   @Inject(
      method = {"sendToPlayer"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void styledChat$sendStyled(ServerPlayer receiver, boolean filterMaskEnabled, ChatType.Bound params, CallbackInfo ci) {
      if (this.styledChat_override != null) {
         StyledChatSentMessage.sendChat(
               receiver,
               this.message,
               this.styledChat_override,
               this.styledChat_parameters,
               this.styledChat_sourceType,
               this.styledChat_colorless,
               filterMaskEnabled,
               params
         );
         ci.cancel();
      }
   }

   @Nullable
   @Override
   public PlayerChatMessage styledChat$message() {
      return this.message;
   }
}
