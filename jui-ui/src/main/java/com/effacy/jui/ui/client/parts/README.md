# Contents

This package contains various elements that can be incorporated into controls in a standardised manner. Mostly consisting of `BuilderItem`'s that can be directly introduced to larger builder structures.

Classes are:

1. `ContentMenu` for the inclusion of a simple menu of selectable items each with a label and optional icon. Follows the builder item pattern.

# Patterns

## Builder item

Specifically for inclusion into a `BuilderDOMDataRenderer` as part of a larger builder. Pattern follows:

```
public class MyBuilderItem<D> extends BuilderItem<D> {
  public interface MyBuilderItemCSS {
    ...
  }
  protected MyBuilderItemCSS styles;
  public MyBuilderItem(MyBuilderItemCSS styles) {
    this.styles = styles;
  }
  
  @Override
  protected Node<D> buildImpl(Container<D> parent) {
      Node<D> node = ...;
      ...
      return node;
  }
}
```