package eu.pb4.styledchat.config.data;

import com.google.gson.annotations.SerializedName;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import eu.pb4.loader.Options;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.Nullable;

public class ChatStyleData implements Cloneable {
   public static ChatStyleData DEFAULT = createDefault();
   public static final Map<String, ChatStyleData.PropertyGetSet> PROPERTIES = new HashMap<>();
   @SerializedName("display_name")
   public String displayName;
   @SerializedName("message_formats")
   public ChatStyleData.Messages messages = new ChatStyleData.Messages();
   @SerializedName("link_style")
   public String linkStyle;
   @SerializedName("mention_style")
   public String mentionStyle;
   @SerializedName("spoiler_style")
   public String spoilerStyle;
   @SerializedName("spoiler_symbol")
   public String spoilerSymbol;
   @SerializedName("formatting")
   public Map<String, Boolean> formatting = new HashMap<>();
   @SerializedName("emoticons")
   public LinkedHashMap<String, String> emoticons = new LinkedHashMap<>();
   @SerializedName("custom_message_types")
   public Map<String, String> custom = new HashMap<>();

   public ChatStyleData() {
   }

   public ChatStyleData clone() {
      try {
         ChatStyleData base = (ChatStyleData)super.clone();
         base.messages = this.messages.clone();
         base.formatting = new HashMap<>(this.formatting);
         base.emoticons = new LinkedHashMap<>(this.emoticons);
         base.custom = new HashMap<>(this.custom);
         return base;
      } catch (CloneNotSupportedException var2) {
         var2.printStackTrace();
         return null;
      }
   }

   public void fillPermissionOptionProvider(CommandSourceStack source) {
      for (Entry<String, ChatStyleData.PropertyGetSet> prop : PROPERTIES.entrySet()) {
         if (prop.getValue().get(this) == null) {
            Optional<String> value = Options.get(source, "styled_chat." + prop.getKey());
            if (value.isPresent()) {
               prop.getValue().set(this, value.get());
            }
         }
      }
   }

   public static ChatStyleData createDefault() {
      ChatStyleData data = new ChatStyleData();
      data.displayName = "${default}";
      data.messages.chat = "<${player}> ${message}";
      data.messages.joinedGame = "<yellow><lang:multiplayer.player.joined:'${player}'></yellow>";
      data.messages.joinedAfterNameChange = "<yellow><lang:multiplayer.player.joined.renamed:'${player}':'${old_name}'></yellow>";
      data.messages.joinedForFirstTime = "<yellow><lang:multiplayer.player.joined:'${player}'></yellow>";
      data.messages.leftGame = "<yellow><lang:multiplayer.player.left:'${player}'></yellow>";
      data.messages.baseDeath = "${default_message}";
      data.messages.advancementTask = "<lang:chat.type.advancement.task:'${player}':'${advancement}'>";
      data.messages.advancementGoal = "<lang:chat.type.advancement.goal:'${player}':'${advancement}'>";
      data.messages.advancementChallenge = "<lang:chat.type.advancement.challenge:'${player}':'${advancement}'>";
      data.messages.sentTeamChat = "<lang:'chat.type.team.sent':'<hover\\:\\'<lang\\:chat.type.team.hover>\\'><suggest_command\\:\\'/teammsg \\'>${team}':'${displayName}':'${message}'>";
      data.messages.receivedTeamChat = "<lang:'chat.type.team.text':'<hover\\:\\'<lang\\:chat.type.team.hover>\\'><suggest_command\\:\\'/teammsg \\'>${team}':'${displayName}':'${message}'>";
      data.messages.privateMessageSent = "<gray><italic><lang:commands.message.display.outgoing:'${receiver}':'${message}'>";
      data.messages.privateMessageReceived = "<gray><italic><lang:commands.message.display.incoming:'${sender}':'${message}'>";
      data.messages.sayCommandMessage = "[${player}] ${message}";
      data.messages.meCommandMessage = "<lang:'chat.type.emote':'${player}':'${message}'>";
      data.messages.petDeathMessage = "${default_message}";
      data.linkStyle = "<underline><c:#7878ff>${link}";
      data.mentionStyle = "<c:#7878ff>%player:displayname%";
      data.spoilerStyle = "<gray>${spoiler}";
      data.spoilerSymbol = "\u258c";
      data.formatting.put("spoiler", true);
      data.formatting.put("bold", true);
      data.formatting.put("italic", true);
      data.formatting.put("strikethrough", true);
      data.formatting.put("underline", true);

      for (ChatFormatting formatting : ChatFormatting.values()) {
         if (formatting.isColor()) {
            data.formatting.put(formatting.getName(), true);
         }
      }

      data.emoticons.put("$emojibase:builtin:joypixels", "${emoji}");
      data.emoticons.put("shrug", "\u00af\\_(\u30c4)_/\u00af");
      data.emoticons.put("table", "(\u256f\u00b0\u25a1\u00b0\uff09\u256f\ufe35 \u253b\u2501\u253b");
      data.emoticons.put("sword", "\ud83d\udde1");
      data.emoticons.put("bow", "\ud83c\udff9");
      data.emoticons.put("trident", "\ud83d\udd31");
      data.emoticons.put("rod", "\ud83c\udfa3");
      data.emoticons.put("potion", "\ud83e\uddea");
      data.emoticons.put("shears", "\u2702");
      data.emoticons.put("bucket", "\ud83e\udea3");
      data.emoticons.put("bell", "\ud83d\udd14");
      data.emoticons.put("item", "[%player:equipment_slot mainhand%]");
      data.emoticons.put("pos", "%player:pos_x% %player:pos_y% %player:pos_z%");
      return data;
   }

