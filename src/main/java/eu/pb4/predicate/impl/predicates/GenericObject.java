package eu.pb4.predicate.impl.predicates;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import eu.pb4.predicate.api.MinecraftPredicate;
import eu.pb4.predicate.api.PredicateRegistry;
import net.minecraft.network.chat.Component;

public final class GenericObject {
   @SuppressWarnings({"unchecked", "rawtypes"})
   public static final Codec<Object> CODEC = (Codec<Object>)(Codec)Codec.either(PredicateRegistry.CODEC, Codec.either(Codec.STRING, Codec.DOUBLE));

   public GenericObject() {
   }

   public static MinecraftPredicate toPredicate(Object valueA) {
      if (valueA instanceof Either<?, ?> either) {
         return either.left().isPresent() ? toPredicate(either.left().get()) : toPredicate(either.right().get());
      } else {
         return valueA instanceof MinecraftPredicate ? (MinecraftPredicate)valueA : MinecraftPredicate.unit(valueA);
      }
   }

   public static double toNumber(Object value, boolean bool) {
      try {
         if (value instanceof Number d) {
            return d.doubleValue();
         }

         if (value instanceof Component text) {
            return Double.parseDouble(text.getString());
         }

         if (value instanceof String string) {
            return Double.parseDouble(string);
         }
      } catch (Throwable var5) {
      }

      return bool ? 1.0 : 0.0;
   }
}
