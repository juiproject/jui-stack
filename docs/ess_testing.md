# Testing

*Examples of the below are provided in the* **playground** *and, where relevant, references are provided.*

## Test mode

There are two scenarios underwhich JUI code can be tested: (1) using a simple unit test and, (2)  running in a browser (or similar).  The former occurs entirely in the JVM (so does not require compilation and will generally not have access to a DOM model or browser emulation) while the second operates in a JavaScript environment (and relies on access to DOM). The latter is what you would expect when running integration tests through the UI and is the topic of this documentation.

From a JUI standpoint we can distinguish among these cases through the `isUnitTestMode()` and `isTestMode()` static methods on `Debug`. The former return `true` if the code is running in a simple unit test (in a JVM). The latter returns `true` only when an appropriately configured META tag is present. This tag will usually be placed into the entry point HTML:

```html
<meta name="jui:test" content="true" />
```

This mode will ensure the varous test related attributes are applied to DOM nodes that can be used for inspection (in the following we will say the application is running in *test mode*).

## Test attributes

When running in test mode components will automatically apply test attributes to their root elements. These are described in the following table.

|Attribute|Description|
|---------|-----------|
|`test-id`| A (hopefully) unique identified that is always present on a component and optionally  on specific (meaning useful) elements in a component. In respect of a component this derived from a composition of the `test-id` of the parent component (where there is one) followed by the test ID from the components configuration, separed by a period. This is the primary means to locate an element when testing. |
|`test-cpt`| The simple name of the component class in lower case. This does not reflect any super-classes (so take care in that respect). |
|`test-state`| Used by a component in a manner that is specific to that component (so may not be present). Generally used to indicate some form of state. |
|`test-ref`| Used to reference an element within a component (and is used by `XXXTester` classes). This is often (though not necessarily) coupled with a `test-id` which then takes the value of the components `test-id` alongside the `test-ref` separated by a dash. |

Note that for the first attribute we make use of the test ID on the component configures (assigned by the `testId(String)` method on `Component.Config`). If this has not been explicitly assigned then the value of `test-cpt` will be used.

