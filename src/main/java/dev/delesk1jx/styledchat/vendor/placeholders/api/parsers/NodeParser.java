package dev.delesk1jx.styledchat.vendor.placeholders.api.parsers;

import com.mojang.serialization.Codec;
import dev.delesk1jx.styledchat.vendor.placeholders.api.ParserContext;
import dev.delesk1jx.styledchat.vendor.placeholders.api.node.TextNode;
import dev.delesk1jx.styledchat.vendor.placeholders.impl.textparser.MergedParser;
import java.util.List;
import net.minecraft.network.chat.Component;

public interface NodeParser {
   NodeParser NOOP = i -> new TextNode[]{i};

   TextNode[] parseNodes(TextNode var1);

   default TextNode parseNode(TextNode input) {
      return TextNode.asSingle(this.parseNodes(input));
   }

   default TextNode parseNode(String input) {
      return this.parseNode(TextNode.of(input));
   }

   default Component parseText(TextNode input, ParserContext context) {
      return TextNode.asSingle(this.parseNodes(input)).toText(context, true);
   }

   default Component parseText(String input, ParserContext context) {
      return this.parseText(TextNode.of(input), context);
   }

   default Codec<WrappedText> codec() {
      return Codec.STRING.xmap(x -> WrappedText.from(this, x), w -> w.input());
   }

   static NodeParser merge(NodeParser... parsers) {
      return (NodeParser)(switch (parsers.length) {
         case 0 -> NOOP;
         case 1 -> parsers[0];
         default -> new MergedParser(parsers);
      });
   }

   static NodeParser merge(List<NodeParser> parsers) {
      return (NodeParser)(switch (parsers.size()) {
         case 0 -> NOOP;
         case 1 -> (NodeParser)parsers.get(0);
         default -> new MergedParser(parsers.toArray(new NodeParser[0]));
      });
   }
}
