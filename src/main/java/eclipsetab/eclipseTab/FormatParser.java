package eclipsetab.eclipseTab;

import java.util.List;

public class FormatParser {

    // Resolve named gradients in the form <gradient:name>text</gradient>
    // Replaces with MiniMessage-compatible <gradient:#from:#to>text</gradient> or multiple stops.
    public static String resolveGradients(EclipseTab plugin, String input) {
        if (input == null || !input.contains("<gradient:")) return input;

        var gradients = plugin.getConfig().getConfigurationSection("gradients");
        if (gradients == null) return input;

        String result = input;

        // Simple parser: find occurrences of <gradient:name> and replace the tag start
        int idx = 0;
        while ((idx = result.indexOf("<gradient:", idx)) != -1) {
            int end = result.indexOf('>', idx);
            if (end == -1) break;
            String name = result.substring(idx + "<gradient:".length(), end).trim();
            String replaceTag = "<gradient:" + name + ">"; // fallback (no change)
            var section = gradients.getConfigurationSection(name);
            if (section != null) {
                if (section.contains("from") && section.contains("to")) {
                    String from = section.getString("from");
                    String to = section.getString("to");
                    if (isHex(from) && isHex(to)) {
                        replaceTag = "<gradient:" + from + ":" + to + ">";
                    }
                } else if (section.contains("stops")) {
                    List<String> stops = section.getStringList("stops");
                    // MiniMessage supports gradient with multiple colors separated by ':'
                    StringBuilder sb = new StringBuilder("<gradient:");
                    boolean ok = false;
                    for (String s : stops) {
                        if (isHex(s)) {
                            if (ok) sb.append(":");
                            sb.append(s);
                            ok = true;
                        }
                    }
                    if (ok) sb.append(">");
                    else sb = new StringBuilder("<gradient:" + name + ">");
                    replaceTag = sb.toString();
                }
            }

            result = result.substring(0, idx) + replaceTag + result.substring(end + 1);
            idx += replaceTag.length();
        }

        // Also convert common color codes like &a to legacy section sign to keep compat
        if (result.indexOf('&') != -1) result = result.replace('&', '§');
        return result;
    }

    private static boolean isHex(String s) {
        if (s == null) return false;
        return s.matches("^#?[A-Fa-f0-9]{6}$");
    }
}
