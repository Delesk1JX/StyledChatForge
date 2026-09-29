package eu.pb4.predicate.api;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapCodec.MapCodecCodec;
import eu.pb4.predicate.impl.BaseCodec;
import eu.pb4.predicate.impl.PredicatesInit;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public final class PredicateRegistry {
   private static final Map<ResourceLocation, MapCodec<MinecraftPredicate>> CODECS = new HashMap<>();
   private static final Map<MapCodec<MinecraftPredicate>, ResourceLocation> CODEC_IDS = new HashMap<>();
   public static final Codec<MinecraftPredicate> CODEC = new MapCodecCodec(new BaseCodec());

   private PredicateRegistry() {
   }

   public static MinecraftPredicate decode(JsonElement object) {
      return decode(JsonOps.INSTANCE, object);
   }

   public static <T> MinecraftPredicate decode(DynamicOps<T> ops, T object) {
      try {
         DataResult<Pair<MinecraftPredicate, T>> data = CODEC.decode(ops, object);
         return (MinecraftPredicate)((Pair)data.getOrThrow(false, s -> {
         })).getFirst();
      } catch (Throwable var3) {
         throw new IllegalArgumentException(var3);
      }
   }

   @Nullable
   public static MapCodec<MinecraftPredicate> getCodec(ResourceLocation identifier) {
      return CODECS.get(identifier);
   }

   @Nullable
   public static ResourceLocation getIdentifier(MapCodec<MinecraftPredicate> codec) {
      return CODEC_IDS.get(codec);
   }

   // Erasure makes this a no-op at runtime; the registry always stored the widened type.
   @SuppressWarnings("unchecked")
   public static <T extends MinecraftPredicate> void register(ResourceLocation identifier, MapCodec<T> predicateCodec) {
      MapCodec<MinecraftPredicate> codec = (MapCodec<MinecraftPredicate>)(MapCodec<?>)predicateCodec;
      CODECS.put(identifier, codec);
      CODEC_IDS.put(codec, identifier);
   }

   static {
      PredicatesInit.initialize();
   }
}
