package com.effacy.jui.text.ui.editor.mermaid;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.effacy.jui.text.ui.editor.mermaid.MermaidModel.Kind;
import com.effacy.jui.text.ui.editor.mermaid.MermaidModel.Node;

public class MermaidParserTest {

    @Test
    public void erParseAndRoundTrip() {
        String src = "erDiagram\n" +
            "    Idea ||--o{ Endorsement : \"backed by\"\n" +
            "    Actor ||--o{ Endorsement : casts\n" +
            "    Actor |o--o{ Endorsement : \"on behalf of\"\n" +
            "    Idea {\n" +
            "        text title\n" +
            "        enum status\n" +
            "    }\n" +
            "    Endorsement {\n" +
            "        enum kind\n" +
            "        ratio weight\n" +
            "    }\n" +
            "    Actor {\n" +
            "        text name\n" +
            "    }";

        MermaidModel m = MermaidParser.parse(src);
        assertNotNull(m);
        assertEquals(Kind.ER, m.kind);
        assertEquals(3, m.nodes.size());   // Idea, Endorsement, Actor
        assertEquals(3, m.edges.size());

        assertEquals("Idea", m.edges.get(0).from);
        assertEquals("Endorsement", m.edges.get(0).to);
        assertEquals("||--o{", m.edges.get(0).connector);
        assertEquals("backed by", m.edges.get(0).label);
        assertEquals("|o--o{", m.edges.get(2).connector);
        assertEquals("on behalf of", m.edges.get(2).label);

        Node idea = m.nodes.get(0);
        assertEquals("Idea", idea.id);
        assertEquals(2, idea.fields.size());
        assertEquals("text", idea.fields.get(0).a);
        assertEquals("title", idea.fields.get(0).b);

        // Serialisation re-parses to an identical model (stable round-trip).
        MermaidModel m2 = MermaidParser.parse(m.toSource());
        assertNotNull(m2);
        assertEquals(m.toSource(), m2.toSource());
        assertTrue(m.toSource().contains("Idea ||--o{ Endorsement : \"backed by\""));
        assertTrue(m.toSource().contains("text title"));
    }

    @Test
    public void erWithKeys() {
        MermaidModel m = MermaidParser.parse("erDiagram\n  Customer {\n    id ref PK\n    text name\n  }");
        assertNotNull(m);
        assertEquals("PK", m.nodes.get(0).fields.get(0).key);
        assertEquals("", m.nodes.get(0).fields.get(1).key);
        assertTrue(m.toSource().contains("id ref PK"));
    }

    @Test
    public void flowchartParseAndRoundTrip() {
        String src = "flowchart TD\n" +
            "    draft(Draft)\n" +
            "    ok{Approved?}\n" +
            "    done([Done])\n" +
            "    draft --> ok\n" +
            "    ok -->|Yes| done";
        MermaidModel m = MermaidParser.parse(src);
        assertNotNull(m);
        assertEquals(Kind.FLOWCHART, m.kind);
        assertEquals("TD", m.dir);
        assertEquals(3, m.nodes.size());
        assertEquals("rounded", node(m, "draft").shape);
        assertEquals("decision", node(m, "ok").shape);
        assertEquals("pill", node(m, "done").shape);
        assertEquals(2, m.edges.size());
        assertEquals("Yes", m.edges.get(1).label);

        MermaidModel m2 = MermaidParser.parse(m.toSource());
        assertNotNull(m2);
        assertEquals(m.toSource(), m2.toSource());
    }

    @Test
    public void classParse() {
        String src = "classDiagram\n" +
            "    class Animal {\n        +name: string\n        +eat()\n    }\n" +
            "    class Dog {\n    }\n" +
            "    Animal <|-- Dog : extends";
        MermaidModel m = MermaidParser.parse(src);
        assertNotNull(m);
        assertEquals(Kind.CLASS, m.kind);
        assertEquals(2, m.nodes.size());
        assertEquals("+", node(m, "Animal").fields.get(0).a);
        assertEquals("name: string", node(m, "Animal").fields.get(0).b);
        assertEquals(1, m.edges.size());
        assertEquals("<|--", m.edges.get(0).connector);
        assertEquals("extends", m.edges.get(0).label);

        MermaidModel m2 = MermaidParser.parse(m.toSource());
        assertNotNull(m2);
        assertEquals(m.toSource(), m2.toSource());
    }

    @Test
    public void unsupportedDiagramReturnsNull() {
        assertNull(MermaidParser.parse("sequenceDiagram\n    Alice->>Bob: Hi"));
        assertNull(MermaidParser.parse("gantt\n    title A"));
        assertNull(MermaidParser.parse(""));
        assertNull(MermaidParser.parse(null));
    }

    @Test
    public void flowchartWithInlineShapesFallsBack() {
        // Inline node shapes inside edges are not modelled — must fall back to code mode.
        assertNull(MermaidParser.parse("flowchart TD\n    A[Start] --> B{Go}"));
    }

    @Test
    public void malformedErFallsBack() {
        // Unbalanced entity block.
        assertNull(MermaidParser.parse("erDiagram\n    Idea {\n        text title"));
        // A dangling attribute token (a type with no name).
        assertNull(MermaidParser.parse("erDiagram\n    Idea {\n        title\n    }"));
    }

    @Test
    public void erInlineEntityBlocks() {
        // Inline "Name { type name }" blocks, multiple attributes on one line, a self relationship,
        // and an entity (Actor) that appears only in a relationship.
        String src = "erDiagram\n" +
            "    Idea }o--o{ Category : \"filed under\"\n" +
            "    Idea |o--o{ Idea : \"merged into\"\n" +
            "    Actor ||--o{ Comment : authors\n" +
            "    Category { text name }\n" +
            "    Comment { text body  date added }";
        MermaidModel m = MermaidParser.parse(src);
        assertNotNull(m);
        assertEquals(Kind.ER, m.kind);
        assertEquals(3, m.edges.size());

        Node comment = node(m, "Comment");
        assertNotNull(comment);
        assertEquals(2, comment.fields.size());
        assertEquals("text", comment.fields.get(0).a);
        assertEquals("body", comment.fields.get(0).b);
        assertEquals("date", comment.fields.get(1).a);
        assertEquals("added", comment.fields.get(1).b);

        assertEquals(1, node(m, "Category").fields.size());
        assertNotNull(node(m, "Actor"));
        assertEquals(0, node(m, "Actor").fields.size());

        MermaidModel m2 = MermaidParser.parse(m.toSource());
        assertNotNull(m2);
        assertEquals(m.toSource(), m2.toSource());
    }

    private static Node node(MermaidModel m, String id) {
        for (Node n : m.nodes)
            if (id.equals(n.id))
                return n;
        return null;
    }
}
