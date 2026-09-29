package dev.delesk1jx.styledchat.vendor.placeholders.api.node;

import dev.delesk1jx.styledchat.vendor.placeholders.api.ParserContext;
import net.minecraft.network.chat.Component;

public record KeybindNode(String value) implements TextNode {
   @Override
   public Component toText(ParserContext context, boolean removeBackslashes) {
      return Component.keybind(this.value());
   }
}
