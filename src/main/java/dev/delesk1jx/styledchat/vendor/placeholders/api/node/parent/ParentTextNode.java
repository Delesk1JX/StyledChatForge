package dev.delesk1jx.styledchat.vendor.placeholders.api.node.parent;

import dev.delesk1jx.styledchat.vendor.placeholders.api.node.TextNode;
import dev.delesk1jx.styledchat.vendor.placeholders.api.parsers.NodeParser;
import dev.delesk1jx.styledchat.vendor.placeholders.impl.textparser.TextParserImpl;
import java.util.Collection;

public interface ParentTextNode extends TextNode {
   TextNode[] getChildren();

   ParentTextNode copyWith(TextNode[] var1);

   default ParentTextNode copyWith(Collection<TextNode> children) {
      return this.copyWith(children.toArray(TextParserImpl.CASTER));
   }

   default boolean isDynamicNoChildren() {
      return false;
   }

   @Override
   default boolean isDynamic() {
      for (TextNode x : this.getChildren()) {
         if (x.isDynamic()) {
            return true;
         }
      }

      return this.isDynamicNoChildren();
   }

   default ParentTextNode copyWith(TextNode[] children, NodeParser parser) {
      return this.copyWith(children);
   }

   default ParentTextNode copyWith(Collection<TextNode> children, NodeParser parser) {
      return this.copyWith(children.toArray(TextParserImpl.CASTER), parser);
   }

   @Deprecated(
      forRemoval = true
   )
   @FunctionalInterface
   public interface Constructor {
      ParentTextNode createNode(String var1, Collection<ParentTextNode> var2);
   }
}
