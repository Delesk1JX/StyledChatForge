package eu.pb4.styledchat;

import dev.delesk1jx.styledchat.vendor.placeholders.api.PlaceholderContext;
import dev.delesk1jx.styledchat.vendor.placeholders.api.Placeholders;
import dev.delesk1jx.styledchat.vendor.placeholders.api.TextParserUtils;
import dev.delesk1jx.styledchat.vendor.placeholders.api.node.EmptyNode;
import dev.delesk1jx.styledchat.vendor.placeholders.api.node.LiteralNode;
import dev.delesk1jx.styledchat.vendor.placeholders.api.node.TextNode;
import dev.delesk1jx.styledchat.vendor.placeholders.api.parsers.LegacyFormattingParser;
import dev.delesk1jx.styledchat.vendor.placeholders.api.parsers.MarkdownLiteParserV1;
import dev.delesk1jx.styledchat.vendor.placeholders.api.parsers.NodeParser;
import dev.delesk1jx.styledchat.vendor.placeholders.api.parsers.PatternPlaceholderParser;
import dev.delesk1jx.styledchat.vendor.placeholders.api.parsers.TextParserV1;
import dev.delesk1jx.styledchat.vendor.placeholders.api.parsers.MarkdownLiteParserV1.MarkdownFormat;
import dev.delesk1jx.styledchat.vendor.placeholders.api.parsers.TextParserV1.NodeList;
import dev.delesk1jx.styledchat.vendor.placeholders.api.parsers.TextParserV1.TagNodeBuilder;
import dev.delesk1jx.styledchat.vendor.placeholders.api.parsers.TextParserV1.TagNodeValue;
import dev.delesk1jx.styledchat.vendor.placeholders.api.parsers.TextParserV1.TextTag;
import dev.delesk1jx.styledchat.vendor.placeholders.impl.GeneralUtils;
import eu.pb4.playerdata.api.PlayerDataApi;
import eu.pb4.playerdata.api.storage.JsonDataStorage;
import eu.pb4.styledchat.config.ChatStyle;
import eu.pb4.styledchat.config.Config;
import eu.pb4.styledchat.config.ConfigManager;
import eu.pb4.styledchat.config.data.ChatStyleData;
import eu.pb4.styledchat.config.data.VersionedChatStyleData;
import eu.pb4.styledchat.ducks.ExtPlayNetworkHandler;
import eu.pb4.styledchat.ducks.ExtSignedMessage;
import eu.pb4.styledchat.parser.LinkParser;
import eu.pb4.styledchat.parser.MentionParser;
import eu.pb4.styledchat.parser.SpoilerNode;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;
import eu.pb4.loader.Permissions;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.ChatType.Bound;
import net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket.Action;
import org.jetbrains.annotations.Nullable;

public final class StyledChatUtils {
   public static final Component IGNORED_TEXT = Component.empty();
   public static final Pattern URL_REGEX = Pattern.compile("(https?:\\/\\/[-a-zA-Z0-9@:%._\\+~#=]+\\.[^ ]+)");
   public static final String ITEM_KEY = "item";
   public static final String POS_KEY = "pos";
   public static final String SPOILER_TAG = "spoiler";
   private static final Function<MutableComponent, MutableComponent> COLOR_CLEARING = t -> t.setStyle(t.getStyle().withColor((TextColor)null));
   public static JsonDataStorage<VersionedChatStyleData> PLAYER_DATA = new JsonDataStorage(
      "styled_chat_style", VersionedChatStyleData.class, ConfigManager.GSON
   );
   public static final TagNodeBuilder SPOILER_TAG_HANDLER = (tag, data, input, handlers, endAt) -> {
      NodeList out = TextParserV1.parseNodesWith(input, handlers, endAt);
      return new TagNodeValue(new SpoilerNode(out.nodes()), out.length());
   };
   public static final TextTag SPOILER_TEXT_TAG = TextTag.of("spoiler", List.of("hide"), "styledchat", true, SPOILER_TAG_HANDLER);
   public static final String FORMAT_PERMISSION_BASE = "styledchat.format.";
   public static final String FORMAT_PERMISSION_UNSAFE = "styledchat.unsafe_format.";
   public static final Pattern EMOTE_PATTERN = Pattern.compile("(?<!((?<!(\\\\))\\\\))[:](?<id>[^:]+)[:]");
   public static final Component EMPTY_TEXT = Component.empty();
   private static final Set<ResourceKey<ChatType>> DECORABLE = Set.of(
      ChatType.CHAT,
      ChatType.EMOTE_COMMAND,
      ChatType.MSG_COMMAND_INCOMING,
      ChatType.MSG_COMMAND_OUTGOING,
      ChatType.SAY_COMMAND,
      ChatType.TEAM_MSG_COMMAND_INCOMING,
      ChatType.TEAM_MSG_COMMAND_OUTGOING
   );

