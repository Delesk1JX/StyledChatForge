package eu.pb4.predicate.api;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractPredicate implements MinecraftPredicate {
   private final ResourceLocation identifier;
   private final MapCodec<MinecraftPredicate> codec;

   // MapCodec is invariant, so the subclass codec is widened to the interface type on the way in.
   // Erasure makes this a no-op at runtime.
   @SuppressWarnings("unchecked")
   public <T extends MinecraftPredicate> AbstractPredicate(ResourceLocation identifier, MapCodec<T> codec) {
      this.identifier = identifier;
      this.codec = (MapCodec<MinecraftPredicate>)(MapCodec<?>)codec;
   }

   @Override
   public ResourceLocation identifier() {
      return this.identifier;
   }

   @Override
   public MapCodec<MinecraftPredicate> codec() {
      return this.codec;
   }
}
