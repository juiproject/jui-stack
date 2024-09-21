# Understanding JUI

## Introduction

### Objectives

This platforms seeks to:

1. Empower Java developers to develop web UI components, consistent the contemporary notions of component-oriented UI design, without relying on a technology skill set outside of core Java for which they may be less than familiar.
2. Provide a flexible basis for web UI development with minimal constraints.

This paradigm affords the opportunity for Java developers, with minimal "font-end" skills, to participate in the building of browser based application experiences; and to do so in a way that offers near seamless transition between client and server. Core to this is the ability to translate Java code to JavaScript, fashioned in a manner that is able to run the the browser. Layered on this is Java-based model of the DOM with integrated event handling. 

So why take this approach? The fundamental driver is flexibility in terms of skill set and UI experience.  The ability to craft one's own unique experience and set that as a principle up-front. This doesn't mean we don't provide standard building blocks (panels, controls and layouts) but all are extensible and replaceable. An added benefit is that there are fewer encumberances: there is not so much to learn.

### Technology

JUI draws its foundation from GWT, though has been progressively decoupling from this as a dependency (particularly with the rise of J2CL alongside the JsInterop standard). For this reason we will refer to an application built using JUI as a *JUI application* (as opposed to a GWT or J2CL one).

#### History

For background, GWT was launched in 2006 by Google as the "Google Web Toolkit" and renamed GWT in 2012 (when it transitioned from a Google project to open source) GWT was well ahead of its time. It afforded Java developers the opportunity to build quality web front-ends while making use of strict typing, IDE tooling and code sharing across client and server.

