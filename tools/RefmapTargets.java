import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts the Fabric refmap's intermediary descriptors into official (Mojang) ones.
 *
 * The {@code target} / {@code method} strings in a mixin have to be written against the mappings
 * the mod is compiled with. The refmap records which intermediary member each Yarn-style string
 * really referred to, so feeding its values through the intermediary -> official table yields the
 * correct official descriptor instead of a hand-maintained guess.
 */
public class RefmapTargets {
    public static void main(String[] args) throws Exception {
        Path tinyPath = Paths.get(args[0]);
        Path refmapPath = Paths.get(args[1]);

        Map<String, String> classes = new HashMap<>();
        Map<String, String> methods = new HashMap<>();

        for (String line : Files.readAllLines(tinyPath, StandardCharsets.UTF_8)) {
            if (line.isEmpty() || line.startsWith("tiny")) {
                continue;
            }
            boolean isMember = line.charAt(0) == '\t';
            String[] p = (isMember ? line.substring(1) : line).split("\t");
            if (isMember) {
                if (p.length >= 4 && p[0].equals("m")) {
                    methods.putIfAbsent(p[2], p[3]);
                }
            } else if (p.length >= 3 && p[0].equals("c")) {
                classes.put(p[1], p[2]);
            }
        }

        String json = Files.readString(refmapPath, StandardCharsets.UTF_8);

        // "key": "value" pairs inside the mappings object; values may hold several ';'-separated pairs.
        Pattern entry = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]+)\"");
        Matcher m = entry.matcher(json);
        while (m.find()) {
            String key = m.group(1);
            String value = m.group(2);
            if (!key.startsWith("L") && !key.contains("/") && !key.contains(";")) {
                continue;
            }
            if (!value.contains("L") && !value.contains(";")) {
                continue;
            }
            for (String pair : value.split("; ")) {
                System.out.println("SOURCE  : " + key);
                System.out.println("OFFICIAL: " + convert(pair, classes, methods));
            }
        }
    }

    private static String convert(String descriptor, Map<String, String> classes, Map<String, String> methods) {
        // Object types first, then method names that follow a ';'.
        String out = Matcher.quoteReplacement("");
        StringBuilder sb = new StringBuilder();
        Matcher typeMatcher = Pattern.compile("L([^;]+);").matcher(descriptor);
        int last = 0;
        while (typeMatcher.find()) {
            sb.append(descriptor, last, typeMatcher.start());
            String fqn = typeMatcher.group(1);
            String mapped = classes.get(fqn);
            sb.append('L').append(mapped == null ? fqn : mapped).append(';');
            last = typeMatcher.end();
        }
        sb.append(descriptor, last, descriptor.length());
        out = sb.toString();

        Matcher nameMatcher = Pattern.compile(";([A-Za-z_$][A-Za-z0-9_$]*)\\(").matcher(out);
        StringBuilder result = new StringBuilder();
        last = 0;
        while (nameMatcher.find()) {
            result.append(out, last, nameMatcher.start());
            String name = nameMatcher.group(1);
            String mapped = methods.get(name);
            result.append(';').append(mapped == null ? name : mapped).append('(');
            last = nameMatcher.end();
        }
        result.append(out, last, out.length());
        return result.toString();
    }
}
