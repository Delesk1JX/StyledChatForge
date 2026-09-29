package eu.pb4.playerdata.api.storage;

import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public interface PlayerDataStorage<T> {
   default boolean save(ServerPlayer player, T settings) {
      return this.save(player.server, player.getUUID(), settings);
   }

   boolean save(MinecraftServer var1, UUID var2, T var3);

   @Nullable
   default T load(ServerPlayer player) {
      return this.load(player.server, player.getUUID());
   }

   @Nullable
   T load(MinecraftServer var1, UUID var2);
}
