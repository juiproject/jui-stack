# Overview

*This is currently experimental.*

This library provides support for rich text formatting, transmission and composition (UI side) as well as (in the future) media generation (i.e. PDF, etc).

There is a dependency on **jui-ui** for the UI components (though the dependency is marked as optional given that the model objects as transportable) and weaker dependenices on **jui-remote** (and [FasterXML](https://github.com/FasterXML) via *jui-remoting*) for annotations (so the model classes are participate in JUI JSON serialisation, such as employed by JUI RPC).

The code is structured as follows:

1. [`com.effacy.text.type`](./src/main/java/com/effacy/jui/text/type/) contains the representation types for formatted text. These include annotations to facilitate serialisation (both with the JUI RPC mechanism for transport and separately for server-side serialisation based persistence).
2. [`com.effacy.text.ui`](./src/main/java/com/effacy/jui/text/ui/) contains the JUI code, which includes the editor itself and fragments that can be used to render formatted text as represented using the types in (1).

# Model

Our underlying model (which is not particularly unique) is premised on the basis that a body of text can be decomposed as a sequence of *blocks* each of which embodies a sequence of *lines* which, themselves, contain positional *formatting*. 

A *block* is typed (i.e. a paragraph, heading or equation), carries some universal attributes (i.e. indentation) and may declare type-specific meta-data (i.e. caption for an equation). The lines in block represent its content (though meta-data can contribute) and the explicit presentation is determined by the type (i.e. multiple lines in a paragraph are presented on new lines).

A *line* consists of a string of characters (the text) along with a collection of *formatting*. Formatting consists of a sequence of *formats* each of which specifies the format to apply (i.e. bold or italic) and range to which the format is applied (i.e. a start index and length into the character string). Formats are atomic so can overlap.

There are some normalisation requirements:

1. Blocks are strictly sequential (i.e. the blocks are read in order).
2. Lines are strictly sequential (i.e. the lines are read in order).
3. Formats are maximal (i.e there are no adjacent formats of the same type).
4. Formats are (weakly) ordered by starting index (by *weak* we mean that the order is simply compliant with there being no two formats A and B such that A appears before B and the start index of B is ahead of that of A).

There are also actions that can be performed, mainly in regard to line coersion and splitting and content insertion and removal. These are mentioned only in that they need to remain complaiant with the normalisation requirements through they are only used in the process of composing (or editing) content.

## Representation

Formatted content is represnted by the following classes (in `com.effacy.text.type`):

1. `FormattedText` represents the entire body of formatted content; this is composed of a sequence of blocks.
2. `FormattedBlock` represents the content *block*; this includes the `BlockType` (for the type) and a sequence of lines (along with attributes and meta-data).
3. `FormattedLine` represents a *line* within a *block*; this includes `Format` to specify formatting and `FormatType` to specify the type of format.

As noted earlier, these classes are serialisable, so can be used both in persistence and transport.

## Presentation (JUI)

Separate from editing (being a complex topic) is the presentation of formatted text. There are supporting fragments `FText` (and `FLine`) (in `com.effacy.jui.text.ui.fragment`) along with the supporting CSS in `jui_text_fragments.css` (this is initialised in the module initialiser).

To use you need to pass through the formatted text:

```java
FormattedText text = ...;
...
Div.$ (option).$ (
    ...
    FText.$ (text),
    ...
)
...
```

or

```java
FormattedText text = ...;
...
Div.$ (option).$ (el -> {
    ...
    FText.$ (el, text),
    ...
})
...
```

Generally `FLine` won't be employed directly, rather it is used by `FText`.

# Editing

## Usage

TBD

## Inner workings

### Block types

#### Paragraph

`ParagraphBlock`

TBD

#### Heading

`HeadingBlock`

TBD

#### Diagram

`DiagramBlock`

This makes use of [PlantUML](http://plantuml.org) to render UML and DITAA source to images. The generation process involves encoding the source into a URI compatible format then retrieving the source by passing that encoded source to a URL endpoint. PlantUML provides a demonstration endpoint at `//www.plantuml.com/plantuml/img/` so one can reference an image as:

```html
<img src="//www.plantuml.com/plantuml/img/{encoded-source}>" />
```

where `<encoded-source>` is the appropriately encoded UML/DITTA source. PlantUML also provides a convenience JS library to perform this encoded available at (as of the time of writing) `https://cdn.rawgit.com/jmnote/plantuml-encoder/d133f316/dist/plantuml-encoder.min.js`. A copy of this is bundled into the module script base of this library and it is that which is used by `DiagramBlock`.

The PlantUML demonstration URL is not one would generally consider for production use. One can either stand up a separate PlantUML server (a Dockerised version is available) or embed a version. The latter comes in various license variants and can be included as a Maven dependency. The `jui-playground` project includes the LGPL version:

```xml
<dependency>
    <groupId>net.sourceforge.plantuml</groupId>
    <artifactId>plantuml-lgpl</artifactId>
    <version>${version.plantuml}</version>
</dependency>
```

Along with the SpringMVC controller class `UMLController` that exposes an image generation endpoint at `/uml/` off the context root. The is configured in the entry point class (`TestApplication`) with:

```java
DiagramBlock.BASE_URL = "/uml/";
```

#### Equation

`EquationBlock`

TBD

## Testing

### Test cases

#### General

|Test|Outome|Comment|
|----|------|-------|
|Start editor, click at begining of in-active paragraph line and hit enter.|A new block should appear above the paragraph and should become active.|Tests newly activated block correctly inserts new paragraph.|
|Create a paragraph "Simple paragraph", place insertion pointer to the right of the 'e' (but to the left of the space) and hit return. Using the navigation keys move one space to the right then one space back again. Hit delete.|The paragraph should be merged with the one above.|Tests that the editor deals appropriately with introducing a leading non-breaking space.|
|Create a list with two items, position the cursor at the head of the second item and kit enter twice.|Two empty list items should be inserted above the line containing the cursor, the cursor should not move.|Tests the creation of empty list items (see Notion)|

#### Paragraphs

|Ref|Condition|Behaviour|
|---|---------|---------|
|PA.01|Press enter at begining of paragraph|New paragraph is inserted above but cursor position remains at begining of original paragraph (see Notion)|
|PA.02|Press enter at end of paragraph|New paragraph is inserted below and cursor is set to begining of new paragraph (see Notion)|
|PA.03|Press enter in middle of paragraph|New paragraph is inserted below with content as per the right of the cursor and the original paragraph retains content to left of cursor; cursor is set to begining of new paragraph (see Notion)|
|PA.04|Clicking on a block|The block will activate and the cursor will locate at the click point, or at the end of the content if no content resides at the point of click.|

#### Headings

|Ref|Condition|Behaviour|
|---|---------|---------|
|HE.01|Press enter at begining of heading|New paragraph is inserted above but cursor position remains at begining of original heading (see Notion)|
|HE.02|Press enter at end of heading|New paragraph is inserted below and cursor is set to begining of new heading (see Notion)|
|HE.03|Press enter in middle of heading|New paragraph is inserted below with content as per the right of the cursor and the original heading retains content to left of cursor; cursor is set to begining of new paragraph (see Notion)|
|HE.04|Clicking on a block|The block will activate and the cursor will locate at the click point, or at the end of the content if no content resides at the point of click.|

#### Lists

|Ref|Condition|Behaviour|
|---|---------|---------|
|LI.01|Press enter at begining of list|New list item is inserted above but cursor position remains at begining of original list (see Paragraph)|
|LI.02|Press enter at end of list|New list is inserted below and cursor is set to begining of new list (see Paragraph)|
|LI.03|Press enter in middle of list|New list is inserted below with content as per the right of the cursor and the original list retains content to left of cursor; cursor is set to begining of new list (see Paragraph)|
|LI.04|Clicking on a block|The block will activate and the cursor will locate at the click point, or at the end of the content if no content resides at the point of click.|
|LI.05|At all times|The numbering of consequtive list items of the same indentation will increase in unitial units commencing at the base number.|
|LI.06|Empty list item that is not active|Will display a placeholder indicating it is a list item (actual text not specified).|
