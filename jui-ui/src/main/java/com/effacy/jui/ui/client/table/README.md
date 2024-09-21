# Overview

Tables are similar to galleries in that they display a collection of records for interaction. Unlike galleries the layout is tabular in nature with column headings and records represented in column segregated rows. Although the bahviour is similar to that of galleries (and they share code) that behaviour is sufficiently different to warrant separation (in particular there is not notion of a single presentable item but rather there are cells).

# Usage

The contract for a table is embodied in `ITable` with the default implementation `Table`. We discuss the default implementation in depth.

A `Table` instance is created with a guiding configuration (an instance of `Table.Config`) and a backing store (i.e. `IStore`); implicit  is that a table is over an underlying record type commensurate with that of the backing store. Unlike galleries tables rely on the configuration to declare and specify each column, including how data is rendered into the column cells, as illustrated in the following example:

```
new Table<SampleRecord> (new Table.Config<SampleRecord> () //
  .header ("Heading 1", header -> {
    header.renderer (LinkTableCellHandler.create (r -> r.name (), r -> {
      Logger.info ("Clicked on cell: " + r.name ());
    }));                
    header.sortable ();
    header.direction (SortDirection.ASC);
    header.sortHandler (s -> ...);
    header.width (Length.em (5));
   }) //
   .header ("Heading 2", header -> {
     ...
   }) //
   .header ("Heading 3", header -> {
     ...
   }), store);
```

Here we see that three columns (headers in the configuration) are declared with the first being explicit (the others follow the same pattern). Each header is given a title (along with other visual presentation facets such as any iconography and dimension constraints), indication on whether it can be sorted on (including a handler to implement the sort) and a mechanism to render content. We expand on these last two points in the following.

## Cell rendering

Rendering is via a `ITableCellRenderer` that declares a `render(...)` method that takes an element and some data and renders the content of the cell based on that data. The method may return a `ITableCellHandler` which can handle UI events emanating from DOM elements in the rendition of the cell.

Nominally the renderer must accept data of the type of the record (for the example above the type is `SimpleRecord` that, though not shown, has a single property `name`) however the renderer will generally only render a subset of the data in that record (and even just one property). This implies that one can establish a library of standard renderers for specific types of property (i.e. text, link, email, etc). Such a library is provided under the `renderer` sub-package.

The challenge with a generic renderer is mapping the record to the data type expected of the renderer. To facilitate this the class `ITableCellRenderer` provides a static conversion method `<R, D> ITableCellRenderer<R> convert(Function<R, D> converter, ITableCellRenderer<D> renderer)` that, via a data type conversion, allows one to employ a standard renderer. All standard renderers contained with employ a creator pattern that builds this conversion in:

```
public class TextTableCellRenderer extends TableCellRenderer<String> {

    ...
    public static <R> ITableCellRenderer<R> create(Function<R, String> converter) {
        return new TextTableCellRenderer ().convert (converter);
    }

}
```

Noting that the `convert` method is provided by `TableCellRenderer` and simply delegates to `ITableCellRenderer`.

The above still has limitations and that is when it comes to handling UI events. In most cases the UI event handler needs access to the enveloping record and this is transformed away by the conversion.  To handle this a separate pattern should be employed by renderers and that inverts the role of handler and renderer.  In this instance one implements a handler then provides a static method to return a renderer. The following example illustrates the approach (comments removed for brevity):

```
public class LinkTableCellHandler<R> implements ITableCellHandler<R> {

  private Element anchorEl;
  private Consumer<R> linkHandler;

  public LinkTableCellHandler(Element el, String data, Consumer<R> linkHandler) {
    this.linkHandler = linkHandler;
    anchorEl = DomSupport.createA (el);
    DomSupport.innerText (anchorEl, data);
    UIEventType.ONCLICK.sink (anchorEl);
  }

  @Override
  public boolean handleUIEvent(UIEvent e, R data) {
    if ((linkHandler == null) || !DomSupport.isChildOf (e.getTarget (), anchorEl))
      return false;
    linkHandler.accept (data);
    return true;
  }

  public static <R> ITableCellRenderer<R> create(Function<R, String> value, Consumer<R> linkHandler) {
    return new ITableCellRenderer<R> () {

      @Override
      public ITableCellHandler<R> render(Element el, R data) {
        return new LinkTableCellHandler<R> (el, value.apply (data), linkHandler);
      }

    };
  }
}
```

Here the class implements `ITableCellHandler` and the constructed immediately generated the relevant DOM and sinks the relevant events (in this case a single anchor tag). The event handler `handleUIEvent` performs the required event handler and return `true` if processed (in this case verifying a click on the anchor and invoking the passed link handler). Finally the static method `create(...)` actually generates the renderer which simple creates and returns an instance of the handler. The conversion from the record type to the string value needed by the constructor (being the data to render into the link) is performed by the conversion function `value`.

The previous example of header configuration uses this handler class as described:

```java
  ...
  header.renderer (LinkTableCellHandler.create (r -> r.name (), r -> {
    Logger.info ("Clicked on cell: " + r.name ());
  }));
  ...
```

## Sorting

Sorting for a header is configured via the `sortable` method on `Table.Config`. This is passed a default sort direction and a handler for implementing the sort. If not invoked the header is assumed not not be sortable. In addition the `sorted()` method configures that header as being the default header to sort when the table is first rendered or is fully refreshed.

The sort direction is employed when a header is clicked on but is not sort-activated. When this is the case and the header becomes sort-activated one of the direct sort directions needs to be selected. This serves as the selected direction.

The sort handled is invoked when the header is sort-activated and the direction is changed (either upon activation or when the direction is toggled). It is the responsibility of the handler to update and refresh the store accordingly.


# References

Some nice table designs:

* [Optio-Tables](https://dribbble.com/shots/20597769-Optio-Tables)
* [Table-Filters-and-Columns](https://dribbble.com/shots/19940747-Table-Filters-and-Columns)
* [Managing-table-column-in-Contact-list](https://dribbble.com/shots/19780063-Kirrivan-CRM-Managing-table-column-in-Contact-list)
* [Filtering-and-Sorting](https://dribbble.com/shots/19063230-Filtering-and-Sorting)