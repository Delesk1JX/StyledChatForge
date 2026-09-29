package eu.pb4.styledchat.config;

import dev.delesk1jx.styledchat.vendor.placeholders.api.PlaceholderContext;
import dev.delesk1jx.styledchat.vendor.placeholders.api.node.TextNode;
import dev.delesk1jx.styledchat.vendor.placeholders.api.parsers.TextParserV1;
import dev.delesk1jx.styledchat.vendor.placeholders.api.parsers.TextParserV1.TextTag;
import eu.pb4.predicate.api.BuiltinPredicates;
import eu.pb4.predicate.api.PredicateContext;
import eu.pb4.styledchat.config.data.ChatStyleData;
import eu.pb4.styledchat.config.data.ConfigData;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public final class Config {
   public final ConfigData configData;
   private final ChatStyle defaultStyle;
   private final List<ChatStyle> permissionStyle;
   public final Set<String> allPossibleAutoCompletionKeys;

   public Config(ConfigData data) {
      this.configData = data;
      this.defaultStyle = new ChatStyle(data.defaultStyle, new ChatStyle(ChatStyleData.DEFAULT));
      this.permissionStyle = new ArrayList<>();
      this.allPossibleAutoCompletionKeys = new HashSet<>();

      for (String key : this.defaultStyle.emoticons.keySet()) {
         this.allPossibleAutoCompletionKeys.add(":" + key + ":");
      }

      for (ConfigData.RequireChatStyleData entry : data.permissionStyles) {
         if (entry.require == null) {
            entry.require = BuiltinPredicates.operatorLevel(4);
         }

         ChatStyle style = new ChatStyle(entry);
         this.permissionStyle.add(style);

         for (String key : style.emoticons.keySet()) {
            this.allPossibleAutoCompletionKeys.add(":" + key + ":");
         }
      }

      for (TextTag tag : TextParserV1.DEFAULT.getTags()) {
         this.allPossibleAutoCompletionKeys.add("<" + tag.name() + ">");
         if (tag.aliases() != null) {
            for (String a : tag.aliases()) {
               this.allPossibleAutoCompletionKeys.add("<" + a + ">");
            }
         }
      }
   }

   public Component getDisplayName(ServerPlayer player, Component vanillaDisplayName) {
      PredicateContext context = PredicateContext.of(player);

      for (ChatStyle entry : this.permissionStyle) {
         if (entry.require.test(context).success()) {
            Component text = entry.getDisplayName(player, vanillaDisplayName);
            if (text != null) {
               return text;
            }
         }
      }

      return this.defaultStyle.getDisplayName(player, vanillaDisplayName);
   }

   public Component getChat(ServerPlayer player, Component message) {
      PredicateContext context = PredicateContext.of(player);

      for (ChatStyle entry : this.permissionStyle) {
         if (entry.require.test(context).success()) {
            Component text = entry.getChat(player, message);
            if (text != null) {
               return text;
            }
         }
      }

      return this.defaultStyle.getChat(player, message);
   }

   public Component getJoin(ServerPlayer player) {
      PredicateContext context = PredicateContext.of(player);

      for (ChatStyle entry : this.permissionStyle) {
         if (entry.require.test(context).success()) {
            Component text = entry.getJoin(player);
            if (text != null) {
               return text;
            }
         }
      }

      return this.defaultStyle.getJoin(player);
   }

   public Component getJoinFirstTime(ServerPlayer player) {
      PredicateContext context = PredicateContext.of(player);

      for (ChatStyle entry : this.permissionStyle) {
         if (entry.require.test(context).success()) {
            Component text = entry.getJoinFirstTime(player);
            if (text != null) {
               return text;
            }
         }
      }

      Component text = this.defaultStyle.getJoinFirstTime(player);
      return text != null ? text : this.getJoin(player);
   }

   public Component getJoinRenamed(ServerPlayer player, String oldName) {
      PredicateContext context = PredicateContext.of(player);

      for (ChatStyle entry : this.permissionStyle) {
         if (entry.require.test(context).success()) {
            Component text = entry.getJoinRenamed(player, oldName);
            if (text != null) {
               return text;
            }
         }
      }

      return this.defaultStyle.getJoinRenamed(player, oldName);
   }

   public Component getLeft(ServerPlayer player) {
      PredicateContext context = PredicateContext.of(player);

      for (ChatStyle entry : this.permissionStyle) {
         if (entry.require.test(context).success()) {
            Component text = entry.getLeft(player);
            if (text != null) {
               return text;
            }
         }
      }

      return this.defaultStyle.getLeft(player);
   }

   public Component getDeath(ServerPlayer player, Component vanillaMessage) {
      PredicateContext context = PredicateContext.of(player);

      for (ChatStyle entry : this.permissionStyle) {
         if (entry.require.test(context).success()) {
            Component text = entry.getDeath(player, vanillaMessage);
            if (text != null) {
               return text;
            }
         }
      }

      return this.defaultStyle.getDeath(player, vanillaMessage);
   }

   public Component getAdvancementTask(ServerPlayer player, Component advancement) {
      PredicateContext context = PredicateContext.of(player);

      for (ChatStyle entry : this.permissionStyle) {
         if (entry.require.test(context).success()) {
            Component text = entry.getAdvancementTask(player, advancement);
            if (text != null) {
               return text;
            }
         }
      }

      return this.defaultStyle.getAdvancementTask(player, advancement);
   }

   public Component getAdvancementGoal(ServerPlayer player, Component advancement) {
      PredicateContext context = PredicateContext.of(player);

      for (ChatStyle entry : this.permissionStyle) {
         if (entry.require.test(context).success()) {
            Component text = entry.getAdvancementGoal(player, advancement);
            if (text != null) {
               return text;
            }
         }
      }

      return this.defaultStyle.getAdvancementGoal(player, advancement);
   }

   public Component getAdvancementChallenge(ServerPlayer player, Component advancement) {
      PredicateContext context = PredicateContext.of(player);

      for (ChatStyle entry : this.permissionStyle) {
         if (entry.require.test(context).success()) {
            Component text = entry.getAdvancementChallenge(player, advancement);
            if (text != null) {
               return text;
            }
         }
      }

      return this.defaultStyle.getAdvancementChallenge(player, advancement);
   }

   public Component getSayCommand(CommandSourceStack source, Component message) {
      PredicateContext context = PredicateContext.of(source);

      for (ChatStyle entry : this.permissionStyle) {
         if (entry.require.test(context).success()) {
            Component text = entry.getSayCommand(source, message);
            if (text != null) {
               return text;
            }
         }
      }

      return this.defaultStyle.getSayCommand(source, message);
   }

   public Component getMeCommand(CommandSourceStack source, Component message) {
      PredicateContext context = PredicateContext.of(source);

      for (ChatStyle entry : this.permissionStyle) {
         if (entry.require.test(context).success()) {
            Component text = entry.getMeCommand(source, message);
            if (text != null) {
               return text;
            }
         }
      }

      return this.defaultStyle.getMeCommand(source, message);
   }

   public Component getPrivateMessageSent(Component sender, Component receiver, Component message, CommandSourceStack context) {
      PlaceholderContext placeholderContext = PlaceholderContext.of(context);
      PredicateContext context2 = PredicateContext.of(context);

      for (ChatStyle entry : this.permissionStyle) {
         if (entry.require.test(context2).success()) {
            Component text = entry.getPrivateMessageSent(sender, receiver, message, placeholderContext);
            if (text != null) {
               return text;
            }
         }
      }

      return this.defaultStyle.getPrivateMessageSent(sender, receiver, message, placeholderContext);
   }

   public Component getPrivateMessageReceived(Component sender, Component receiver, Component message, CommandSourceStack context) {
      PlaceholderContext placeholderContext = PlaceholderContext.of(context);
      PredicateContext context2 = PredicateContext.of(context);

      for (ChatStyle entry : this.permissionStyle) {
         if (entry.require.test(context2).success()) {
            Component text = entry.getPrivateMessageReceived(sender, receiver, message, placeholderContext);
            if (text != null) {
               return text;
            }
         }
      }

      return this.defaultStyle.getPrivateMessageReceived(sender, receiver, message, placeholderContext);
   }

   public Component getTeamChatSent(Component team, Component displayName, Component message, CommandSourceStack context) {
      PredicateContext context2 = PredicateContext.of(context);

      for (ChatStyle entry : this.permissionStyle) {
         if (entry.require.test(context2).success()) {
            Component text = entry.getTeamChatSent(team, displayName, message, context);
            if (text != null) {
               return text;
            }
         }
      }

      return this.defaultStyle.getTeamChatSent(team, displayName, message, context);
   }

   public Component getTeamChatReceived(Component team, Component displayName, Component message, CommandSourceStack context) {
      PredicateContext context2 = PredicateContext.of(context);

      for (ChatStyle entry : this.permissionStyle) {
         if (entry.require.test(context2).success()) {
            Component text = entry.getTeamChatReceived(team, displayName, message, context);
            if (text != null) {
               return text;
            }
         }
      }

      return this.defaultStyle.getTeamChatReceived(team, displayName, message, context);
   }

   public TextNode getSpoilerStyle(PlaceholderContext ctx) {
      PredicateContext context2 = PredicateContext.of(ctx.source());

      for (ChatStyle entry : this.permissionStyle) {
         if (entry.require.test(context2).success()) {
            TextNode text = entry.getSpoilerStyle();
            if (text != null) {
               return text;
            }
         }
      }

      return this.defaultStyle.getSpoilerStyle();
   }

   public String getSpoilerSymbole(PlaceholderContext ctx) {
      PredicateContext context2 = PredicateContext.of(ctx.source());

      for (ChatStyle entry : this.permissionStyle) {
         if (entry.require.test(context2).success()) {
            String text = entry.getSpoilerSymbol();
            if (text != null) {
               return text;
            }
         }
      }

      return this.defaultStyle.getSpoilerSymbol();
   }

   public TextNode getLinkStyle(PlaceholderContext ctx) {
      PredicateContext context2 = PredicateContext.of(ctx.source());

      for (ChatStyle entry : this.permissionStyle) {
         if (entry.require.test(context2).success()) {
            TextNode text = entry.getLink();
            if (text != null) {
               return text;
            }
         }
      }

      return this.defaultStyle.getLink();
   }

   public TextNode getMentionStyle(PlaceholderContext ctx) {
      PredicateContext context2 = PredicateContext.of(ctx.source());

      for (ChatStyle entry : this.permissionStyle) {
         if (entry.require.test(context2).success()) {
            TextNode text = entry.getMention();
            if (text != null) {
               return text;
            }
         }
      }

      return this.defaultStyle.getMention();
   }

   public Component getPetDeath(TamableAnimal entity, Component vanillaMessage) {
      PredicateContext context2 = PredicateContext.of(entity);

      for (ChatStyle entry : this.permissionStyle) {
         if (entry.require.test(context2).success()) {
            Component text = this.defaultStyle.getPetDeath(entity, vanillaMessage);
            if (text != null) {
               return text;
            }
         }
      }

      return this.defaultStyle.getPetDeath(entity, vanillaMessage);
   }

   public Map<String, TextNode> getEmotes(CommandSourceStack source) {
      HashMap<String, TextNode> base = new HashMap<>(this.defaultStyle.emoticons);
      PredicateContext context = PredicateContext.of(source);

      for (ChatStyle entry : this.permissionStyle) {
         if (entry.require.test(context).success()) {
            for (Entry<String, TextNode> emoticon : entry.emoticons.entrySet()) {
               if (!base.containsKey(emoticon.getKey())) {
                  base.put(emoticon.getKey(), emoticon.getValue());
               }
            }
         }
      }

      return base;
   }

   public Object2BooleanOpenHashMap<String> getAllowedFormatting(CommandSourceStack source) {
      Object2BooleanOpenHashMap<String> base = new Object2BooleanOpenHashMap(this.defaultStyle.formatting);
      PredicateContext context = PredicateContext.of(source);

      for (ChatStyle entry : this.permissionStyle) {
         if (entry.require.test(context).success()) {
            ObjectIterator var6 = entry.formatting.object2BooleanEntrySet().iterator();

            while (var6.hasNext()) {
               it.unimi.dsi.fastutil.objects.Object2BooleanMap.Entry<String> formatting = (it.unimi.dsi.fastutil.objects.Object2BooleanMap.Entry<String>)var6.next();
               if (!base.containsKey(formatting.getKey())) {
                  base.put((String)formatting.getKey(), formatting.getBooleanValue());
               }
            }
         }
      }

      return base;
   }

   @Nullable
   public Component getCustom(ResourceLocation identifier, Component displayName, Component message, @Nullable Component receiver, CommandSourceStack source) {
      PredicateContext context2 = PredicateContext.of(source);

      for (ChatStyle entry : this.permissionStyle) {
         if (entry.require.test(context2).success()) {
            Component text = entry.getCustom(identifier, displayName, message, receiver, source);
            if (text != null) {
               return text;
            }
         }
      }

      return this.defaultStyle.getCustom(identifier, displayName, message, receiver, source);
   }
}
