package dev.delesk1jx.styledchat.vendor.placeholders.api.node.parent;

import dev.delesk1jx.styledchat.vendor.placeholders.api.ParserContext;
import dev.delesk1jx.styledchat.vendor.placeholders.api.node.TextNode;
import java.util.Arrays;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class StrikethroughNode extends ParentNode {
   private final boolean value;

   public StrikethroughNode(TextNode[] nodes, boolean value) {
      super(nodes);
      this.value = value;
   }

   @Override
   protected Component applyFormatting(MutableComponent out, ParserContext context) {
      return out.setStyle(out.getStyle().withStrikethrough(this.value));
   }

   @Override
   public ParentTextNode copyWith(TextNode[] children) {
      return new StrikethroughNode(children, this.value);
   }

   @Override
   public String toString() {
      return "StrikethroughNode{children=" + Arrays.toString((Object[])this.children) + ", value=" + this.value + "}";
   }
}
