package com.effacy.jui.text.ui.editor.mermaid;

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.text.ui.editor.mermaid.MermaidModel.Edge;
import com.effacy.jui.text.ui.editor.mermaid.MermaidModel.Field;
import com.effacy.jui.text.ui.editor.mermaid.MermaidModel.Kind;
import com.effacy.jui.text.ui.editor.mermaid.MermaidModel.Node;

/**
 * Parses a (subset of) Mermaid source into a {@link MermaidModel} for the form-based editor.
 * Supports flowchart, entity-relationship and class diagrams in the common shapes the editor
 * itself produces (and many hand-written ones).
 * <p>
 * <b>It is strict by design.</b> If the source uses anything the model cannot represent
 * faithfully, {@link #parse(String)} returns {@code null} — the editor then keeps the user in
 * raw code mode rather than silently dropping content. No regex is used (GWT-safe).
 */
public final class MermaidParser {

    /**
     * Parses Mermaid source into a model, or returns {@code null} if it cannot be parsed
     * faithfully (the caller should then use raw code mode).
     *
     * @param source
     *               the Mermaid source.
     * @return the model, or {@code null}.
     */
    public static MermaidModel parse(String source) {
        if (source == null)
            return null;
        List<String> lines = contentLines(source);
        if (lines.isEmpty())
            return null;
        String header = lines.get(0);
        String hl = header.toLowerCase();
        try {
            if (hl.startsWith("erdiagram"))
                return parseEr(lines);
            if (hl.startsWith("classdiagram"))
                return parseClass(lines);
            if (hl.startsWith("flowchart") || hl.startsWith("graph"))
                return parseFlowchart(lines, header);
        } catch (RuntimeException e) {
            return null;
        }
        return null;
    }

    /************************************************************************
     * Entity relationship.
     ************************************************************************/

