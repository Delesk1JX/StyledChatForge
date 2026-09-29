package eu.pb4.styledchat;

import dev.delesk1jx.styledchat.vendor.placeholders.api.PlaceholderContext;
import dev.delesk1jx.styledchat.vendor.placeholders.api.node.TextNode;
import eu.pb4.styledchat.config.ConfigManager;
import java.util.Map;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ChatType.Bound;
import org.jetbrains.annotations.Nullable;

public final class StyledChatStyles {
   public StyledChatStyles() {
   }

   public static Component getDisplayName(ServerPlayer player, Component vanillaDisplayName) {
      Component style = StyledChatUtils.getPersonalStyle(player).getDisplayName(player, vanillaDisplayName);
      return style != null ? style : ConfigManager.getConfig().getDisplayName(player, vanillaDisplayName);
   }

   public static Component getChat(ServerPlayer player, Component message) {
      Component style = StyledChatUtils.getPersonalStyle(player).getChat(player, message);
      return style != null ? style : ConfigManager.getConfig().getChat(player, message);
   }

   public static Component getJoin(ServerPlayer player) {
      Component style = StyledChatUtils.getPersonalStyle(player).getJoin(player);
      return style != null ? style : ConfigManager.getConfig().getJoin(player);
   }

   public static Component getJoinFirstTime(ServerPlayer player) {
      Component style = StyledChatUtils.getPersonalStyle(player).getJoinFirstTime(player);
      return style != null ? style : ConfigManager.getConfig().getJoinFirstTime(player);
   }

   public static Component getJoinRenamed(ServerPlayer player, String oldName) {
      Component style = StyledChatUtils.getPersonalStyle(player).getJoinRenamed(player, oldName);
      return style != null ? style : ConfigManager.getConfig().getJoinRenamed(player, oldName);
   }

   public static Component getLeft(ServerPlayer player) {
      Component style = StyledChatUtils.getPersonalStyle(player).getLeft(player);
      return style != null ? style : ConfigManager.getConfig().getLeft(player);
   }

   public static Component getDeath(ServerPlayer player, Component vanillaMessage) {
      Component style = StyledChatUtils.getPersonalStyle(player).getDeath(player, vanillaMessage);
      return style != null ? style : ConfigManager.getConfig().getDeath(player, vanillaMessage);
   }

   public static Component getAdvancementTask(ServerPlayer player, Component advancement) {
      Component style = StyledChatUtils.getPersonalStyle(player).getAdvancementTask(player, advancement);
      return style != null ? style : ConfigManager.getConfig().getAdvancementTask(player, advancement);
   }

   public static Component getAdvancementGoal(ServerPlayer player, Component advancement) {
      Component style = StyledChatUtils.getPersonalStyle(player).getAdvancementGoal(player, advancement);
      return style != null ? style : ConfigManager.getConfig().getAdvancementGoal(player, advancement);
   }

   public static Component getAdvancementChallenge(ServerPlayer player, Component advancement) {
      Component style = StyledChatUtils.getPersonalStyle(player).getAdvancementChallenge(player, advancement);
      return style != null ? style : ConfigManager.getConfig().getAdvancementChallenge(player, advancement);
   }

   public static Component getSayCommand(CommandSourceStack source, Component message) {
      if (source.isPlayer()) {
         Component style = StyledChatUtils.getPersonalStyle(source.getPlayer()).getSayCommand(source, message);
         if (style != null) {
            return style;
         }
      }

      return ConfigManager.getConfig().getSayCommand(source, message);
   }

   public static Component getMeCommand(CommandSourceStack source, Component message) {
      if (source.isPlayer()) {
         Component style = StyledChatUtils.getPersonalStyle(source.getPlayer()).getMeCommand(source, message);
         if (style != null) {
            return style;
         }
      }

      return ConfigManager.getConfig().getMeCommand(source, message);
   }

   public static Component getPrivateMessageSent(Component sender, Component receiver, Component message, CommandSourceStack source) {
      if (source.isPlayer()) {
         Component style = StyledChatUtils.getPersonalStyle(source.getPlayer())
            .getPrivateMessageSent(sender, receiver, message, PlaceholderContext.of(source));
         if (style != null) {
            return style;
         }
      }

      return ConfigManager.getConfig().getPrivateMessageSent(sender, receiver, message, source);
   }

   public static Component getPrivateMessageReceived(Component sender, Component receiver, Component message, CommandSourceStack source) {
      if (source.isPlayer()) {
         Component style = StyledChatUtils.getPersonalStyle(source.getPlayer())
            .getPrivateMessageReceived(sender, receiver, message, PlaceholderContext.of(source));
         if (style != null) {
            return style;
         }
      }

      return ConfigManager.getConfig().getPrivateMessageReceived(sender, receiver, message, source);
   }

   public static Component getTeamChatSent(Component team, Component displayName, Component message, CommandSourceStack source) {
      if (source.isPlayer()) {
         Component style = StyledChatUtils.getPersonalStyle(source.getPlayer()).getTeamChatSent(team, displayName, message, source);
         if (style != null) {
            return style;
         }
      }

      return ConfigManager.getConfig().getTeamChatSent(team, displayName, message, source);
   }

   public static Component getTeamChatReceived(Component team, Component displayName, Component message, CommandSourceStack source) {
      if (source.isPlayer()) {
         Component style = StyledChatUtils.getPersonalStyle(source.getPlayer()).getTeamChatReceived(team, displayName, message, source);
         if (style != null) {
            return style;
         }
      }

      return ConfigManager.getConfig().getTeamChatReceived(team, displayName, message, source);
   }

   public static Component getPetDeath(TamableAnimal entity, Component vanillaMessage) {
      return ConfigManager.getConfig().getPetDeath(entity, vanillaMessage);
   }

   public static Map<String, TextNode> getEmotes(CommandSourceStack source) {
      return ConfigManager.getConfig().getEmotes(source);
   }

   public static Component getCustom(ResourceLocation identifier, Component displayName, Component message, @Nullable Component receiver, CommandSourceStack source) {
      if (source.isPlayer()) {
         Component style = StyledChatUtils.getPersonalStyle(source.getPlayer()).getCustom(identifier, displayName, message, receiver, source);
         if (style != null) {
            return style;
         }
      }

      Component out = ConfigManager.getConfig().getCustom(identifier, displayName, message, receiver, source);
      if (out != null) {
         return out;
      } else {
         ChatType type = (ChatType)StyledChatMod.server.registryAccess().registryOrThrow(Registries.CHAT_TYPE).get(identifier);
         if (type == null) {
            return Component.empty();
         } else {
            Bound params = type.bind(displayName);
            if (receiver != null) {
               params = params.withTargetName(receiver);
            }

            return type.chat().decorate(message, params);
         }
      }
   }
}