   public StyledChatUtils() {
   }

   @Deprecated
   public static TextNode parseText(String input) {
      return (TextNode)(!input.isEmpty() ? Placeholders.parseNodes(TextParserUtils.formatNodes(input)) : EmptyNode.INSTANCE);
   }

   public static NodeParser createParser(CommandSourceStack source) {
      return createParser(PlaceholderContext.of(source));
   }

   public static NodeParser createParser(PlaceholderContext context) {
      Config config = ConfigManager.getConfig();
      ArrayList<NodeParser> list = new ArrayList<>();
      TextParserV1 base = createTextParserV1(context.source());
      list.add(base);
      if (config.configData.formatting.parseLinksInChat) {
         list.add(new LinkParser(ConfigManager.getConfig().getLinkStyle(context)));
      }

      if (config.configData.formatting.parseMentionsInChat) {
         list.add(new MentionParser(ConfigManager.getConfig().getMentionStyle(context), context));
      }

      if (config.configData.formatting.markdown) {
         ArrayList<MarkdownFormat> form = new ArrayList<>();
         if (base.getTagParser("bold") != null) {
            form.add(MarkdownFormat.BOLD);
         }

         if (base.getTagParser("italic") != null) {
            form.add(MarkdownFormat.ITALIC);
         }

         if (base.getTagParser("underline") != null) {
            form.add(MarkdownFormat.UNDERLINE);
         }

         if (base.getTagParser("strikethrough") != null) {
            form.add(MarkdownFormat.STRIKETHROUGH);
         }

         if (base.getTagParser("spoiler") != null) {
            form.add(MarkdownFormat.SPOILER);
         }

         if (base.getTagParser("link") != null) {
            form.add(MarkdownFormat.URL);
         }

         if (!form.isEmpty()) {
            list.add(new MarkdownLiteParserV1(SpoilerNode::new, MarkdownLiteParserV1::defaultQuoteFormatting, form.toArray(new MarkdownFormat[0])));
         }
      }

      if (config.configData.formatting.legacyChatFormatting) {
         ArrayList<ChatFormatting> formx = new ArrayList<>();

         for (ChatFormatting formatting : ChatFormatting.values()) {
            if (base.getTagParser(formatting.getName()) != null) {
               formx.add(formatting);
            }
         }

         boolean color = base.getTagParser("color") != null;
         if (!formx.isEmpty() || color) {
            list.add(new LegacyFormattingParser(color, formx.toArray(new ChatFormatting[0])));
         }
      }

      Map<String, TextNode> emotes = getEmotes(context);
      if (!emotes.isEmpty()) {
         list.add(new PatternPlaceholderParser(EMOTE_PATTERN, emotes::get));
      }

      return NodeParser.merge(list);
   }

   public static TextParserV1 createTextParserV1(CommandSourceStack source) {
      TextParserV1 parser = new TextParserV1();
      Config config = ConfigManager.getConfig();
      Object2BooleanOpenHashMap<String> allowedFormatting = config.getAllowedFormatting(source);

      for (TextTag entry : TextParserV1.DEFAULT.getTags()) {
         if (allowedFormatting.getBoolean(entry.name())
            || Permissions.check(source, (entry.userSafe() ? "styledchat.format." : "styledchat.unsafe_format.") + entry.name(), entry.userSafe() ? 2 : 4)
            || Permissions.check(
               source, (entry.userSafe() ? "styledchat.format." : "styledchat.unsafe_format.") + ".type." + entry.type(), entry.userSafe() ? 2 : 4
            )) {
            parser.register(entry);
         }
      }

      if (allowedFormatting.getBoolean("spoiler") || Permissions.check(source, "styledchat.format.spoiler", 2)) {
         parser.register(SPOILER_TEXT_TAG);
      }

      ((StyledChatEvents.FormattingCreationEvent)StyledChatEvents.FORMATTING_CREATION_EVENT.invoker()).onFormattingBuild(source, parser);
      return parser;
   }

