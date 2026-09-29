package eu.pb4.styledchat.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.delesk1jx.styledchat.vendor.placeholders.api.ParserContext;
import dev.delesk1jx.styledchat.vendor.placeholders.api.PlaceholderContext;
import dev.delesk1jx.styledchat.vendor.placeholders.api.Placeholders;
import dev.delesk1jx.styledchat.vendor.placeholders.api.node.EmptyNode;
import dev.delesk1jx.styledchat.vendor.placeholders.api.node.TextNode;
import dev.delesk1jx.styledchat.vendor.placeholders.api.parsers.NodeParser;
import dev.delesk1jx.styledchat.vendor.placeholders.api.parsers.PatternPlaceholderParser;
import dev.delesk1jx.styledchat.vendor.placeholders.api.parsers.StaticPreParser;
import dev.delesk1jx.styledchat.vendor.placeholders.api.parsers.TextParserV1;
import eu.pb4.predicate.api.BuiltinPredicates;
import eu.pb4.predicate.api.MinecraftPredicate;
import eu.pb4.styledchat.StyledChatUtils;
import eu.pb4.styledchat.config.data.ChatStyleData;
import eu.pb4.styledchat.config.data.ConfigData;
import eu.pb4.styledchat.parser.DynamicNode;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class ChatStyle {
   public static final ChatStyle EMPTY = new ChatStyle(new ChatStyleData());
   public static final NodeParser PARSER = NodeParser.merge(
      new NodeParser[]{
         TextParserV1.DEFAULT,
         Placeholders.DEFAULT_PLACEHOLDER_PARSER,
         new PatternPlaceholderParser(PatternPlaceholderParser.PREDEFINED_PLACEHOLDER_PATTERN, DynamicNode::of),
         StaticPreParser.INSTANCE
      }
   );
   public final MinecraftPredicate require;
   public final TextNode displayName;
   public final TextNode chat;
   public final TextNode join;
   public final TextNode joinFirstTime;
   public final TextNode joinRenamed;
   public final TextNode left;
   public final TextNode death;
   public final TextNode advancementTask;
   public final TextNode advancementChallenge;
   public final TextNode advancementGoal;
   public final TextNode privateMessageSent;
   public final TextNode privateMessageReceived;
   public final TextNode teamChatSent;
   public final TextNode teamChatReceived;
   public final TextNode sayCommand;
   public final TextNode meCommand;
   public final TextNode petDeath;
   public final TextNode spoilerStyle;
   public final String spoilerSymbol;
   public final TextNode linkStyle;
   public final TextNode mentionStyle;
   public final Map<String, TextNode> emoticons = new HashMap<>();
   public final Object2BooleanMap<String> formatting = new Object2BooleanOpenHashMap();
   public final Map<ResourceLocation, TextNode> custom = new HashMap<>();

   public ChatStyle(ChatStyleData data, ChatStyle defaultStyle) {
      this.require = data instanceof ConfigData.RequireChatStyleData data1 ? data1.require : BuiltinPredicates.operatorLevel(0);
      this.displayName = data.displayName != null ? parseText(data.displayName) : defaultStyle.displayName;
      this.chat = data.messages.chat != null ? parseText(data.messages.chat) : defaultStyle.chat;
      this.join = data.messages.joinedGame != null ? parseText(data.messages.joinedGame) : defaultStyle.join;
      this.joinFirstTime = data.messages.joinedForFirstTime != null ? parseText(data.messages.joinedForFirstTime) : this.join;
      this.joinRenamed = data.messages.joinedAfterNameChange != null ? parseText(data.messages.joinedAfterNameChange) : defaultStyle.joinRenamed;
      this.left = data.messages.leftGame != null ? parseText(data.messages.leftGame) : defaultStyle.left;
      this.death = data.messages.baseDeath != null ? parseText(data.messages.baseDeath) : defaultStyle.death;
      this.advancementTask = data.messages.advancementTask != null ? parseText(data.messages.advancementTask) : defaultStyle.advancementTask;
      this.advancementChallenge = data.messages.advancementChallenge != null
         ? parseText(data.messages.advancementChallenge)
         : defaultStyle.advancementChallenge;
      this.advancementGoal = data.messages.advancementGoal != null ? parseText(data.messages.advancementGoal) : defaultStyle.advancementGoal;
      this.privateMessageSent = data.messages.privateMessageSent != null ? parseText(data.messages.privateMessageSent) : defaultStyle.privateMessageSent;
      this.privateMessageReceived = data.messages.privateMessageReceived != null
         ? parseText(data.messages.privateMessageReceived)
         : defaultStyle.privateMessageReceived;
      this.teamChatSent = data.messages.sentTeamChat != null ? parseText(data.messages.sentTeamChat) : defaultStyle.teamChatSent;
      this.teamChatReceived = data.messages.receivedTeamChat != null ? parseText(data.messages.receivedTeamChat) : defaultStyle.teamChatReceived;
      this.sayCommand = data.messages.sayCommandMessage != null ? parseText(data.messages.sayCommandMessage) : defaultStyle.sayCommand;
      this.meCommand = data.messages.meCommandMessage != null ? parseText(data.messages.meCommandMessage) : defaultStyle.meCommand;
      this.petDeath = data.messages.petDeathMessage != null ? parseText(data.messages.petDeathMessage) : defaultStyle.petDeath;
      this.spoilerStyle = data.spoilerStyle != null ? parseText(data.spoilerStyle) : defaultStyle.spoilerStyle;
      this.spoilerSymbol = data.spoilerSymbol != null ? data.spoilerSymbol : defaultStyle.spoilerSymbol;
      this.linkStyle = data.linkStyle != null ? parseText(data.linkStyle) : defaultStyle.linkStyle;
      this.mentionStyle = data.mentionStyle != null ? parseText(data.mentionStyle) : defaultStyle.mentionStyle;

      for (Entry<String, String> emoticon : data.emoticons.entrySet()) {
         if (emoticon.getKey().startsWith("$")) {
            this.decodeSpecialEmoticon(emoticon.getKey(), emoticon.getValue());
         } else {
            this.emoticons.put(emoticon.getKey(), parseText(emoticon.getValue()));
         }
      }

      for (Entry<String, Boolean> formatting : data.formatting.entrySet()) {
         this.formatting.put(formatting.getKey(), formatting.getValue());
      }

      if (data.custom != null) {
         for (Entry<String, String> entry : data.custom.entrySet()) {
            ResourceLocation id = ResourceLocation.tryParse(entry.getKey());
            if (id != null) {
               this.custom.put(id, parseText(entry.getValue()));
            }
         }
      }
   }

   public ChatStyle(ChatStyleData data) {
      this.require = data instanceof ConfigData.RequireChatStyleData data1 ? data1.require : BuiltinPredicates.operatorLevel(0);
      this.displayName = data.displayName != null ? parseText(data.displayName) : null;
      this.chat = data.messages.chat != null ? parseText(data.messages.chat) : null;
      this.join = data.messages.joinedGame != null ? parseText(data.messages.joinedGame) : null;
      this.joinRenamed = data.messages.joinedAfterNameChange != null ? parseText(data.messages.joinedAfterNameChange) : null;
      this.joinFirstTime = data.messages.joinedForFirstTime != null ? parseText(data.messages.joinedForFirstTime) : null;
      this.left = data.messages.leftGame != null ? parseText(data.messages.leftGame) : null;
      this.death = data.messages.baseDeath != null ? parseText(data.messages.baseDeath) : null;
      this.advancementTask = data.messages.advancementTask != null ? parseText(data.messages.advancementTask) : null;
      this.advancementChallenge = data.messages.advancementChallenge != null ? parseText(data.messages.advancementChallenge) : null;
      this.advancementGoal = data.messages.advancementGoal != null ? parseText(data.messages.advancementGoal) : null;
      this.privateMessageSent = data.messages.privateMessageSent != null ? parseText(data.messages.privateMessageSent) : null;
      this.privateMessageReceived = data.messages.privateMessageReceived != null ? parseText(data.messages.privateMessageReceived) : null;
      this.teamChatSent = data.messages.sentTeamChat != null ? parseText(data.messages.sentTeamChat) : null;
      this.teamChatReceived = data.messages.receivedTeamChat != null ? parseText(data.messages.receivedTeamChat) : null;
      this.sayCommand = data.messages.sayCommandMessage != null ? parseText(data.messages.sayCommandMessage) : null;
      this.meCommand = data.messages.meCommandMessage != null ? parseText(data.messages.meCommandMessage) : null;
      this.petDeath = data.messages.petDeathMessage != null ? parseText(data.messages.petDeathMessage) : null;
      this.spoilerStyle = data.spoilerStyle != null ? parseText(data.spoilerStyle) : null;
      this.spoilerSymbol = data.spoilerSymbol != null ? data.spoilerSymbol : null;
      this.linkStyle = data.linkStyle != null ? parseText(data.linkStyle) : null;
      this.mentionStyle = data.mentionStyle != null ? parseText(data.mentionStyle) : null;

      for (Entry<String, String> emoticon : data.emoticons.entrySet()) {
         if (emoticon.getKey().startsWith("$")) {
            this.decodeSpecialEmoticon(emoticon.getKey(), emoticon.getValue());
         } else {
            this.emoticons.put(emoticon.getKey(), parseText(emoticon.getValue()));
         }
      }

      for (Entry<String, Boolean> formatting : data.formatting.entrySet()) {
         this.formatting.put(formatting.getKey(), formatting.getValue());
      }

      if (data.custom != null) {
         for (Entry<String, String> entry : data.custom.entrySet()) {
            ResourceLocation id = ResourceLocation.tryParse(entry.getKey());
            if (id != null) {
               this.custom.put(id, parseText(entry.getValue()));
            }
         }
      }
   }

   private static TextNode parseText(String input) {
      return (TextNode)(!input.isEmpty() ? PARSER.parseNode(input) : EmptyNode.INSTANCE);
   }

   private void decodeSpecialEmoticon(String baseKey, String baseValue) {
      String[] parts = baseKey.substring(1).split(":", 3);
      if (parts.length == 3) {
         JsonObject json;
         if (parts[1].equals("from_file")) {
            json = ConfigManager.loadJson(parts[2]);
         } else {
            if (!parts[1].equals("builtin")) {
               return;
            }

            json = ConfigManager.loadJsonBuiltin(parts[2]);
         }

         if (parts[0].equals("default")) {
            try {
               for (Entry<String, JsonElement> entry : json.entrySet()) {
                  this.emoticons
                     .put(
                        entry.getKey(),
                        NodeParser.merge(
                              new NodeParser[]{
                                 TextParserV1.DEFAULT,
                                 Placeholders.DEFAULT_PLACEHOLDER_PARSER,
                                 new PatternPlaceholderParser(
                                    PatternPlaceholderParser.PREDEFINED_PLACEHOLDER_PATTERN, xx -> parseText(entry.getValue().getAsString())
                                 ),
                                 StaticPreParser.INSTANCE
                              }
                           )
                           .parseNode(baseValue)
                     );
               }
            } catch (Throwable var17) {
               var17.printStackTrace();
            }
         } else if (parts[0].equals("emojibase") || parts[1].equals("emojibase_unlocked")) {
            try {
               boolean validate = parts[0].equals("emojibase");

               label102:
               for (Entry<String, JsonElement> entry : json.entrySet()) {
                  StringBuilder b = new StringBuilder();

                  for (String x : entry.getKey().split("-")) {
                     int i = Integer.parseInt(x, 16);
                     if (validate && (i >= 127995 && i <= 127999 || i == 65039 || i == 8205)) {
                        continue label102;
                     }

                     b.appendCodePoint(Integer.parseInt(x, 16));
                  }

                  TextNode output = NodeParser.merge(
                        new NodeParser[]{
                           TextParserV1.DEFAULT,
                           Placeholders.DEFAULT_PLACEHOLDER_PARSER,
                           new PatternPlaceholderParser(PatternPlaceholderParser.PREDEFINED_PLACEHOLDER_PATTERN, xx -> TextNode.of(b.toString())),
                           StaticPreParser.INSTANCE
                        }
                     )
                     .parseNode(baseValue);
                  if (entry.getValue().isJsonArray()) {
                     for (JsonElement x : entry.getValue().getAsJsonArray()) {
                        this.emoticons.put(x.getAsString(), output);
                     }
                  } else {
                     this.emoticons.put(entry.getValue().getAsString(), output);
                  }
               }
            } catch (Throwable var16) {
               var16.printStackTrace();
            }
         } else if (parts[0].equals("cldr")) {
            try {
               json = json.getAsJsonObject("annotations").getAsJsonObject("annotations");

               for (Entry<String, JsonElement> entry : json.entrySet()) {
                  try {
                     TextNode value = NodeParser.merge(
                           new NodeParser[]{
                              TextParserV1.DEFAULT,
                              Placeholders.DEFAULT_PLACEHOLDER_PARSER,
                              new PatternPlaceholderParser(PatternPlaceholderParser.PREDEFINED_PLACEHOLDER_PATTERN, xx -> TextNode.of(entry.getKey())),
                              StaticPreParser.INSTANCE
                           }
                        )
                        .parseNode(baseValue);

                     for (JsonElement key : entry.getValue().getAsJsonObject().getAsJsonArray("default")) {
                        this.emoticons.put(key.getAsString().replace(' ', '_').replace(':', '_'), value);
                     }
                  } catch (Throwable var14) {
                  }
               }
            } catch (Throwable var15) {
               var15.printStackTrace();
            }
         }
      }
   }

   public Component getDisplayName(ServerPlayer player, Component vanillaDisplayName) {
      if (this.displayName == null) {
         return null;
      } else if (this.displayName == EmptyNode.INSTANCE) {
         return vanillaDisplayName;
      } else {
         ParserContext context = PlaceholderContext.of(player)
            .asParserContext()
            .with(
               DynamicNode.NODES,
               Map.of("vanillaDisplayName", vanillaDisplayName, "player", vanillaDisplayName, "default", vanillaDisplayName, "name", player.getName())
            );
         return this.displayName.toText(context);
      }
   }

   @Nullable
   public Component getChat(ServerPlayer player, Component message) {
      if (this.chat == null) {
         return null;
      } else {
         return this.chat == EmptyNode.INSTANCE
            ? StyledChatUtils.IGNORED_TEXT
            : this.chat
               .toText(PlaceholderContext.of(player).asParserContext().with(DynamicNode.NODES, Map.of("player", player.getDisplayName(), "message", message)));
      }
   }

   @Nullable
   public Component getJoin(ServerPlayer player) {
      if (this.join == null) {
         return null;
      } else {
         return this.join == EmptyNode.INSTANCE
            ? StyledChatUtils.IGNORED_TEXT
            : this.join.toText(PlaceholderContext.of(player).asParserContext().with(DynamicNode.NODES, Map.of("player", player.getDisplayName())));
      }
   }

   @Nullable
   public Component getJoinFirstTime(ServerPlayer player) {
      if (this.joinFirstTime == null) {
         return null;
      } else {
         return this.joinFirstTime == EmptyNode.INSTANCE
            ? StyledChatUtils.IGNORED_TEXT
            : this.joinFirstTime.toText(PlaceholderContext.of(player).asParserContext().with(DynamicNode.NODES, Map.of("player", player.getDisplayName())));
      }
   }

   @Nullable
   public Component getJoinRenamed(ServerPlayer player, String oldName) {
      if (this.joinRenamed == null) {
         return null;
      } else {
         return this.joinRenamed == EmptyNode.INSTANCE
            ? StyledChatUtils.IGNORED_TEXT
            : this.joinRenamed
               .toText(
                  PlaceholderContext.of(player)
                     .asParserContext()
                     .with(DynamicNode.NODES, Map.of("player", player.getDisplayName(), "old_name", Component.literal(oldName)))
               );
      }
   }

   @Nullable
   public Component getLeft(ServerPlayer player) {
      if (this.left == null) {
         return null;
      } else {
         return this.left == EmptyNode.INSTANCE
            ? StyledChatUtils.IGNORED_TEXT
            : this.left.toText(PlaceholderContext.of(player).asParserContext().with(DynamicNode.NODES, Map.of("player", player.getDisplayName())));
      }
   }

   @Nullable
   public Component getDeath(ServerPlayer player, Component vanillaMessage) {
      if (this.death == null) {
         return null;
      } else {
         return this.death == EmptyNode.INSTANCE
            ? StyledChatUtils.IGNORED_TEXT
            : this.death
               .toText(
                  PlaceholderContext.of(player)
                     .asParserContext()
                     .with(DynamicNode.NODES, Map.of("player", player.getDisplayName(), "default_message", vanillaMessage))
               );
      }
   }

   @Nullable
   public Component getAdvancementGoal(ServerPlayer player, Component advancement) {
      if (this.advancementGoal == null) {
         return null;
      } else {
         return this.advancementGoal == EmptyNode.INSTANCE
            ? StyledChatUtils.IGNORED_TEXT
            : this.advancementGoal
               .toText(
                  PlaceholderContext.of(player).asParserContext().with(DynamicNode.NODES, Map.of("player", player.getDisplayName(), "advancement", advancement))
               );
      }
   }

   @Nullable
   public Component getAdvancementTask(ServerPlayer player, Component advancement) {
      if (this.advancementTask == null) {
         return null;
      } else {
         return this.advancementTask == EmptyNode.INSTANCE
            ? StyledChatUtils.IGNORED_TEXT
            : this.advancementTask
               .toText(
                  PlaceholderContext.of(player).asParserContext().with(DynamicNode.NODES, Map.of("player", player.getDisplayName(), "advancement", advancement))
               );
      }
   }

   @Nullable
   public Component getAdvancementChallenge(ServerPlayer player, Component advancement) {
      if (this.advancementChallenge == null) {
         return null;
      } else {
         return this.advancementChallenge == EmptyNode.INSTANCE
            ? StyledChatUtils.IGNORED_TEXT
            : this.advancementChallenge
               .toText(
                  PlaceholderContext.of(player).asParserContext().with(DynamicNode.NODES, Map.of("player", player.getDisplayName(), "advancement", advancement))
               );
      }
   }

   @Nullable
   public Component getSayCommand(CommandSourceStack source, Component message) {
      if (this.sayCommand == null) {
         return null;
      } else {
         return this.sayCommand == EmptyNode.INSTANCE
            ? StyledChatUtils.IGNORED_TEXT
            : this.sayCommand
               .toText(
                  PlaceholderContext.of(source)
                     .asParserContext()
                     .with(DynamicNode.NODES, Map.of("player", source.getDisplayName(), "displayName", source.getDisplayName(), "message", message))
               );
      }
   }

   @Nullable
   public Component getMeCommand(CommandSourceStack source, Component message) {
      if (this.meCommand == null) {
         return null;
      } else {
         return this.meCommand == EmptyNode.INSTANCE
            ? StyledChatUtils.IGNORED_TEXT
            : this.meCommand
               .toText(
                  PlaceholderContext.of(source)
                     .asParserContext()
                     .with(DynamicNode.NODES, Map.of("player", source.getDisplayName(), "displayName", source.getDisplayName(), "message", message))
               );
      }
   }

   @Nullable
   public Component getPrivateMessageSent(Component sender, Component receiver, Component message, PlaceholderContext context) {
      if (this.privateMessageSent == null) {
         return null;
      } else {
         return this.privateMessageSent == EmptyNode.INSTANCE
            ? StyledChatUtils.IGNORED_TEXT
            : this.privateMessageSent
               .toText(context.asParserContext().with(DynamicNode.NODES, Map.of("sender", sender, "receiver", receiver, "message", message)));
      }
   }

   @Nullable
   public Component getPrivateMessageReceived(Component sender, Component receiver, Component message, PlaceholderContext context) {
      if (this.privateMessageReceived == null) {
         return null;
      } else {
         return this.privateMessageReceived == EmptyNode.INSTANCE
            ? StyledChatUtils.IGNORED_TEXT
            : this.privateMessageReceived
               .toText(context.asParserContext().with(DynamicNode.NODES, Map.of("sender", sender, "receiver", receiver, "message", message)));
      }
   }

   @Nullable
   public Component getTeamChatSent(Component team, Component displayName, Component message, CommandSourceStack context) {
      if (this.teamChatSent == null) {
         return null;
      } else {
         return this.teamChatSent == EmptyNode.INSTANCE
            ? StyledChatUtils.IGNORED_TEXT
            : this.teamChatSent
               .toText(
                  PlaceholderContext.of(context)
                     .asParserContext()
                     .with(DynamicNode.NODES, Map.of("team", team, "displayName", displayName, "message", message))
               );
      }
   }

   @Nullable
   public Component getTeamChatReceived(Component team, Component displayName, Component message, CommandSourceStack context) {
      if (this.teamChatReceived == null) {
         return null;
      } else {
         return this.teamChatReceived == EmptyNode.INSTANCE
            ? StyledChatUtils.IGNORED_TEXT
            : this.teamChatReceived
               .toText(
                  PlaceholderContext.of(context)
                     .asParserContext()
                     .with(DynamicNode.NODES, Map.of("team", team, "displayName", displayName, "message", message))
               );
      }
   }

   @Nullable
   public Component getCustom(ResourceLocation identifier, Component displayName, Component message, @Nullable Component receiver, CommandSourceStack source) {
      TextNode node = this.custom.get(identifier);
      if (node == null) {
         return null;
      } else {
         return node == EmptyNode.INSTANCE
            ? StyledChatUtils.IGNORED_TEXT
            : node.toText(
               PlaceholderContext.of(source)
                  .asParserContext()
                  .with(
                     DynamicNode.NODES,
                     Map.of("receiver", receiver == null ? Component.empty() : receiver, "displayName", displayName, "message", message)
                  )
            );
      }
   }

   @Nullable
   public TextNode getLink() {
      return this.linkStyle;
   }

   @Nullable
   public TextNode getMention() {
      return this.mentionStyle;
   }

   @Nullable
   public TextNode getSpoilerStyle() {
      return this.spoilerStyle;
   }

   @Nullable
   public String getSpoilerSymbol() {
      return this.spoilerSymbol;
   }

   public Component getPetDeath(TamableAnimal entity, Component vanillaMessage) {
      return this.petDeath == null
         ? null
         : this.petDeath
            .toText(
               PlaceholderContext.of(entity).asParserContext().with(DynamicNode.NODES, Map.of("pet", entity.getDisplayName(), "default_message", vanillaMessage))
            );
   }
}
