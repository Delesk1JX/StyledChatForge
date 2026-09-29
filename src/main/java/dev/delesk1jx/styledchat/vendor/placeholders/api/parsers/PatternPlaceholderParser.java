package dev.delesk1jx.styledchat.vendor.placeholders.api.parsers;

import dev.delesk1jx.styledchat.vendor.placeholders.api.ParserContext;
import dev.delesk1jx.styledchat.vendor.placeholders.api.PlaceholderContext;
import dev.delesk1jx.styledchat.vendor.placeholders.api.Placeholders;
import dev.delesk1jx.styledchat.vendor.placeholders.api.node.DirectTextNode;
import dev.delesk1jx.styledchat.vendor.placeholders.api.node.LiteralNode;
import dev.delesk1jx.styledchat.vendor.placeholders.api.node.TextNode;
import dev.delesk1jx.styledchat.vendor.placeholders.api.node.TranslatedNode;
import dev.delesk1jx.styledchat.vendor.placeholders.api.node.parent.ParentTextNode;
import dev.delesk1jx.styledchat.vendor.placeholders.impl.placeholder.PlaceholderNode;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;

public record PatternPlaceholderParser(Pattern pattern, Function<String, TextNode> placeholderProvider) implements NodeParser {
   public static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("(?<!((?<!(\\\\))\\\\))[%](?<id>[^%]+:[^%]+)[%]");
   public static final Pattern ALT_PLACEHOLDER_PATTERN = Pattern.compile("(?<!((?<!(\\\\))\\\\))[{](?<id>[^{}]+:[^{}]+)[}]");
   public static final Pattern PLACEHOLDER_PATTERN_CUSTOM = Pattern.compile("(?<!((?<!(\\\\))\\\\))[%](?<id>[^%]+)[%]");
   public static final Pattern ALT_PLACEHOLDER_PATTERN_CUSTOM = Pattern.compile("(?<!((?<!(\\\\))\\\\))[{](?<id>[^{}]+)[}]");
   public static final Pattern PREDEFINED_PLACEHOLDER_PATTERN = Pattern.compile("(?<!((?<!(\\\\))\\\\))\\$[{](?<id>[^}]+)}");

   public static PatternPlaceholderParser of(Pattern pattern, ParserContext.Key<PlaceholderContext> contextKey, Placeholders.PlaceholderGetter placeholders) {
      return new PatternPlaceholderParser(
         pattern,
         arg -> {
            String[] args = arg.split(" ", 2);
            return placeholders.exists(args[0])
               ? new PlaceholderNode(contextKey, args[0], placeholders, placeholders.isContextOptional(), args.length == 2 ? args[1] : null)
               : null;
         }
      );
   }

   public static PatternPlaceholderParser ofNodeMap(Pattern pattern, Map<String, TextNode> map) {
      return new PatternPlaceholderParser(pattern, map::get);
   }

   public static PatternPlaceholderParser ofTextMap(Pattern pattern, Map<String, Component> map) {
      return new PatternPlaceholderParser(pattern, arg -> {
         Component x = map.get(arg);
         return x != null ? new DirectTextNode(x) : null;
      });
   }

   @Override
   public TextNode[] parseNodes(TextNode text) {
      if (text instanceof TranslatedNode translatedNode) {
         ArrayList<Object> list = new ArrayList<>();

         for (Object arg : translatedNode.args()) {
            if (arg instanceof TextNode textNode) {
               list.add(TextNode.asSingle(this.parseNodes(textNode)));
            } else {
               list.add(arg);
            }
         }

         return new TextNode[]{TranslatedNode.ofFallback(translatedNode.key(), translatedNode.fallback(), list.toArray())};
      } else if (text instanceof LiteralNode literalNode) {
         ArrayList<TextNode> out = new ArrayList<>();
         String string = literalNode.value();
         Matcher matcher = this.pattern.matcher(string);
         int previousEnd = 0;

         while (matcher.find()) {
            String placeholder = matcher.group("id");
            int start = matcher.start();
            int end = matcher.end();
            TextNode output = this.placeholderProvider.apply(placeholder);
            if (output != null) {
               if (start != 0) {
                  out.add(new LiteralNode(string.substring(previousEnd, start)));
               }

               out.add(output);
               previousEnd = end;
            } else {
               matcher.region(start + 1, string.length());
            }
         }

         if (previousEnd != string.length()) {
            out.add(new LiteralNode(string.substring(previousEnd)));
         }

         return out.toArray(new TextNode[0]);
      } else if (!(text instanceof ParentTextNode parentNode)) {
         return new TextNode[]{text};
      } else {
         ArrayList<TextNode> out = new ArrayList<>();

         for (TextNode text1 : parentNode.getChildren()) {
            out.add(TextNode.asSingle(this.parseNodes(text1)));
         }

         return new TextNode[]{parentNode.copyWith(out.toArray(new TextNode[0]), this)};
      }
   }
}