   public static Map<String, TextNode> getEmotes(PlaceholderContext context) {
      return StyledChatStyles.getEmotes(context.hasPlayer() ? context.player().createCommandSourceStack() : context.server().createCommandSourceStack());
   }

   public static Component formatFor(PlaceholderContext context, String input) {
      NodeParser parser = createParser(context);
      Config config = ConfigManager.getConfig();
      if (StyledChatMod.USE_FABRIC_API) {
         input = ((StyledChatEvents.PreMessageEvent)StyledChatEvents.PRE_MESSAGE_CONTENT.invoker()).onPreMessage(input, context);
      }

      TextNode value = TextNode.asSingle(parser.parseNodes(new LiteralNode(input)));
      if (StyledChatMod.USE_FABRIC_API) {
         value = ((StyledChatEvents.MessageEvent)StyledChatEvents.MESSAGE_CONTENT.invoker()).onMessage(value, context);
      }

      Component text = value.toText(context);
      if (config.configData.formatting.respectColors) {
         try {
            text = (Component)context.server().getChatDecorator().decorate(context.player(), text).get();
         } catch (Exception var7) {
         }
      }

      return text;
   }

   public static String legacyFormatMessage(String input, Set<String> handlers) {
      Config config = ConfigManager.getConfig();

      try {
         if (config.configData.formatting.markdown) {
            if (handlers.contains("spoiler")) {
               input = input.replaceAll(getMarkdownRegex("||", "\\|\\|"), "<spoiler>$2</spoiler>");
            }

            if (handlers.contains("bold")) {
               input = input.replaceAll(getMarkdownRegex("**", "\\*\\*"), "<bold>$2</bold>");
            }

            if (handlers.contains("underline")) {
               input = input.replaceAll(getMarkdownRegex("__", "__"), "<underline>$2</underline>");
            }

            if (handlers.contains("strikethrough")) {
               input = input.replaceAll(getMarkdownRegex("~~", "~~"), "<strikethrough>$2</strikethrough>");
            }

            if (handlers.contains("italic")) {
               input = input.replaceAll(getMarkdownRegex("*", "\\*"), "<italic>$2</italic>");
            }
         }
      } catch (Exception var4) {
         var4.printStackTrace();
      }

      return input;
   }

   private static String getMarkdownRegex(String base, String sides) {
      return "(" + sides + ")(?<id>[^" + base + "]+)(" + sides + ")";
   }

   public static ChatDecorator getChatDecorator() {
      return (player, message) -> player != null
            ? CompletableFuture.completedFuture(StyledChatStyles.getChat(player, formatFor(PlaceholderContext.of(player), message.getString())))
            : CompletableFuture.completedFuture(formatFor(PlaceholderContext.of(StyledChatMod.server), message.getString()));
   }

   public static ChatDecorator getRawDecorator() {
      return (player, message) -> player != null
            ? CompletableFuture.completedFuture(formatFor(PlaceholderContext.of(player), message.getString()))
            : CompletableFuture.completedFuture(formatFor(PlaceholderContext.of(StyledChatMod.server), message.getString()));
   }

   public static <T> ChatDecorator getCommandDecorator(String context, CommandSourceStack source, BiFunction<String, Class<?>, Object> argumentGetter) {
      Config config = ConfigManager.getConfig();
      return (player, message) -> {
         Component input = formatFor(player != null ? PlaceholderContext.of(player) : PlaceholderContext.of(StyledChatMod.server), message.getString());

         return CompletableFuture.completedFuture(
            switch (context) {
               case "msg" -> {
                  Component var17;
                  try {
                     var17 = config.getPrivateMessageReceived(
                        source.getDisplayName(),
                        ((ServerPlayer)((EntitySelector)argumentGetter.apply("targets", EntitySelector.class)).findPlayers(source).get(0)).getDisplayName(),
                        input,
                        source
                     );
                  } catch (Exception var12) {
                     var12.printStackTrace();
                     MutableComponent var16 = Component.literal("");
                     yield var16;
                  }

                  yield var17;
               }
               case "teammsg" -> {
                  Component var15;
                  try {
                     var15 = config.getTeamChatReceived(((PlayerTeam)source.getEntity().getTeam()).getFormattedDisplayName(), source.getDisplayName(), input, source);
                  } catch (Exception var11) {
                     MutableComponent var14 = Component.literal("");
                     yield var14;
                  }

                  yield var15;
               }
               case "say" -> {
                  Component var13 = config.getSayCommand(source, input);
                  yield var13;
               }
               case "me" -> {
                  Component var9 = config.getMeCommand(source, input);
                  yield var9;
               }
               default -> input;
            }
         );
      };
   }

