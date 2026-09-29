package dev.delesk1jx.styledchat.vendor.placeholders.impl.placeholder;

import dev.delesk1jx.styledchat.vendor.placeholders.api.PlaceholderContext;
import net.minecraft.resources.ResourceLocation;

public record ViewObjectImpl(ResourceLocation identifier) implements PlaceholderContext.ViewObject {
}
