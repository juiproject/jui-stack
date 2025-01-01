# Development

*This document is a work-in-progress.*

Here we describe how to setup your environment to maintain and / or further develop JUI.

## Environment setup

### Required software

The following will setup an Eclipse environment for development and involved installation of the following:

1. JDK (17)
2. Git
3. Maven (3.8 or greater) 
4. A suitable IDE (Eclipse and VS Code are described below)

In addition the following assumes your are installing on MacOS (though other systems should no be too different).

#### MacOS

##### JDK

JDK 17 is the most recent LTS and well supported by GCP (along with others). To install the JDK (if you are not running bash but zsh then replace `.bash_profile` with `.zshrc`):

1. Download JDK 17 from [Oracle Java](https://jdk.java.net/archive/) (select from the list on the page).
2. Extract with 	`tar -xf` then copy the extract to `sudo mv <jdk-extract-dir> /Library/Java/JavaVirtualMachines/`.
3. `Run /usr/libexec/java_home -V` (that's an upper case `V`) to verify that the version is available.
4. Add to your `~/.bash_profile` (or replace) ``export JAVA_HOME=`/usr/libexec/java_home -v <version>` `` where `<version>` is the newly installed version (i.e. 17).
5. Run source `~/.bash_profile` to set the environment and check `JAVA_HOME` with `env`. Finally verify the Java version `java -version`.

Note that you can have as many versions of Java as you like and can switch between them by setting JAVA_HOME. Your Eclipse installation can reference any of these versions as well (and will likely pick them up automatically).

##### Git

This should already be installed as part of the XCode Command Line Tools. If not installed run the following command:

```bash
git --version
```

and follow the installation prompts.

##### Maven

*Eclipse will make an embedded Maven available however it will use the `settings.xml` file below for configuration; a separate Maven installation is required to perform builds outside of Eclipse.*

Moving onto Maven the suggested approach is to follow the standard [installation guide](https://maven.apache.org/install.html). However you can also install it using homebrew.

Note that you should create at least a minimal `~/.m2/settings.xml` settings file:

```xml
<settings>
  <localRepository>${user.home}/.m2/repository</localRepository>
  <profiles>
    <profile>
      <id>default</id>
    </profile>
  </profiles>
  <activeProfiles>
    <activeProfile>default</activeProfile>
  </activeProfiles>
</settings>
```

which will locate the Maven repository under `.m2/` in your home directory.

#### IDE (VS Code)

This can be installed from [VS Code download](https://code.visualstudio.com/download). Once running you should install the following extensions:

1. Debugger for Java (Microsoft)
2. Extension Pack for Java (Microsoft)
3. Language Support for Java (Red Hat)
4. Project Manager for Java (Microsoft)
5. Test Runner for Java (Microsoft)

## Appendix

### XML catalog

*The DTD for the module files may referenced externally (i.e. https://juiproject.github.io/jui-stack/jui-module-1.0.0.dtd) or internally from the source. To so so internally you should setup a catalog. This is described here in the context of VS Code.*

For intenal reference use the following in the module file (strictly speaking the URI is not that relevant for internal reference):

```xml
<!DOCTYPE module PUBLIC "-//JUI//1.0.0" "jui-module-1.0.0.dtd">
```

Then map `-//JUI//1.0.0` to the DTD file in the source using the XML catalog found in the `support` directory. To do this simply add the following to your `settings.json`:

```json
"xml.catalogs": [
    "<path-to-jui-stack>/jui-stack/support/catalog.xml"
],
```

where `<path-to-jui-stack>` is the absolute path to the checked out version of `jui-stack`.