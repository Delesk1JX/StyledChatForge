package dev.delesk1jx.styledchat.vendor.placeholders.api.node;

import dev.delesk1jx.styledchat.vendor.placeholders.api.ParserContext;
import net.minecraft.network.chat.Component;

public record ScoreNode(String name, String objective) implements TextNode {
   @Override
   public Component toText(ParserContext context, boolean removeBackslashes) {
      return Component.score(this.name, this.objective);
   }
}
