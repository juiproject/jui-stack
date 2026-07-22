package com.effacy.jui.text.ui.editor.mermaid;

import java.util.ArrayList;
import java.util.List;

/**
 * A structured, editable representation of a (subset of) Mermaid diagram — the model behind
 * a form-based "build it without the syntax" editor. Covers three diagram kinds: flowchart,
 * entity-relationship and class.
 * <p>
 * The model deliberately stores the <em>raw</em> Mermaid tokens for shapes and connectors
 * (e.g. an ER cardinality {@code ||--o{}, a class relation {@code <|--}, a flowchart arrow
 * {@code -->}) so that {@link #toSource()} round-trips faithfully even for tokens a friendly
 * picker does not enumerate. {@link MermaidParser} produces a model only when it can parse
 * faithfully; otherwise the editor falls back to raw code mode.
 *
 * @see MermaidParser
 */
public class MermaidModel {

    /** The supported diagram kinds (the form builder handles these). */
    public enum Kind { FLOWCHART, ER, CLASS }

    /** A field within an entity (ER: type + name + key) or class (CLASS: visibility + member text). */
    public static class Field {
        /** ER: the data type. CLASS: the visibility marker ({@code +}/{@code -}/{@code #}). */
        public String a = "";
        /** ER: the field name. CLASS: the member text (e.g. {@code name: string} or {@code save()}). */
        public String b = "";
        /** ER only: a key marker ({@code PK} / {@code FK}), or empty. */
        public String key = "";

        public Field() {}
        public Field(String a, String b, String key) { this.a = a; this.b = b; this.key = (key == null) ? "" : key; }
    }

    /** A box: flowchart node, ER entity or class. */
    public static class Node {
        /** The Mermaid identifier. For ER/CLASS this is the entity/class name. */
        public String id;
        /** Flowchart display label (ER/CLASS use {@link #id} as the name). */
        public String label = "";
        /** Flowchart shape token key: {@code rounded|box|decision|pill|circle}. */
        public String shape = "rounded";
        /** ER attributes / class members. */
        public List<Field> fields = new ArrayList<>();

        public Node() {}
        public Node(String id) { this.id = id; }
    }

    /** A link: flowchart connection, ER relationship or class relationship. */
    public static class Edge {
        public String from;
        public String to;
        public String label = "";
        /** The raw connector token (ER cardinality, class relation, or flowchart arrow). */
        public String connector = "";

        public Edge() {}
        public Edge(String from, String to, String connector, String label) {
            this.from = from; this.to = to; this.connector = connector; this.label = (label == null) ? "" : label;
        }
    }

    public Kind kind = Kind.FLOWCHART;
    public String dir = "TD";
    public List<Node> nodes = new ArrayList<>();
    public List<Edge> edges = new ArrayList<>();

    /** Flowchart shape syntax (open/close delimiters), keyed by the {@link Node#shape} token. */
    public static String shapeOpen(String shape) {
        switch (shape == null ? "" : shape) {
            case "box":      return "[";
            case "decision": return "{";
            case "pill":     return "([";
            case "circle":   return "((";
            default:         return "(";
        }
    }
    public static String shapeClose(String shape) {
        switch (shape == null ? "" : shape) {
            case "box":      return "]";
            case "decision": return "}";
            case "pill":     return "])";
            case "circle":   return "))";
            default:         return ")";
        }
    }

    /**
     * Serialises this model back to Mermaid source.
     *
     * @return the Mermaid source.
     */
    public String toSource() {
        StringBuilder sb = new StringBuilder();
        switch (kind) {
            case ER:        return erSource(sb);
            case CLASS:     return classSource(sb);
            case FLOWCHART:
            default:        return flowchartSource(sb);
        }
    }

    private String flowchartSource(StringBuilder sb) {
        sb.append("flowchart ").append((dir == null || dir.isEmpty()) ? "TD" : dir);
        for (Node n : nodes)
            sb.append("\n    ").append(n.id).append(shapeOpen(n.shape)).append(blank(n.label)).append(shapeClose(n.shape));
        for (Edge e : edges) {
            String conn = (e.connector == null || e.connector.isEmpty()) ? "-->" : e.connector;
            sb.append("\n    ").append(e.from).append(' ').append(conn);
            if (e.label != null && !e.label.isEmpty())
                sb.append('|').append(e.label).append('|');
            sb.append(' ').append(e.to);
        }
        return sb.toString();
    }

    private String erSource(StringBuilder sb) {
        sb.append("erDiagram");
        for (Edge e : edges) {
            String conn = (e.connector == null || e.connector.isEmpty()) ? "||--o{" : e.connector;
            sb.append("\n    ").append(e.from).append(' ').append(conn).append(' ').append(e.to)
              .append(" : \"").append(e.label == null ? "" : e.label.replace("\"", "")).append('"');
        }
        for (Node n : nodes) {
            sb.append("\n    ").append(n.id).append(" {");
            for (Field f : n.fields) {
                sb.append("\n        ").append(blank(f.a, "text")).append(' ').append(blank(f.b, "field"));
                if (f.key != null && !f.key.isEmpty())
                    sb.append(' ').append(f.key);
            }
            sb.append("\n    }");
        }
        return sb.toString();
    }

    private String classSource(StringBuilder sb) {
        sb.append("classDiagram");
        for (Node n : nodes) {
            sb.append("\n    class ").append(n.id).append(" {");
            for (Field f : n.fields)
                sb.append("\n        ").append(blank(f.a, "+")).append(f.b == null ? "" : f.b);
            sb.append("\n    }");
        }
        for (Edge e : edges) {
            String conn = (e.connector == null || e.connector.isEmpty()) ? "-->" : e.connector;
            sb.append("\n    ").append(e.from).append(' ').append(conn).append(' ').append(e.to);
            if (e.label != null && !e.label.isEmpty())
                sb.append(" : ").append(e.label);
        }
        return sb.toString();
    }

    private static String blank(String s) { return (s == null || s.isEmpty()) ? " " : s; }
    private static String blank(String s, String dflt) { return (s == null || s.isEmpty()) ? dflt : s; }
}