   public void fillMissing() {
      this.displayName = Objects.requireNonNullElse(this.displayName, DEFAULT.displayName);
      this.messages.chat = Objects.requireNonNullElse(this.messages.chat, DEFAULT.messages.chat);
      this.messages.joinedGame = Objects.requireNonNullElse(this.messages.joinedGame, DEFAULT.messages.joinedGame);
      this.messages.joinedAfterNameChange = Objects.requireNonNullElse(this.messages.joinedAfterNameChange, DEFAULT.messages.joinedAfterNameChange);
      this.messages.joinedForFirstTime = Objects.requireNonNullElse(this.messages.joinedForFirstTime, DEFAULT.messages.joinedForFirstTime);
      this.messages.leftGame = Objects.requireNonNullElse(this.messages.leftGame, DEFAULT.messages.leftGame);
      this.messages.baseDeath = Objects.requireNonNullElse(this.messages.baseDeath, DEFAULT.messages.baseDeath);
      this.messages.advancementTask = Objects.requireNonNullElse(this.messages.advancementTask, DEFAULT.messages.advancementTask);
      this.messages.advancementChallenge = Objects.requireNonNullElse(this.messages.advancementChallenge, DEFAULT.messages.advancementChallenge);
      this.messages.advancementGoal = Objects.requireNonNullElse(this.messages.advancementGoal, DEFAULT.messages.advancementGoal);
      this.messages.privateMessageReceived = Objects.requireNonNullElse(this.messages.privateMessageReceived, DEFAULT.messages.privateMessageReceived);
      this.messages.privateMessageSent = Objects.requireNonNullElse(this.messages.privateMessageSent, DEFAULT.messages.privateMessageSent);
      this.messages.sentTeamChat = Objects.requireNonNullElse(this.messages.sentTeamChat, DEFAULT.messages.sentTeamChat);
      this.messages.receivedTeamChat = Objects.requireNonNullElse(this.messages.receivedTeamChat, DEFAULT.messages.receivedTeamChat);
      this.messages.sayCommandMessage = Objects.requireNonNullElse(this.messages.sayCommandMessage, DEFAULT.messages.sayCommandMessage);
      this.messages.meCommandMessage = Objects.requireNonNullElse(this.messages.meCommandMessage, DEFAULT.messages.meCommandMessage);
      this.messages.petDeathMessage = Objects.requireNonNullElse(this.messages.petDeathMessage, DEFAULT.messages.petDeathMessage);
      this.linkStyle = Objects.requireNonNullElse(this.linkStyle, DEFAULT.linkStyle);
      this.spoilerStyle = Objects.requireNonNullElse(this.spoilerStyle, DEFAULT.spoilerStyle);
      this.spoilerSymbol = Objects.requireNonNullElse(this.spoilerSymbol, DEFAULT.spoilerSymbol);
      this.custom = Objects.requireNonNullElse(this.custom, new HashMap<>());

      for (String key : DEFAULT.formatting.keySet()) {
         if (!this.formatting.containsKey(key)) {
            this.formatting.put(key, DEFAULT.formatting.get(key));
         }
      }
   }

