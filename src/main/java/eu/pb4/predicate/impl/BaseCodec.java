package eu.pb4.predicate.impl;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import eu.pb4.predicate.api.MinecraftPredicate;
import eu.pb4.predicate.api.PredicateRegistry;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;

public class BaseCodec extends MapCodec<MinecraftPredicate> {
   public BaseCodec() {
   }

   public <T> Stream<T> keys(DynamicOps<T> ops) {
      return Stream.of((T[])(new Object[]{ops.createString("type"), ops.createString("config")}));
   }

   public <T> DataResult<MinecraftPredicate> decode(DynamicOps<T> ops, MapLike<T> input) {
      DataResult<String> value = ops.getStringValue(input.get("type"));
      return value.flatMap(type -> {
         ResourceLocation id = ResourceLocation.tryParse(type);
         MapCodec<MinecraftPredicate> codec = PredicateRegistry.getCodec(id);
         return codec != null ? codec.decode(ops, input) : DataResult.error(() -> "Invalid predicate type \"" + type + "\"!");
      });
   }

   public <T> RecordBuilder<T> encode(MinecraftPredicate input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
      String type = input.identifier().getNamespace().equals("minecraft") ? input.identifier().getPath() : input.identifier().toString();
      return input.codec().encode(input, ops, prefix.add("type", ops.createString(type)));
   }
}
