package eu.pb4.styledchat.ducks;

import java.util.function.BiFunction;
import net.minecraft.commands.CommandSourceStack;

public interface ExtMessageFormat {
   <T> void styledChat_setSource(String var1, CommandSourceStack var2, BiFunction<String, Class<T>, T> var3);
}
