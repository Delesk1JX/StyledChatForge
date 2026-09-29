import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Rewrites decompiled intermediary Java into official (Mojang) names.
 *
 * Intermediary member names are globally unique, so class/field/method tokens can be
 * resolved with a single map and applied with regex substitution. Everything else in
 * the source (the eu.pb4.* API, java.*, the mod's own logic) is left untouched.
 */
public class RemapSource {
    public static void main(String[] args) throws Exception {
        Path tinyPath = Paths.get(args[0]);
        Path inDir = Paths.get(args[1]);
        Path outDir = Paths.get(args[2]);

        Map<String, String> classByFqn = new HashMap<>();
        Map<String, String> simple = new HashMap<>();
        Map<String, String> fields = new HashMap<>();
        Map<String, String> methods = new HashMap<>();
        Map<String, List<String>> ambiguous = new HashMap<>();
        List<String> conflicts = new ArrayList<>();

        Map<String, List<String>> candidates = new HashMap<>();

        for (String line : Files.readAllLines(tinyPath, StandardCharsets.UTF_8)) {
            if (line.isEmpty() || line.startsWith("tiny")) {
                continue;
            }
            boolean isMember = line.charAt(0) == '\t';
            String[] p = (isMember ? line.substring(1) : line).split("\t");
            if (isMember) {
                if (p.length < 4) {
                    continue;
                }
                String from = p[2];
                String to = p[3];
                if (from.equals(to)) {
                    continue;
                }
                Map<String, String> target = p[0].equals("f") ? fields : methods;
                candidates.computeIfAbsent(from, k -> new ArrayList<>());
                if (!candidates.get(from).contains(to)) {
                    candidates.get(from).add(to);
                }
                String prev = target.putIfAbsent(from, to);
                if (prev != null && !prev.equals(to)) {
                    conflicts.add(from + " -> " + prev + " / " + to);
                    ambiguous.put(from, candidates.get(from));
                }
            } else {
                if (p.length < 3 || !p[0].equals("c")) {
                    continue;
                }
                String from = p[1];
                String to = p[2];
                if (from.equals(to)) {
                    continue;
                }
                classByFqn.put(from, to);
                String simpleFrom = from.substring(from.lastIndexOf('/') + 1);
                String simpleTo = to.substring(to.lastIndexOf('/') + 1);
                if (simpleFrom.contains("$")) {
                    // Vineflower imports inner classes as Outer.Inner, so the bare token
                    // seen in code is the innermost segment.
                    String innerFrom = simpleFrom.substring(simpleFrom.lastIndexOf('$') + 1);
                    String innerTo = simpleTo.substring(simpleTo.lastIndexOf('$') + 1);
                    String prevInner = simple.putIfAbsent(innerFrom, innerTo);
                    if (prevInner != null && !prevInner.equals(innerTo)) {
                        conflicts.add("inner " + innerFrom + " -> " + prevInner + " / " + innerTo);
                        ambiguous.put(innerFrom, List.of(prevInner, innerTo));
                    }
                } else {
                    String prevSimple = simple.putIfAbsent(simpleFrom, simpleTo);
                    if (prevSimple != null && !prevSimple.equals(simpleTo)) {
                        conflicts.add("simple " + simpleFrom + " -> " + prevSimple + " / " + simpleTo);
                    }
                }
            }
        }

        System.out.println("classes      : " + classByFqn.size());
        System.out.println("fields       : " + fields.size());
        System.out.println("methods      : " + methods.size());
        System.out.println("conflicts    : " + conflicts.size());
        for (int i = 0; i < Math.min(15, conflicts.size()); i++) {
            System.out.println("   " + conflicts.get(i));
        }

        // Dotted FQN view so import lines can be rewritten directly.
        Map<String, String> classByDotted = new HashMap<>();
        for (Map.Entry<String, String> e : classByFqn.entrySet()) {
            classByDotted.put(e.getKey().replace('/', '.'), e.getValue().replace('/', '.'));
        }

        Pattern pFqn = Pattern.compile("net\\.minecraft\\.(class_\\d+(?:\\$class_\\d+)?)");
        Pattern pClass = Pattern.compile("\\b(class_\\d+)\\b");
        Pattern pField = Pattern.compile("\\b(field_\\d+)\\b");
        Pattern pMethod = Pattern.compile("\\b((?:method|comp)_\\d+)\\b");

        List<String> unmapped = new ArrayList<>();
        java.util.Set<String> usedAmbiguous = new java.util.TreeSet<>();
        int written = 0;

        try (Stream<Path> stream = Files.walk(inDir)) {
            for (Path file : stream.filter(p -> p.toString().endsWith(".java")).toList()) {
                String src = Files.readString(file, StandardCharsets.UTF_8);

                Matcher m = pFqn.matcher(src);
                StringBuffer sb1 = new StringBuffer();
                while (m.find()) {
                    String full = m.group(1);
                    String key = "net/minecraft/" + full;
                    String to = classByFqn.get(key);
                    if (to == null) {
                        unmapped.add(key);
                        to = full;
                    }
                    m.appendReplacement(sb1, Matcher.quoteReplacement(to.replace('/', '.')));
                }
                m.appendTail(sb1);
                String out = sb1.toString();

                out = replace(out, pClass, fqn -> {
                    String to = simple.get(fqn);
                    if (to == null) {
                        String full = classByFqn.get("net/minecraft/" + fqn);
                        if (full == null) {
                            unmapped.add("net/minecraft/" + fqn);
                            return fqn;
                        }
                        to = full.substring(full.lastIndexOf('/') + 1);
                    }
                    return to;
                });
                out = replace(out, pField, k -> {
                    if (ambiguous.containsKey(k)) {
                        usedAmbiguous.add(k + "  candidates=" + ambiguous.get(k));
                    }
                    return fields.getOrDefault(k, k);
                });
                out = replace(out, pMethod, k -> {
                    if (ambiguous.containsKey(k)) {
                        usedAmbiguous.add(k + "  candidates=" + ambiguous.get(k));
                    }
                    return methods.getOrDefault(k, k);
                });

                Path rel = inDir.relativize(file);
                Path dest = outDir.resolve(rel);
                Files.createDirectories(dest.getParent());
                Files.writeString(dest, out, StandardCharsets.UTF_8);
                written++;
            }
        }

        System.out.println("files written: " + written);
        System.out.println("unmapped     : " + unmapped.size());
        unmapped.stream().distinct().limit(15).forEach(s -> System.out.println("   ? " + s));
        System.out.println("AMBIGUOUS KEYS USED IN SOURCE: " + usedAmbiguous.size());
        usedAmbiguous.forEach(s -> System.out.println("   ! " + s));
    }

    interface Lookup {
        String get(String key);
    }

    private static String replace(String input, Pattern pattern, Lookup lookup) {
        Matcher m = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(lookup.get(m.group(1))));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
