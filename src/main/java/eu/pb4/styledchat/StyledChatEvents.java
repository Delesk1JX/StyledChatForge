package eu.pb4.styledchat;

import eu.pb4.loader.SimpleEvent;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.parsers.TextParserV1;
import net.minecraft.commands.CommandSourceStack;

public class StyledChatEvents {
   public static final SimpleEvent<PreMessageEvent> PRE_MESSAGE_CONTENT = new SimpleEvent<>(
      callbacks -> (message, player) -> {
         for (PreMessageEvent callback : callbacks) {
            message = callback.onPreMessage(message, player);
         }

         return message;
      }
   );
   public static final SimpleEvent<MessageEvent> MESSAGE_CONTENT = new SimpleEvent<>(
      callbacks -> (message, player) -> {
         for (MessageEvent callback : callbacks) {
            message = callback.onMessage(message, player);
         }

         return message;
      }
   );
   public static final SimpleEvent<FormattingCreationEvent> FORMATTING_CREATION_EVENT = new SimpleEvent<>(
      callbacks -> (player, builder) -> {
         for (FormattingCreationEvent callback : callbacks) {
            callback.onFormattingBuild(player, builder);
         }
      }
   );

   private StyledChatEvents() {
   }

   public interface FormattingCreationEvent {
      void onFormattingBuild(CommandSourceStack var1, TextParserV1 var2);
   }

   public interface MessageEvent {
      TextNode onMessage(TextNode var1, PlaceholderContext var2);
   }

   public interface PreMessageEvent {
      String onPreMessage(String var1, PlaceholderContext var2);
   }
}
