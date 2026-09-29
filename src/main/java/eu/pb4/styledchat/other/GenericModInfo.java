package eu.pb4.styledchat.other;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import eu.pb4.loader.Platform;
import eu.pb4.styledchat.StyledChatMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

/**
 * Builds the "about" text printed by {@code /about} and shown in the server list.
 *
 * The icon is rendered pixel by pixel out of {@code icon_small.png}, exactly like the Fabric
 * original; only the source of the mod metadata changed, because Forge exposes it through
 * {@link Platform} rather than through a loader container.
 */
public class GenericModInfo {
   private static Component[] icon = new Component[0];
   private static Component[] about = new Component[0];
   private static Component[] consoleAbout = new Component[0];

   private GenericModInfo() {
   }

   public static void build() {
      Platform.ModInfo info = Platform.getMod(StyledChatMod.MOD_ID).orElse(null);

      if (info == null) {
         about = new Component[] {Component.literal("Styled Chat: mod metadata unavailable")};
         consoleAbout = about;
         return;
      }

      boolean useIcon = true;
      String chr = "█";
      ArrayList<MutableComponent> icon = new ArrayList<>();

      try (InputStream is = Platform.openModResource(StyledChatMod.MOD_ID, "assets/styledchatforge/icon_small.png")
              .orElseThrow(() -> new IllegalStateException("icon_small.png is missing"))) {
         BufferedImage source = ImageIO.read(is);

         for (int y = 0; y < source.getHeight(); y++) {
            MutableComponent base = Component.empty();
            int line = 0;
            int color = source.getRGB(0, y) & 16777215;

            for (int x = 0; x < source.getWidth(); x++) {
               int colorPixel = source.getRGB(x, y) & 16777215;
               if (color == colorPixel) {
                  line++;
               } else {
                  int runColor = color;
                  base.append(Component.literal(chr.repeat(line)).withStyle(style -> style.withColor(runColor)));
                  color = colorPixel;
                  line = 1;
               }
            }

            int lastColor = color;
            base.append(Component.literal(chr.repeat(line)).withStyle(style -> style.withColor(lastColor)));
            icon.add(base);
         }
      } catch (Throwable var12) {
         useIcon = false;
         var12.printStackTrace();

         while (icon.size() < 16) {
            icon.add(Component.literal("/!\\ [ Invalid icon file ] /!\\")
                  .withStyle(style -> style.withColor(16711680).withBold(true)));
         }
      }

      GenericModInfo.icon = icon.toArray(new Component[0]);
      ArrayList<MutableComponent> about = new ArrayList<>();
      ArrayList<Component> plainIcon = new ArrayList<>(List.copyOf(icon));
      ArrayList<Component> output = new ArrayList<>();

      try {
         MutableComponent title = Component.literal(info.name());
         about.add(title.copy()
               .withStyle(style -> style
                     .withColor(ChatFormatting.BOLD)
                     .withItalic(false)
                     .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://pb4.eu"))));
         about.add(Component.literal("Version: ")
               .withStyle(style -> style.withColor(16245159))
               .append(Component.literal(info.version()).withStyle(style -> style.withColor(ChatFormatting.GRAY))));
         plainIcon.addAll(about);
         plainIcon.add(Component.empty());
         plainIcon.add(Component.literal(info.description()));

         ArrayList<String> contributors = new ArrayList<>(StyledChatMod.CONTRIBUTORS);

         about.add(Component.literal("")
               .append(Component.literal("Contributors")
                     .withStyle(style -> style
                           .withColor(ChatFormatting.DARK_AQUA)
                           .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                 Component.literal(String.join(", ", contributors))))))
               .withStyle(style -> style.withColor(ChatFormatting.RESET)));
         about.add(Component.empty());

         ArrayList<String> desc = new ArrayList<>(List.of(info.description().split(" ")));
         if (!desc.isEmpty()) {
            StringBuilder descPart = new StringBuilder();

            while (!desc.isEmpty()) {
               (descPart.isEmpty() ? descPart : descPart.append(" ")).append(desc.remove(0));
               if (descPart.length() > 16) {
                  about.add(Component.literal(descPart.toString()).withStyle(style -> style.withColor(ChatFormatting.GRAY)));
                  descPart = new StringBuilder();
               }
            }

            if (descPart.length() > 0) {
               about.add(Component.literal(descPart.toString()).withStyle(style -> style.withColor(ChatFormatting.GRAY)));
            }
         }

         if (GenericModInfo.icon.length > about.size() + 2 && useIcon) {
            int a = 0;

            for (int i = 0; i < GenericModInfo.icon.length; i++) {
               if (i == (GenericModInfo.icon.length - about.size() - 1) / 2 + a && a < about.size()) {
                  output.add(GenericModInfo.icon[i].copy()
                        .append(Component.literal("  ")
                              .withStyle(style -> style.withColor(ChatFormatting.GRAY).withItalic(false))
                              .append(about.get(a++))));
               } else {
                  output.add(GenericModInfo.icon[i]);
               }
            }
         } else {
            Collections.addAll(output, GenericModInfo.icon);
            output.addAll(about);
         }
      } catch (Exception var11) {
         var11.printStackTrace();
         MutableComponent invalid = Component.literal("/!\\ [ Invalid about mod info ] /!\\")
               .withStyle(style -> style.withColor(16711680).withBold(true));
         output.add(invalid);
         about.add(invalid);
      }

      GenericModInfo.about = output.toArray(new Component[0]);
      consoleAbout = plainIcon.toArray(new Component[0]);
   }

   public static Component[] getIcon() {
      return icon;
   }

   public static Component[] getAboutFull() {
      return about;
   }

   public static Component[] getAboutConsole() {
      return consoleAbout;
   }
}
