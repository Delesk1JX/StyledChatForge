package eu.pb4.styledchat.parser;

import dev.delesk1jx.styledchat.vendor.placeholders.api.PlaceholderContext;
import dev.delesk1jx.styledchat.vendor.placeholders.api.node.DirectTextNode;
import dev.delesk1jx.styledchat.vendor.placeholders.api.node.LiteralNode;
import dev.delesk1jx.styledchat.vendor.placeholders.api.node.TextNode;
import dev.delesk1jx.styledchat.vendor.placeholders.api.node.parent.ParentTextNode;
import dev.delesk1jx.styledchat.vendor.placeholders.api.parsers.NodeParser;
import eu.pb4.styledchat.StyledChatMod;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.server.level.ServerPlayer;

public record MentionParser(TextNode style, PlaceholderContext context) implements NodeParser {
   public static final Method IS_VANISHED = resolveVanishCheck();

   /**
    * Melius' Vanish is an optional mod, so it is looked up reflectively instead of being compiled
    * against. A missing or renamed method simply means vanish support stays off.
    */
   private static Method resolveVanishCheck() {
      try {
         return Class.forName("me.drex.vanish.api.VanishAPI")
               .getMethod("isVanished", net.minecraft.world.entity.Entity.class);
      } catch (Throwable e) {
         return null;
      }
   }

   public static boolean isVanished(ServerPlayer player) {
      if (IS_VANISHED == null) {
         return false;
      }

      try {
         return Boolean.TRUE.equals(IS_VANISHED.invoke(null, player));
      } catch (Throwable e) {
         return false;
      }
   }

   public TextNode[] parseNodes(TextNode node) {
      if (node instanceof LiteralNode literalNode) {
         return this.parseInput(literalNode.value());
      } else if (!(node instanceof ParentTextNode parentTextNode)) {
         return new TextNode[]{node};
      } else {
         ArrayList<TextNode> list = new ArrayList<>();

         for (TextNode child : parentTextNode.getChildren()) {
            list.addAll(List.of(this.parseNodes(child)));
         }

         return new TextNode[]{parentTextNode.copyWith(list.toArray(new TextNode[0]))};
      }
   }

   public TextNode[] parseInput(String input) {
      if (input.isEmpty()) {
         return new TextNode[0];
      } else {
         for (ServerPlayer player : this.context.server().getPlayerList().getPlayers()) {
            if (!isVanished(player)) {
               int startPos = input.indexOf(player.getScoreboardName());
               if (startPos != -1) {
                  int endPos = startPos + player.getScoreboardName().length();
                  TextNode[] before = this.parseInput(input.substring(0, startPos));
                  TextNode mention = new DirectTextNode(this.style.toText(PlaceholderContext.of(player)));
                  TextNode[] after = this.parseInput(input.substring(Math.min(endPos, input.length())));
                  return Stream.of(before, new TextNode[]{mention}, after).flatMap(Stream::of).toArray(TextNode[]::new);
               }
            }
         }

         return new TextNode[]{new LiteralNode(input)};
      }
   }
}
