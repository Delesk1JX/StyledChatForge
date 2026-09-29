package eu.pb4.playerdata.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.lang.reflect.Type;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.registries.BuiltInRegistries;

public class BaseGson {
   public static final Gson GSON = new GsonBuilder()
      .disableHtmlEscaping()
      .registerTypeHierarchyAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
      .registerTypeHierarchyAdapter(Item.class, new BaseGson.RegistrySerializer(BuiltInRegistries.ITEM))
      .registerTypeHierarchyAdapter(Block.class, new BaseGson.RegistrySerializer(BuiltInRegistries.BLOCK))
      .registerTypeHierarchyAdapter(Enchantment.class, new BaseGson.RegistrySerializer(BuiltInRegistries.ENCHANTMENT))
      .registerTypeHierarchyAdapter(SoundEvent.class, new BaseGson.RegistrySerializer(BuiltInRegistries.SOUND_EVENT))
      .registerTypeHierarchyAdapter(MobEffect.class, new BaseGson.RegistrySerializer(BuiltInRegistries.MOB_EFFECT))
      .registerTypeHierarchyAdapter(EntityType.class, new BaseGson.RegistrySerializer(BuiltInRegistries.ENTITY_TYPE))
      .registerTypeHierarchyAdapter(BlockEntityType.class, new BaseGson.RegistrySerializer(BuiltInRegistries.BLOCK_ENTITY_TYPE))
      .registerTypeHierarchyAdapter(Component.class, new Component.Serializer())
      .registerTypeHierarchyAdapter(Style.class, new Style.Serializer())
      .registerTypeHierarchyAdapter(ItemStack.class, new BaseGson.CodecSerializer(ItemStack.CODEC))
      .registerTypeHierarchyAdapter(BlockPos.class, new BaseGson.CodecSerializer(BlockPos.CODEC))
      .registerTypeHierarchyAdapter(Vec3.class, new BaseGson.CodecSerializer(Vec3.CODEC))
      .setLenient()
      .create();

   public BaseGson() {
   }

   private static record CodecSerializer<T>(Codec<T> codec) implements JsonSerializer<T>, JsonDeserializer<T> {
      public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
         try {
            return (T)((Pair)this.codec.decode(JsonOps.INSTANCE, json).getOrThrow(false, x -> {
            })).getFirst();
         } catch (Throwable var5) {
            return null;
         }
      }

      public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
         try {
            return (JsonElement)(src != null ? (JsonElement)this.codec.encodeStart(JsonOps.INSTANCE, src).getOrThrow(false, x -> {
            }) : JsonNull.INSTANCE);
         } catch (Throwable var5) {
            return JsonNull.INSTANCE;
         }
      }
   }

   private static record RegistrySerializer<T>(Registry<T> registry) implements JsonSerializer<T>, JsonDeserializer<T> {
      public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
         return (T)(json.isJsonPrimitive() ? this.registry.get(ResourceLocation.tryParse(json.getAsString())) : null);
      }

      public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
         return new JsonPrimitive(this.registry.getKey(src) + "");
      }
   }
}
