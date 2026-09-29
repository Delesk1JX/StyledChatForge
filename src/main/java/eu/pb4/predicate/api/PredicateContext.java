package eu.pb4.predicate.api;

import com.mojang.authlib.GameProfile;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;

public record PredicateContext(
   MinecraftServer server,
   CommandSourceStack source,
   @Nullable ServerLevel world,
   @Nullable ServerPlayer player,
   @Nullable Entity entity,
   @Nullable GameProfile gameProfile
) {
   public boolean hasWorld() {
      return this.world != null;
   }

   public boolean hasPlayer() {
      return this.player != null;
   }

   public boolean hasGameProfile() {
      return this.gameProfile != null;
   }

   public boolean hasEntity() {
      return this.entity != null;
   }

   public static PredicateContext of(MinecraftServer server) {
      return new PredicateContext(server, server.createCommandSourceStack(), null, null, null, null);
   }

   public static PredicateContext of(GameProfile profile, MinecraftServer server) {
      String name = profile.getName() != null ? profile.getName() : profile.getId().toString();
      return new PredicateContext(
         server,
         new CommandSourceStack(
            CommandSource.NULL,
            Vec3.ZERO,
            Vec2.ZERO,
            server.overworld(),
            server.getProfilePermissions(profile),
            name,
            Component.literal(name),
            server,
            null
         ),
         null,
         null,
         null,
         profile
      );
   }

   public static PredicateContext of(ServerPlayer player) {
      return new PredicateContext(player.getServer(), player.createCommandSourceStack(), player.serverLevel(), player, player, player.getGameProfile());
   }

   public static PredicateContext of(CommandSourceStack source) {
      return new PredicateContext(
         source.getServer(),
         source,
         source.getLevel(),
         source.getPlayer(),
         source.getEntity(),
         source.getPlayer() != null ? source.getPlayer().getGameProfile() : null
      );
   }

   public static PredicateContext of(Entity entity) {
      return entity instanceof ServerPlayer player
         ? of(player)
         : new PredicateContext(entity.getServer(), entity.createCommandSourceStack(), (ServerLevel)entity.level(), null, entity, null);
   }
}
