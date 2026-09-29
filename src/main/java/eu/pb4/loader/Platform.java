package eu.pb4.loader;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import net.minecraft.SharedConstants;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.language.IModInfo;

/**
 * Loader services the rest of the mod needs, in one place.
 *
 * The upstream Fabric build asked these questions of {@code FabricLoader}. Forge answers them
 * through {@link ModList}, so every call site goes through here instead of talking to a loader
 * type directly. That also keeps the bundled Placeholder / Predicate / Player Data APIs free of
 * any hard dependency on a specific loader.
 */
public final class Platform {
    public static final boolean IS_DEV = !FMLLoader.isProduction();
    public static final Path CONFIG_DIR = FMLPaths.CONFIGDIR.get();

    private Platform() {
    }

    public static boolean isModLoaded(String modId) {
        return ModList.get() != null && ModList.get().isLoaded(modId);
    }

    public static Optional<ModInfo> getMod(String modId) {
        if (ModList.get() == null) {
            return Optional.empty();
        }

        return ModList.get().getModContainerById(modId).map(container -> {
            IModInfo info = container.getModInfo();
            return new ModInfo(info.getModId(), info.getDisplayName(), info.getDescription(),
                    String.valueOf(info.getVersion()));
        });
    }

    public static int loadedModCount() {
        return ModList.get() == null ? 0 : ModList.get().getMods().size();
    }

    public static List<IModInfo> loadedMods() {
        return ModList.get() == null ? List.of() : ModList.get().getMods();
    }

    /**
     * Vanilla renamed the translation-key constructor overload in 1.19.4, and the bundled text
     * parser still has to call the right one.
     */
    public static boolean isLegacyTranslation() {
        return compareVersions(SharedConstants.getCurrentVersion().getName(), "1.19.4") < 0;
    }

    /**
     * Reads a file shipped inside another mod. Mods on Forge are ordinary jars, so the resource
     * sits on the classpath rather than behind a loader-supplied path.
     */
    public static Optional<InputStream> openModResource(String modId, String path) {
        ClassLoader loader = Platform.class.getClassLoader();

        if (loader != null) {
            InputStream stream = loader.getResourceAsStream(path);
            if (stream != null) {
                return Optional.of(stream);
            }
        }

        return Optional.ofNullable(Platform.class.getResourceAsStream("/" + path));
    }

    private static int compareVersions(String a, String b) {
        String[] left = a.split("\\.");
        String[] right = b.split("\\.");

        for (int i = 0; i < Math.max(left.length, right.length); i++) {
            int l = i < left.length ? parse(left[i]) : 0;
            int r = i < right.length ? parse(right[i]) : 0;

            if (l != r) {
                return Integer.compare(l, r);
            }
        }

        return 0;
    }

    private static int parse(String value) {
        StringBuilder digits = new StringBuilder();

        for (char c : value.toCharArray()) {
            if (Character.isDigit(c)) {
                digits.append(c);
            } else {
                break;
            }
        }

        if (digits.length() == 0) {
            return 0;
        }

        try {
            return Integer.parseInt(digits.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public record ModInfo(String id, String name, String description, String version) {
    }
}
