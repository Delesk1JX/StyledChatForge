package eu.pb4.playerdata.api;

import com.google.common.collect.ImmutableSet;
import eu.pb4.playerdata.api.storage.NbtDataStorage;
import eu.pb4.playerdata.api.storage.PlayerDataStorage;
import eu.pb4.playerdata.impl.PMI;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.TagType;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public final class PlayerDataApi {
   private static final PlayerDataStorage<CompoundTag> GLOBAL_DATA_STORAGE = new NbtDataStorage("general");
   private static final Set<PlayerDataStorage<?>> STORAGE = new HashSet<>();

   private PlayerDataApi() {
   }

   public static <T extends PlayerDataStorage<?>> boolean register(T dataStorage) {
      return STORAGE.add(dataStorage);
   }

   @Nullable
   public static Tag getGlobalDataFor(ServerPlayer player, ResourceLocation identifier) {
      CompoundTag data = getCustomDataFor(player, GLOBAL_DATA_STORAGE);
      return data != null ? data.get(identifier.toString()) : null;
   }

   @Nullable
   public static <T extends Tag> T getGlobalDataFor(ServerPlayer player, ResourceLocation identifier, TagType<T> type) {
      Tag data = getGlobalDataFor(player, identifier);
      return (T)(data != null && data.getType() == type ? data : null);
   }

   public static void setGlobalDataFor(ServerPlayer player, ResourceLocation identifier, Tag element) {
      CompoundTag data = getCustomDataFor(player, GLOBAL_DATA_STORAGE);
      if (data == null) {
         data = new CompoundTag();
         setCustomDataFor(player, GLOBAL_DATA_STORAGE, data);
      }

      if (element != null) {
         data.put(identifier.toString(), element);
      } else {
         data.remove(identifier.toString());
      }
   }

   @Nullable
   public static <T> T getCustomDataFor(ServerPlayer player, PlayerDataStorage<T> storage) {
      return getCustomDataFor(player.server, player.getUUID(), storage);
   }

   public static <T> void setCustomDataFor(ServerPlayer player, PlayerDataStorage<T> storage, T value) {
      setCustomDataFor(player.server, player.getUUID(), storage, value);
   }

   @Nullable
   public static <T> T getCustomDataFor(MinecraftServer server, UUID uuid, PlayerDataStorage<T> storage) {
      PMI pmi = (PMI)server.getPlayerList();
      return pmi.pda_isStored(uuid) ? pmi.pda_getStorageValue(uuid, storage) : storage.load(server, uuid);
   }

   public static <T> void setCustomDataFor(MinecraftServer server, UUID uuid, PlayerDataStorage<T> storage, T value) {
      PMI pmi = (PMI)server.getPlayerList();
      if (pmi.pda_isStored(uuid)) {
         pmi.pda_setStorageValue(uuid, storage, value);
      } else {
         storage.save(server, uuid, value);
      }
   }

   public static ImmutableSet<PlayerDataStorage<?>> getDataStorageSet() {
      return ImmutableSet.copyOf(STORAGE);
   }

   public static Path getPathFor(ServerPlayer player) {
      return getPathFor(player.server, player.getUUID());
   }

   public static Path getPathFor(MinecraftServer server, UUID uuid) {
      return server.getWorldPath(LevelResource.ROOT).resolve("player-mod-data").resolve(uuid.toString());
   }

   static {
      register(GLOBAL_DATA_STORAGE);
   }
}
