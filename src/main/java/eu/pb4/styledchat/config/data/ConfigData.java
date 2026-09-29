package eu.pb4.styledchat.config.data;

import com.google.gson.annotations.SerializedName;
import eu.pb4.predicate.api.MinecraftPredicate;
import java.util.ArrayList;
import java.util.List;

public class ConfigData {
   public static final int VERSION = 3;
   @SerializedName("CONFIG_VERSION_DONT_TOUCH_THIS")
   public int configVersion = 3;
   public String _comment = "Before changing anything, see https://github.com/Patbox/StyledChat#configuration";
   @SerializedName("text_formatting")
   public ConfigData.Formatting formatting = new ConfigData.Formatting();
   @SerializedName("auto_completion")
   public ConfigData.AutoCompletion autoCompletion = new ConfigData.AutoCompletion();
   @SerializedName("default")
   public ChatStyleData defaultStyle = ChatStyleData.createDefault();
   @SerializedName("styles")
   public List<ConfigData.RequireChatStyleData> permissionStyles = new ArrayList<>();

   public ConfigData() {
   }

   public static class AutoCompletion {
      @SerializedName("tags")
      public boolean tags = false;
      @SerializedName("tag_aliases")
      public boolean tagAliases = false;
      @SerializedName("emoticons")
      public boolean emoticons = false;

      public AutoCompletion() {
      }
   }

   public static class Formatting {
      @SerializedName("legacy_formatting")
      public boolean legacyChatFormatting = false;
      @SerializedName("parse_links")
      public boolean parseLinksInChat = true;
      @SerializedName("parse_mentions")
      public boolean parseMentionsInChat = false;
      @SerializedName("markdown")
      public boolean markdown = true;
      @SerializedName("respect_colors_client_setting")
      public boolean respectColors = true;

      public Formatting() {
      }
   }

   public static class RequireChatStyleData extends ChatStyleData {
      @SerializedName("require")
      public MinecraftPredicate require;

      public RequireChatStyleData() {
      }
   }
}
