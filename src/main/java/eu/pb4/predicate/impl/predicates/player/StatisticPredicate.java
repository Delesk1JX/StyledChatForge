package eu.pb4.predicate.impl.predicates.player;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.predicate.api.AbstractPredicate;
import eu.pb4.predicate.api.PredicateContext;
import eu.pb4.predicate.api.PredicateResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.core.registries.BuiltInRegistries;

public final class StatisticPredicate extends AbstractPredicate {
   public static final ResourceLocation ID = new ResourceLocation("statistic");
   public static final MapCodec<StatisticPredicate> CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(
               BuiltInRegistries.STAT_TYPE.byNameCodec().optionalFieldOf("stat_type", Stats.CUSTOM).forGetter(StatisticPredicate::statType),
               ResourceLocation.CODEC.fieldOf("key").forGetter(StatisticPredicate::key)
            )
            .apply(instance, StatisticPredicate::new)
   );
   private final StatType<?> type;
   private final ResourceLocation key;
   private final Object realKey;

   public StatisticPredicate(StatType<?> type, ResourceLocation key) {
      super(ID, CODEC);
      this.type = type;
      this.key = (ResourceLocation)BuiltInRegistries.CUSTOM_STAT.get(key);
      this.realKey = type.getRegistry().get(this.key);
   }

   private ResourceLocation key() {
      return this.key;
   }

   private StatType<?> statType() {
      return this.type;
   }

   // realKey is the resolved registry object for this stat, so the lookup is inherently untyped;
   // passing the stat type raw lets T infer to Object and reach the erased call the mod needs.
   @Override
   @SuppressWarnings({"unchecked", "rawtypes"})
   public PredicateResult<?> test(PredicateContext context) {
      if (context.hasPlayer()) {
         int val = context.player().getStats().getValue((StatType)this.type, this.realKey);
         return new PredicateResult<>(val > 0, val);
      } else {
         return PredicateResult.ofFailure();
      }
   }
}
