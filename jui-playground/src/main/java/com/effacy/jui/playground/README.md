# Description

This contains the server-side application classes configured as a SpringBoot application. The class `TestApplication` (no relation to the same named class in the `ui` package) is the application class that can be run directly to stand up the SpringBoot server. The `PagesController` class provides Spring MVC configuration that maps the resource [`com.effacy.gwt.test.main.html`](../../../../../resources/templates/main.html) to the root context. The page resource is a Thymeleaf template that bootstraps the GWT application as declared in the module file [`TestApplication.gwt.xml`](../../../../../../gwt/java/com/effacy/gwt/test/PlaygroundApp.gwt.xml).

This is purposefully a simple application sufficient to run the samples and demonstrate the principles of remoting.