package eu.pb4.styledchat.config.data.old;

import eu.pb4.predicate.api.BuiltinPredicates;
import eu.pb4.predicate.api.MinecraftPredicate;
import eu.pb4.styledchat.config.data.ConfigData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.util.Tuple;

public class ConfigDataV2 {
   public static final int VERSION = 2;
   public int CONFIG_VERSION_DONT_TOUCH_THIS = 2;
   public String _comment = "Before changing anything, see https://github.com/Patbox/StyledChat#configuration";
   public ChatStyleDataV2 defaultStyle;
   public List<ConfigDataV2.PermissionPriorityStyle> permissionStyles = new ArrayList<>();
   public String petDeathMessage = "${default_message}";
   public Map<String, String> emoticons = new HashMap<>();
   public List<ConfigDataV2.PermissionEmotes> permissionEmoticons = new ArrayList<>();
   public boolean legacyChatFormatting = true;
   public boolean parseLinksInChat = true;
   public boolean enableMarkdown = true;
   public boolean allowModdedDecorators = true;
   public boolean sendFullMessageInChatPreview = false;
   public boolean requireChatPreviewForFormatting = false;
   public boolean sendAutoCompletionForTags = false;
   public boolean sendAutoCompletionForTagAliases = false;
   public boolean sendAutoCompletionForEmotes = true;
   public String linkStyle = "<underline><c:#7878ff>${link}";
   public String spoilerStyle = "<gray>${spoiler}";
   public String spoilerSymbol = "\u258c";
   public HashMap<String, Boolean> defaultEnabledFormatting = new HashMap<>();

   public ConfigDataV2() {
   }

   public ConfigData update() {
      ConfigData data = new ConfigData();
      data.defaultStyle = this.defaultStyle.update();
      data.defaultStyle.messages.petDeathMessage = this.petDeathMessage;
      data.defaultStyle.linkStyle = this.linkStyle;
      data.defaultStyle.spoilerStyle = this.spoilerStyle;
      data.defaultStyle.spoilerSymbol = this.spoilerSymbol;
      data.defaultStyle.emoticons.putAll(this.emoticons);

      for (Entry<String, Boolean> e : this.defaultEnabledFormatting.entrySet()) {
         if (e.getValue()) {
            data.defaultStyle.formatting.put(e.getKey(), true);
         }
      }

      data.autoCompletion.tagAliases = this.sendAutoCompletionForTagAliases;
      data.autoCompletion.tags = this.sendAutoCompletionForTags;
      data.autoCompletion.emoticons = this.sendAutoCompletionForEmotes;
      data.formatting.markdown = this.enableMarkdown;
      data.formatting.legacyChatFormatting = this.legacyChatFormatting;
      data.formatting.respectColors = this.allowModdedDecorators;
      data.formatting.parseLinksInChat = this.parseLinksInChat;
      ArrayList<Tuple<ConfigDataV2.PermissionPriorityStyle, ConfigDataV2.PermissionEmotes>> pairs = new ArrayList<>();

      for (ConfigDataV2.PermissionPriorityStyle x : this.permissionStyles) {
         pairs.add(new Tuple(x, null));
      }

      for (ConfigDataV2.PermissionEmotes x : this.permissionEmoticons) {
         boolean hasPair = false;

         for (Tuple<ConfigDataV2.PermissionPriorityStyle, ConfigDataV2.PermissionEmotes> pair : pairs) {
            if (pair.getB() == null
               && ((ConfigDataV2.PermissionPriorityStyle)pair.getA()).opLevel == x.opLevel
               && ((ConfigDataV2.PermissionPriorityStyle)pair.getA()).permission.equals(x.permission)) {
               hasPair = true;
               pair.setB(x);
               break;
            }
         }

         if (!hasPair) {
            pairs.add(new Tuple(null, x));
         }
      }

      for (Tuple<ConfigDataV2.PermissionPriorityStyle, ConfigDataV2.PermissionEmotes> pairx : pairs) {
         ConfigData.RequireChatStyleData style = new ConfigData.RequireChatStyleData();
         if (pairx.getA() != null) {
            style.require = this.createRequire(
               ((ConfigDataV2.PermissionPriorityStyle)pairx.getA()).permission, ((ConfigDataV2.PermissionPriorityStyle)pairx.getA()).opLevel
            );
            ((ConfigDataV2.PermissionPriorityStyle)pairx.getA()).style.copyInto(style);
         } else {
            style.require = this.createRequire(
               ((ConfigDataV2.PermissionEmotes)pairx.getB()).permission, ((ConfigDataV2.PermissionEmotes)pairx.getB()).opLevel
            );
         }

         if (pairx.getB() != null) {
            style.emoticons.putAll(((ConfigDataV2.PermissionEmotes)pairx.getB()).emoticons);
         }

         data.permissionStyles.add(style);
      }

      return data;
   }

   private MinecraftPredicate createRequire(String permission, int opLevel) {
      if (permission.isEmpty()) {
         return BuiltinPredicates.operatorLevel(opLevel);
      } else {
         return opLevel <= 4 && opLevel >= 1 ? BuiltinPredicates.modPermissionApi(permission, opLevel) : BuiltinPredicates.modPermissionApi(permission);
      }
   }

   public static class PermissionEmotes {
      public String permission = "";
      public int opLevel = 3;
      public Map<String, String> emoticons = Collections.EMPTY_MAP;

      public PermissionEmotes() {
      }
   }

   public static class PermissionPriorityStyle {
      public ChatStyleDataV2 style;
      public String permission = "";
      public int opLevel = 5;

      public PermissionPriorityStyle() {
      }
   }
}
