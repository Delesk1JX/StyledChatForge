package eu.pb4.predicate.impl.predicates.generic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.predicate.api.AbstractPredicate;
import eu.pb4.predicate.api.MinecraftPredicate;
import eu.pb4.predicate.api.PredicateContext;
import eu.pb4.predicate.api.PredicateRegistry;
import eu.pb4.predicate.api.PredicateResult;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public final class AnyPredicate extends AbstractPredicate {
   public static final ResourceLocation ID = new ResourceLocation("any");
   public static final MapCodec<AnyPredicate> CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(Codec.list(PredicateRegistry.CODEC).fieldOf("values").forGetter(AnyPredicate::values)).apply(instance, AnyPredicate::new)
   );
   private final List<MinecraftPredicate> values;

   public AnyPredicate(List<MinecraftPredicate> valueA) {
      super(ID, CODEC);
      this.values = valueA;
   }

   public List<MinecraftPredicate> values() {
      return this.values;
   }

   @Override
   public PredicateResult<?> test(PredicateContext context) {
      for (MinecraftPredicate predicate : this.values) {
         PredicateResult<?> val = predicate.test(context);
         if (val.success()) {
            return val;
         }
      }

      return PredicateResult.ofFailure();
   }
}
