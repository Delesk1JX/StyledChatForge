package eu.pb4.predicate.api;

import com.mojang.serialization.MapCodec;
import eu.pb4.predicate.impl.predicates.generic.UnitPredicate;
import net.minecraft.resources.ResourceLocation;

public interface MinecraftPredicate {
   static MinecraftPredicate unit(Object valueA) {
      return new UnitPredicate(valueA);
   }

   PredicateResult<?> test(PredicateContext var1);

   MapCodec<MinecraftPredicate> codec();

   ResourceLocation identifier();
}
