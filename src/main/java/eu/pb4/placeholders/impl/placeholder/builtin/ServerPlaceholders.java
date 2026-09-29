package eu.pb4.placeholders.impl.placeholder.builtin;

import eu.pb4.loader.Platform;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.loader.Platform;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.loader.Platform;
import eu.pb4.placeholders.impl.GeneralUtils;
import eu.pb4.loader.Platform;
import java.lang.management.ManagementFactory;
import eu.pb4.loader.Platform;
import java.lang.management.MemoryMXBean;
import eu.pb4.loader.Platform;
import java.lang.management.MemoryUsage;
import eu.pb4.loader.Platform;
import java.lang.ref.WeakReference;
import eu.pb4.loader.Platform;
import java.text.SimpleDateFormat;
import eu.pb4.loader.Platform;
import java.util.Collection;
import eu.pb4.loader.Platform;
import java.util.Date;
import eu.pb4.loader.Platform;
import java.util.Objects;
import eu.pb4.loader.Platform;
import java.util.Optional;
import eu.pb4.loader.Platform;
import net.minecraft.ChatFormatting;
import eu.pb4.loader.Platform;
import net.minecraft.network.chat.Component;
import eu.pb4.loader.Platform;
import net.minecraft.world.scores.Objective;
import eu.pb4.loader.Platform;
import net.minecraft.world.scores.Score;
import eu.pb4.loader.Platform;
import net.minecraft.network.protocol.status.ServerStatus;
import eu.pb4.loader.Platform;
import net.minecraft.resources.ResourceLocation;
import eu.pb4.loader.Platform;
import net.minecraft.server.ServerScoreboard;
import eu.pb4.loader.Platform;
import net.minecraft.server.MinecraftServer;
import eu.pb4.loader.Platform;
import org.apache.commons.lang3.time.DurationFormatUtils;

public class ServerPlaceholders {
   public ServerPlaceholders() {
   }

