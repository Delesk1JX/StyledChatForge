package dev.delesk1jx.styledchat.vendor.placeholders.api;

import dev.delesk1jx.styledchat.vendor.placeholders.api.node.LiteralNode;
import dev.delesk1jx.styledchat.vendor.placeholders.api.node.parent.ParentNode;
import dev.delesk1jx.styledchat.vendor.placeholders.api.node.parent.ParentTextNode;
import dev.delesk1jx.styledchat.vendor.placeholders.api.parsers.TextParserV1;
import net.minecraft.network.chat.Component;

public final class TextParserUtils {
   private TextParserUtils() {
   }

   public static Component formatText(String text) {
      return formatNodes(text).toText(null, true);
   }

   public static Component formatTextSafe(String text) {
      return formatNodesSafe(text).toText(null, true);
   }

   public static Component formatText(String text, TextParserV1.TagParserGetter getter) {
      return formatNodes(text, getter).toText(null, true);
   }

   public static ParentTextNode formatNodes(String text) {
      return new ParentNode(TextParserV1.DEFAULT.parseNodes(new LiteralNode(text)));
   }

   public static ParentTextNode formatNodesSafe(String text) {
      return new ParentNode(TextParserV1.DEFAULT.parseNodes(new LiteralNode(text)));
   }

   public static ParentTextNode formatNodes(String text, TextParserV1.TagParserGetter getter) {
      return new ParentNode(TextParserV1.parseNodesWith(new LiteralNode(text), getter));
   }
}
