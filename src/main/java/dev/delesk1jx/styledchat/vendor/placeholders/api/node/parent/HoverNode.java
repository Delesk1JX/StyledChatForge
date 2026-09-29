package dev.delesk1jx.styledchat.vendor.placeholders.api.node.parent;

import dev.delesk1jx.styledchat.vendor.placeholders.api.ParserContext;
import dev.delesk1jx.styledchat.vendor.placeholders.api.node.TextNode;
import dev.delesk1jx.styledchat.vendor.placeholders.api.parsers.NodeParser;
import java.util.Arrays;
import java.util.UUID;
import net.minecraft.world.entity.EntityType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.HoverEvent.EntityTooltipInfo;
import net.minecraft.network.chat.HoverEvent.ItemStackInfo;
import org.jetbrains.annotations.Nullable;

public final class HoverNode<T, H> extends ParentNode {
   private final HoverNode.Action<T, H> action;
   private final T value;

   public HoverNode(TextNode[] children, HoverNode.Action<T, H> action, T value) {
      super(children);
      this.action = action;
      this.value = value;
   }

   // The vanilla Action is paired with a value whose static type is erased per branch, so the
   // constructor is invoked raw. Erasure makes this identical to the typed call.
   @SuppressWarnings({"unchecked", "rawtypes"})
   private static HoverEvent buildHoverEvent(HoverEvent.Action<?> action, Object value) {
      return new HoverEvent((HoverEvent.Action)action, value);
   }

   @Override
   protected Component applyFormatting(MutableComponent out, ParserContext context) {
      if (this.action == HoverNode.Action.TEXT) {
         return out.setStyle(out.getStyle().withHoverEvent(buildHoverEvent(this.action.vanillaType(), ((TextNode)this.value).toText(context, true))));
      } else {
         return this.action == HoverNode.Action.ENTITY
            ? out.setStyle(
               out.getStyle().withHoverEvent(buildHoverEvent(this.action.vanillaType(), ((HoverNode.EntityNodeContent)this.value).toVanilla(context)))
            )
            : out.setStyle(out.getStyle().withHoverEvent(buildHoverEvent(this.action.vanillaType(), this.value)));
      }
   }

   @Override
   public ParentTextNode copyWith(TextNode[] children) {
      return new HoverNode<>(children, this.action, this.value);
   }

   @Override
   public ParentTextNode copyWith(TextNode[] children, NodeParser parser) {
      if (this.action == HoverNode.Action.TEXT) {
         return new HoverNode<>(children, HoverNode.Action.TEXT, parser.parseNode((TextNode)this.value));
      } else if (this.action == HoverNode.Action.ENTITY && ((HoverNode.EntityNodeContent)this.value).name != null) {
         HoverNode.EntityNodeContent val = (HoverNode.EntityNodeContent)this.value;
         return new HoverNode<>(children, HoverNode.Action.ENTITY, new HoverNode.EntityNodeContent(val.entityType, val.uuid, parser.parseNode(val.name)));
      } else {
         return this.copyWith(children);
      }
   }

   public HoverNode.Action<T, H> action() {
      return this.action;
   }

   public T value() {
      return this.value;
   }

   @Override
   public String toString() {
      return "HoverNode{value=" + this.value + ", children=" + Arrays.toString((Object[])this.children) + "}";
   }

   @Override
   public boolean isDynamicNoChildren() {
      return this.action == HoverNode.Action.TEXT && ((TextNode)this.value).isDynamic()
         || this.action == HoverNode.Action.ENTITY && ((HoverNode.EntityNodeContent)this.value).name.isDynamic();
   }

   public static record Action<T, H>(HoverEvent.Action<H> vanillaType) {
      public static final HoverNode.Action<HoverNode.EntityNodeContent, EntityTooltipInfo> ENTITY = new HoverNode.Action<>(HoverEvent.Action.SHOW_ENTITY);
      public static final HoverNode.Action<ItemStackInfo, ItemStackInfo> ITEM_STACK = new HoverNode.Action<>(HoverEvent.Action.SHOW_ITEM);
      public static final HoverNode.Action<TextNode, Component> TEXT = new HoverNode.Action<>(HoverEvent.Action.SHOW_TEXT);
   }

   public static record EntityNodeContent(EntityType<?> entityType, UUID uuid, @Nullable TextNode name) {
      public EntityTooltipInfo toVanilla(ParserContext context) {
         return new EntityTooltipInfo(this.entityType, this.uuid, this.name != null ? this.name.toText(context, true) : null);
      }
   }
}
