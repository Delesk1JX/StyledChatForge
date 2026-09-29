package dev.delesk1jx.styledchat.vendor.placeholders.api.node;

import dev.delesk1jx.styledchat.vendor.placeholders.api.ParserContext;
import net.minecraft.network.chat.Component;

public record EmptyNode() implements TextNode {
   public static final EmptyNode INSTANCE = new EmptyNode();

   @Override
   public Component toText(ParserContext context, boolean removeBackslashes) {
      return Component.empty();
   }
}
