package dev.delesk1jx.styledchat.vendor.placeholders.api.parsers;

import dev.delesk1jx.styledchat.vendor.placeholders.api.ParserContext;
import dev.delesk1jx.styledchat.vendor.placeholders.api.node.TextNode;
import net.minecraft.network.chat.Component;

public record WrappedText(String input, TextNode textNode, Component text) {
   public static WrappedText from(NodeParser parser, String input) {
      TextNode node = TextNode.asSingle(parser.parseNodes(TextNode.of(input)));
      return new WrappedText(input, node, node.toText(ParserContext.of(), true));
   }
}