GWT itself built on a core consisting of a Java-to-JavaScript compiler/linker (which JUI makes use of), a collection of classes to interact with the browser DOM and a suite of UI components (alongside a widget model). The latter were not always well accepted (aesthetically at least) and what emerged was an ecosystem of libraries and UI frameworks (such as Sencha and Vaadin) built on top of core GWT. As of 2015 GWT enjoyed an active user base in excess of 100,000 [[1](https://vaadin.com/blog/what-is-the-future-of-gwt-)].

From the mid 2010's came a rise in other JavaScript frameworks (according to Google Trends, Angular began gaing significant traction in 2014 to be overtaken by React in 2016 and Vue in 2018) and the popularity of GWT began to wane. Google formally dropped support and moved its focus to J2CL.

J2CL could arguably be compared with the absolute core of GWT: the Java-to-JavaScript transpiler (indeed as late as 2015 it was part of the GWT project). However, unlike GWT, it is not positioned as a framework but rather as a tool of interoperability between Java and JavaScript thereby enabling the use of a plethora of existing and emerging JavaScript frameworks to operate within a Java environment (for example, sharing data transfer objects). It is purportedly being used in many of Google's products including GMail, Docs, Slides and Calendar. With this, one can view GWT through the lens of a toolset and SDK.

For the Java developer picking up and running with one of the newer JavaScript frameworks still imposes a burden (and will always impose an additional overhead dealing with secondary technologies) so the problem is still far from solved. For a Java team that does not want to specialise and still desires a highly customisable and componentised UI coupled with a suitable Java to JavaScript compiler finds a way forward; and these libraries seek to provide a basis for that journey.

#### Comparison with other JavaScript frameworks

An obvious question arises: why JUI? This was touched on above, but it's helpful to contextualize this question by comparing it with other technologies, with one of the most prevalent being the family JavaScript frameworks. To this end we consider two prominent representatives: React and Vue.

Conceptually the underlying principles between these two and JUI are similar: all are component oriented and each make use of similar component lifecycles. In addition all three leverage transpiling from one language formalism to JavaScript. With JUI that is Java, which imposes its own constraints. With React and Vue the constructs are more closely engineered to the task of UI component design so can be more expressive.

Consider the following React example of a simple button component that is configured with a title and a click handler:

```javascript
const MyButton = ({title, onclick}) => {
    return (
        <div class="my-button">
            <button
                onClick={() => onclick ()}
            >{title}</button>
        </div>
    );
};
export default MyButton;
```

With JUI one can elect from a variety of approaches that appeal to ones sense of formalism. At the most relaxed end the equivalant code looks something like the following:

```java
public class MyButton extends SimpleComponent {

    public MyButton(String title, Consumer<UIEvent> onclick) {
        renderer (root -> {
            root.style ("my-button");
            Button.$ (root).$ (
                Text.$ (title)
            ).onclick (onclick);
        });
    }
}
```

This maybe not quite as expressive as the React example but certainly is straight forward. One may also take a more formal approach with a defined configuration class and separate rendering method (or any of the varieties in between), it comes down to preference.

So although JUI is constrained by Java, which in some cases is a limitation and other cases a benefit (particularly when it comes to tooling), it is arguable whether this imposes any significant imposition over other frameworks (given writing code is only one dimension to the challenge of UI development). For the Java developer there is the added benefit of a familar language and familiar tooling (even more so when debugging in the browser can be done using the Java source rather than the compiled JavaScript).

Having said the above there has been a concerted effort to align JUI with the likes of React and Vue so skills should be reasonable portable across frameworks.

#### Comparison with other Java frameworks

There are other options available to the Java developer including [Sencha](https://www.sencha.com/products/gxt/) and [Vaadin](https://vaadin.com/) as well as a number of open source frameworks such as [DominoKit](https://github.com/DominoKit). These tend to be quite encompassing where as JUI is much lighter and focusses more on the component model and supporting structures that a full suite of UI components (though it does provide some).

If you are looking for design flexibility then JUI may be the better option, for more of an out-of-the-box experience (particularly in the enterprise space) then these other frameworks are excellent alternatives.

### Required skill-set

We propose five (related) categories of skills which guide those needed for specific outcomes when building out a development team.

|Category|Description|
|--------|-----------|
|**UI design**| 1. Has a working knowledge of the principles of component-oriented design (see [Component Driven User Interfaces](https://www.componentdriven.org/)). 2. Has a working knowledge of navigation principles and information architecture. 3. Has a good knowledge of HTML and CSS.|
|**UI development**| 1. Has the ability to modify and add-to (in a limited capacity but including forms and modals) an existing JUI application. 2. Has a good understanding of HTML, CSS and JavaScript. 3. Has the ability to translate page-scoped UI designs to working JUI code using existing components and fragments. 4. Can create new fragments and inline-components. 5. Can contribute to existing style sheets (both injected and generated). |
|**Component development**| 1. Has the same skills as **UI development**. 2. Can create self-contained components, including custom controls, as part of a common library of application components. 3. Understands component-oriented design and can translate a system of design into fragments and components. 4. Has an in-depth understanding of HTML, CSS and JavaScript. 6. Can create alternative themes and styles for existing components and new ones for new components.|
|**UI architecture**| 1. Has a working knowlegde of **UI development** and **Component development**. 2. Has an in-depth understanding of component-oriented design and frameworks. 3. Has an in-depth understanding of application structures, navigation structures (as employed by JUI) and inter-application data flows. 4. Can develop the UI architecture of an application from the ground up.|
|**JUI platform development**| 1. Has the same skills as **Component development**. 2. Has an in-depth understanding of the JUI platform (event model, component model, control model and compilation). 3. Has an in-depth understanding of supporting mechanisms and their implementation (modal dialogs, navigation, themes and styles). 4. Can comfortably modify and contribute to the JUI codebase. | 

At a minimum a successful JUI application would employ the skills across **UI architecture** (to establish the foundations and on-going guidance), **UI design** and **UI development**. A shift from **UI development** to **Component development** would reduce the on-going demands on **UI architecture** so would constitute a senisible professional development pathway.

The **JUI platform development** category of skills would only be considered relevant to manage business risk with regard to support of the platform itself; this would be a natural pathway from **Component development** and one that would likely develop organically. JUI has been designed to be a minimal as possible and detailed implementation documentation is available both in the general documentation set and in-code documentation.

## Project anatomy

A JUI project consists of Java code that will be compiled to JavaScript and run in the browser. Much of this code will be dedicated for that purpose but code can be shared between client and server (for example, data transfer objects and common rule sets).

The compilation to JavaScript requires the use of a dedicated compiler (which is incorporated into the Maven build). For our purposes we make use of the GWT compiler (which has some attractive features, the other option being J2CL for which some more work needs to be done to support). During compilation the compiler is directed to the source code it needs to compile and generates web-ready JavaScript assets that can be directly included in an HTML page. These assets include bootstrap code that starts up your JUI application using a pre-selected *entry point* class.

The compiler can also create JavaScript from Java that is accessible directly from JavaScript code you write outside of JUI (here one leverages the [JsInterop](https://www.gwtproject.org/doc/latest/DevGuideCodingBasicsJsInterop.html) standard), so affords quite a degree of flexibility in how you construct an application. Though normally applications are completely crafted in Java.

There is also support to ease development by means of a *code server*. This is a separate application (that can be run from your IDE) that operates alongside your application when you run it locally. This serves the compiled JavaScript and associated sources-maps (for in-browser debugging) from a separate server running on a separate port. You can direct this code server to incorporate changes you make to the JUI code as you make them (and it does so efficiently).

Finally your JUI code needs to interact with the browser DOM, this is achieved using a collection of *shadow* classes (which leverage JsInterop, JUI makes use of [Elemental2](https://github.com/google/elemental2) for these purposes) that overaly existing JavaScript classes (and map into these classes during compilation). These same principles apply to non-DOM classes such as other JavaScript frameworks (including [JQuery](https://jquery.com/) which JUI also makes use of).

More details on the setup of a project can be found in [Creating an app](ess_create.md) and well as an inspection of the *playground* project `jui-playground`.

## Next steps

Now that you have some background you should move onto [Getting started](intro_gettingstarted.md). There you will be guided through some steps to familiarise youself with JUI then how to create an initial JUI application. Finally how to integrate JUI into your own application (or create the foundations from scratch).

## References

Some useful references:

1. [GWT official documentation](https://www.gwtproject.org/)
2. [J2CL official project](https://github.com/google/j2cl)
3. [JsInterop](https://docs.google.com/document/d/10fmlEYIHcyead_4R1S5wKGs1t2I7Fnp_PaNaa7XTEk0)
4. [Integrating React into GWT (1)](https://medium.com/@aldouille/how-to-integrate-react-into-a-java-legacy-gwt-client-b76d7614b7d5)
5. [Integrating React into GWT (2)](https://medium.com/whatfix-techblog/tips-on-how-to-integrate-react-in-legacy-gwt-e3186e8fd2e2)
6. [GWT based React component model](https://github.com/react4j/react4j) (a good example of how low-level one can get with GWT)
7. [Material Design and GWT/J2CL](https://github.com/GwtMaterialDesign)
8. [Sencha](https://www.sencha.com/products/gxt/) (Sencha UI framework GXT built on top of GWT)
9. [React vs GWT](https://stackoverflow.com/questions/43711127/react-vs-gwt-for-large-scale-web-application) (not strictly a comparison but a nice discussion of libraries from a Java developers perspective).
10. [VueGWT](https://vuegwt.github.io/vue-gwt/) (a great example of J2CL merging VueJS and GWT)
11. [JSweet](https://www.jsweet.org/) (another Java to JavaScript compiler)