   @Deprecated
   public static TextNode additionalParsing(TextNode node, PlaceholderContext context) {
      if (ConfigManager.getConfig().configData.formatting.parseLinksInChat) {
         node = parseLinks(node, context);
      }

      return node;
   }

   @Deprecated
   public static TextNode parseLinks(TextNode node, PlaceholderContext context) {
      return TextNode.asSingle(LinkParser.parse(node, context));
   }

   public static Bound removeColor(Bound parameters) {
      return new Bound(parameters.chatType(), removeColor(parameters.name()), parameters.targetName());
   }

   public static Component removeColor(Component text) {
      return GeneralUtils.cloneTransformText(text, COLOR_CLEARING);
   }

   public static boolean isHandledByMod(ResourceKey<ChatType> typeKey) {
      return DECORABLE.contains(typeKey);
   }

   public static void modifyForSending(PlayerChatMessage message, CommandSourceStack source, ResourceKey<ChatType> type) {
      try {
         ExtSignedMessage.setArg(message, "override", formatMessage(message, source, type));
         ((ExtSignedMessage)(Object) message).styledChat_setType(type);
         ((ExtSignedMessage)(Object) message).styledChat_setSource(source);
      } catch (Exception var4) {
         var4.printStackTrace();
      }
   }

   public static Component formatMessage(PlayerChatMessage message, CommandSourceStack source, ResourceKey<ChatType> type) {
      ExtSignedMessage ext = (ExtSignedMessage)(Object) message;
      Component baseInput = ext.styledChat_getArg("base_input");
      Component input = baseInput != null && baseInput.getContents() != ComponentContents.EMPTY
         ? baseInput
         : maybeFormatFor(source, ext.styledChat_getOriginal(), message.decoratedContent());
      if (baseInput == EMPTY_TEXT) {
         ext.styledChat_setArg("base_input", input);
      }

      String var6 = type.location().getPath();

      return (Component)(switch (var6) {
         case "msg_command_incoming" -> {
            Component var24;
            try {
               var24 = StyledChatStyles.getPrivateMessageReceived(source.getDisplayName(), ext.styledChat_getArg("targets"), input, source);
            } catch (Exception var13) {
               var13.printStackTrace();
               MutableComponent var23 = Component.empty();
               yield var23;
            }

            yield var24;
         }
         case "msg_command_outgoing" -> {
            Component var22;
            try {
               var22 = StyledChatStyles.getPrivateMessageSent(source.getDisplayName(), ext.styledChat_getArg("targets"), input, source);
            } catch (Exception var12) {
               var12.printStackTrace();
               MutableComponent var21 = Component.empty();
               yield var21;
            }

            yield var22;
         }
         case "team_msg_command_incoming" -> {
            Component var20;
            try {
               var20 = StyledChatStyles.getTeamChatReceived(((PlayerTeam)source.getEntity().getTeam()).getFormattedDisplayName(), source.getDisplayName(), input, source);
            } catch (Exception var11) {
               MutableComponent var19 = Component.literal("");
               yield var19;
            }

            yield var20;
         }
         case "team_msg_command_outgoing" -> {
            Component var18;
            try {
               var18 = StyledChatStyles.getTeamChatSent(((PlayerTeam)source.getEntity().getTeam()).getFormattedDisplayName(), source.getDisplayName(), input, source);
            } catch (Exception var10) {
               MutableComponent var17 = Component.literal("");
               yield var17;
            }

            yield var18;
         }
         case "say_command" -> {
            Component var16 = StyledChatStyles.getSayCommand(source, input);
            yield var16;
         }
         case "emote_command" -> {
            Component var15 = StyledChatStyles.getMeCommand(source, input);
            yield var15;
         }
         case "chat" -> {
            Component var14 = StyledChatStyles.getChat(source.getPlayer(), input);
            yield var14;
         }
         default -> {
            Component var8 = StyledChatStyles.getCustom(type.location(), source.getDisplayName(), input, null, source);
            yield var8;
         }
      });
   }

