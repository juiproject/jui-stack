# Interoperability

Interoperability refers (in this context) to the two-way interaction between JUI and JS. Such interaction allows for JS code to call JUI code directly (i.e. embedding JUI into an existing JS application) as well as incorporating existing JS (including libraruies) into JUI code. The mechanism used to achieve this is JsInterop.

## JsInterop

[JsInterop](https://docs.google.com/document/d/10fmlEYIHcyead_4R1S5wKGs1t2I7Fnp_PaNaa7XTEk0/edit#) is a mechanism that allows one to expose Java classes to JS and gain access to JS classes in Java. This is supported natively in J2CL and became one of the core features of GWT 2.8.

The mechanism relies on annotations to guide the compiler to correctly treat those classes that are being shared ([GWT JsInterop](https://www.gwtproject.org/doc/latest/DevGuideCodingBasicsJsInterop.html) provides a brief but informative discussion). In the following we provide some explicit guidance and supporting examples for its use in JUI. Concrete examples are available **Playground** > **Samples** > **Interoperability**.

One specific note is that when using the GWT compiler or code server you need to explicitly direct it to expose classes for export. This can be achieved for the code server by adding the option `-generateJsInteropExports` to the argument list of the code server (i.e. if using eclipse then adding this to the *program arguments* of the run configuration). As for compilation with the Maven plugin you need to add the configuration option `<generateJsInteropExports>true</generateJsInteropExports>`.

## JUI calling JS

Here we create a shadow version of the JS class using the `@JsType` annotation but with the option `isNative = true`. From our tutorial example:

```java
@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class Interop {
  public native void button1();
  ...
}
```

and corresponding JS:

```javascript
Interop = function () {}

Interop.prototype.button1 = function() {
  window.alert ("Button 1 pressed");
}
```

from which you can make use of in JUI:

```java
Logger.log (new Interop ().button1 ());
```

This, of course, applies to JS from third party libraries (see `JQueryElement` for an example that shadows JQuery). Since this is quite a common approach (take a look at `elemental2.dom.Element`) there are plenty of examples available.

## JS calling JUI

It is fairly straight forward to expose a class and that is to annotate it with `@JsType`:

```java
package mypackage;

@JsType
public class MyClass {
  public MyClass() {}
  
  public String message () {
    return "This is a message";
  }
}
```

This can then be access from JS as the following illustrates (note that the class is only available after the JUI application has loaded):

```html
<script>
  function onApplicationLoad() {
    var obj = new mypackage.MyClass ();
    alert (obj.message ());
  }
</script>
```

The aforementioned tutorial illustrates this process with the `button2` and `button3` examples.

## Remoting

*It is expected that you are familiar with [Remoting](ess_remoting.md) before reading this section.*

This could be considered a special case of calling JUI from JS (with call-backs). In this case you can expose JUI remoting (being a command-pattern based RPC rather than a RESTful API) to JS by mapping DTO classes and exposing a suitably configured service handler. A representative example (without actually remoting) is given in the playground using `InteropExported` (invoked via `Interop.button3()`).

There are some considerations however:

1. You will need to appropriately annotate the DTO classes with `@JsMethod`. This is needed (rather than `@JsType`) since the serialiser extends the DTO classes for construction, so these will allow for exposing the sub-classes from the serialiser.
2. The annotations apply to both the result DTOs as well as the lookups and commands.
3. You will need to create a special API class (much like `InteropExported` referenced above in the playground) that exposes query and execute methods. You could consider create speciality methods for common queries to simplify lookup and command creation.
4. Use call-backs (much like `InteropExported` referenced above in the playground) to return the results. You could craft these so that error conditions are also returned.

Having said the above, in practice exposing remoting this way is both effective and straightforward.

## Other frameworks

It is possible to incorporate other frameworks into a JUI application, though the detailed process is quite dependent on the choice of framework. By way of a fairly simple example we show (in the tutorial referenced above and present in the `Interoperability` class) the creation of a [VueJS](https://vuejs.org/) component wrapped in a modal (the example replicates [Spring Boot Vue](https://github.com/eugenp/tutorials/tree/master/spring-boot-modules/spring-boot-vue)):

```java
ModalDialogCreator.create ((Supplier<Component<Component.Config>>) () -> {
    return ComponentCreator.$ (root -> {
        root.id ("interop_id");
        Ul.$ (root).$ (ul -> {
            Li.$ (ul).attr ("v-for", "player in players").$ (li -> {
                Custom.$ (li, "player-card")
                    .attr ("v-bind:player", "player")
                    .attr ("v-bind:key", "player.id");
            });
        });
    }, dom -> {
        // Need a bit of delay to ensure that we are in the DOM.
        TimerSupport.defer (() -> new Interop ().attach ("interop_id"));
    });
}, cfg -> {
    cfg.title ("VueJS dialog");
    cfg.width (Length.px (500));
    cfg.action (a -> a.label ("Close").handler (h -> h.success ()));
    cfg.removeOnClose ();
}).open ();
```

and

```java
@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class Interop {
  ...
  public native void attach(String elementId);
}
```

with the following JS (either injected or referenced in the entry HTML file):

```javascript
Interop = function () {}
...
Interop.prototype.attach = function(elementId) {
  new Vue({
    el: '#' + elementId,
    data: {
      players: [
        { id: "1", 
          name: "Lionel Messi", 
          description: "Argentina's superstar" },
        { id: "2", 
          name: "Christiano Ronaldo", 
          description: "World #1-ranked player from Portugal" }
      ]
    }
  });
}

Vue.component('player-card', {
  props: ['player'],
  template: `<div class="card">
      <div class="card-body">
          <h6 class="card-title">
              {{ player.name }}
          </h6>
          <p class="card-text">
            <div>
              {{ player.description }}
            </div>
          </p>
        </div>
      </div>`
});
```

The modal is configured for single instance (so is removed when closed) so can be invoked by a button (or the like). The DOM of the containing component includes an ID `intero_id` which is passed through to `Interop.attach(String)` which create a new VueJS application whose root is that element. At this point VueJS takes over and translates the contents to DOM (in this case the DOM acts as a template with the custom element `player-card` being mapped to the `player-card` VueJS component declared in the JavaScript.

## Post-load

In order to make use of classes exported by JUI you need to wait until the JUI application has loaded. One approach is to signal loading from the application entry point by an invocation of a JS function.  Now you could expose a JS class to JUI allowing it to create an instance of it and invoke a method, another approach is to expose a single global function and invoke it directly. For the latter one may make use of a pure GWT feature which is the JavaScript Native Interface (JSNI):

```java
public class MyApplication extends EntryPoint {

  public void onModuleLoad() {
    ...
    loadApplication ();
  }
  
  private native void loadApplication()
  /*-{
    if ($wnd.onApplicationLoad && typeof $wnd.onApplicationLoad == 'function')
      $wnd.onApplicationLoad();
  }-*/;
}
```

Placing the following in the HTML entry page will result in `onApplicationLoad()` being invoked (here you can perform whatever operations you need, even just setting an initialised flag for the rest of the JS application to know that the exported classes are now available):

```html
...
<script>
  function onApplicationLoad() {
    ...
  }
</script>
...
```

Note that JSNI is not part of the JsInterop specification and is purely a GWT capability offered by the GWT JS compiler. If you want to avoid that dependency the consider the first suggestion.


