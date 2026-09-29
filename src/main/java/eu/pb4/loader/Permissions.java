package eu.pb4.loader;

import java.util.function.Predicate;

import net.minecraft.commands.CommandSourceStack;

/**
 * Permission checks for the port.
 *
 * The Fabric build called into lucko's permissions API. Forge has no equivalent in core, and its
 * permission handler API requires nodes to be registered before the handler gathers them, whereas
 * these checks are first evaluated lazily while Brigadier builds the command tree. Wiring that up
 * would make the result depend on event ordering for no real gain, so the checks resolve to the op
 * level instead — which is exactly what the original API falls back to when no permission provider
 * is installed. Install a permission mod and grant the same nodes to non-ops by raising their level.
 */
public final class Permissions {
    public static final int DEFAULT_OPERATOR_LEVEL = 2;

    private Permissions() {
    }

    public static boolean check(CommandSourceStack source, String permission) {
        return check(source, permission, DEFAULT_OPERATOR_LEVEL);
    }

    public static boolean check(CommandSourceStack source, String permission, int operator) {
        return source.hasPermission(operator);
    }

    public static Predicate<CommandSourceStack> require(String permission) {
        return require(permission, DEFAULT_OPERATOR_LEVEL);
    }

    public static Predicate<CommandSourceStack> require(String permission, int operator) {
        return source -> check(source, permission, operator);
    }

    /**
     * Mirrors lucko's {@code require(String, boolean)}: when {@code value} is false the node is
     * meant to be visible to everyone, so nothing is required.
     */
    public static Predicate<CommandSourceStack> require(String permission, boolean value) {
        return value ? require(permission) : source -> true;
    }
}