   public static Component maybeFormatFor(CommandSourceStack source, String original, Component originalContent) {
      return formatFor(source, original);
   }

   public static Component formatFor(CommandSourceStack source, String original) {
      return source.getEntity() instanceof ServerPlayer player
         ? formatFor(PlaceholderContext.of(player), original)
         : formatFor(PlaceholderContext.of(source.getServer()), original);
   }

   @Deprecated
   public static PlayerChatMessage toEventMessage(PlayerChatMessage message, PlaceholderContext context) {
      ExtSignedMessage ext = (ExtSignedMessage)(Object) message;
      Component baseInput = ext.styledChat_getArg("base_input");
      Component input = baseInput != EMPTY_TEXT && baseInput.getContents() != ComponentContents.EMPTY
         ? baseInput
         : formatFor(context, ext.styledChat_getOriginal());
      if (baseInput == EMPTY_TEXT) {
         ext.styledChat_setArg("base_input", input);
      }

      return new PlayerChatMessage(message.link(), null, SignedMessageBody.unsigned(message.signedContent()), input, null);
   }

   @Deprecated(
      forRemoval = true
   )
   public static void sendAutocompliton(ServerPlayer player) {
      sendAutoCompletion(player, ConfigManager.getConfig().allPossibleAutoCompletionKeys);
   }

   public static void sendAutoCompletion(ServerPlayer player, Collection<String> oldAutoCompletion) {
      Config config = ConfigManager.getConfig();
      player.connection.send(new ClientboundCustomChatCompletionsPacket(Action.REMOVE, new ArrayList<>(oldAutoCompletion)));
      HashSet<String> set = new HashSet<>();
      CommandSourceStack source = player.createCommandSourceStack();
      TextParserV1 handler = createTextParserV1(source);
      if (config.configData.autoCompletion.tags) {
         for (TextTag tag : handler.getTags()) {
            set.add("<" + tag.name() + ">");
            if (config.configData.autoCompletion.tagAliases && tag.aliases() != null) {
               for (String a : tag.aliases()) {
                  set.add("<" + a + ">");
               }
            }
         }
      }

      if (config.configData.autoCompletion.emoticons) {
         for (String emote : config.getEmotes(source).keySet()) {
            set.add(":" + emote + ":");
         }
      }

      if (!set.isEmpty()) {
         player.connection.send(new ClientboundCustomChatCompletionsPacket(Action.ADD, new ArrayList<>(set)));
      }
   }

   public static ChatStyle getPersonalStyle(ServerPlayer player) {
      return player.connection != null ? ((ExtPlayNetworkHandler)player.connection).styledChat$getStyle() : ChatStyle.EMPTY;
   }

   public static void updateStyle(ServerPlayer player) {
      if (player.connection != null) {
         ((ExtPlayNetworkHandler)player.connection).styledChat$setStyle(createStyleOf(player));
      }
   }

   @Nullable
   public static ChatStyleData getPersonalData(ServerPlayer player) {
      return (ChatStyleData)PlayerDataApi.getCustomDataFor(player, PLAYER_DATA);
   }

   public static ChatStyleData getOrCreatePersonalData(ServerPlayer player) {
      VersionedChatStyleData style = (VersionedChatStyleData)PlayerDataApi.getCustomDataFor(player, PLAYER_DATA);
      if (style == null) {
         style = new VersionedChatStyleData();
         PlayerDataApi.setCustomDataFor(player, PLAYER_DATA, style);
      }

      return style;
   }

   public static void clearPersonalStyleData(ServerPlayer player) {
      PlayerDataApi.setCustomDataFor(player, PLAYER_DATA, new VersionedChatStyleData());
   }

   public static ChatStyle createStyleOf(ServerPlayer player) {
      VersionedChatStyleData style = (VersionedChatStyleData)PlayerDataApi.getCustomDataFor(player, PLAYER_DATA);
      if (style == null) {
         style = new VersionedChatStyleData();
      } else {
         style = (VersionedChatStyleData)style.clone();
      }

      style.fillPermissionOptionProvider(player.createCommandSourceStack());
      return new ChatStyle(style);
   }

   public static Bound createParameters(Component override) {
      return new Bound(StyledChatMod.getMessageType(), override, null);
   }
}
