package eu.pb4.predicate.impl.predicates.compat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.delesk1jx.styledchat.vendor.placeholders.api.PlaceholderContext;
import dev.delesk1jx.styledchat.vendor.placeholders.api.PlaceholderResult;
import dev.delesk1jx.styledchat.vendor.placeholders.api.Placeholders;
import eu.pb4.predicate.api.AbstractPredicate;
import eu.pb4.predicate.api.PredicateContext;
import eu.pb4.predicate.api.PredicateResult;
import net.minecraft.resources.ResourceLocation;

public final class PlaceholderPredicate extends AbstractPredicate {
   public static final ResourceLocation ID = new ResourceLocation("placeholder");
   public static final MapCodec<PlaceholderPredicate> CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(
               Codec.STRING.fieldOf("placeholder").forGetter(PlaceholderPredicate::value),
               Codec.BOOL.optionalFieldOf("raw", false).forGetter(PlaceholderPredicate::raw)
            )
            .apply(instance, PlaceholderPredicate::new)
   );
   private final ResourceLocation placeholderId;
   private final String arg;
   private final boolean raw;

   public PlaceholderPredicate(String placeholder, boolean raw) {
      super(ID, CODEC);
      String[] data = placeholder.split(" ", 2);
      this.placeholderId = ResourceLocation.tryParse(data[0]);
      this.arg = data.length == 2 ? data[1] : null;
      this.raw = raw;
   }

   public String value() {
      return this.placeholderId + (this.arg != null ? " " + this.arg : "");
   }

   public boolean raw() {
      return this.raw;
   }

   @Override
   public PredicateResult<?> test(PredicateContext context) {
      PlaceholderContext pCon = new PlaceholderContext(
         context.server(), context.source(), context.world(), context.player(), context.entity(), context.gameProfile()
      );
      PlaceholderResult result = Placeholders.parsePlaceholder(this.placeholderId, this.arg, pCon);
      return new PredicateResult<>(result.isValid(), this.raw ? result.string() : result.text());
   }
}
