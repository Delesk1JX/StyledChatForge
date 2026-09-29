package eu.pb4.styledchat.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.styledchat.StyledChatMod;
import eu.pb4.styledchat.StyledChatUtils;
import eu.pb4.styledchat.config.ConfigManager;
import eu.pb4.styledchat.config.data.ChatStyleData;
import eu.pb4.styledchat.other.GenericModInfo;
import java.util.Collection;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;
import eu.pb4.loader.Permissions;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.registries.Registries;
import net.minecraft.commands.Commands.CommandSelection;

public class Commands {
   public Commands() {
   }

   public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, CommandSelection environment) {
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)net.minecraft.commands.Commands.literal(
                              "styledchat"
                           )
                           .requires(Permissions.require("styledchat.main", true)))
                        .executes(Commands::about))
                     .then(
                        ((LiteralArgumentBuilder)net.minecraft.commands.Commands.literal("reload").requires(Permissions.require("styledchat.reload", 3)))
                           .executes(Commands::reloadConfig)
                     ))
                  .then(
                     ((LiteralArgumentBuilder)net.minecraft.commands.Commands.literal("set").requires(Permissions.require("styledchat.set", 2)))
                        .then(
                           fillWithProperties(
                              net.minecraft.commands.Commands.argument("players", EntityArgument.players()),
                              (x, p) -> x.then(
                                    net.minecraft.commands.Commands.argument("value", StringArgumentType.greedyString())
                                       .executes(ctx -> setProperty(ctx, (ChatStyleData.PropertyGetSet)p.apply(ctx)))
                                 )
                           )
                        )
                  ))
               .then(
                  ((LiteralArgumentBuilder)net.minecraft.commands.Commands.literal("get").requires(Permissions.require("styledchat.get", 2)))
                     .then(
                        fillWithProperties(
                           net.minecraft.commands.Commands.argument("player", EntityArgument.player()),
                           (x, p) -> x.executes(ctx -> getProperty(ctx, (ChatStyleData.PropertyGetSet)p.apply(ctx)))
                        )
                     )
               ))
            .then(
               ((LiteralArgumentBuilder)net.minecraft.commands.Commands.literal("clear").requires(Permissions.require("styledchat.clear", 3)))
                  .then(
                     fillWithProperties(
                           net.minecraft.commands.Commands.argument("players", EntityArgument.players()),
                           (x, p) -> x.executes(ctx -> clearProperty(ctx, (ChatStyleData.PropertyGetSet)p.apply(ctx)))
                        )
                        .then(net.minecraft.commands.Commands.literal("*").executes(ctx -> clearProperty(ctx, null)))
                  )
            )
      );
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)net.minecraft.commands.Commands.literal("tellform").requires(Permissions.require("styledchat.tellform", 2)))
            .then(
               net.minecraft.commands.Commands.argument("targets", EntityArgument.players())
                  .then(
                     net.minecraft.commands.Commands.argument("message", StringArgumentType.greedyString())
                        .executes(
                           context -> {
                              int i = 0;
                              Component parsed = Placeholders.parseText(
                                 TextNode.asSingle(
                                    StyledChatUtils.createParser((CommandSourceStack)context.getSource())
                                       .parseNodes(TextNode.of(StringArgumentType.getString(context, "message")))
                                 ),
                                 PlaceholderContext.of((CommandSourceStack)context.getSource())
                              );

                              for (ServerPlayer player : EntityArgument.getPlayers(context, "targets")) {
                                 player.sendSystemMessage(parsed);
                              }

                              return i;
                           }
                        )
                  )
            )
      );
   }

   private static int getProperty(CommandContext<CommandSourceStack> context, ChatStyleData.PropertyGetSet propertyGetSet) throws CommandSyntaxException {
      ServerPlayer player = EntityArgument.getPlayer(context, "player");
      ChatStyleData data = StyledChatUtils.getPersonalData(player);
      if (data == null) {
         ((CommandSourceStack)context.getSource()).sendSuccess(() -> Component.literal("<not set>").withStyle(ChatFormatting.ITALIC), false);
         return 0;
      } else {
         String val = propertyGetSet.get(data);
         if (val == null) {
            ((CommandSourceStack)context.getSource()).sendSuccess(() -> Component.literal("<not set>").withStyle(ChatFormatting.ITALIC), false);
            return 0;
         } else {
            ((CommandSourceStack)context.getSource()).sendSuccess(() -> Component.literal(val), false);
            return 1;
         }
      }
   }

   private static int setProperty(CommandContext<CommandSourceStack> context, ChatStyleData.PropertyGetSet propertySet) throws CommandSyntaxException {
      Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
      String val = StringArgumentType.getString(context, "value");

      for (ServerPlayer player : players) {
         propertySet.set(StyledChatUtils.getOrCreatePersonalData(player), val);
         StyledChatUtils.updateStyle(player);
      }

      ((CommandSourceStack)context.getSource()).sendSuccess(() -> Component.literal("Changed style of " + players.size() + " player(s)"), false);
      return players.size();
   }

   private static int clearProperty(CommandContext<CommandSourceStack> context, ChatStyleData.PropertyGetSet propertySet) throws CommandSyntaxException {
      Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");

      for (ServerPlayer player : players) {
         if (propertySet != null) {
            propertySet.set(StyledChatUtils.getOrCreatePersonalData(player), null);
         } else {
            StyledChatUtils.clearPersonalStyleData(player);
         }

         StyledChatUtils.updateStyle(player);
      }

      ((CommandSourceStack)context.getSource()).sendSuccess(() -> Component.literal("Cleared style for " + players.size() + " player(s)"), false);
      return players.size();
   }

   private static ArgumentBuilder<CommandSourceStack, ?> fillWithProperties(
      ArgumentBuilder<CommandSourceStack, ?> base,
      BiConsumer<ArgumentBuilder<CommandSourceStack, ?>, Function<CommandContext<CommandSourceStack>, ChatStyleData.PropertyGetSet>> command
   ) {
      for (Entry<String, ChatStyleData.PropertyGetSet> prop : ChatStyleData.PROPERTIES.entrySet()) {
         LiteralArgumentBuilder<CommandSourceStack> x = net.minecraft.commands.Commands.literal(prop.getKey());
         command.accept(x, ctx -> prop.getValue());
         base = base.then(x);
      }

      RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> x = net.minecraft.commands.Commands.argument("id", ResourceLocationArgument.id()).suggests((context, builder) -> {
         for (ResourceLocation id : ((CommandSourceStack)context.getSource()).getServer().registryAccess().registryOrThrow(Registries.CHAT_TYPE).keySet()) {
            if (!id.getNamespace().equals("minecraft") && !id.equals(StyledChatMod.MESSAGE_TYPE_ID.location())) {
               builder.suggest(id.toString());
            }
         }

         return builder.buildFuture();
      });
      command.accept(x, ctx -> ChatStyleData.PropertyGetSet.ofCustom(ResourceLocationArgument.getId(ctx, "id").toString()));
      return base.then(net.minecraft.commands.Commands.literal("custom").then(x));
   }

   private static int reloadConfig(CommandContext<CommandSourceStack> context) {
      Set<String> old = ConfigManager.getConfig().allPossibleAutoCompletionKeys;
      if (ConfigManager.loadConfig()) {
         ((CommandSourceStack)context.getSource()).sendSuccess(() -> Component.literal("Reloaded config!"), false);

         for (ServerPlayer player : ((CommandSourceStack)context.getSource()).getServer().getPlayerList().getPlayers()) {
            StyledChatUtils.sendAutoCompletion(player, old);
         }
      } else {
         ((CommandSourceStack)context.getSource())
            .sendFailure(
               Component.literal("Error occurred while reloading config! Check console for more information!").withStyle(ChatFormatting.RED)
            );
      }

      return 1;
   }

   private static int about(CommandContext<CommandSourceStack> context) {
      for (Component text : ((CommandSourceStack)context.getSource()).getEntity() instanceof ServerPlayer
         ? GenericModInfo.getAboutFull()
         : GenericModInfo.getAboutConsole()) {
         ((CommandSourceStack)context.getSource()).sendSuccess(() -> text, false);
      }

      return 1;
   }
}
