package eu.pb4.styledchat.mixin;

import eu.pb4.styledchat.StyledChatStyles;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin({TamableAnimal.class})
public class TameableEntityMixin {
   public TameableEntityMixin() {
   }

   @ModifyArg(
      method = {"die"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/entity/LivingEntity;sendSystemMessage(Lnet/minecraft/network/chat/Component;)V"
      )
   )
   private Component styledChat_replaceDeathMessage(Component text) {
      return StyledChatStyles.getPetDeath((TamableAnimal)(Object) this, text);
   }
}
