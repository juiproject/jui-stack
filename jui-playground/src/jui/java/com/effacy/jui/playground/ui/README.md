# Description

## Entry point and bootstrapping

This is the top-level package for the client-side application and contains (among other things) the entry point to the application `TestApplication`. Recall that this entry point is declared in a separate *module* file [`TestApplication.gwt.xml`](../../../../../../resources/com/effacy/jui/playground/TestApplication.gwt.xml) (located under the [`/src/gwt/resources`](../../../../../../resources) source tree). If we look into this file:

```
...
<module>
  <inherits name="com.effacy.gwt.ui.UI" />
  <source path="ui" />
  <entry-point class="com.effacy.jui.playground.ui.TestApplication" />
</module>
```

we note the reference `<entry-point class="com.effacy.jui.playground.ui.TestApplication" />` to the entry point class. We also note the source directive `<source path="ui" />` which informs the compiler of a package-relative location (there can be multiple such declarations where source is split across multiple packages).  The remaining directive `<inherits name="com.effacy.gwt.ui.UI" />` tells the complier to include sources (and associated directives) declared in the module `UI.gwt.xml` found in the `com.effact.gwt.ui` package (the compiler will scan the class path for these; this is one of the reasons that source files are bundled with libraries). In this case this particular reference is to the project-module [`gwt-ui`](../../../../../../../../../gwt-stack-ui/).

Recall that when an HTML page loads up the application (see below) this entry point is resolved and it's `onModuleLoad()` method is invoked. Looking into the code:

```
public class TestApplication implements EntryPoint {

    @Override
    public void onModuleLoad() {
        ...
        RootPanel panel = RootPanel.get ("pageBody");
        panel.getElement ().removeAllChildren ();
        panel.add (new ApplicationUI ());
    }

}
```

This creates a top-level GWT panel (`RootPanel`) that is bound to the HTML tag whose ID is `pageBody`. We then remove all the children of this tag (which will often contain some HTML to present a loading indicator to the user). Finally we add our application component to the panel; in this case it is an instance of `ApplicationUI`.

To bring the above together lets take a look at the HTML page that loads the application (see [`main.html`](../../../../../../../../src/main/resources/templates/main.html) which is a template relayed through SpringMVC):

```
...
<script
   src="../static/com.effacy.gwt.test.TestApplication/com.effacy.gwt.test.TestApplication.nocache.js"></script>
...
<body>
   <div id="pageBody">GWT Application Loading...</div>
   <iframe src="javascript:''" id="__gwt_historyFrame"
      style="position: absolute; width: 0; height: 0; border: 0"></iframe>
</body>
...
```

We see a tag that has the ID `pageBody` and this is the tag that the `RootPanel` above binds to.  We also see the script tag which references our module (the path) and the entry point (the `.nocache.js`). Once you have the project setup and running you will note that the `src/main/resources/static` directory contains   `com.effacy.gwt.test.TestApplication` directory (this is not managed under version control) which itself contains a number of assets. These are the outputs of the GWT compilation and linking process that was kicked off during the Maven install (see the `gwt-maven-plugin` plugin in the [`pom.xml`](../../../../../../../../pom.xml)).

The `.nocache.js` file contains the necessary code to bootstrap the application which (as needed) pull in the associated assets.

## The application itself

As described above `TestApplication.onModuleLoad()` binds a `RootPanel` to the `pageBody` element then adds to that root panel an instance of `ApplicationUI`. This is the top-level application component that contains all the other application components (there is a lot of flexibility here and you don't need to stick to a single such component; you can add components across the page as you see fit).

This component extends `TabbedPanel` and declares tabs for each of the clusters of components.