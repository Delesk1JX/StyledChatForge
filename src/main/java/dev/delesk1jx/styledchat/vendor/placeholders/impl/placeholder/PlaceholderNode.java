package dev.delesk1jx.styledchat.vendor.placeholders.impl.placeholder;

import dev.delesk1jx.styledchat.vendor.placeholders.api.ParserContext;
import dev.delesk1jx.styledchat.vendor.placeholders.api.PlaceholderContext;
import dev.delesk1jx.styledchat.vendor.placeholders.api.PlaceholderHandler;
import dev.delesk1jx.styledchat.vendor.placeholders.api.Placeholders;
import dev.delesk1jx.styledchat.vendor.placeholders.api.node.TextNode;
import dev.delesk1jx.styledchat.vendor.placeholders.impl.GeneralUtils;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public record PlaceholderNode(
   ParserContext.Key<PlaceholderContext> contextKey,
   String placeholder,
   Placeholders.PlaceholderGetter getter,
   boolean optionalContext,
   @Nullable String argument
) implements TextNode {
   @Override
   public Component toText(ParserContext context, boolean removeBackslashes) {
      PlaceholderContext ctx = context.get(this.contextKey);
      PlaceholderHandler handler = this.getter.getPlaceholder(this.placeholder, context);
      if ((ctx != null || this.optionalContext) && handler != null) {
         try {
            return handler.onPlaceholderRequest(ctx, this.argument).text();
         } catch (Throwable var6) {
            GeneralUtils.LOGGER.error("Error occurred while parsing placeholder " + this.placeholder + " / " + this.contextKey.key() + "!", var6);
            return Component.empty();
         }
      } else {
         if (GeneralUtils.IS_DEV) {
            GeneralUtils.LOGGER
               .error("Missing context for placeholders requiring them (" + this.placeholder + " / " + this.contextKey.key() + ")!", new NullPointerException());
         }

         return Component.empty();
      }
   }

   @Override
   public boolean isDynamic() {
      return true;
   }
}
