import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds intermediary -> official (Mojang) tiny v2 mappings.
 *
 * The Fabric intermediary artifact maps obf <-> intermediary, and the Mojang
 * client.txt maps official -> obf, so the two have to be chained through obf.
 * Obfuscated member names are only unique per class, so every member is
 * resolved inside its owning class.
 */
public class ChainedMappings {
    public static void main(String[] args) throws Exception {
        Path tinyPath = Paths.get(args[0]);
        Path pgPath = Paths.get(args[1]);
        Path outPath = Paths.get(args[2]);

        List<String> pgLines = Files.readAllLines(pgPath, StandardCharsets.UTF_8);
        System.err.println("proguard lines: " + pgLines.size());

        Map<String, String> obfToOfficialClass = new HashMap<>();
        Map<String, Map<String, List<String[]>>> fieldsByClass = new LinkedHashMap<>();
        Map<String, Map<String, List<String[]>>> methodsByClass = new LinkedHashMap<>();

        String currentClass = null;
        for (String raw : pgLines) {
            if (raw.isEmpty() || raw.startsWith("#")) {
                continue;
            }
            if (!Character.isWhitespace(raw.charAt(0))) {
                String line = raw.trim();
                int arrow = line.lastIndexOf(" -> ");
                if (arrow < 0) {
                    currentClass = null;
                    continue;
                }
                String right = line.substring(arrow + 4).trim();
                if (right.endsWith(":")) {
                    right = right.substring(0, right.length() - 1);
                }
                currentClass = right.replace('.', '/');
                obfToOfficialClass.put(currentClass, line.substring(0, arrow).trim().replace('.', '/'));
                fieldsByClass.computeIfAbsent(currentClass, k -> new LinkedHashMap<>());
                methodsByClass.computeIfAbsent(currentClass, k -> new LinkedHashMap<>());
                continue;
            }
            if (currentClass == null) {
                continue;
            }
            String line = raw.trim();
            int arrow = line.lastIndexOf(" -> ");
            if (arrow < 0) {
                continue;
            }
            String left = stripLineNumbers(line.substring(0, arrow).trim());
            String obfName = line.substring(arrow + 4).trim();
            int paren = left.indexOf('(');
            if (paren >= 0) {
                String name = left.substring(0, paren).trim();
                int nameEnd = name.lastIndexOf(' ');
                if (nameEnd >= 0) {
                    name = name.substring(nameEnd + 1).trim();
                }
                String params = left.substring(paren + 1, left.lastIndexOf(')')).trim();
                List<String> p = new ArrayList<>();
                if (!params.isEmpty()) {
                    for (String t : splitParams(params)) {
                        p.add(t);
                    }
                }
                methodsByClass.get(currentClass)
                        .computeIfAbsent(obfName, k -> new ArrayList<>())
                        .add(new String[] {name, String.join(",", p)});
            } else {
                int sp = left.lastIndexOf(' ');
                if (sp < 0) {
                    continue;
                }
                fieldsByClass.get(currentClass)
                        .computeIfAbsent(obfName, k -> new ArrayList<>())
                        .add(new String[] {left.substring(sp + 1).trim(), ""});
            }
        }
        System.err.println("pass1 done, classes: " + obfToOfficialClass.size());

        List<String> tinyLines = Files.readAllLines(tinyPath, StandardCharsets.UTF_8);
        System.err.println("tiny lines: " + tinyLines.size());

        StringBuilder sb = new StringBuilder();
        sb.append("tiny\t2\t0\tintermediary\tofficial\n");

        int classes = 0;
        int fieldLines = 0;
        int methodLines = 0;
        int unresolvedClasses = 0;
        int unresolvedFields = 0;
        int unresolvedMethods = 0;
        int debugUnmatched = 0;

        String curObf = null;
        String curInter = null;
        boolean officialKnown = false;

        for (String line : tinyLines) {
            if (line.isEmpty() || line.startsWith("tiny")) {
                continue;
            }
            if (line.charAt(0) != '\t') {
                String[] p = line.split("\t");
                if (p.length < 3 || !p[0].equals("c")) {
                    continue;
                }
                curObf = p[1];
                curInter = p[2];
                String off = obfToOfficialClass.get(curObf);
                officialKnown = off != null;
                if (officialKnown) {
                    classes++;
                    sb.append("c\t").append(curInter).append('\t').append(off).append('\n');
                } else {
                    unresolvedClasses++;
                    sb.append("c\t").append(curInter).append('\t').append(curInter).append('\n');
                }
                continue;
            }
            if (!officialKnown) {
                continue;
            }
            String[] p = line.substring(1).split("\t");
            if (p.length < 4) {
                continue;
            }
            String kind = p[0];
            String desc = p[1];
            String obfName = p[2];
            String interName = p[3];

            if (kind.equals("f")) {
                fieldLines++;
                List<String[]> cands = fieldsByClass
                        .getOrDefault(curObf, Map.of())
                        .get(obfName);
                String official = null;
                if (cands != null && !cands.isEmpty()) {
                    official = cands.get(0)[0];
                } else {
                    unresolvedFields++;
                }
                sb.append("\tf\t").append(desc).append('\t').append(interName).append('\t')
                        .append(official == null ? interName : official).append('\n');
            } else if (kind.equals("m")) {
                methodLines++;
                List<String[]> cands = methodsByClass
                        .getOrDefault(curObf, Map.of())
                        .get(obfName);
                String official = null;
                if (cands != null && !cands.isEmpty()) {
                    // Obfuscated method names are overloaded within a class, so the
                    // descriptor is the only reliable discriminator.
                    List<String> want = new ArrayList<>();
                    for (String param : descParams(desc)) {
                        int dims = 0;
                        String base = param;
                        while (base.endsWith("[]")) {
                            base = base.substring(0, base.length() - 2);
                            dims++;
                        }
                        String off = obfToOfficialClass.get(base);
                        String resolved = canon(off == null ? base : off);
                        for (int i = 0; i < dims; i++) {
                            resolved = resolved + "[]";
                        }
                        want.add(resolved);
                    }
                    for (String[] cand : cands) {
                        if (paramsMatch(cand[1], want)) {
                            official = cand[0];
                            break;
                        }
                    }
                    if (official == null) {
                        // Leave it mapped to itself rather than guessing: a wrong name compiles
                        // into something that looks right but calls the wrong method, while an
                        // unmapped intermediary name is obvious.
                        official = interName;
                        if (debugUnmatched < 10) {
                            debugUnmatched++;
                            System.err.println("NOMATCH " + curObf + " obfName=" + obfName
                                    + " desc=" + desc + " want=" + want);
                            for (String[] c : cands) {
                                System.err.println("      cand: " + c[0] + "(" + c[1] + ")");
                            }
                        }
                        unresolvedMethods++;
                    }
                } else {
                    if (debugUnmatched < 10) {
                        debugUnmatched++;
                        System.err.println("NOCAND " + curObf + " obfName=" + obfName + " desc=" + desc);
                    }
                    unresolvedMethods++;
                }
                sb.append("\tm\t").append(desc).append('\t').append(interName).append('\t')
                        .append(official == null ? interName : official).append('\n');
            }
        }

        try (PrintStream ps = new PrintStream(Files.newOutputStream(outPath), false, "UTF-8")) {
            ps.print(sb);
        }

        System.out.println("classes mapped      : " + classes);
        System.out.println("classes unresolved  : " + unresolvedClasses);
        System.out.println("field lines         : " + fieldLines + " (unresolved " + unresolvedFields + ")");
        System.out.println("method lines        : " + methodLines + " (unresolved " + unresolvedMethods + ")");
        System.out.println("written             : " + outPath);
    }

