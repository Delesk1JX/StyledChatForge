package eu.pb4.styledchat.parser;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.node.DirectTextNode;
import eu.pb4.placeholders.api.node.LiteralNode;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.node.parent.ParentNode;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.styledchat.StyledChatUtils;
import eu.pb4.styledchat.config.ConfigManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent.Action;

public record LinkParser(TextNode style) implements NodeParser {
   public TextNode[] parseNodes(TextNode node) {
      if (node instanceof LiteralNode literalNode) {
         String input = literalNode.value();
         ArrayList<TextNode> list = new ArrayList<>();
         Matcher matcher = StyledChatUtils.URL_REGEX.matcher(input);
         int currentPos = 0;

         int currentEnd;
         for (currentEnd = input.length(); matcher.find() && currentEnd > matcher.start(); currentPos = matcher.end()) {
            String betweenText = input.substring(currentPos, matcher.start());
            if (betweenText.length() != 0) {
               list.add(new LiteralNode(betweenText));
            }

            String link = matcher.group();
            Component text = this.style
               .toText(ParserContext.of(DynamicNode.NODES, Map.of("url", Component.literal(link), "link", Component.literal(link))));
            list.add(
               new DirectTextNode(
                  Component.empty().append(text).setStyle(Style.EMPTY.withClickEvent(new ClickEvent(Action.OPEN_URL, link)))
               )
            );
         }

         if (currentPos < currentEnd) {
            String restOfText = input.substring(currentPos, currentEnd);
            if (restOfText.length() != 0) {
               list.add(new LiteralNode(restOfText));
            }
         }

         return list.toArray(new TextNode[0]);
      } else if (!(node instanceof ParentNode parentNode)) {
         return new TextNode[]{node};
      } else {
         ArrayList<TextNode> list = new ArrayList<>();

         for (TextNode child : parentNode.getChildren()) {
            list.addAll(List.of(this.parseNodes(child)));
         }

         return new TextNode[]{parentNode.copyWith(list.toArray(new TextNode[0]))};
      }
   }

   public static TextNode[] parse(TextNode node, PlaceholderContext context) {
      return new LinkParser(ConfigManager.getConfig().getLinkStyle(context)).parseNodes(node);
   }
}
