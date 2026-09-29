package eu.pb4.playerdata.api.storage;

import eu.pb4.playerdata.api.PlayerDataApi;
import eu.pb4.playerdata.impl.PMI;
import java.nio.file.Path;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;

public record NbtDataStorage(String path) implements PlayerDataStorage<CompoundTag> {
   public boolean save(MinecraftServer server, UUID player, CompoundTag settings) {
      if (settings == null) {
         return false;
      } else {
         try {
            Path path = PlayerDataApi.getPathFor(server, player);
            path.toFile().mkdirs();
            NbtIo.writeCompressed(settings, path.resolve(this.path + ".dat").toFile());
            return true;
         } catch (Exception var5) {
            PMI.LOGGER.error(String.format("Couldn't save player data of %s for path %s", player, this.path));
            var5.printStackTrace();
            return false;
         }
      }
   }

   public CompoundTag load(MinecraftServer server, UUID player) {
      try {
         Path path = PlayerDataApi.getPathFor(server, player).resolve(this.path + ".dat");
         return !path.toFile().exists() ? null : NbtIo.readCompressed(path.toFile());
      } catch (Exception var4) {
         PMI.LOGGER.error(String.format("Couldn't load player data of %s for path %s", player, this.path));
         var4.printStackTrace();
         return null;
      }
   }
}
