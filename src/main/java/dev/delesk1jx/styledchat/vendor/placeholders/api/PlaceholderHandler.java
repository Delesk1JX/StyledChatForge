package dev.delesk1jx.styledchat.vendor.placeholders.api;

import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface PlaceholderHandler {
   PlaceholderHandler EMPTY = (ctx, arg) -> PlaceholderResult.invalid();

   PlaceholderResult onPlaceholderRequest(PlaceholderContext var1, @Nullable String var2);
}