   static {
      for (Field field : ChatStyleData.class.getFields()) {
         if (!Modifier.isStatic(field.getModifiers())) {
            if (field.getType() == String.class) {
               PROPERTIES.put(((SerializedName)field.getAnnotation(SerializedName.class)).value(), ChatStyleData.PropertyGetSet.of(field));
            } else if (field.getType() == ChatStyleData.Messages.class) {
               for (Field field2 : ChatStyleData.Messages.class.getFields()) {
                  PROPERTIES.put(
                     "message_formats." + ((SerializedName)field2.getAnnotation(SerializedName.class)).value(), ChatStyleData.PropertyGetSet.ofMessage(field2)
                  );
               }
            }
         }
      }
   }

   public static class Messages implements Cloneable {
      @SerializedName("chat")
      public String chat;
      @SerializedName("joined_the_game")
      public String joinedGame;
      @SerializedName("joined_after_name_change")
      public String joinedAfterNameChange;
      @SerializedName("joined_for_first_time")
      public String joinedForFirstTime;
      @SerializedName("left_game")
      public String leftGame;
      @SerializedName("base_death")
      public String baseDeath;
      @SerializedName("advancement_task")
      public String advancementTask;
      @SerializedName("advancement_challenge")
      public String advancementChallenge;
      @SerializedName("advancement_goal")
      public String advancementGoal;
      @SerializedName("sent_team_chat")
      public String sentTeamChat;
      @SerializedName("received_team_chat")
      public String receivedTeamChat;
      @SerializedName("sent_private_message")
      public String privateMessageSent;
      @SerializedName("received_private_message")
      public String privateMessageReceived;
      @SerializedName("say_command")
      public String sayCommandMessage;
      @SerializedName("me_command")
      public String meCommandMessage;
      @SerializedName("pet_death")
      public String petDeathMessage;

      public Messages() {
      }

      public ChatStyleData.Messages clone() {
         try {
            return (ChatStyleData.Messages)super.clone();
         } catch (CloneNotSupportedException var2) {
            return null;
         }
      }
   }

   public interface PropertyGetSet {
      static ChatStyleData.PropertyGetSet of(Field field) {
         return new ChatStyleData.PropertyGetSet() {
            @Override
            public void set(ChatStyleData data, String value) {
               try {
                  field.set(data, value);
               } catch (IllegalAccessException var4) {
                  var4.printStackTrace();
               }
            }

            @Override
            public String get(ChatStyleData data) {
               try {
                  return (String)field.get(data);
               } catch (IllegalAccessException var3) {
                  var3.printStackTrace();
                  return null;
               }
            }
         };
      }

      static ChatStyleData.PropertyGetSet ofCustom(String key) {
         return new ChatStyleData.PropertyGetSet() {
            @Override
            public void set(ChatStyleData data, String value) {
               if (value == null) {
                  data.custom.remove(key);
               } else {
                  data.custom.put(key, value);
               }
            }

            @Override
            public String get(ChatStyleData data) {
               return data.custom.get(key);
            }
         };
      }

      static ChatStyleData.PropertyGetSet ofMessage(Field field) {
         return new ChatStyleData.PropertyGetSet() {
            @Override
            public void set(ChatStyleData data, String value) {
               try {
                  field.set(data.messages, value);
               } catch (IllegalAccessException var4) {
                  var4.printStackTrace();
               }
            }

            @Override
            public String get(ChatStyleData data) {
               try {
                  return (String)field.get(data.messages);
               } catch (IllegalAccessException var3) {
                  var3.printStackTrace();
                  return null;
               }
            }
         };
      }

      void set(ChatStyleData var1, String var2);

      @Nullable
      String get(ChatStyleData var1);
   }
}
