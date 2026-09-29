package eu.pb4.styledchat.parser;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.node.parent.ParentNode;
import eu.pb4.placeholders.api.node.parent.ParentTextNode;
import eu.pb4.styledchat.config.Config;
import eu.pb4.styledchat.config.ConfigManager;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.HoverEvent.Action;

public class SpoilerNode extends ParentNode {
   public SpoilerNode(TextNode[] children) {
      super(children);
   }

   protected Component applyFormatting(MutableComponent out, ParserContext context) {
      Config config = ConfigManager.getConfig();
      PlaceholderContext ctx = (PlaceholderContext)context.get(PlaceholderContext.KEY);
      Component obj = config.getSpoilerStyle(ctx)
         .toText(
            ctx.asParserContext()
               .with(DynamicNode.NODES, Map.of("spoiler", Component.literal(config.getSpoilerSymbole(ctx).repeat(out.getString().length()))))
         );
      return Component.empty().append(obj).setStyle(obj.getStyle().withHoverEvent(new HoverEvent(Action.SHOW_TEXT, out)));
   }

   public ParentTextNode copyWith(TextNode[] children) {
      return new SpoilerNode(this.children);
   }
}
