package dev.delesk1jx.styledchat.vendor.placeholders.api.parsers;

import dev.delesk1jx.styledchat.vendor.placeholders.api.node.DirectTextNode;
import dev.delesk1jx.styledchat.vendor.placeholders.api.node.TextNode;
import dev.delesk1jx.styledchat.vendor.placeholders.api.node.parent.ParentNode;
import java.util.ArrayList;

public record StaticPreParser() implements NodeParser {
   public static final NodeParser INSTANCE = new StaticPreParser();

   @Override
   public TextNode[] parseNodes(TextNode input) {
      return new TextNode[]{parse(input)};
   }

   public static TextNode parse(TextNode node) {
      if (!node.isDynamic()) {
         return new DirectTextNode(node.toText());
      } else if (!(node instanceof ParentNode parentNode)) {
         return node;
      } else {
         ArrayList<TextNode> c = new ArrayList<>();

         for (TextNode child : parentNode.getChildren()) {
            c.add(parse(child));
         }

         return parentNode.copyWith(c.toArray(new TextNode[0]));
      }
   }
}
