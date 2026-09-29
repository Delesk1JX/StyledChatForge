package eu.pb4.styledchat.ducks;

import eu.pb4.styledchat.config.ChatStyle;

public interface ExtPlayNetworkHandler {
   void styledChat$setStyle(ChatStyle var1);

   ChatStyle styledChat$getStyle();

   boolean styledChat$chatColors();
}
