package dev.delesk1jx.styledchat.vendor.placeholders.impl.textparser;

import dev.delesk1jx.styledchat.vendor.placeholders.api.node.TextNode;
import dev.delesk1jx.styledchat.vendor.placeholders.api.parsers.NodeParser;
import java.util.Arrays;

public record MergedParser(NodeParser[] parsers) implements NodeParser {
   public MergedParser(NodeParser[] parsers) {
      this.parsers = Arrays.copyOf(parsers, parsers.length);
   }

   @Override
   public TextNode[] parseNodes(TextNode input) {
      TextNode[] out = new TextNode[]{input};

      for (int i = 0; i < this.parsers.length; i++) {
         out = this.parsers[i].parseNodes(TextNode.asSingle(out));
      }

      return out;
   }
}