    private static MermaidModel parseEr(List<String> lines) {
        MermaidModel m = new MermaidModel();
        m.kind = Kind.ER;
        Node current = null;
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (current != null) {
                // Inside a multi-line entity block: attribute lines, and a "}" (possibly sharing
                // a line with a trailing attribute) closes it.
                int close = line.indexOf('}');
                String body = (close >= 0) ? line.substring(0, close).trim() : line;
                if (!body.isEmpty() && !appendAttrs(current, body))
                    return null;
                if (close >= 0)
                    current = null;
                continue;
            }
            int brace = line.indexOf('{');
            String beforeBrace = (brace >= 0) ? line.substring(0, brace).trim() : null;
            if ((brace >= 0) && isIdent(beforeBrace)) {
                // Entity block start (a "{" right after an identifier). Handles the multi-line
                // form ("Name {") and the inline form ("Name { type name ... }"). A "{" inside a
                // cardinality token (e.g. "||--o{") is not preceded by a bare identifier, so it
                // falls through to relationship parsing below.
                Node node = nodeNamed(m, beforeBrace);
                String rest = line.substring(brace + 1);
                int close = rest.indexOf('}');
                String body = ((close >= 0) ? rest.substring(0, close) : rest).trim();
                if (!body.isEmpty() && !appendAttrs(node, body))
                    return null;
                current = (close >= 0) ? null : node;
                continue;
            }
            // Relationship: LEFT <card> RIGHT [: label]
            Edge e = relLine(line);
            if (e == null)
                return null;
            // The connector must look like an ER cardinality (contains -- or ..).
            if (!isErConnector(e.connector))
                return null;
            nodeNamed(m, e.from);
            nodeNamed(m, e.to);
            m.edges.add(e);
        }
        return (current == null) ? m : null;   // unbalanced entity block → fail
    }

    /**
     * Parses one or more attributes from an entity-block body. Each attribute is
     * {@code type name} optionally followed by a {@code PK}/{@code FK} key; several may be
     * packed on one line (the inline block form). Returns {@code false} on a dangling token.
     */
    private static boolean appendAttrs(Node node, String text) {
        List<String> w = words(text);
        int i = 0;
        while (i < w.size()) {
            if ((i + 1) >= w.size())
                return false;   // a type with no name
            String type = w.get(i);
            String name = w.get(i + 1);
            i += 2;
            String key = "";
            if (i < w.size()) {
                String maybe = w.get(i).toUpperCase();
                if ("PK".equals(maybe) || "FK".equals(maybe)) {
                    key = maybe;
                    i++;
                }
            }
            node.fields.add(new Field(type, name, key));
        }
        return true;
    }

    /************************************************************************
     * Class.
     ************************************************************************/

    private static MermaidModel parseClass(List<String> lines) {
        MermaidModel m = new MermaidModel();
        m.kind = Kind.CLASS;
        Node current = null;
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (current != null) {
                if ("}".equals(line)) { current = null; continue; }
                // Member: optional visibility marker then text.
                String vis = "";
                String text = line;
                char c0 = line.charAt(0);
                if ((c0 == '+') || (c0 == '-') || (c0 == '#') || (c0 == '~')) {
                    vis = String.valueOf(c0);
                    text = line.substring(1).trim();
                }
                current.fields.add(new Field(vis, text, ""));
                continue;
            }
            if (line.startsWith("class ")) {
                String rest = line.substring(6).trim();
                boolean block = rest.endsWith("{");
                String name = block ? rest.substring(0, rest.length() - 1).trim() : rest;
                if (!isIdent(name))
                    return null;
                Node n = nodeNamed(m, name);
                if (block)
                    current = n;
                continue;
            }
            // Relationship: LEFT <rel> RIGHT [: label]
            Edge e = relLine(line);
            if (e == null)
                return null;
            if (!isClassConnector(e.connector))
                return null;
            nodeNamed(m, e.from);
            nodeNamed(m, e.to);
            m.edges.add(e);
        }
        return (current == null) ? m : null;
    }

    /************************************************************************
     * Flowchart.
     ************************************************************************/

    private static MermaidModel parseFlowchart(List<String> lines, String header) {
        MermaidModel m = new MermaidModel();
        m.kind = Kind.FLOWCHART;
        List<String> hw = words(header);
        if (hw.size() >= 2)
            m.dir = hw.get(1);
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.contains("-->")) {
                if (!parseFlowEdge(m, line))
                    return null;
                continue;
            }
            // A standalone node declaration: id<shape>label<close>.
            if (!parseFlowNode(m, line))
                return null;
        }
        return m;
    }

    private static boolean parseFlowEdge(MermaidModel m, String line) {
        int arrow = line.indexOf("-->");
        String left = line.substring(0, arrow).trim();
        String right = line.substring(arrow + 3).trim();
        String label = "";
        if (right.startsWith("|")) {
            int end = right.indexOf('|', 1);
            if (end < 0)
                return false;
            label = right.substring(1, end);
            right = right.substring(end + 1).trim();
        }
        if (!isIdent(left) || !isIdent(right))
            return false;   // inline shapes etc. → fall back to code mode
        nodeIded(m, left);
        nodeIded(m, right);
        Edge e = new Edge(left, right, "-->", label);
        m.edges.add(e);
        return true;
    }

    private static boolean parseFlowNode(MermaidModel m, String line) {
        int i = 0;
        while ((i < line.length()) && isIdentChar(line.charAt(i)))
            i++;
        if (i == 0)
            return false;
        String id = line.substring(0, i);
        String rest = line.substring(i);
        String shape = shapeOf(rest);
        if (shape == null)
            return false;
        String open = MermaidModel.shapeOpen(shape);
        String close = MermaidModel.shapeClose(shape);
        if (!rest.startsWith(open) || !rest.endsWith(close) || (rest.length() < open.length() + close.length()))
            return false;
        String label = rest.substring(open.length(), rest.length() - close.length());
        Node n = nodeIded(m, id);
        n.shape = shape;
        n.label = label;
        return true;
    }

    /** Determines the shape from the delimiter sequence (checks two-char forms first). */
    private static String shapeOf(String rest) {
        if (rest.startsWith("([") && rest.endsWith("])")) return "pill";
        if (rest.startsWith("((") && rest.endsWith("))")) return "circle";
        if (rest.startsWith("[") && rest.endsWith("]"))   return "box";
        if (rest.startsWith("{") && rest.endsWith("}"))   return "decision";
        if (rest.startsWith("(") && rest.endsWith(")"))   return "rounded";
        return null;
    }

    /************************************************************************
     * Shared.
     ************************************************************************/

    /** Parses a "LEFT connector RIGHT [: label]" relationship line, or {@code null}. */
    private static Edge relLine(String line) {
        String label = "";
        String head = line;
        int colon = line.indexOf(':');
        if (colon >= 0) {
            head = line.substring(0, colon).trim();
            label = stripQuotes(line.substring(colon + 1).trim());
        }
        List<String> w = words(head);
        if (w.size() != 3)
            return null;
        if (!isIdent(w.get(0)) || !isIdent(w.get(2)))
            return null;
        return new Edge(w.get(0), w.get(2), w.get(1), label);
    }

    private static boolean isErConnector(String c) {
        return (c != null) && (c.contains("--") || c.contains("..")) && containsAny(c, "|}{o");
    }

    private static boolean isClassConnector(String c) {
        return (c != null) && (c.contains("--") || c.contains("..") || c.contains("**")) && containsAny(c, "<>|*o.");
    }

    private static Node nodeNamed(MermaidModel m, String name) {
        for (Node n : m.nodes)
            if (name.equals(n.id))
                return n;
        Node n = new Node(name);
        m.nodes.add(n);
        return n;
    }

    private static Node nodeIded(MermaidModel m, String id) {
        return nodeNamed(m, id);
    }

    /** Splits to non-blank, trimmed, comment-stripped content lines. */
    private static List<String> contentLines(String src) {
        List<String> out = new ArrayList<>();
        for (String raw : src.split("\n", -1)) {
            String line = raw.trim();
            if (line.isEmpty())
                continue;
            if (line.startsWith("%%"))       // mermaid comment
                continue;
            out.add(line);
        }
        return out;
    }

    /** Splits on runs of whitespace (no regex). */
    private static List<String> words(String s) {
        List<String> out = new ArrayList<>();
        int i = 0, n = s.length();
        while (i < n) {
            while ((i < n) && isWs(s.charAt(i))) i++;
            int start = i;
            while ((i < n) && !isWs(s.charAt(i))) i++;
            if (i > start)
                out.add(s.substring(start, i));
        }
        return out;
    }

    private static boolean isWs(char c) { return (c == ' ') || (c == '\t'); }
    private static boolean isIdentChar(char c) { return Character.isLetterOrDigit(c) || (c == '_'); }
    private static boolean isIdent(String s) {
        if ((s == null) || s.isEmpty())
            return false;
        for (int i = 0; i < s.length(); i++)
            if (!isIdentChar(s.charAt(i)))
                return false;
        return true;
    }
    private static boolean containsAny(String s, String chars) {
        for (int i = 0; i < chars.length(); i++)
            if (s.indexOf(chars.charAt(i)) >= 0)
                return true;
        return false;
    }
    private static String stripQuotes(String s) {
        if ((s.length() >= 2) && s.startsWith("\"") && s.endsWith("\""))
            return s.substring(1, s.length() - 1);
        return s;
    }

    private MermaidParser() {}
}