   public static void register() {
      Placeholders.register(new ResourceLocation("server", "tps"), (ctx, arg) -> {
         double tps = (double)(1000.0F / Math.max(ctx.server().getAverageTickTime(), 50.0F));
         String format = "%.1f";
         if (arg != null) {
            try {
               int x = Integer.parseInt(arg);
               format = "%." + x + "f";
            } catch (Exception var6) {
               format = "%.1f";
            }
         }

         return PlaceholderResult.value(String.format(format, tps));
      });
      Placeholders.register(
         new ResourceLocation("server", "tps_colored"),
         (ctx, arg) -> {
            double tps = (double)(1000.0F / Math.max(ctx.server().getAverageTickTime(), 50.0F));
            String format = "%.1f";
            if (arg != null) {
               try {
                  int x = Integer.parseInt(arg);
                  format = "%." + x + "f";
               } catch (Exception var6) {
                  format = "%.1f";
               }
            }

            return PlaceholderResult.value(
               Component.literal(String.format(format, tps))
                  .withStyle(tps > 19.0 ? ChatFormatting.GREEN : (tps > 16.0 ? ChatFormatting.GOLD : ChatFormatting.RED))
            );
         }
      );
      Placeholders.register(new ResourceLocation("server", "mspt"), (ctx, arg) -> PlaceholderResult.value(String.format("%.0f", ctx.server().getAverageTickTime())));
      Placeholders.register(
         new ResourceLocation("server", "mspt_colored"),
         (ctx, arg) -> {
            float x = ctx.server().getAverageTickTime();
            return PlaceholderResult.value(
               Component.literal(String.format("%.0f", x))
                  .withStyle(x < 45.0F ? ChatFormatting.GREEN : (x < 51.0F ? ChatFormatting.GOLD : ChatFormatting.RED))
            );
         }
      );
      Placeholders.register(new ResourceLocation("server", "time"), (ctx, arg) -> {
         SimpleDateFormat format = new SimpleDateFormat(arg != null ? arg : "HH:mm:ss");
         return PlaceholderResult.value(format.format(new Date(System.currentTimeMillis())));
      });
      var ref = new Object() {
         WeakReference<MinecraftServer> server;
         long ms;
      };
      Placeholders.register(
         new ResourceLocation("server", "uptime"),
         (ctx, arg) -> {
            if (ref.server == null || !ref.server.refersTo(ctx.server())) {
               ref.server = new WeakReference<>(ctx.server());
               ref.ms = System.currentTimeMillis() - (long)ctx.server().getTickCount() * 50L;
            }

            return PlaceholderResult.value(
               arg != null
                  ? DurationFormatUtils.formatDuration(System.currentTimeMillis() - ref.ms, arg, true)
                  : GeneralUtils.durationToString((System.currentTimeMillis() - ref.ms) / 1000L)
            );
         }
      );
      Placeholders.register(new ResourceLocation("server", "version"), (ctx, arg) -> PlaceholderResult.value(ctx.server().getServerVersion()));
      Placeholders.register(new ResourceLocation("server", "motd"), (ctx, arg) -> {
         ServerStatus metadata = ctx.server().getStatus();
         return metadata == null ? PlaceholderResult.invalid("Server metadata missing!") : PlaceholderResult.value(metadata.description());
      });
      Placeholders.register(new ResourceLocation("server", "mod_version"), (ctx, arg) -> {
         if (arg != null) {
            Optional<Platform.ModInfo> container = Platform.getMod(arg);
            if (container.isPresent()) {
               return PlaceholderResult.value(Component.literal(container.get().version()));
            }
         }

         return PlaceholderResult.invalid("Invalid argument");
      });
      Placeholders.register(new ResourceLocation("server", "mod_name"), (ctx, arg) -> {
         if (arg != null) {
            Optional<Platform.ModInfo> container = Platform.getMod(arg);
            if (container.isPresent()) {
               return PlaceholderResult.value(Component.literal(container.get().name()));
            }
         }

         return PlaceholderResult.invalid("Invalid argument");
      });
      Placeholders.register(new ResourceLocation("server", "brand"), (ctx, arg) -> PlaceholderResult.value(Component.literal(ctx.server().getServerModName())));
      Placeholders.register(
         new ResourceLocation("server", "mod_count"),
         (ctx, arg) -> PlaceholderResult.value(Component.literal(Platform.loadedModCount() + ""))
      );
      Placeholders.register(new ResourceLocation("server", "mod_description"), (ctx, arg) -> {
         if (arg != null) {
            Optional<Platform.ModInfo> container = Platform.getMod(arg);
            if (container.isPresent()) {
               return PlaceholderResult.value(Component.literal(container.get().description()));
            }
         }

         return PlaceholderResult.invalid("Invalid argument");
      });
      Placeholders.register(new ResourceLocation("server", "name"), (ctx, arg) -> PlaceholderResult.value(ctx.server().name()));
      Placeholders.register(
         new ResourceLocation("server", "used_ram"),
         (ctx, arg) -> {
            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
            return PlaceholderResult.value(
               Objects.equals(arg, "gb")
                  ? String.format("%.1f", (float)heapUsage.getUsed() / 1.0737418E9F)
                  : String.format("%d", heapUsage.getUsed() / 1048576L)
            );
         }
      );
      Placeholders.register(
         new ResourceLocation("server", "max_ram"),
         (ctx, arg) -> {
            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
            return PlaceholderResult.value(
               Objects.equals(arg, "gb") ? String.format("%.1f", (float)heapUsage.getMax() / 1.0737418E9F) : String.format("%d", heapUsage.getMax() / 1048576L)
            );
         }
      );
      Placeholders.register(
         new ResourceLocation("server", "online"), (ctx, arg) -> PlaceholderResult.value(String.valueOf(ctx.server().getPlayerList().getPlayerCount()))
      );
      Placeholders.register(
         new ResourceLocation("server", "max_players"), (ctx, arg) -> PlaceholderResult.value(String.valueOf(ctx.server().getPlayerList().getMaxPlayers()))
      );
      Placeholders.register(new ResourceLocation("server", "objective_name_top"), (ctx, arg) -> {
         String[] args = arg.split(" ");
         if (args.length >= 2) {
            ServerScoreboard scoreboard = ctx.server().getScoreboard();
            Objective scoreboardObjective = scoreboard.getOrCreateObjective(args[0]);
            if (scoreboardObjective == null) {
               return PlaceholderResult.invalid("Invalid objective!");
            } else {
               try {
                  int position = Integer.parseInt(args[1]);
                  Collection<Score> playerScores = scoreboard.getPlayerScores(scoreboardObjective);
                  Score score = playerScores.toArray(Score[]::new)[playerScores.size() - position];
                  return PlaceholderResult.value(score.getOwner());
               } catch (Exception var8) {
                  return PlaceholderResult.invalid("Invalid position!");
               }
            }
         } else {
            return PlaceholderResult.invalid("Not enough arguments!");
         }
      });
      Placeholders.register(new ResourceLocation("server", "objective_score_top"), (ctx, arg) -> {
         String[] args = arg.split(" ");
         if (args.length >= 2) {
            ServerScoreboard scoreboard = ctx.server().getScoreboard();
            Objective scoreboardObjective = scoreboard.getOrCreateObjective(args[0]);
            if (scoreboardObjective == null) {
               return PlaceholderResult.invalid("Invalid objective!");
            } else {
               try {
                  int position = Integer.parseInt(args[1]);
                  Collection<Score> playerScores = scoreboard.getPlayerScores(scoreboardObjective);
                  Score score = playerScores.toArray(Score[]::new)[playerScores.size() - position];
                  return PlaceholderResult.value(String.valueOf(score.getScore()));
               } catch (Exception var8) {
                  return PlaceholderResult.invalid("Invalid position!");
               }
            }
         } else {
            return PlaceholderResult.invalid("Not enough arguments!");
         }
      });
   }
}
