package dev.delesk1jx.styledchat.vendor.placeholders.api.node.parent;

import dev.delesk1jx.styledchat.vendor.placeholders.api.ParserContext;
import dev.delesk1jx.styledchat.vendor.placeholders.api.node.TextNode;
import java.util.Arrays;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.MutableComponent;

public final class FontNode extends ParentNode {
   private final ResourceLocation font;

   public FontNode(TextNode[] children, ResourceLocation font) {
      super(children);
      this.font = font;
   }

   @Override
   protected Component applyFormatting(MutableComponent out, ParserContext context) {
      return out.setStyle(out.getStyle().withFont(this.font));
   }

   @Override
   public ParentTextNode copyWith(TextNode[] children) {
      return new FontNode(children, this.font);
   }

   @Override
   public String toString() {
      return "FontNode{font=" + this.font + ", children=" + Arrays.toString((Object[])this.children) + "}";
   }
}
