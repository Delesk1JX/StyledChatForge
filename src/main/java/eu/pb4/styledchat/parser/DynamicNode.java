package eu.pb4.styledchat.parser;

import dev.delesk1jx.styledchat.vendor.placeholders.api.ParserContext;
import dev.delesk1jx.styledchat.vendor.placeholders.api.ParserContext.Key;
import dev.delesk1jx.styledchat.vendor.placeholders.api.node.TextNode;
import java.util.Map;
import net.minecraft.network.chat.Component;

public record DynamicNode(String key, Component text) implements TextNode {
   public static final Key<Map<String, Component>> NODES = new Key("styled_chat:dynamic", null);

   public static DynamicNode of(String key) {
      return new DynamicNode(key, Component.literal("${" + key + "}"));
   }

   // The registry is an erased Map, so the lookup result has to be re-typed at the boundary.
   @SuppressWarnings("unchecked")
   public Component toText(ParserContext context, boolean removeBackslashes) {
      return (Component)((Map<String, Component>)context.get(NODES)).getOrDefault(this.key, this.text);
   }

   public boolean isDynamic() {
      return true;
   }
}
