package eu.pb4.playerdata.impl;

import eu.pb4.playerdata.api.storage.PlayerDataStorage;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
public interface PMI {
   Logger LOGGER = LoggerFactory.getLogger("Player Data API");

   Map<PlayerDataStorage<Object>, Object> pda_getStorageMap(UUID var1);

   <T> T pda_getStorageValue(UUID var1, PlayerDataStorage<T> var2);

   <T> void pda_setStorageValue(UUID var1, PlayerDataStorage<T> var2, T var3);

   boolean pda_isStored(UUID var1);
}
