package eu.pb4.styledchat.other;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

import eu.pb4.styledchat.StyledChatMod;
import eu.pb4.styledchat.StyledChatStyles;
import eu.pb4.styledchat.StyledChatUtils;
import eu.pb4.styledchat.ducks.ExtPlayNetworkHandler;
import eu.pb4.styledchat.ducks.ExtSignedMessage;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.OutgoingChatMessage;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

/**
 * The styled replacement for a vanilla outgoing chat message.
 *
 * The upstream Fabric build swapped the object returned by {@code OutgoingChatMessage#of} wholesale.
 * Mixin cannot inject into an interface, and {@code OutgoingChatMessage} is one, so the port keeps
 * the vanilla implementations in place and instead hooks their {@code sendToPlayer}. The logic that
 * used to live in the swapped-out records lives here, and {@link #indexOf} recovers the
 * {@link PlayerChatMessage} a disguised message was built from.
 */
public interface StyledChatSentMessage extends OutgoingChatMessage, ExtendedSentMessage {
   Component override();

   PlayerChatMessage message();

   StyledChatSentMessage reformat(ChatType.Bound var1, ResourceKey<ChatType> var2);

   ResourceKey<ChatType> sourceType();

   @Override
   default PlayerChatMessage styledChat$message() {
      return this.message();
   }

   /**
    * Vanilla builds a {@link OutgoingChatMessage.Disguised} from
    * {@code message.decoratedContent()}, which drops the message it came from. Since the disguised
    * branch is the one used for command output, that link has to be recovered to reach the styling
    * arguments. Keys are weak so a finished message cannot pin its content component.
    *
    * <p>Keys use {@link Component} equality rather than identity. Two messages with byte-identical
    * content would share an entry, but they would also be formatted identically, so the result of a
    * collision is the same text.
    */
   final class Index {
      private static final Map<Component, PlayerChatMessage> BY_CONTENT =
            Collections.synchronizedMap(new WeakHashMap<>());

      private Index() {
      }

      public static void record(Component content, PlayerChatMessage message) {
         if (content != null && message != null) {
            BY_CONTENT.put(content, message);
         }
      }

      @Nullable
      public static PlayerChatMessage lookup(Component content) {
         return content == null ? null : BY_CONTENT.get(content);
      }
   }

   static void sendChat(
      ServerPlayer receiver,
      PlayerChatMessage message,
      Component override,
      ChatType.Bound parameters,
      ResourceKey<ChatType> sourceType,
      MutableObject<ChatType.Bound> colorless,
      boolean filterMaskEnabled,
      ChatType.Bound params
   ) {
      PlayerChatMessage signedMessage = message.filter(filterMaskEnabled);
      boolean color = ((ExtPlayNetworkHandler)receiver.connection).styledChat$chatColors();
      if (!color && colorless.getValue() == null) {
         colorless.setValue(StyledChatUtils.removeColor(parameters));
      }

      if (!signedMessage.isFullyFiltered()) {
         ResourceLocation id = receiver.server.registryAccess().registryOrThrow(Registries.CHAT_TYPE).getKey(params.chatType());
         if (sourceType != null && !Objects.equals(id, sourceType.location())) {
            Component baseInput = ExtSignedMessage.getArg(signedMessage, "base_input");
            CommandSourceStack source = ExtSignedMessage.of(signedMessage).styledChat_getSource();
            Component input = baseInput != StyledChatUtils.EMPTY_TEXT && baseInput.getContents() != ComponentContents.EMPTY
               ? baseInput
               : signedMessage.decoratedContent();
            Component text = StyledChatStyles.getCustom(
               id, params.name(), input, params.targetName(), source != null ? source : StyledChatMod.server.createCommandSourceStack()
            );
            if (!color) {
               text = StyledChatUtils.removeColor(text);
            }

            receiver.connection.sendPlayerChatMessage(signedMessage, StyledChatUtils.createParameters(text));
         } else {
            receiver.connection.sendPlayerChatMessage(signedMessage, color ? parameters : colorless.getValue());
         }
      }
   }

   static void sendSystem(
      ServerPlayer receiver,
      PlayerChatMessage message,
      Component override,
      ChatType.Bound parameters,
      ResourceKey<ChatType> sourceType,
      MutableObject<ChatType.Bound> colorless,
      ChatType.Bound params
   ) {
      boolean color = ((ExtPlayNetworkHandler)receiver.connection).styledChat$chatColors();
      if (!color && colorless.getValue() == null) {
         colorless.setValue(StyledChatUtils.removeColor(parameters));
      }

      ResourceLocation id = receiver.server.registryAccess().registryOrThrow(Registries.CHAT_TYPE).getKey(params.chatType());
      if (sourceType != null && !Objects.equals(id, sourceType.location())) {
         Component baseInput = ExtSignedMessage.getArg(message, "base_input");
         CommandSourceStack source = ExtSignedMessage.of(message).styledChat_getSource();
         Component input = baseInput != StyledChatUtils.EMPTY_TEXT && baseInput.getContents() != ComponentContents.EMPTY
            ? baseInput
            : message.decoratedContent();
         Component text = StyledChatStyles.getCustom(
            id, params.name(), input, params.targetName(), source != null ? source : StyledChatMod.server.createCommandSourceStack()
         );
         if (!color) {
            text = StyledChatUtils.removeColor(text);
         }

         receiver.connection.sendDisguisedChatMessage(message.decoratedContent(), StyledChatUtils.createParameters(text));
      } else {
         receiver.connection.sendDisguisedChatMessage(message.decoratedContent(), color ? parameters : colorless.getValue());
      }
   }

   public static record Chat(
      PlayerChatMessage message, Component override, ChatType.Bound parameters, ResourceKey<ChatType> sourceType, MutableObject<ChatType.Bound> colorless
   ) implements StyledChatSentMessage {
      public Component content() {
         return this.message.unsignedContent();
      }

      public void sendToPlayer(ServerPlayer receiver, boolean filterMaskEnabled, ChatType.Bound params) {
         sendChat(receiver, this.message, this.override, this.parameters, this.sourceType, this.colorless, filterMaskEnabled, params);
      }

      @Override
      public StyledChatSentMessage reformat(ChatType.Bound pars, ResourceKey<ChatType> sourceType) {
         return new StyledChatSentMessage.Chat(this.message, this.override, pars, sourceType, new MutableObject<>());
      }
   }

   public static record System(
      PlayerChatMessage message, Component override, ChatType.Bound parameters, ResourceKey<ChatType> sourceType, MutableObject<ChatType.Bound> colorless
   ) implements StyledChatSentMessage {
      public Component content() {
         return this.message.decoratedContent();
      }

      public void sendToPlayer(ServerPlayer receiver, boolean filterMaskEnabled, ChatType.Bound params) {
         sendSystem(receiver, this.message, this.override, this.parameters, this.sourceType, this.colorless, params);
      }

      @Override
      public StyledChatSentMessage reformat(ChatType.Bound pars, ResourceKey<ChatType> sourceType) {
         return new StyledChatSentMessage.Chat(this.message, this.override, pars, sourceType, new MutableObject<>());
      }
   }
}
