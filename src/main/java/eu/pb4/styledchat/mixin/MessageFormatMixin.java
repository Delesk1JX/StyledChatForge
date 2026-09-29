package eu.pb4.styledchat.mixin;

import eu.pb4.styledchat.ducks.ExtMessageFormat;
import java.util.function.BiFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.MessageArgument.Message;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({Message.class})
public class MessageFormatMixin implements ExtMessageFormat {
   @Unique
   private String styledChat_context;
   @Unique
   private CommandSourceStack styledChat_source;
   @Unique
   private BiFunction<String, Class<?>, Object> styledChat_args;

   public MessageFormatMixin() {
   }

   // The stored field is erased to BiFunction<String, Class<?>, Object>; erasure makes the raw
   // assignment equivalent to the typed one.
   @Override
   @SuppressWarnings({"unchecked", "rawtypes"})
   public <T> void styledChat_setSource(String command, CommandSourceStack source, BiFunction<String, Class<T>, T> argumentGetter) {
      this.styledChat_context = command;
      this.styledChat_source = source;
      this.styledChat_args = (BiFunction)argumentGetter;
   }
}
