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

/**
 * Applies chat styles to a disguised (system / command) outgoing message.
 *
 * Vanilla builds {@code new OutgoingChatMessage.Disguised(message.decoratedContent())}, which keeps
 * only the rendered component. The originating {@link PlayerChatMessage} is looked back up through
 * {@link StyledChatSentMessage.Index}, which the {@code PlayerChatMessage} mixin fills in.
 */
@Mixin({OutgoingChatMessage.Disguised.class})
public class OutgoingDisguisedChatMessageMixin implements ExtendedSentMessage {
   @Shadow
   @Final
   private Component content;

   @Unique
   private Component styledChat_override = null;
   @Unique
   private ChatType.Bound styledChat_parameters = null;
   @Unique
   private ResourceKey<ChatType> styledChat_sourceType = null;
   @Unique
   private MutableObject<ChatType.Bound> styledChat_colorless = null;
   @Unique
   private PlayerChatMessage styledChat_message = null;

   public OutgoingDisguisedChatMessageMixin() {
   }

   @Inject(
      method = {"sendToPlayer"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void styledChat$sendStyled(ServerPlayer receiver, boolean filterMaskEnabled, ChatType.Bound params, CallbackInfo ci) {
      PlayerChatMessage message = this.styledChat$resolve();
      if (message == null || StyledChatMod.server == null) {
         return;
      }

      if (this.styledChat_override == null) {
         Component override = ((ExtSignedMessage)(Object)message).styledChat_getArg("override");

         if (override != StyledChatUtils.EMPTY_TEXT) {
            this.styledChat_override = override;
            this.styledChat_parameters = StyledChatUtils.createParameters(override);
            this.styledChat_sourceType = ((ExtSignedMessage)(Object)message).styledChat_getType();
            this.styledChat_colorless = new MutableObject<>();
         } else {
            return;
         }
      }

      StyledChatSentMessage.sendSystem(
            receiver,
            message,
            this.styledChat_override,
            this.styledChat_parameters,
            this.styledChat_sourceType,
            this.styledChat_colorless,
            params
      );
      ci.cancel();
   }

   @Unique
   private PlayerChatMessage styledChat$resolve() {
      if (this.styledChat_message == null) {
         this.styledChat_message = StyledChatSentMessage.Index.lookup(this.content);
      }

      return this.styledChat_message;
   }

   @Nullable
   @Override
   public PlayerChatMessage styledChat$message() {
      return this.styledChat$resolve();
   }
}
