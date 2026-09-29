package eu.pb4.predicate.impl.predicates.player;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.predicate.api.AbstractPredicate;
import eu.pb4.predicate.api.PredicateContext;
import eu.pb4.predicate.api.PredicateResult;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.resources.ResourceLocation;

public class EntityPredicatePredicate extends AbstractPredicate {
   public static final ResourceLocation ID = new ResourceLocation("entity");
   public static final MapCodec<EntityPredicatePredicate> CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(EntityPredicatePredicate.EntityPredicateCodec.INSTANCE.fieldOf("value").forGetter(EntityPredicatePredicate::predicate))
            .apply(instance, EntityPredicatePredicate::new)
   );
   private final EntityPredicate entityPredicate;

   public EntityPredicatePredicate(EntityPredicate predicate) {
      super(ID, CODEC);
      this.entityPredicate = predicate;
   }

   private EntityPredicate predicate() {
      return this.entityPredicate;
   }

   @Override
   public PredicateResult<?> test(PredicateContext context) {
      return context.hasEntity()
         ? PredicateResult.ofBoolean(this.entityPredicate.matches(context.world(), context.entity().position(), context.entity()))
         : PredicateResult.ofFailure();
   }

   private static class EntityPredicateCodec implements Codec<EntityPredicate> {
      private static final EntityPredicatePredicate.EntityPredicateCodec INSTANCE = new EntityPredicatePredicate.EntityPredicateCodec();

      private EntityPredicateCodec() {
      }

      public <T> DataResult<Pair<EntityPredicate, T>> decode(DynamicOps<T> ops, T input) {
         return DataResult.success(
            new Pair(
               EntityPredicate.fromJson(input instanceof JsonElement jsonElement ? jsonElement : (JsonElement)ops.convertTo(JsonOps.INSTANCE, input)), input
            )
         );
      }

      public <T> DataResult<T> encode(EntityPredicate input, DynamicOps<T> ops, T prefix) {
         T encoded = (T)(ops instanceof JsonOps ? input.serializeToJson() : JsonOps.INSTANCE.convertTo(ops, input.serializeToJson()));
         return ops.getMap(encoded).flatMap(map -> ops.mergeToMap(prefix, map));
      }
   }
}
