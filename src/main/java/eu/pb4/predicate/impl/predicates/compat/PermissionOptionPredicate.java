package eu.pb4.predicate.impl.predicates.compat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import eu.pb4.predicate.api.AbstractPredicate;
import eu.pb4.predicate.api.PredicateContext;
import eu.pb4.predicate.api.PredicateResult;
import eu.pb4.loader.Options;
import net.minecraft.resources.ResourceLocation;

public final class PermissionOptionPredicate extends AbstractPredicate {
   public static final ResourceLocation ID = new ResourceLocation("permission_option");
   public static final MapCodec<PermissionOptionPredicate> CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(Codec.STRING.fieldOf("option").forGetter(PermissionOptionPredicate::permission))
            .apply(instance, PermissionOptionPredicate::new)
   );
   private final String permission;

   public PermissionOptionPredicate(String permission) {
      super(ID, CODEC);
      this.permission = permission;
   }

   public String permission() {
      return this.permission;
   }

   @Override
   public PredicateResult<?> test(PredicateContext context) {
      return PredicateResult.ofOptional(Options.get(context.source(), this.permission));
   }
}