When building a (testable) component one will often want to identify other elements in the components DOM as targets for inspection. If you are coupling the component with an associated `XXXTester` class (see [Test framework](#test-framework) below) then you should add a `test-ref` attribite (which allows for easy selection). However, if you want the node to be able to be referenced directly then you can assign it a `test-id`. The most flexible approach is to add both generating the `test-id` as the composition of the `test-id` of the component with the `test-ref` separated by a dash. To facilitate this `Component` provides a `buildTestId(String)` method that will append the passed value to the test ID of the component, separated by a dash. The following code snippet illustrates these concepts:

```java
Wrap.$ (el).$ (el, root -> {
    root.a (a -> {
        if (Debug.isTestMode ()) {
            a.setAttribute ("test-id", buildTestId ("action"));
            a.setAttribute ("test-ref", action);
        }
    });
});
```

The `DomBuilder` framework also provides ultility methods specifically for this purpose:

```java
Wrap.$ (el).$ (el, root -> {
    root.a (a -> {
        a.testId (buildTestId ("action"));
        a.testRef ("action");
    });
});
```

Note that it is a matter of convention to assign to `test-id` the components `test-id` extended by the `test-ref` (when present).

## Integration testing

### Local server with Selenium

A very good approach to developing UI integration tests is to operate against a locally run instance of the application you are testing (for example, what you may use during development). In this case you can run the code as a bare-bones unit test.

The following example illustrates the arrangement as if you were testing some behaviour of the UI with the assumption that you must login first (in reality this would probably be bundled up into a separate method). It is also assumed that the local server is running on `http://127.0.0.1:8080` and the entry point is a suitable login page `/login` (though this will be specific to your application):

```java
public MyTest {

    @Test
    public void testSomeUIBehaviour() throws Exception {
        // Setup a Selenium web driver based on Chrome (which needs to be
        // installed). This is not headless so you will see a browser open
        // and the various interactions occurring.
        ChromeOptions options = new ChromeOptions ();
        WebDriver driver = new ChromeDriver (options);
        try {
            // Hit our login page.
            driver.get ("http://127.0.0.1:8080/login");

            // Here we assume that there are two input fields (username and
            // password) and a form construct for submission. However, the
            // exact arrangement will be application dependent.
            driver.findElement (By.name ("username")).sendKeys (username);
            driver.findElement (By.name ("password")).sendKeys (password);
            driver.findElement (By.tagName ("form")).submit ();

            // Here we wait for the appearance of the `pageBody` element
            // (which assumed to be our JUI binding point, again this will
            // be application specific).
            new FluentWait<WebDriver>(driver)
                .withTimeout (Duration.ofMillis (2000))
                .pollingEvery (Duration.ofMillis (100))
                .ignoring (Exception.class)
                .until (d -> d.findElement(By.id ("pageBody")) != null);
            
            // Give a little bit of to ensure the application starts up. You
            // could alternatively wait for the appearance of a known element
            // which would be a safer approach.
            Thread.sleep (200);

            // Now the JUI application should be running, so we create a page
            // tester wrapped around the Selenium web driver and we can start
            // testing the application.
            PageTester.$ (driver).run (page -> {
                // Test the behaviour here.
            });
        } finally {
            driver.quit ();
        }
    }
}
```

The browser is not run in headless mode so you will see all the interactions occurring. This is good for establishing your tests in the first instance before moving to a more self-contained and repeatable approach (i.e. running the application on demand and using test specific resources, such as a database provisioned through [Test Containers](https://testcontainers.com/)).

As alluded to, when your test runs any updates that it performs will be persisted over multiple runs of the test. Essentially you need to remember to clean up after each test run. For this reason along this is not a viable test strategy and is really only useful to setup you tests in the first instance.

Also note that the JUI code server is not available for these tests (at this stage) so you need to run this against a fully build version of the JUI code.

### Spring Boot

We now consider moving to a self-contained model where we run the application in the context of the test itself (we won't talk about provision other resources, such as a database, only noting, as above, that there are tools available for this such as [Test containers](https://testcontainers.com/)).

We also assume that the application based on [Spring MVC](https://docs.spring.io/spring-framework/docs/3.2.x/spring-framework-reference/html/mvc.html) which provides us a number of fairly straightforward mechanisms to bootstrap your application for testing.

We begin with a browser based approach ([Selenium](https://www.selenium.dev/)) then describe an entirely emulated variant ([HtmlUnit](https://htmlunit.sourceforge.io/)).

#### Selenium

Here we need to a annotate our test with `@SpringBootTest` passing through (at a minimum) the relevant context classes. For our purposes we assume there is a single such class `WebApplicationIT` (this maybe annotated itself with `@SpringBootApplication` for example) and the provides the relevant bootstrapping for testing.

We also pass through the `webEnvironment` assigning it to `RANDOM_PORT`. This will launch the server on a randomly selected port which is then injected.

```java
@SpringBootTest(classes = WebApplicationIT.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProfileITTest extends AbstractServerMvcIT {

    // The port will be injected here.
    @LocalServerPort
    private int port;

    @Test
    public void testSomeUIBehaviour() throws Exception {
        ChromeOptions options = new ChromeOptions ();
        // Here we can ask Chrome to run headless.
        options.addArguments ("headless");
        WebDriver driver = new ChromeDriver (options);
        try {
            // Hit our login page (not the injection of the port).
            driver.get ("http://127.0.0.1:" + port + "/login");

            // Perform our login and tests here.
            ...

        } finally {
            driver.quit ();
        }
    }
}
```

When the test is run an instance of the server is created which becomes available at localhost on a random port. When you start the web driver, as with the [Local server](#local-server-with-selenium) described above, an instance of Chrome is created and the web driver executes actions against that instance (which interacts with the application as hosted). If you want to observer the interactions ensure that the headless argument is commented out.

Note that Chrome is not the only option available and you are urged to explore [Selenium](https://www.selenium.dev/) more thoroughly. Also note that if you are running these tests on a separate server (i.e. in a CI/CD framework) you will need to have the relevant browsers installed.

#### HtmlUnit

An alternative to Selenium is [HtmlUnit](https://htmlunit.sourceforge.io/). This is a popular framework for testing UI's in a purely headless manner (in that no actual browser is needed). In the most part JUI plays well with HtmlUnit however that may not always be the case as HtmlUnit does have a number of limitation in terms of DOM API support and the limitations with [Mozilla Rhino](https://github.com/mozilla/rhino). It is worth noting that Rhino appears to be waning in support so there is an open question as to the future of HtmlUnit. Having said this, the testing framework presented here is agnostic as to the use of HtmlUnit or Selenium (in fact Selenium has itself an HtmlUnit backed web driver) and it is pretty easy to switch to the pure Selenium configuration as described above.

With HtmlUnit we can avoid standing up a server and make use of Spring's [MockMvc](https://docs.spring.io/spring-framework/reference/testing/spring-mvc-test-framework.html). The requires adding to the unit test the `@AutoConfigureMockMvc` annotation:

```java
@AutoConfigureMockMvc
@SpringBootTest(classes = WebApplicationIT.class)
public class ProfileITTest extends AbstractServerMvcIT {

    // This gets injected by Spring.
    @Autowired
    protected MockMvc mockMvc;

    @Test
    public void testSomeUIBehaviour() throws Exception {
        // Here we setup a web client using the mock injected by Spring. We
        // also configure for compatibility with Chrome (again there are
        // other options).
        WebClient client = MockMvcWebClientBuilder.mockMvcSetup (mockMvc)
          .withDelegate (new WebClient (BrowserVersion.CHROME))
          .build ();

        // Note that we now access via this URL.
        HtmlPage page = client.getPage("http://localhost/login");
        // Use page to perform a login (in a similar manner as in the
        // Selenium case above).

        // A version of page tester wraps a web client.
        PageTester.$ (driver).run (page -> {
            // Test the behaviour here.
        });
    }
}
```

## Test framework

As introduced above we provide a simple test framework for testing components when running through HtmlUnit (i.e. when integration testing). This is based on the class `PageTester` and its associate classes.

### Principles

The underlying principle is to shadow each component with an associated `XXXTester` class (for example, the `TextControl` component is shadowed by `TextControlTester`) that takes a reference `test-id`, resolves against the associated DOM element as well as child elements (i.e. by using `test-ref`'s) and exposes methods to validate state and content as well as perform standard actions (such as clicks and data entry).  These classes are brought together as a script of operations that can be used to mimic user behaviour on the UI and verify its response.

?> Tester classes are provided for all the standard JUI pre-built components and these can be created using convenience methods on `ITester`.

### Demonstration

We provide a simple test flow to illusrate how the framework can be used. This comes from the `ControlITTest` test case in the **playground** (you can following along in the **playground** by looking at the **Controls** tab as well as using the browser to inspect the DOM for the relevant `test-id`'s):

```java
PageTester.$ (webClient, "http://localhost/playground?test=true", 4000) //

    // Grab the top-level tabset and activate "controls"
    .with (TabbedPanelTester.$ ("applicationui").subclass (), tabs -> {
            tabs.validateTabs ("themes", "samples", "controls", "gallery", "editor", "dialogs");
            tabs.validateActiveTab ("themes");
            tabs.activate ("controls");
            tabs.validateActiveTab ("controls");

            // Under controls grab the add button and click to open the add dialog.
            tabs.with (ButtonTester.$ ("controls.controlsection.controlsectiongroup.button"), btn -> {
                btn.validateLabel ("Add");
                btn.click ();
            });
    }) //

    // Grab the dialog that has been opened above.
    .modal ("controlpanel001_dialog", dialog -> {
        dialog.validateTitle ("Create project");

        // Click on the create button with no content.
        dialog.button ("btn_create_project", btn -> btn.validateLabel ("Create project").click (1000));

        // Perform checks on the form controls.
        dialog.scope ("controlpanel01.controlsection", form -> {
            form.textControl ("controlsectiongroup.name", ctl -> {
                ctl.validateInput ("");
                ctl.validateNotReadOnly ();
                ctl.validateInvalid ();
                ctl.field (field -> {
                    field.validateLabel ("Name of project");
                    field.validateError ("please enter the name of the project");
                });
            });
        });
    });
```

We begin with priming the `PageTester` from the URL `http://localhost/playground?test=true` (a described previously). There is no authentication involved so this is straight forward.

Our first test is the tabbed panel representing the top-level navigation structure. Our entry class is `ApplicationUI` so makes use of the default `testid="applicationui"`. Since `ApplicationUI` extends `TabbedPabel` we create an instance of `TabbedPanelTester` with that `test-id` and note that this is a subclass (this prevents a check being done on the `test-cpt` which will contain `applicationui` rather than `tabbedpanel` that `TabbedPanelTester` would otherwise expect). The `with` simple serves as a mechanism to allow for a lambda expression to be used to operate on the `TabbedPanelTester` instance while first resolving it (see `IResolvable` which declares the method `resolve(IPage)` that, when called, allows for the various DOM node looksup to occur setting the testers in a state that they can be worked with). As far as our initial validations are concerned we first verify the expected tab references and that we are on the default tab `themes`. When then activate the `controls` tab and subsequently validate that it is now the active tab.

At this stage our UI would (if we could see it) be displaying the **Controls** tab. There is an **Add** button that we want to activate to display modal dialog. This is activation is performed with:

```java
tabs.with (ButtonTester.$ ("controls.controlsection.controlsectiongroup.button"), btn -> {
    btn.validateLabel ("Add");
    btn.click ();
});
```

In this case we use the `with(...)` method on `TabbedPanelTester` (it's actually declared on `ITester` so is available on all tester classes). This is our first introduction to scoping where all references to `test-id`'s that reside "under" this `with` will have `'applicationiu.'` prepending to them. So the reference to the button `controls.controlsection.controlsectiongroup.button` is to the actual `test-id="applicationui.controls.controlsection.controlsectiongroup.button"` (you can verify this attribute by inspection in the browser). This approach makes it easier to manage long (and possibly malleable) `test-id`'s.

Having clicked on the button (as well as validating its label) we should be able to test for a modal (note that a click will be preceeded by a small delay to allow for rendering, an explicit delay can be provided to the click method). This is achieved with:

```java
...
.modal ("controlpanel001_dialog", dialog -> {
    form.textControl ("controlsectiongroup.name", ctl -> {
        ctl.validateInput ("");
        ctl.validateNotReadOnly ();
        ctl.validateInvalid ();
        ctl.field (field -> {
            field.validateLabel ("Name of project");
            field.validateError ("please enter the name of the project");
        });
    });
})
...
```

This invocation is on the `PageTester` instance so is not scoped (and all dialogs are top-level so don't resided under `applicationui`). Now that we have a tester for the dialog we can verify its title and click its submit button (here we make use of a utility method which is a short hand for `with (ButtonTester.$("btn_create_project"), ...)`). Note that since this is being invoked from the modal tester instance the `test-id`'s are scoped by `controlpanel001_dialog.`, so the buttons actual `test-id="controlpanel001_dialog.btn_create_project"` but we only need to refer to the unscoped part.

Once submitted we should find there is an error since we have no content for the name field (you can check this behaviour manually in the playground). To verify this we create a scope `controlpanel01.controlsection` (for convenience really) and grab a reference to the associated text field via a `TextControlTester` (using the relative `test-id="controlsectiongroup.name"`). We first perform some basic checks on the control then grab a reference to its associated *field*.

Now this needs some explanation. The name control appears in a `ControlPanel` under `ControlSection` and within a `ControlSectionGroup` (see [Controls](ess_controls.md) for details). The later provides structure to layout controls which includes placement of a label and the location for error messages. The `field` method (available on all control tester classes) is a short hand to creating a `ControlGroupFieldTester` which is given the ID of a control component. It used that ID to locate the control component and walk up the DOM hierarchy to find the various nodes that are created within the `ControlSectionGroup` instance that layout the control and its associated meta-data (so assumes the control does sit inside one of these). From the vantage we can then validate the associated label and error messages.

### Printing

Now `PageTester` has a family of `print(...)` methods that allows for printing of the state of the pages at the time of printing. The default is to print to the console:

```java
PageTester.$ (...)
    ...
    .print ();
```

However one can print to a file:

```java
PageTester.$ (...)
    ...
    .print (() -> {
        try {
            return Files.createTempFile (null, ".html").toFile ();
        } catch (Throwable e) {
            return null;
        }
    });
```

In this example the file is a temporary file and its location will be printed to the console. This file will have been processed specifically to be able to be opened in a browser (it has the JavaScript stripped and referenced CSS inlined, among other clean ups) for inspection (on MacOS you can open this with the command `>open <file>` run from the command line).

### Creating custom testers

Given a component to test (say, `MyComponent`) the simplest approach to creating a shadow tester class is to extend `ComponentTester`:

```java
public class MyComponentTester extends ComponentTester {

    public MyComponentTester(String testId) {
        super (testId);
    }

    public void resolve() {
        super.resolve();
        assertTestCpt ("button");

        // Resolve other elements.
    }

    // Validation methods and assignments here.

    ...
}
```

The `test-id` for the component instance is passed through the constructor and then through to the base class. When the tester is resolved the base class will expose the member `el` (of type `INode`) which maps to the DOM node that has that `test-id` (and is presumed to be the root node of the associated component).

#### Resolution

Resolution is the process by which a `IPage` instance is passed to a tester and the tester extracts the various DOM nodes it expects to find. Anything that needs resolution implements `IResolvable` and must provide a `resolve(IPage)` method.

The `ComponentTester` is resolvable (through its class hierarchy) and when resolved it extracts the components root element (as described above) and exposes the passed `IPage` as the `page` member. It then calls it's `resolve()` method which is generally overridden by sub-classes. It is here that you extract you own relevant DOM nodes for which you can make use of facilities on `el` or `page`, for example:

```java
this.labelEl = el.selectByRef ("label");
Assertions.assertNotNull (this.labelEl, "Unable to find button label");
```

This comes from `ButtonTester` and attempts to extract the DOM node associated to the button's label tag that has the `test-ref="label"`. The assertion assures you are getting what you expect in this instance (this being part of the testing process).

?> Although `INode` provides the means to select by `test-ref` (as well as by XPath), `IPage` provides a means to select by `test-id` (being global). There is some resilence built in here in that should a node not be found then a retry will occur. Only once all retry attempts have been exhausted then a `null` value is returned.

#### Child testers

The `ComponentTester` has the ability to register child testers for the case where a component employs child components (of a known type). An example of this is the `TabbedPanelTester` which makes use of a tab set and thus includes a `TabSetTester`.

Child components can be registered as follows:

```java
register (new TabSetTester (testId + ".tabset"));
```

Noting here that the tab set component will have been assigned a test ID `tabset` so the `test-id` will be resolved to that of the parent `TabbedPanel` extended by `.tabset` (see [Test attributes](#test-attributes) above).

During resolution each of the child testers will be resolved (and will be resolved prior to `resolve()` being called).

#### Validations

You can then add validation methods as you see fit (again, using `ButtonTester`):

```java
public ButtonTester validateLabel(String label) {
    validate (new TextValidator (() -> labelEl.textContent (), label));
    return this;
}
```

which is equivalent to:

```java
public ButtonTester validateLabel(String label) {
    validate (() -> {
        Assertions.assertEquals (label, labelEl);
    });
    return this;
}
```

The `validate(...)` method is provided by `Tester` (the base class for `ComponentTester`) and will either perform the validation immediately (if the tester has been resolved) or will store it and validate it on resolution.

?> In addition the `validate(...)` method also wraps the validation in a retry mechanism. The default is to retry 10 times with a 500ms delay between each attempt. This is to allow for any running JavaScript to complete and ensures a degree of resilience.

#### Actions

Actions can also be declared and will generally involve mouse clicks and keyboard input:

```java
public ButtonTester click(long delay) {
    try {
        anchorEl.click ();
        sleep (delay);
    } catch (Exception e) {
        Assertions.fail ("Failed to click button [test-id=\"" + testId + "\"]", e);
    }
    return this;
}
```

Here we make use of the facilities of `INode` followed by a prescribed delay (this allows for any changes to propagate or any remote calls to finish).

#### Use

Testers are intended to be used in the context of a scoping `Tester` (such as `PageTester`) through a call to `with(...)`. This method is passed an instance of the tester and a consumer that operates on the resolved tester. The `with` will first resolve the tester then pass it onto the consumer.

For example:

```java
PageTester.$ (...)
  .with (new MyTester ("testid), tester -> {
    // Do stuff with the tester instance.
  });
```

#### Standards and guidelines

There are numerous examples of tester classes that you can call upon for inspiration, however here are a few standards that should be followed:

1. They have a single constructor that takes a `test-id` as well as a single static method `$` that provides a short-hand for invoking the constructor.
2. They implement `IResolvable` (which allows it to resolve DOM elements against a page), though often extend `Tester`.
3. Can operate in a scoped manner where the scoping provides a prefix to the `test-id`'s passed (for example scoping by `applicationui.controls` will prepend `applicationui.controls.` to all test IDs). Support for this is provided out-of-the-box with `IPage`.

These allow the tester classes to operating with `PageTester`.