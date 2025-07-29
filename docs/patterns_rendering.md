# Rendering Patterns and Examples Guide

This document provides comprehensive examples and patterns for implementing rendering in JUI applications. It covers component creation, fragment usage, DOM building techniques, styling patterns, and advanced rendering tricks that developers can use to build effective UI components.

## Table of Contents

1. [Basic Component Patterns](#basic-component-patterns)
2. [Fragment Creation and Usage](#fragment-creation-and-usage)
3. [DOM Building Techniques](#dom-building-techniques)
4. [Event Handling Patterns](#event-handling-patterns)
5. [Styling and CSS Patterns](#styling-and-css-patterns)
6. [Runtime Rendering and Manipulation](#runtime-rendering-and-manipulation)
7. [Advanced Component Patterns](#advanced-component-patterns)
8. [Layout and Composition Patterns](#layout-and-composition-patterns)
9. [Best Practices and Tricks](#best-practices-and-tricks)
10. [Real-World Examples](#real-world-examples)

## Basic Component Patterns

### SimpleComponent Pattern

The simplest component extends `SimpleComponent` and uses constructor-based rendering:

```java
public class HelloWorld extends SimpleComponent {
    
    public HelloWorld(String message) {
        renderer(root -> {
            H1.$(root).text("Hello World");
            P.$(root).text(message);
        });
    }
}
```

### Component with Configuration

For components requiring complex configuration:

```java
public class MyCard extends Component<MyCard.Config> {
    
    public static class Config extends Component.Config {
        private String title;
        private String content;
        private boolean bordered = true;
        
        public Config title(String title) {
            this.title = title;
            return this;
        }
        
        public Config content(String content) {
            this.content = content;
            return this;
        }
        
        public Config bordered(boolean bordered) {
            this.bordered = bordered;
            return this;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public MyCard build(LayoutData... data) {
            return (MyCard) super.build(new MyCard(this), data);
        }
    }
    
    public MyCard(Config config) {
        super(config);
    }
    
    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$(el).$(root -> {
            Div.$(root)
                .style("card")
                .test(config().bordered, b -> b.style("bordered"))
                .$(card -> {
                    if (config().title != null) {
                        H3.$(card).style("card-title").text(config().title);
                    }
                    if (config().content != null) {
                        P.$(card).style("card-content").text(config().content);
                    }
                });
        }).build();
    }
}
```

### Record-Based Configuration

For simpler configuration using records:

```java
public class StatusBadge extends SimpleComponent {
    
    public static record Config(String text, BadgeType type, boolean rounded) {
        public static Config of(String text) {
            return new Config(text, BadgeType.DEFAULT, false);
        }
        
        public static Config of(String text, BadgeType type) {
            return new Config(text, type, false);
        }
    }
    
    public enum BadgeType {
        DEFAULT, PRIMARY, SUCCESS, WARNING, ERROR
    }
    
    public StatusBadge(Config config) {
        renderer(root -> {
            Span.$(root)
                .style("badge", config.type().name().toLowerCase())
                .test(config.rounded(), b -> b.style("rounded"))
                .text(config.text());
        });
    }
}
```

### Inline Component Creation

For quick, one-off components:

```java
// Simple inline component
ComponentCreator.build(root -> {
    Div.$(root).style("alert", "alert-info").$(alert -> {
        Em.$(alert).style(FontAwesome.infoCircle());
        Span.$(alert).text("This is an information message");
    });
});

// Inline component with configuration
ComponentCreator.build(cfg -> {
    cfg.testId("my-component");
    cfg.styles("custom-component");
}, root -> {
    // Component content
});
```

## Fragment Creation and Usage

### Basic Fragment Pattern

Standard fragment implementation:

```java
public class Alert {
    
    public enum Type {
        INFO, SUCCESS, WARNING, ERROR
    }
    
    public static AlertFragment $(Type type, String message) {
        return new AlertFragment(type, message);
    }
    
    public static AlertFragment $(IDomInsertableContainer<?> parent, Type type, String message) {
        AlertFragment frg = $(type, message);
        if (parent != null)
            parent.insert(frg);
        return frg;
    }
    
    public static class AlertFragment extends BaseFragment<AlertFragment> {
        
        private Type type;
        private String message;
        
        public AlertFragment(Type type, String message) {
            this.type = type;
            this.message = message;
        }
        
        @Override
        protected void buildInto(ElementBuilder root) {
            root.style("juiAlert", type.name().toLowerCase())
                .$(alert -> {
                    Em.$(alert).style(getIconForType(type));
                    Span.$(alert).text(message);
                });
        }
        
        private String getIconForType(Type type) {
            return switch (type) {
                case INFO -> FontAwesome.infoCircle();
                case SUCCESS -> FontAwesome.checkCircle();
                case WARNING -> FontAwesome.exclamationTriangle();
                case ERROR -> FontAwesome.timesCircle();
            };
        }
    }
}
```

### Fragment with Children

Fragment that can contain child elements:

```java
public class Panel {
    
    public static PanelFragment $(String title) {
        return new PanelFragment(title);
    }
    
    public static PanelFragment $(IDomInsertableContainer<?> parent, String title) {
        PanelFragment frg = $(title);
        if (parent != null)
            parent.insert(frg);
        return frg;
    }
    
    public static class PanelFragment extends BaseFragmentWithChildren<PanelFragment> {
        
        private String title;
        
        public PanelFragment(String title) {
            this.title = title;
        }
        
        @Override
        protected void buildInto(ElementBuilder root) {
            root.style("juiPanel").$(panel -> {
                if (title != null) {
                    Div.$(panel).style("panel-header").$(header -> {
                        H4.$(header).text(title);
                    });
                }
                Div.$(panel).style("panel-body").$(body -> {
                    // Children will be added here
                    super.buildInto(body);
                });
            });
        }
    }
}
```

### Fragment with Advanced Configuration

Fragment with fluent configuration:

```java
public class Card {
    
    public static CardFragment $() {
        return new CardFragment();
    }
    
    public static CardFragment $(IDomInsertableContainer<?> parent) {
        CardFragment frg = $();
        if (parent != null)
            parent.insert(frg);
        return frg;
    }
    
    public static class CardFragment extends BaseFragmentWithChildren<CardFragment> {
        
        private String title;
        private String subtitle;
        private Length width;
        private Length height;
        private boolean clickable = false;
        private Runnable onclick;
        
        public CardFragment title(String title) {
            this.title = title;
            return this;
        }
        
        public CardFragment subtitle(String subtitle) {
            this.subtitle = subtitle;
            return this;
        }
        
        public CardFragment width(Length width) {
            this.width = width;
            return this;
        }
        
        public CardFragment height(Length height) {
            this.height = height;
            return this;
        }
        
        public CardFragment clickable(Runnable onclick) {
            this.clickable = true;
            this.onclick = onclick;
            return this;
        }
        
        @Override
        protected void buildInto(ElementBuilder root) {
            root.style("juiCard")
                .test(clickable, r -> r.style("clickable"))
                .test(width != null, r -> r.css(CSS.WIDTH, width))
                .test(height != null, r -> r.css(CSS.HEIGHT, height))
                .test(clickable && onclick != null, r -> r.onclick(e -> onclick.run()))
                .$(card -> {
                    if (title != null || subtitle != null) {
                        Div.$(card).style("card-header").$(header -> {
                            if (title != null) {
                                H3.$(header).style("card-title").text(title);
                            }
                            if (subtitle != null) {
                                P.$(header).style("card-subtitle").text(subtitle);
                            }
                        });
                    }
                    
                    Div.$(card).style("card-content").$(content -> {
                        super.buildInto(content);
                    });
                });
        }
    }
}
```

## DOM Building Techniques

### Standard DOM Builder Pattern

Basic DOM construction using the standard pattern:

```java
@Override
protected INodeProvider buildNode(Element el) {
    return Wrap.$(el).$(root -> {
        // Main container
        Div.$(root).style("container").$(container -> {
            
            // Header section
            Div.$(container).style("header").$(header -> {
                H1.$(header).text("Page Title");
                P.$(header).style("subtitle").text("Page description");
            });
            
            // Content section
            Div.$(container).style("content").$(content -> {
                // Multiple content blocks
                createContentBlock(content, "Block 1", "Content for block 1");
                createContentBlock(content, "Block 2", "Content for block 2");
            });
            
            // Footer section
            Div.$(container).style("footer").$(footer -> {
                Button.$(footer).text("Action 1").onclick(this::handleAction1);
                Button.$(footer).text("Action 2").onclick(this::handleAction2);
            });
        });
    }).build();
}

private void createContentBlock(ContainerBuilder<?> parent, String title, String content) {
    Div.$(parent).style("content-block").$(block -> {
        H3.$(block).text(title);
        P.$(block).text(content);
    });
}
```

### Using Multiple Helper Classes

Example showing various DOM builder helper classes:

```java
Wrap.$(el).$(root -> {
    
    // Typography elements
    H1.$(root).text("Main Heading");
    H2.$(root).text("Sub Heading");
    P.$(root).text("Paragraph text with ").$(
        Strong.$().text("bold text"),
        Em.$().text(" and emphasized text")
    );
    
    // Lists
    Ul.$(root).$(list -> {
        Li.$(list).text("First item");
        Li.$(list).text("Second item");
        Li.$(list).$(item -> {
            Span.$(item).text("Third item with ");
            A.$(item).href("#").text("a link");
        });
    });
    
    // Form elements
    Form.$(root).$(form -> {
        Div.$(form).style("form-group").$(group -> {
            Label.$(group).text("Name:");
            Input.$(group).type("text").placeholder("Enter your name");
        });
        
        Div.$(form).style("form-group").$(group -> {
            Label.$(group).text("Email:");
            Input.$(group).type("email").placeholder("Enter your email");
        });
        
        Button.$(form).type("submit").text("Submit");
    });
    
    // Tables
    Table.$(root).$(table -> {
        Thead.$(table).$(head -> {
            Tr.$(head).$(row -> {
                Th.$(row).text("Name");
                Th.$(row).text("Age");
                Th.$(row).text("City");
            });
        });
        Tbody.$(table).$(body -> {
            Tr.$(body).$(row -> {
                Td.$(row).text("John");
                Td.$(row).text("25");
                Td.$(row).text("New York");
            });
        });
    });
    
}).build();
```

### Conditional and Dynamic DOM Building

Techniques for conditional rendering and dynamic content:

```java
@Override
protected INodeProvider buildNode(Element el) {
    return Wrap.$(el).$(root -> {
        
        // Conditional rendering
        if (config().showHeader) {
            Div.$(root).style("header").$(header -> {
                H1.$(header).text(config().title);
            });
        }
        
        // Using test() method for conditional styling
        Div.$(root)
            .style("content")
            .test(config().bordered, div -> div.style("bordered"))
            .test(config().highlighted, div -> div.style("highlighted"))
            .$(content -> {
                // Content here
            });
        
        // Dynamic list rendering
        config().items.forEach(item -> {
            Div.$(root).style("item").$(itemDiv -> {
                Span.$(itemDiv).text(item.getName());
                if (item.isImportant()) {
                    Em.$(itemDiv).style(FontAwesome.star(), "important");
                }
            });
        });
        
        // Conditional footer
        if (config().actions != null && !config().actions.isEmpty()) {
            Div.$(root).style("actions").$(actions -> {
                config().actions.forEach(action -> {
                    Button.$(actions)
                        .text(action.getLabel())
                        .onclick(e -> action.execute());
                });
            });
        }
        
    }).build();
}
```

### Using Streams for Dynamic Content

Advanced pattern using Java streams for content generation:

```java
// Generate cards from a list of data
Div.$(root).style("card-grid").$(
    data.stream()
        .map(item -> Card.$()
            .title(item.getTitle())
            .subtitle(item.getSubtitle())
            .clickable(() -> selectItem(item))
            .$(card -> {
                P.$(card).text(item.getDescription());
                if (item.hasImage()) {
                    Img.$(card).src(item.getImageUrl()).alt(item.getTitle());
                }
            })
        )
        .toArray(CardFragment[]::new)
);

// Generate a navigation menu
Nav.$(root).$(nav -> {
    Ul.$(nav).style("nav-list").$(
        menuItems.stream()
            .map(menuItem -> Li.$()
                .style("nav-item")
                .test(menuItem.isActive(), li -> li.style("active"))
                .$(li -> {
                    A.$(li)
                        .href(menuItem.getUrl())
                        .text(menuItem.getLabel())
                        .onclick(e -> handleNavigation(menuItem));
                })
            )
            .toArray(ElementBuilder[]::new)
    );
});
```

## Event Handling Patterns

### Basic Event Handling

Simple event handlers for common interactions:

```java
// Click handlers
Button.$(root).text("Click me").onclick(e -> {
    Logger.info("Button clicked!");
});

// Mouse events
Div.$(root).style("hover-area")
    .onmouseenter(e -> Logger.info("Mouse entered"))
    .onmouseleave(e -> Logger.info("Mouse left"))
    .onclick(e -> Logger.info("Area clicked"));

// Keyboard events
Input.$(root).type("text")
    .onkeypress(e -> {
        if (e.getKeyCode() == 13) { // Enter key
            handleSubmit();
        }
    })
    .onkeyup(e -> {
        validateInput(((HTMLInputElement) e.getTarget()).value);
    });

// Form events
Form.$(root)
    .onsubmit(e -> {
        e.preventDefault();
        handleFormSubmit();
    });
```

### Advanced Event Handling with Element Access

Event handlers that need to manipulate the target element:

```java
Button.$(root).text("Toggle").onclick((e, n) -> {
    HTMLButtonElement button = (HTMLButtonElement) n;
    Element element = (Element) n;
    
    if (element.classList.contains("active")) {
        element.classList.remove("active");
        button.textContent = "Activate";
    } else {
        element.classList.add("active");
        button.textContent = "Deactivate";
    }
});

// Input validation with immediate feedback
Input.$(root).type("email").oninput((e, n) -> {
    HTMLInputElement input = (HTMLInputElement) n;
    Element element = (Element) n;
    
    if (isValidEmail(input.value)) {
        element.classList.remove("invalid");
        element.classList.add("valid");
    } else {
        element.classList.remove("valid");
        element.classList.add("invalid");
    }
});
```

### Event Handling with State Management

Patterns for managing component state through events:

```java
public class ToggleButton extends SimpleComponent {
    
    private boolean active = false;
    private Consumer<Boolean> onToggle;
    
    public ToggleButton(String label, Consumer<Boolean> onToggle) {
        this.onToggle = onToggle;
        
        renderer(root -> {
            Button.$(root)
                .text(label)
                .style("toggle-button")
                .test(active, btn -> btn.style("active"))
                .onclick(this::handleToggle);
        });
    }
    
    private void handleToggle(UIEvent e) {
        active = !active;
        
        // Update DOM immediately
        Element button = (Element) e.getTarget();
        if (active) {
            button.classList.add("active");
        } else {
            button.classList.remove("active");
        }
        
        // Notify listeners
        if (onToggle != null) {
            onToggle.accept(active);
        }
    }
    
    public void setActive(boolean active) {
        if (this.active != active) {
            this.active = active;
            if (isRendered()) {
                // Update existing DOM
                rerender();
            }
        }
    }
}
```

### Async Event Handling

Patterns for handling asynchronous operations:

```java
Button.$(root).text("Load Data").onclick(e -> {
    Element button = (Element) e.getTarget();
    
    // Disable button and show loading state
    ((HTMLButtonElement) button).disabled = true;
    button.classList.add("loading");
    
    // Perform async operation
    loadDataAsync().then(data -> {
        // Success
        button.classList.remove("loading");
        button.classList.add("success");
        ((HTMLButtonElement) button).disabled = false;
        ((HTMLButtonElement) button).textContent = "Data Loaded";
        
        // Process data
        processLoadedData(data);
        
    }).catch_(error -> {
        // Error
        button.classList.remove("loading");
        button.classList.add("error");
        ((HTMLButtonElement) button).disabled = false;
        ((HTMLButtonElement) button).textContent = "Load Failed";
        
        // Show error message
        showErrorMessage("Failed to load data: " + error);
    });
});
```

## Styling and CSS Patterns

### Inline Styles

Direct style application for dynamic styling:

```java
// Using CSS helper class
Div.$(root)
    .css(CSS.DISPLAY, "flex")
    .css(CSS.FLEX_DIRECTION, "column")
    .css(CSS.GAP, Length.em(1))
    .css(CSS.PADDING, Insets.em(1, 2));

// Using direct CSS strings
Div.$(root)
    .css("display: flex; flex-direction: column;")
    .css("gap", "1em")
    .css("padding", "1em 2em");

// Conditional styles
Div.$(root)
    .css(config().fullWidth ? "width: 100%;" : "width: auto;")
    .test(config().centered, div -> div.css("margin: 0 auto;"));
```

### CSS Class Application

Applying CSS classes conditionally:

```java
Div.$(root)
    .style("component", "card")
    .test(config().bordered, div -> div.style("bordered"))
    .test(config().shadow, div -> div.style("shadow"))
    .test(config().rounded, div -> div.style("rounded"))
    .test(config().size != null, div -> div.style("size-" + config().size.name().toLowerCase()));

// Dynamic class building
List<String> classes = new ArrayList<>();
classes.add("base-component");
if (isActive()) classes.add("active");
if (isDisabled()) classes.add("disabled");
if (getType() != null) classes.add("type-" + getType().name().toLowerCase());

Div.$(root).style(classes.toArray(String[]::new));
```

### Local CSS Pattern

Component with localized CSS resources:

```java
public class StyledComponent extends Component<StyledComponent.Config> {
    
    @Override
    protected ILocalCSS styles() {
        return LocalCSS.instance();
    }
    
    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$(el).$(root -> {
            Div.$(root).style(styles().container()).$(container -> {
                H2.$(container).style(styles().title()).text(config().title);
                P.$(container).style(styles().content()).text(config().content);
                
                Div.$(container).style(styles().actions()).$(actions -> {
                    Button.$(actions).style(styles().primaryButton()).text("Primary");
                    Button.$(actions).style(styles().secondaryButton()).text("Secondary");
                });
            });
        }).build();
    }
    
    public static interface ILocalCSS extends IComponentCSS {
        String container();
        String title();
        String content();
        String actions();
        String primaryButton();
        String secondaryButton();
    }
    
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "com/example/StyledComponent.css"
    })
    public static abstract class LocalCSS implements ILocalCSS {
        
        private static LocalCSS STYLES;
        
        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (LocalCSS) GWT.create(LocalCSS.class);
                STYLES.ensureInjected();
            }
            return STYLES;
        }
    }
}
```

### CSS Variable Usage

Using CSS variables for dynamic theming:

```java
// Component with CSS variables
Div.$(root)
    .style("themed-component")
    .css("--primary-color", config().primaryColor)
    .css("--font-size", config().fontSize.toString())
    .css("--border-radius", config().borderRadius.toString())
    .$(component -> {
        // Component content that uses the variables
    });

// Responsive design with CSS variables
Div.$(root)
    .style("responsive-grid")
    .css("--columns", String.valueOf(config().columns))
    .css("--gap", config().gap.toString())
    .css("--min-width", config().minItemWidth.toString());
```

## Runtime Rendering and Manipulation

### Node Referencing and Extraction

Extracting DOM nodes for later manipulation:

```java
private Element titleEl;
private Element contentEl;
private Element statusEl;

@Override
protected INodeProvider buildNode(Element el) {
    return Wrap.$(el).$(root -> {
        Div.$(root).style("header").$(header -> {
            H1.$(header).by("title").text("Initial Title");
            Span.$(header).by("status").style("status").text("Ready");
        });
        
        Div.$(root).by("content").style("content").$(content -> {
            P.$(content).text("Initial content");
        });
        
    }).build(dom -> {
        titleEl = dom.first("title");
        contentEl = dom.first("content");
        statusEl = dom.first("status");
    });
}

public void updateTitle(String newTitle) {
    if (titleEl != null) {
        titleEl.textContent = newTitle;
    }
}

public void updateStatus(String status, String statusClass) {
    if (statusEl != null) {
        statusEl.textContent = status;
        statusEl.className = "status " + statusClass;
    }
}
```

### Runtime DOM Building

Building DOM content at runtime:

```java
public void updateContent(List<DataItem> items) {
    buildInto(contentEl, content -> {
        if (items.isEmpty()) {
            Div.$(content).style("empty-state").$(empty -> {
                Em.$(empty).style(FontAwesome.inbox());
                P.$(empty).text("No items to display");
            });
        } else {
            Div.$(content).style("item-grid").$(grid -> {
                items.forEach(item -> {
                    Card.$(grid)
                        .title(item.getTitle())
                        .clickable(() -> selectItem(item))
                        .$(card -> {
                            P.$(card).text(item.getDescription());
                            if (item.hasActions()) {
                                Div.$(card).style("item-actions").$(actions -> {
                                    item.getActions().forEach(action -> {
                                        Button.$(actions)
                                            .text(action.getLabel())
                                            .onclick(e -> {
                                                e.stopPropagation();
                                                action.execute();
                                            });
                                    });
                                });
                            }
                        });
                });
            });
        }
    });
}
```

### Animated Updates

Patterns for animated DOM updates:

```java
public void updateWithAnimation(String newContent) {
    Element container = contentEl;
    
    // Fade out
    container.style.opacity = "0";
    container.style.transition = "opacity 0.3s ease-in-out";
    
    // Update content after fade out
    TimerSupport.timer(() -> {
        buildInto(container, content -> {
            P.$(content).text(newContent);
        });
        
        // Fade in
        TimerSupport.timer(() -> {
            container.style.opacity = "1";
        }, 50); // Small delay to ensure DOM update
        
    }, 300); // Match transition duration
}

public void slideInNewContent(List<String> items) {
    Div containerEl = (Div) this.contentEl;
    
    // Build new content off-screen
    Element tempContainer = DomGlobal.document.createElement("div");
    tempContainer.style.transform = "translateX(100%)";
    tempContainer.style.transition = "transform 0.3s ease-in-out";
    
    buildInto(tempContainer, temp -> {
        items.forEach(item -> {
            P.$(temp).text(item);
        });
    });
    
    containerEl.appendChild(tempContainer);
    
    // Slide in
    TimerSupport.timer(() -> {
        tempContainer.style.transform = "translateX(0)";
    }, 50);
}
```

### Progressive Content Loading

Patterns for loading content progressively:

```java
public class ProgressiveLoader extends SimpleComponent {
    
    private Element containerEl;
    private List<ContentItem> remainingItems;
    private boolean loading = false;
    
    public ProgressiveLoader(List<ContentItem> items) {
        this.remainingItems = new ArrayList<>(items);
        
        renderer(root -> {
            Div.$(root).by("container").style("progressive-container");
            loadMore();
        }, dom -> {
            containerEl = dom.first("container");
        });
    }
    
    private void loadMore() {
        if (loading || remainingItems.isEmpty()) return;
        
        loading = true;
        showLoadingIndicator();
        
        // Simulate async loading
        TimerSupport.timer(() -> {
            int batchSize = Math.min(5, remainingItems.size());
            List<ContentItem> batch = remainingItems.subList(0, batchSize);
            
            addItems(batch);
            remainingItems = remainingItems.subList(batchSize, remainingItems.size());
            
            hideLoadingIndicator();
            loading = false;
            
            if (!remainingItems.isEmpty()) {
                showLoadMoreButton();
            }
        }, 1000);
    }
    
    private void addItems(List<ContentItem> items) {
        items.forEach(item -> {
            buildInto(containerEl, container -> {
                Div.$(container).style("item").$(itemDiv -> {
                    H3.$(itemDiv).text(item.getTitle());
                    P.$(itemDiv).text(item.getContent());
                });
            }, dom -> {
                // Animate in the new item
                Element newItem = (Element) containerEl.lastChild;
                newItem.style.opacity = "0";
                newItem.style.transform = "translateY(20px)";
                newItem.style.transition = "all 0.3s ease-in-out";
                
                TimerSupport.timer(() -> {
                    newItem.style.opacity = "1";
                    newItem.style.transform = "translateY(0)";
                }, 50);
            });
        });
    }
    
    private void showLoadingIndicator() {
        buildInto(containerEl, container -> {
            Div.$(container).style("loading-indicator").$(loading -> {
                Em.$(loading).style(FontAwesome.spinner(), "fa-spin");
                Span.$(loading).text("Loading more items...");
            });
        });
    }
    
    private void hideLoadingIndicator() {
        NodeList<Node> children = containerEl.childNodes;
        for (int i = 0; i < children.length; i++) {
            Node child = children.item(i);
            if (child instanceof Element && ((Element) child).classList.contains("loading-indicator")) {
                containerEl.removeChild(child);
                break;
            }
        }
    }
    
    private void showLoadMoreButton() {
        buildInto(containerEl, container -> {
            Button.$(container)
                .style("load-more-button")
                .text("Load More")
                .onclick(e -> {
                    containerEl.removeChild(e.getTarget());
                    loadMore();
                });
        });
    }
}
```

## Advanced Component Patterns

### State Management Component

Component that manages internal state and re-renders accordingly:

```java
public class StatefulComponent extends Component<StatefulComponent.Config> {
    
    public enum ViewMode {
        LIST, GRID, TABLE
    }
    
    private ViewMode currentMode = ViewMode.LIST;
    private List<DataItem> items = new ArrayList<>();
    private String searchTerm = "";
    private boolean loading = false;
    
    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$(el).$(root -> {
            // Controls
            Div.$(root).style("controls").$(controls -> {
                renderModeSelector(controls);
                renderSearchBox(controls);
                renderActions(controls);
            });
            
            // Content area
            Div.$(root).style("content").$(content -> {
                if (loading) {
                    renderLoadingState(content);
                } else if (getFilteredItems().isEmpty()) {
                    renderEmptyState(content);
                } else {
                    renderItems(content);
                }
            });
        }).build();
    }
    
    private void renderModeSelector(ContainerBuilder<?> parent) {
        Div.$(parent).style("mode-selector").$(selector -> {
            for (ViewMode mode : ViewMode.values()) {
                Button.$(selector)
                    .style("mode-button")
                    .test(mode == currentMode, btn -> btn.style("active"))
                    .text(mode.name())
                    .onclick(e -> changeMode(mode));
            }
        });
    }
    
    private void renderSearchBox(ContainerBuilder<?> parent) {
        Div.$(parent).style("search-box").$(search -> {
            Input.$(search)
                .type("text")
                .placeholder("Search items...")
                .value(searchTerm)
                .oninput((e, n) -> {
                    searchTerm = ((HTMLInputElement) n).value;
                    rerender(); // Re-render with new search term
                });
            
            if (!searchTerm.isEmpty()) {
                Button.$(search)
                    .style("clear-search")
                    .text("×")
                    .onclick(e -> {
                        searchTerm = "";
                        rerender();
                    });
            }
        });
    }
    
    private void renderItems(ContainerBuilder<?> parent) {
        switch (currentMode) {
            case LIST -> renderListView(parent);
            case GRID -> renderGridView(parent);
            case TABLE -> renderTableView(parent);
        }
    }
    
    private void changeMode(ViewMode newMode) {
        if (currentMode != newMode) {
            currentMode = newMode;
            rerender();
        }
    }
    
    private List<DataItem> getFilteredItems() {
        if (searchTerm.isEmpty()) {
            return items;
        }
        return items.stream()
            .filter(item -> item.getTitle().toLowerCase().contains(searchTerm.toLowerCase()))
            .collect(Collectors.toList());
    }
}
```

### Composite Component Pattern

Component that manages multiple child components:

```java
public class Dashboard extends Component<Dashboard.Config> {
    
    private Map<String, IComponent> widgets = new HashMap<>();
    private RegionPoint mainRegion;
    private RegionPoint sidebarRegion;
    
    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$(el).$(root -> {
            Div.$(root).style("dashboard").$(dashboard -> {
                
                // Header
                Div.$(dashboard).style("dashboard-header").$(header -> {
                    H1.$(header).text("Dashboard");
                    renderHeaderActions(header);
                });
                
                // Main layout
                Div.$(dashboard).style("dashboard-body").$(body -> {
                    // Sidebar
                    Div.$(body).style("dashboard-sidebar")
                        .use(n -> sidebarRegion.setElement((Element) n));
                    
                    // Main content
                    Div.$(body).style("dashboard-main")
                        .use(n -> mainRegion.setElement((Element) n));
                });
            });
        }).build();
    }
    
    @Override
    protected void onAfterRender() {
        mainRegion = findRegionPoint("main");
        sidebarRegion = findRegionPoint("sidebar");
        
        // Add initial widgets
        addWidget("summary", new SummaryWidget());
        addWidget("chart", new ChartWidget());
        addWidget("table", new DataTableWidget());
        
        // Add sidebar components
        addSidebarComponent("filters", new FilterPanel());
        addSidebarComponent("actions", new ActionPanel());
    }
    
    public void addWidget(String id, IComponent widget) {
        widgets.put(id, widget);
        mainRegion.add(widget);
    }
    
    public void removeWidget(String id) {
        IComponent widget = widgets.remove(id);
        if (widget != null) {
            mainRegion.remove(widget);
        }
    }
    
    public void addSidebarComponent(String id, IComponent component) {
        sidebarRegion.add(component);
    }
    
    private void renderHeaderActions(ContainerBuilder<?> parent) {
        Div.$(parent).style("header-actions").$(actions -> {
            Button.$(actions)
                .text("Add Widget")
                .onclick(e -> showAddWidgetDialog());
            
            Button.$(actions)
                .text("Export")
                .onclick(e -> exportDashboard());
            
            Button.$(actions)
                .text("Settings")
                .onclick(e -> showSettings());
        });
    }
}
```

### Custom CSS Integration

Component with multiple style variants:

```java
public class ThemedButton extends Component<ThemedButton.Config> {
    
    public static class Config extends Component.Config {
        
        public interface Style {
            ILocalCSS styles();
            
            static Style create(ILocalCSS styles) {
                return () -> styles;
            }
            
            Style PRIMARY = create(PrimaryCSS.instance());
            Style SECONDARY = create(SecondaryCSS.instance());
            Style SUCCESS = create(SuccessCSS.instance());
            Style WARNING = create(WarningCSS.instance());
            Style DANGER = create(DangerCSS.instance());
        }
        
        private Style style = Style.PRIMARY;
        private String text;
        private String icon;
        private boolean outline = false;
        
        public Config style(Style style) {
            this.style = style;
            return this;
        }
        
        public Config text(String text) {
            this.text = text;
            return this;
        }
        
        public Config icon(String icon) {
            this.icon = icon;
            return this;
        }
        
        public Config outline(boolean outline) {
            this.outline = outline;
            return this;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public ThemedButton build(LayoutData... data) {
            return (ThemedButton) super.build(new ThemedButton(this), data);
        }
    }
    
    @Override
    protected ILocalCSS styles() {
        return config().style.styles();
    }
    
    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$(el).$(root -> {
            Button.$(root)
                .style(styles().button())
                .test(config().outline, btn -> btn.style(styles().outline()))
                .$(button -> {
                    if (config().icon != null) {
                        Em.$(button).style(config().icon);
                    }
                    if (config().text != null) {
                        Span.$(button).text(config().text);
                    }
                });
        }).build();
    }
    
    public static interface ILocalCSS extends IComponentCSS {
        String button();
        String outline();
    }
    
    @CssResource({
        IComponentCSS.COMPONENT_CSS,
        "ThemedButton.css",
        "ThemedButton_Primary.css"
    })
    public static abstract class PrimaryCSS implements ILocalCSS {
        private static PrimaryCSS STYLES;
        public static ILocalCSS instance() {
            if (STYLES == null) {
                STYLES = (PrimaryCSS) GWT.create(PrimaryCSS.class);
                STYLES.ensureInjected();
            }
            return STYLES;
        }
    }
    
    // Similar implementations for other style variants...
}
```

## Layout and Composition Patterns

### Flexible Layout System

Component that provides flexible layout capabilities:

```java
public class FlexLayout extends Component<FlexLayout.Config> {
    
    public static class Config extends Component.Config {
        
        public enum Direction {
            ROW, COLUMN, ROW_REVERSE, COLUMN_REVERSE
        }
        
        public enum Justify {
            START, END, CENTER, SPACE_BETWEEN, SPACE_AROUND, SPACE_EVENLY
        }
        
        public enum Align {
            START, END, CENTER, STRETCH, BASELINE
        }
        
        private Direction direction = Direction.ROW;
        private Justify justify = Justify.START;
        private Align align = Align.START;
        private Length gap;
        private boolean wrap = false;
        
        public Config direction(Direction direction) {
            this.direction = direction;
            return this;
        }
        
        public Config justify(Justify justify) {
            this.justify = justify;
            return this;
        }
        
        public Config align(Align align) {
            this.align = align;
            return this;
        }
        
        public Config gap(Length gap) {
            this.gap = gap;
            return this;
        }
        
        public Config wrap(boolean wrap) {
            this.wrap = wrap;
            return this;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public FlexLayout build(LayoutData... data) {
            return (FlexLayout) super.build(new FlexLayout(this), data);
        }
    }
    
    private RegionPoint contentRegion;
    
    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$(el).$(root -> {
            Div.$(root)
                .style("flex-layout")
                .css(CSS.DISPLAY, "flex")
                .css(CSS.FLEX_DIRECTION, getDirectionValue())
                .css("justify-content", getJustifyValue())
                .css("align-items", getAlignValue())
                .test(config().wrap, div -> div.css(CSS.FLEX_WRAP, "wrap"))
                .test(config().gap != null, div -> div.css("gap", config().gap.toString()))
                .use(n -> contentRegion.setElement((Element) n));
        }).build();
    }
    
    @Override
    protected void onAfterRender() {
        contentRegion = findRegionPoint("content");
    }
    
    public void add(IComponent component) {
        contentRegion.add(component);
    }
    
    public void add(IComponent component, FlexItemData layoutData) {
        contentRegion.add(component, layoutData);
    }
    
    private String getDirectionValue() {
        return switch (config().direction) {
            case ROW -> "row";
            case COLUMN -> "column";
            case ROW_REVERSE -> "row-reverse";
            case COLUMN_REVERSE -> "column-reverse";
        };
    }
    
    private String getJustifyValue() {
        return switch (config().justify) {
            case START -> "flex-start";
            case END -> "flex-end";
            case CENTER -> "center";
            case SPACE_BETWEEN -> "space-between";
            case SPACE_AROUND -> "space-around";
            case SPACE_EVENLY -> "space-evenly";
        };
    }
    
    private String getAlignValue() {
        return switch (config().align) {
            case START -> "flex-start";
            case END -> "flex-end";
            case CENTER -> "center";
            case STRETCH -> "stretch";
            case BASELINE -> "baseline";
        };
    }
    
    public static class FlexItemData extends LayoutData {
        private Integer flex;
        private Integer order;
        private String alignSelf;
        
        public FlexItemData flex(int flex) {
            this.flex = flex;
            return this;
        }
        
        public FlexItemData order(int order) {
            this.order = order;
            return this;
        }
        
        public FlexItemData alignSelf(String alignSelf) {
            this.alignSelf = alignSelf;
            return this;
        }
        
        @Override
        public void apply(Element element) {
            if (flex != null) {
                element.style.setProperty("flex", String.valueOf(flex));
            }
            if (order != null) {
                element.style.setProperty("order", String.valueOf(order));
            }
            if (alignSelf != null) {
                element.style.setProperty("align-self", alignSelf);
            }
        }
    }
}
```

### Responsive Grid Layout

Grid layout component with responsive capabilities:

```java
public class ResponsiveGrid extends Component<ResponsiveGrid.Config> {
    
    public static class Config extends Component.Config {
        private int columns = 1;
        private int smColumns = 2;
        private int mdColumns = 3;
        private int lgColumns = 4;
        private Length gap = Length.em(1);
        private Length minItemWidth = Length.px(200);
        
        public Config columns(int columns) {
            this.columns = columns;
            return this;
        }
        
        public Config responsive(int sm, int md, int lg) {
            this.smColumns = sm;
            this.mdColumns = md;
            this.lgColumns = lg;
            return this;
        }
        
        public Config gap(Length gap) {
            this.gap = gap;
            return this;
        }
        
        public Config minItemWidth(Length minItemWidth) {
            this.minItemWidth = minItemWidth;
            return this;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public ResponsiveGrid build(LayoutData... data) {
            return (ResponsiveGrid) super.build(new ResponsiveGrid(this), data);
        }
    }
    
    private RegionPoint itemsRegion;
    
    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$(el).$(root -> {
            Div.$(root)
                .style("responsive-grid")
                .css(CSS.DISPLAY, "grid")
                .css("gap", config().gap.toString())
                .css("grid-template-columns", buildGridTemplate())
                .use(n -> itemsRegion.setElement((Element) n));
        }).build();
    }
    
    @Override
    protected void onAfterRender() {
        itemsRegion = findRegionPoint("items");
        setupResponsiveCSS();
    }
    
    private String buildGridTemplate() {
        // Auto-fit with minimum width
        return String.format("repeat(auto-fit, minmax(%s, 1fr))", config().minItemWidth.toString());
    }
    
    private void setupResponsiveCSS() {
        // Inject responsive CSS for fixed column layouts
        String css = String.format("""
            @media (min-width: 576px) {
                .responsive-grid {
                    grid-template-columns: repeat(%d, 1fr);
                }
            }
            @media (min-width: 768px) {
                .responsive-grid {
                    grid-template-columns: repeat(%d, 1fr);
                }
            }
            @media (min-width: 992px) {
                .responsive-grid {
                    grid-template-columns: repeat(%d, 1fr);
                }
            }
            """, config().smColumns, config().mdColumns, config().lgColumns);
        
        // Inject the CSS (you would typically use CSSInjector for this)
        injectCSS(css);
    }
    
    public void add(IComponent component) {
        itemsRegion.add(component);
    }
    
    public void add(IComponent component, GridItemData layoutData) {
        itemsRegion.add(component, layoutData);
    }
    
    public static class GridItemData extends LayoutData {
        private Integer columnSpan;
        private Integer rowSpan;
        private Integer columnStart;
        private Integer rowStart;
        
        public GridItemData columnSpan(int span) {
            this.columnSpan = span;
            return this;
        }
        
        public GridItemData rowSpan(int span) {
            this.rowSpan = span;
            return this;
        }
        
        public GridItemData start(int column, int row) {
            this.columnStart = column;
            this.rowStart = row;
            return this;
        }
        
        @Override
        public void apply(Element element) {
            if (columnSpan != null) {
                element.style.setProperty("grid-column", "span " + columnSpan);
            }
            if (rowSpan != null) {
                element.style.setProperty("grid-row", "span " + rowSpan);
            }
            if (columnStart != null) {
                element.style.setProperty("grid-column-start", String.valueOf(columnStart));
            }
            if (rowStart != null) {
                element.style.setProperty("grid-row-start", String.valueOf(rowStart));
            }
        }
    }
}
```

## Best Practices and Tricks

### Performance Optimization

Techniques for optimizing rendering performance:

```java
public class OptimizedList extends Component<OptimizedList.Config> {
    
    private static final int VISIBLE_BUFFER = 5;
    private List<DataItem> allItems = new ArrayList<>();
    private int visibleStart = 0;
    private int visibleCount = 20;
    private Element containerEl;
    private Map<String, Element> itemElements = new HashMap<>();
    
    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$(el).$(root -> {
            Div.$(root).style("optimized-list").$(list -> {
                Div.$(list).by("container").style("list-container")
                    .css("height", "400px")
                    .css("overflow-y", "auto")
                    .onscroll(this::handleScroll);
            });
        }).build(dom -> {
            containerEl = dom.first("container");
        });
    }
    
    private void handleScroll(UIEvent e) {
        Element scrollElement = (Element) e.getTarget();
        double scrollTop = scrollElement.scrollTop;
        double itemHeight = 50; // Assuming fixed item height
        
        int newVisibleStart = (int) Math.floor(scrollTop / itemHeight) - VISIBLE_BUFFER;
        newVisibleStart = Math.max(0, newVisibleStart);
        
        if (newVisibleStart != visibleStart) {
            visibleStart = newVisibleStart;
            renderVisibleItems();
        }
    }
    
    private void renderVisibleItems() {
        // Clear existing items
        itemElements.clear();
        DomSupport.removeAllChildren(containerEl);
        
        // Render spacer for items above visible area
        if (visibleStart > 0) {
            Div spacerTop = (Div) DomGlobal.document.createElement("div");
            spacerTop.style.height = (visibleStart * 50) + "px";
            containerEl.appendChild(spacerTop);
        }
        
        // Render visible items
        int endIndex = Math.min(visibleStart + visibleCount + VISIBLE_BUFFER, allItems.size());
        for (int i = visibleStart; i < endIndex; i++) {
            DataItem item = allItems.get(i);
            renderItem(item, i);
        }
        
        // Render spacer for items below visible area
        int remainingItems = allItems.size() - endIndex;
        if (remainingItems > 0) {
            Div spacerBottom = (Div) DomGlobal.document.createElement("div");
            spacerBottom.style.height = (remainingItems * 50) + "px";
            containerEl.appendChild(spacerBottom);
        }
    }
    
    private void renderItem(DataItem item, int index) {
        buildInto(containerEl, container -> {
            Div.$(container).style("list-item").css("height", "50px").$(itemDiv -> {
                H4.$(itemDiv).text(item.getTitle());
                P.$(itemDiv).text(item.getDescription());
            });
        }, dom -> {
            Element itemEl = (Element) containerEl.lastChild;
            itemElements.put(item.getId(), itemEl);
        });
    }
}
```

### Memory Management

Patterns for proper cleanup and memory management:

```java
public class MemoryAwareComponent extends Component<MemoryAwareComponent.Config> {
    
    private Timer refreshTimer;
    private List<EventHandler> eventHandlers = new ArrayList<>();
    private Map<String, IComponent> childComponents = new HashMap<>();
    
    @Override
    protected void onAfterRender() {
        // Setup automatic refresh
        refreshTimer = TimerSupport.timer(this::refresh, 30000, true);
        
        // Register external event listeners
        EventHandler windowResizeHandler = DOM.addEventListener(
            DomGlobal.window, 
            "resize", 
            this::handleWindowResize
        );
        eventHandlers.add(windowResizeHandler);
    }
    
    @Override
    protected void onDispose() {
        // Clean up timer
        if (refreshTimer != null) {
            refreshTimer.cancel();
            refreshTimer = null;
        }
        
        // Clean up event handlers
        eventHandlers.forEach(EventHandler::remove);
        eventHandlers.clear();
        
        // Dispose child components
        childComponents.values().forEach(IComponent::dispose);
        childComponents.clear();
        
        super.onDispose();
    }
    
    private void refresh() {
        // Refresh logic that doesn't leak memory
        if (isDisposed()) {
            return; // Safety check
        }
        
        // Update component state
        updateContent();
    }
    
    private void handleWindowResize(Event e) {
        if (isDisposed()) {
            return; // Safety check
        }
        
        // Handle resize logic
        recalculateLayout();
    }
    
    public void addChildComponent(String id, IComponent component) {
        // Remove existing component with same ID
        IComponent existing = childComponents.remove(id);
        if (existing != null) {
            existing.dispose();
        }
        
        childComponents.put(id, component);
        // Add to layout...
    }
}
```

### Accessibility Patterns

Implementing accessibility features in components:

```java
public class AccessibleDialog extends Component<AccessibleDialog.Config> {
    
    private Element dialogEl;
    private Element titleEl;
    private Element closeButtonEl;
    private Element previousFocusEl;
    private boolean open = false;
    
    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$(el).$(root -> {
            Div.$(root)
                .by("dialog")
                .role("dialog")
                .ariaModal("true")
                .ariaLabelledby("dialog-title")
                .tabindex(-1)
                .style("dialog")
                .onkeydown(this::handleKeyDown)
                .$(dialog -> {
                    
                    // Dialog header
                    Div.$(dialog).style("dialog-header").$(header -> {
                        H2.$(header)
                            .by("title")
                            .id("dialog-title")
                            .text(config().title);
                        
                        Button.$(header)
                            .by("closeButton")
                            .ariaLabel("Close dialog")
                            .text("×")
                            .onclick(this::close);
                    });
                    
                    // Dialog content
                    Div.$(dialog)
                        .style("dialog-content")
                        .use(n -> findRegionPoint("content").setElement((Element) n));
                });
        }).build(dom -> {
            dialogEl = dom.first("dialog");
            titleEl = dom.first("title");
            closeButtonEl = dom.first("closeButton");
        });
    }
    
    public void open() {
        if (open) return;
        
        // Store current focus
        previousFocusEl = (Element) DomGlobal.document.activeElement;
        
        // Show dialog
        dialogEl.style.display = "block";
        open = true;
        
        // Focus management
        TimerSupport.timer(() -> {
            dialogEl.focus();
            manageFocusTrap();
        }, 50);
        
        // Announce to screen readers
        dialogEl.setAttribute("aria-live", "polite");
        dialogEl.setAttribute("aria-atomic", "true");
    }
    
    public void close() {
        if (!open) return;
        
        dialogEl.style.display = "none";
        open = false;
        
        // Restore focus
        if (previousFocusEl != null) {
            previousFocusEl.focus();
        }
        
        removeFocusTrap();
    }
    
    private void handleKeyDown(UIEvent e) {
        if (e.getKeyCode() == 27) { // Escape key
            close();
        } else if (e.getKeyCode() == 9) { // Tab key
            handleTabNavigation(e);
        }
    }
    
    private void manageFocusTrap() {
        // Get all focusable elements within the dialog
        NodeList<Element> focusableElements = dialogEl.querySelectorAll(
            "button, [href], input, select, textarea, [tabindex]:not([tabindex=\"-1\"])"
        );
        
        if (focusableElements.length > 0) {
            Element firstElement = focusableElements.item(0);
            Element lastElement = focusableElements.item(focusableElements.length - 1);
            
            // Focus first element initially
            firstElement.focus();
            
            // Set up tab trapping
            firstElement.addEventListener("keydown", e -> {
                if (e.keyCode == 9 && e.shiftKey) {
                    e.preventDefault();
                    lastElement.focus();
                }
            });
            
            lastElement.addEventListener("keydown", e -> {
                if (e.keyCode == 9 && !e.shiftKey) {
                    e.preventDefault();
                    firstElement.focus();
                }
            });
        }
    }
    
    private void removeFocusTrap() {
        // Remove event listeners added for focus trapping
        // Implementation depends on how you track the listeners
    }
}
```

### Testing-Friendly Patterns

Patterns that make components easier to test:

```java
public class TestableComponent extends Component<TestableComponent.Config> {
    
    public static class Config extends Component.Config {
        public Config testId(String testId) {
            return (Config) super.testId(testId);
        }
    }
    
    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$(el).$(root -> {
            Div.$(root)
                .testId("main-container") // Explicit test ID
                .style("testable-component")
                .$(container -> {
                    
                    Button.$(container)
                        .testId("action-button")
                        .text("Perform Action")
                        .onclick(this::performAction);
                    
                    Div.$(container)
                        .testId("status-display")
                        .style("status")
                        .text(getStatusText());
                    
                    Div.$(container)
                        .testId("content-area")
                        .style("content")
                        .$(content -> {
                            renderContent(content);
                        });
                });
        }).build();
    }
    
    // Public methods for testing
    public void performAction() {
        // Action logic here
        updateStatus("Action performed");
        fireEvent(new ActionPerformedEvent());
    }
    
    public String getStatus() {
        return getCurrentStatus();
    }
    
    public boolean isActionEnabled() {
        return actionEnabled;
    }
    
    // Helper methods that can be tested independently
    protected String getStatusText() {
        return switch (getCurrentStatus()) {
            case READY -> "Ready";
            case PROCESSING -> "Processing...";
            case COMPLETE -> "Complete";
            case ERROR -> "Error occurred";
        };
    }
    
    protected void updateStatus(String message) {
        // Update internal state
        setCurrentStatus(Status.COMPLETE);
        
        // Update DOM if rendered
        if (isRendered()) {
            Element statusEl = getElement().querySelector("[data-testid='status-display']");
            if (statusEl != null) {
                statusEl.textContent = message;
            }
        }
    }
}
```

## Real-World Examples

### Data Table Component

Complete implementation of a data table with sorting, filtering, and pagination:

```java
public class DataTable<T> extends Component<DataTable.Config<T>> {
    
    public static class Config<T> extends Component.Config {
        private List<Column<T>> columns = new ArrayList<>();
        private List<T> data = new ArrayList<>();
        private boolean sortable = true;
        private boolean filterable = true;
        private int pageSize = 10;
        private Function<T, String> rowKeyProvider = Object::toString;
        
        public Config<T> column(String header, Function<T, String> valueProvider) {
            columns.add(new Column<>(header, valueProvider));
            return this;
        }
        
        public Config<T> column(String header, Function<T, String> valueProvider, 
                               Function<T, IFragment> rendererProvider) {
            columns.add(new Column<>(header, valueProvider, rendererProvider));
            return this;
        }
        
        public Config<T> data(List<T> data) {
            this.data = new ArrayList<>(data);
            return this;
        }
        
        public Config<T> pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public DataTable<T> build(LayoutData... data) {
            return (DataTable<T>) super.build(new DataTable<>(this), data);
        }
    }
    
    public static class Column<T> {
        private String header;
        private Function<T, String> valueProvider;
        private Function<T, IFragment> rendererProvider;
        private boolean sortable = true;
        
        public Column(String header, Function<T, String> valueProvider) {
            this.header = header;
            this.valueProvider = valueProvider;
        }
        
        public Column(String header, Function<T, String> valueProvider, 
                     Function<T, IFragment> rendererProvider) {
            this.header = header;
            this.valueProvider = valueProvider;
            this.rendererProvider = rendererProvider;
        }
    }
    
    private List<T> filteredData;
    private String sortColumn;
    private boolean sortAscending = true;
    private String filterText = "";
    private int currentPage = 0;
    private Element tableBodyEl;
    private Element paginationEl;
    
    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$(el).$(root -> {
            Div.$(root).style("data-table").$(table -> {
                
                // Filters
                if (config().filterable) {
                    renderFilters(table);
                }
                
                // Table
                Table.$(table).style("table").$(tableEl -> {
                    renderTableHeader(tableEl);
                    Tbody.$(tableEl).by("tableBody");
                });
                
                // Pagination
                Div.$(table).by("pagination").style("pagination");
            });
        }).build(dom -> {
            tableBodyEl = dom.first("tableBody");
            paginationEl = dom.first("pagination");
            initializeData();
        });
    }
    
    private void renderFilters(ContainerBuilder<?> parent) {
        Div.$(parent).style("table-filters").$(filters -> {
            Input.$(filters)
                .type("text")
                .placeholder("Filter...")
                .value(filterText)
                .oninput((e, n) -> {
                    filterText = ((HTMLInputElement) n).value;
                    applyFilters();
                });
        });
    }
    
    private void renderTableHeader(ContainerBuilder<?> parent) {
        Thead.$(parent).$(head -> {
            Tr.$(head).$(row -> {
                config().columns.forEach(column -> {
                    Th.$(row)
                        .style("table-header")
                        .test(column.sortable && config().sortable, 
                              th -> th.style("sortable").onclick(e -> sortBy(column.header)))
                        .$(th -> {
                            Span.$(th).text(column.header);
                            if (column.header.equals(sortColumn)) {
                                Em.$(th).style(sortAscending ? 
                                    FontAwesome.sortUp() : FontAwesome.sortDown());
                            }
                        });
                });
            });
        });
    }
    
    private void initializeData() {
        filteredData = new ArrayList<>(config().data);
        applyFilters();
    }
    
    private void applyFilters() {
        // Filter data
        if (filterText.isEmpty()) {
            filteredData = new ArrayList<>(config().data);
        } else {
            filteredData = config().data.stream()
                .filter(item -> config().columns.stream()
                    .anyMatch(col -> col.valueProvider.apply(item)
                        .toLowerCase().contains(filterText.toLowerCase())))
                .collect(Collectors.toList());
        }
        
        // Apply sorting
        if (sortColumn != null) {
            applySorting();
        }
        
        // Reset to first page
        currentPage = 0;
        
        // Re-render table content
        renderTableBody();
        renderPagination();
    }
    
    private void sortBy(String columnHeader) {
        if (columnHeader.equals(sortColumn)) {
            sortAscending = !sortAscending;
        } else {
            sortColumn = columnHeader;
            sortAscending = true;
        }
        
        applySorting();
        renderTableBody();
        renderPagination();
        
        // Re-render header to update sort indicators
        rerender();
    }
    
    private void applySorting() {
        Column<T> sortCol = config().columns.stream()
            .filter(col -> col.header.equals(sortColumn))
            .findFirst()
            .orElse(null);
        
        if (sortCol != null) {
            Comparator<T> comparator = Comparator.comparing(sortCol.valueProvider);
            if (!sortAscending) {
                comparator = comparator.reversed();
            }
            filteredData.sort(comparator);
        }
    }
    
    private void renderTableBody() {
        buildInto(tableBodyEl, tbody -> {
            int startIndex = currentPage * config().pageSize;
            int endIndex = Math.min(startIndex + config().pageSize, filteredData.size());
            
            for (int i = startIndex; i < endIndex; i++) {
                T item = filteredData.get(i);
                Tr.$(tbody).style("table-row").$(row -> {
                    config().columns.forEach(column -> {
                        Td.$(row).style("table-cell").$(cell -> {
                            if (column.rendererProvider != null) {
                                cell.insert(column.rendererProvider.apply(item));
                            } else {
                                cell.text(column.valueProvider.apply(item));
                            }
                        });
                    });
                });
            }
            
            if (filteredData.isEmpty()) {
                Tr.$(tbody).$(row -> {
                    Td.$(row)
                        .colspan(config().columns.size())
                        .style("empty-message")
                        .text("No data available");
                });
            }
        });
    }
    
    private void renderPagination() {
        buildInto(paginationEl, pagination -> {
            int totalPages = (int) Math.ceil((double) filteredData.size() / config().pageSize);
            
            if (totalPages > 1) {
                // Previous button
                Button.$(pagination)
                    .text("Previous")
                    .disabled(currentPage == 0)
                    .onclick(e -> {
                        if (currentPage > 0) {
                            currentPage--;
                            renderTableBody();
                            renderPagination();
                        }
                    });
                
                // Page numbers
                for (int i = 0; i < totalPages; i++) {
                    final int pageNum = i;
                    Button.$(pagination)
                        .text(String.valueOf(i + 1))
                        .test(i == currentPage, btn -> btn.style("active"))
                        .onclick(e -> {
                            currentPage = pageNum;
                            renderTableBody();
                            renderPagination();
                        });
                }
                
                // Next button
                Button.$(pagination)
                    .text("Next")
                    .disabled(currentPage >= totalPages - 1)
                    .onclick(e -> {
                        if (currentPage < totalPages - 1) {
                            currentPage++;
                            renderTableBody();
                            renderPagination();
                        }
                    });
            }
            
            // Info text
            int startIndex = currentPage * config().pageSize + 1;
            int endIndex = Math.min((currentPage + 1) * config().pageSize, filteredData.size());
            
            Span.$(pagination)
                .style("pagination-info")
                .text(String.format("Showing %d-%d of %d items", 
                     startIndex, endIndex, filteredData.size()));
        });
    }
    
    public void updateData(List<T> newData) {
        config().data = new ArrayList<>(newData);
        applyFilters();
    }
    
    public List<T> getSelectedItems() {
        // Implementation for row selection would go here
        return new ArrayList<>();
    }
}
```

### Dashboard Widget System

A complete dashboard widget system with drag-and-drop layout:

```java
public class DashboardWidget extends Component<DashboardWidget.Config> {
    
    public static class Config extends Component.Config {
        private String title;
        private String subtitle;
        private IComponent content;
        private boolean removable = true;
        private boolean resizable = true;
        private List<WidgetAction> actions = new ArrayList<>();
        
        public Config title(String title) {
            this.title = title;
            return this;
        }
        
        public Config subtitle(String subtitle) {
            this.subtitle = subtitle;
            return this;
        }
        
        public Config content(IComponent content) {
            this.content = content;
            return this;
        }
        
        public Config action(String label, Runnable handler) {
            actions.add(new WidgetAction(label, handler));
            return this;
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public DashboardWidget build(LayoutData... data) {
            return (DashboardWidget) super.build(new DashboardWidget(this), data);
        }
    }
    
    public static class WidgetAction {
        private String label;
        private Runnable handler;
        
        public WidgetAction(String label, Runnable handler) {
            this.label = label;
            this.handler = handler;
        }
        
        // getters...
    }
    
    private boolean expanded = true;
    private boolean loading = false;
    private Element contentEl;
    
    @Override
    protected INodeProvider buildNode(Element el) {
        return Wrap.$(el).$(root -> {
            Div.$(root).style("dashboard-widget").$(widget -> {
                
                // Widget header
                Div.$(widget).style("widget-header").$(header -> {
                    Div.$(header).style("widget-title-area").$(titleArea -> {
                        if (config().title != null) {
                            H3.$(titleArea).style("widget-title").text(config().title);
                        }
                        if (config().subtitle != null) {
                            P.$(titleArea).style("widget-subtitle").text(config().subtitle);
                        }
                    });
                    
                    // Widget actions
                    Div.$(header).style("widget-actions").$(actions -> {
                        // Custom actions
                        config().actions.forEach(action -> {
                            Button.$(actions)
                                .style("widget-action-btn")
                                .text(action.getLabel())
                                .onclick(e -> action.getHandler().run());
                        });
                        
                        // Collapse/expand
                        Button.$(actions)
                            .style("widget-action-btn")
                            .title(expanded ? "Collapse" : "Expand")
                            .onclick(this::toggleExpanded)
                            .$(btn -> {
                                Em.$(btn).style(expanded ? 
                                    FontAwesome.chevronUp() : FontAwesome.chevronDown());
                            });
                        
                        // Remove widget
                        if (config().removable) {
                            Button.$(actions)
                                .style("widget-action-btn", "remove-btn")
                                .title("Remove widget")
                                .onclick(this::removeWidget)
                                .$(btn -> {
                                    Em.$(btn).style(FontAwesome.times());
                                });
                        }
                    });
                });
                
                // Widget content
                Div.$(widget)
                    .by("content")
                    .style("widget-content")
                    .test(!expanded, div -> div.style("collapsed"))
                    .$(content -> {
                        if (config().content != null) {
                            content.insert(config().content);
                        }
                    });
            });
        }).build(dom -> {
            contentEl = dom.first("content");
        });
    }
    
    private void toggleExpanded(UIEvent e) {
        expanded = !expanded;
        
        Element contentElement = contentEl;
        Element buttonElement = (Element) e.getTarget().closest(".widget-action-btn");
        Element iconElement = buttonElement.querySelector("em");
        
        if (expanded) {
            contentElement.classList.remove("collapsed");
            iconElement.className = FontAwesome.chevronUp();
            buttonElement.title = "Collapse";
        } else {
            contentElement.classList.add("collapsed");
            iconElement.className = FontAwesome.chevronDown();
            buttonElement.title = "Expand";
        }
    }
    
    private void removeWidget(UIEvent e) {
        // Show confirmation dialog
        showConfirmDialog("Remove Widget", 
            "Are you sure you want to remove this widget?",
            () -> {
                // Animate removal
                Element widgetEl = getRootEl();
                widgetEl.style.opacity = "0";
                widgetEl.style.transform = "scale(0.8)";
                widgetEl.style.transition = "all 0.3s ease-in-out";
                
                TimerSupport.timer(() -> {
                    fireEvent(new WidgetRemovedEvent(this));
                    dispose();
                }, 300);
            });
    }
    
    public void setLoading(boolean loading) {
        this.loading = loading;
        
        if (contentEl != null) {
            if (loading) {
                contentEl.classList.add("loading");
                // Add loading spinner
                buildInto(contentEl, content -> {
                    Div.$(content).style("loading-overlay").$(overlay -> {
                        Em.$(overlay).style(FontAwesome.spinner(), "fa-spin");
                        Span.$(overlay).text("Loading...");
                    });
                });
            } else {
                contentEl.classList.remove("loading");
                // Remove loading overlay
                Element overlay = contentEl.querySelector(".loading-overlay");
                if (overlay != null) {
                    contentEl.removeChild(overlay);
                }
            }
        }
    }
    
    public void updateContent(IComponent newContent) {
        if (contentEl != null) {
            buildInto(contentEl, content -> {
                content.insert(newContent);
            });
        }
    }
    
    public void refresh() {
        setLoading(true);
        
        // Simulate async refresh
        TimerSupport.timer(() -> {
            // Refresh logic here
            setLoading(false);
            fireEvent(new WidgetRefreshedEvent(this));
        }, 2000);
    }
}
```

This comprehensive guide provides extensive examples and patterns for implementing effective rendering in JUI applications. These patterns cover everything from simple components to complex, feature-rich widgets that can be used as building blocks for sophisticated user interfaces.