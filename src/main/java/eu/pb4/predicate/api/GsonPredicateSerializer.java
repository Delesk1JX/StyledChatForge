package eu.pb4.predicate.api;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.serialization.JsonOps;
import java.lang.reflect.Type;

public final class GsonPredicateSerializer implements JsonSerializer<MinecraftPredicate>, JsonDeserializer<MinecraftPredicate> {
   public static final GsonPredicateSerializer INSTANCE = new GsonPredicateSerializer();

   private GsonPredicateSerializer() {
   }

   public MinecraftPredicate deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
      try {
         return PredicateRegistry.decode(jsonElement);
      } catch (Throwable var5) {
         throw new JsonParseException(var5);
      }
   }

   public JsonElement serialize(MinecraftPredicate minecraftPredicate, Type type, JsonSerializationContext jsonSerializationContext) {
      return (JsonElement)PredicateRegistry.CODEC.encode(minecraftPredicate, JsonOps.INSTANCE, new JsonObject()).result().get();
   }
}
