package eu.pb4.playerdata.mixin;

import com.google.common.collect.UnmodifiableIterator;
import eu.pb4.playerdata.api.PlayerDataApi;
import eu.pb4.playerdata.api.storage.PlayerDataStorage;
import eu.pb4.playerdata.impl.PMI;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {PlayerList.class},
   priority = 500
)
public class PlayerManagerMixin implements PMI {
   @Unique
   private final Map<UUID, Map<PlayerDataStorage<Object>, Object>> pda_playerDataMap = new Object2ObjectOpenHashMap();

   public PlayerManagerMixin() {
   }

   // The map is keyed by storage; every storage instance is registered as some PlayerDataStorage<T>
   // and looked back up the same way, so the erased key is safe to reuse.
   @SuppressWarnings({"unchecked", "rawtypes"})
   private static PlayerDataStorage<Object> asKey(PlayerDataStorage<?> storage) {
      return (PlayerDataStorage)storage;
   }

   @Inject(
      method = {"placeNewPlayer"},
      at = {@At("HEAD")}
   )
   private void loadData(Connection connection, ServerPlayer player, CallbackInfo ci) {
      Object2ObjectOpenHashMap<PlayerDataStorage<Object>, Object> map = new Object2ObjectOpenHashMap();
      UnmodifiableIterator var5 = PlayerDataApi.getDataStorageSet().iterator();

      while (var5.hasNext()) {
         PlayerDataStorage<?> storage = (PlayerDataStorage<?>)var5.next();

         try {
            map.put(asKey(storage), storage.load(player));
         } catch (Exception var8) {
            var8.printStackTrace();
         }
      }

      this.pda_playerDataMap.put(player.getUUID(), map);
   }

   @Inject(
      method = {"save"},
      at = {@At("HEAD")}
   )
   private void pda_saveData(ServerPlayer player, CallbackInfo ci) {
      Map<PlayerDataStorage<Object>, Object> map = this.pda_playerDataMap.get(player.getUUID());
      if (map != null) {
         for (Entry<PlayerDataStorage<Object>, Object> entry : map.entrySet()) {
            try {
               entry.getKey().save(player, entry.getValue());
            } catch (Exception var7) {
               var7.printStackTrace();
            }
         }
      }
   }

   @Inject(
      method = {"remove"},
      at = {@At("TAIL")}
   )
   private void pda_dontHoldOfflineData(ServerPlayer player, CallbackInfo ci) {
      this.pda_playerDataMap.remove(player.getUUID());
   }

   @Override
   public Map<PlayerDataStorage<Object>, Object> pda_getStorageMap(UUID uuid) {
      return this.pda_playerDataMap.get(uuid);
   }

   @Override
   public <T> T pda_getStorageValue(UUID uuid, PlayerDataStorage<T> storage) {
      Map<PlayerDataStorage<Object>, Object> map = this.pda_playerDataMap.get(uuid);
      return (T)(map != null ? map.get(storage) : null);
   }

   @Override
   public <T> void pda_setStorageValue(UUID uuid, PlayerDataStorage<T> storage, T value) {
      Map<PlayerDataStorage<Object>, Object> map = this.pda_playerDataMap.get(uuid);
      if (map != null) {
         map.put(asKey(storage), value);
      }
   }

   @Override
   public boolean pda_isStored(UUID uuid) {
      return this.pda_playerDataMap.containsKey(uuid);
   }
}