    private static String outer(String fqn) {
        int dollar = fqn.indexOf('$');
        return dollar < 0 ? fqn : fqn.substring(0, dollar);
    }

    /**
     * Puts a JVM descriptor type and a ProGuard type into the same spelling so the two can be
     * compared: expands primitive letters, resolves arrays and drops inner-class suffixes.
     */
    private static String canon(String type) {
        String s = type.trim().replace("...", "[]").replace('.', '/');
        int dims = 0;
        while (s.endsWith("[]")) {
            s = s.substring(0, s.length() - 2);
            dims++;
        }
        s = switch (s) {
            case "Z" -> "boolean";
            case "B" -> "byte";
            case "C" -> "char";
            case "S" -> "short";
            case "I" -> "int";
            case "J" -> "long";
            case "F" -> "float";
            case "D" -> "double";
            case "V" -> "void";
            default -> s;
        };
        StringBuilder sb = new StringBuilder(outer(s));
        for (int i = 0; i < dims; i++) {
            sb.append("[]");
        }
        return sb.toString();
    }

    private static boolean paramsMatch(String candidate, List<String> want) {
        List<String> have = new ArrayList<>();
        if (!candidate.trim().isEmpty()) {
            for (String t : candidate.split(",")) {
                have.add(canon(t));
            }
        }
        if (have.size() != want.size()) {
            return false;
        }
        for (int i = 0; i < have.size(); i++) {
            if (!have.get(i).equals(want.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static String stripLineNumbers(String s) {
        int idx = 0;
        int digits = 0;
        while (idx < s.length() && Character.isDigit(s.charAt(idx))) {
            idx++;
            digits++;
        }
        if (digits > 0 && idx + 1 < s.length() && s.charAt(idx) == ':' && s.charAt(idx + 1) == ':') {
            return s.substring(idx + 2);
        }
        return s;
    }

    private static List<String> splitParams(String params) {
        List<String> out = new ArrayList<>();
        int depth = 0;
        StringBuilder cur = new StringBuilder();
        for (char c : params.toCharArray()) {
            if (c == '<') {
                depth++;
            } else if (c == '>') {
                depth--;
            }
            if (c == ',' && depth == 0) {
                out.add(cur.toString().trim());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        if (cur.length() > 0) {
            out.add(cur.toString().trim());
        }
        return out;
    }

    private static List<String> descParams(String desc) {
        List<String> out = new ArrayList<>();
        int i = desc.indexOf('(');
        if (i < 0) {
            return out;
        }
        i++;
        while (i < desc.length() && desc.charAt(i) != ')') {
            int dims = 0;
            while (i < desc.length() && desc.charAt(i) == '[') {
                dims++;
                i++;
            }
            if (i >= desc.length()) {
                break;
            }
            char c = desc.charAt(i);
            if (c == 'L') {
                int end = desc.indexOf(';', i);
                if (end < 0) {
                    break;
                }
                String type = desc.substring(i + 1, end);
                i = end + 1;
                for (int d = 0; d < dims; d++) {
                    type = type + "[]";
                }
                out.add(type);
            } else {
                String type = String.valueOf(c);
                for (int d = 0; d < dims; d++) {
                    type = type + "[]";
                }
                out.add(type);
                i++;
            }
        }
        return out;
    }
}
