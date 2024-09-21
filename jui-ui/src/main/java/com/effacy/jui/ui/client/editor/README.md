# Overview

## Applying text formatting

*This applies to how textual content appears, such as being underlined, being in bold or appearing as a superscript.*

## Blocks

### Paragraph

`ParagraphBlock`

TBD

### Diagram

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

### Equation

`EquationBlock`


## Testing

|Test|Outome|Comment|
|----|------|-------|
|Start editor, click at begining of in-active paragraph line and hit enter.|A new block should appear above the paragraph and should become active.|Tests newly activated block correctly inserts new paragraph.|
|Create a paragraph "Simple paragraph", place insertion pointer to the right of the 'e' (but to the left of the space) and hit return. Using the navigation keys move one space to the right then one space back again. Hit delete.|The paragraph should be merged with the one above.|Tests that the editor deals appropriately with introducing a leading non-breaking space.|
|Create a list with two items, position the cursor at the head of the second item and kit enter twice.|Two empty list items should be inserted above the line containing the cursor, the cursor should not move.|Tests the creation of empty list items (see Notion)|

## Behaviours

### Paragraphs

|Ref|Condition|Behaviour|
|---|---------|---------|
|PA.01|Press enter at begining of paragraph|New paragraph is inserted above but cursor position remains at begining of original paragraph (see Notion)|
|PA.02|Press enter at end of paragraph|New paragraph is inserted below and cursor is set to begining of new paragraph (see Notion)|
|PA.03|Press enter in middle of paragraph|New paragraph is inserted below with content as per the right of the cursor and the original paragraph retains content to left of cursor; cursor is set to begining of new paragraph (see Notion)|
|PA.04|Clicking on a block|The block will activate and the cursor will locate at the click point, or at the end of the content if no content resides at the point of click.|

### Headings

|Ref|Condition|Behaviour|
|---|---------|---------|
|HE.01|Press enter at begining of heading|New paragraph is inserted above but cursor position remains at begining of original heading (see Notion)|
|HE.02|Press enter at end of heading|New paragraph is inserted below and cursor is set to begining of new heading (see Notion)|
|HE.03|Press enter in middle of heading|New paragraph is inserted below with content as per the right of the cursor and the original heading retains content to left of cursor; cursor is set to begining of new paragraph (see Notion)|
|HE.04|Clicking on a block|The block will activate and the cursor will locate at the click point, or at the end of the content if no content resides at the point of click.|

### Lists

|Ref|Condition|Behaviour|
|---|---------|---------|
|LI.01|Press enter at begining of list|New list item is inserted above but cursor position remains at begining of original list (see Paragraph)|
|LI.02|Press enter at end of list|New list is inserted below and cursor is set to begining of new list (see Paragraph)|
|LI.03|Press enter in middle of list|New list is inserted below with content as per the right of the cursor and the original list retains content to left of cursor; cursor is set to begining of new list (see Paragraph)|
|LI.04|Clicking on a block|The block will activate and the cursor will locate at the click point, or at the end of the content if no content resides at the point of click.|
|LI.05|At all times|The numbering of consequtive list items of the same indentation will increase in unitial units commencing at the base number.|
|LI.06|Empty list item that is not active|Will display a placeholder indicating it is a list item (actual text not specified).|
