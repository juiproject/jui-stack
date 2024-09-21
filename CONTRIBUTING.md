# Setup for development

If you intend to build on and expand these libraries or you want to work through the **tutorial** in the [jui-playground](./jui-playground/) then you first need to prepare your environment.

The following describes the core software that you need as well a guide to setting up a suitable IDE.

## Required software

The following will setup an Eclipse environment for development and involved installation of the following:

1. JDK (17)
2. Git
3. Maven (3.8 or greater) 
4. A suitable IDE (Eclipse and VS Code are described below)

In addition the following assumes your are installing on MacOS (though other systems should no be too different).

### MacOS

#### JDK

JDK 17 is the most recent LTS and well supported by GCP (along with others). To install the JDK (if you are not running bash but zsh then replace `.bash_profile` with `.zshrc`):

1. Download JDK 17 from [Oracle Java](https://jdk.java.net/archive/) (select from the list on the page).
2. Extract with 	`tar -xf` then copy the extract to `sudo mv <jdk-extract-dir> /Library/Java/JavaVirtualMachines/`.
3. `Run /usr/libexec/java_home -V` (that's an upper case `V`) to verify that the version is available.
4. Add to your `~/.bash_profile` (or replace) ``export JAVA_HOME=`/usr/libexec/java_home -v <version>` `` where `<version>` is the newly installed version (i.e. 17).
5. Run source `~/.bash_profile` to set the environment and check `JAVA_HOME` with `env`. Finally verify the Java version `java -version`.

Note that you can have as many versions of Java as you like and can switch between them by setting JAVA_HOME. Your Eclipse installation can reference any of these versions as well (and will likely pick them up automatically).

#### Git

This should already be installed as part of the XCode Command Line Tools. If not installed run the following command:

```bash
git --version
```

and follow the installation prompts.

#### Maven

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

#### IDE (Eclipse)

To setup eclipse (if you are not running bash but zsh then replace `.bash_profile` with `.zshrc`):

1. Create the directory `~/Dev/Eclipse`; this will be the base installation for all things Eclipse.
2. Download the latest version of the Eclipse dmg and open. Run the installer in-situ (don't copy to Application) and select the JEE version to install. For the installation location choose `~/Dev/Eclipse/jee-XXXX-YY` where `XXXX-YY` is the version of eclipse (e.g. `2022-12`). During the installation ensure that JDK 17 is selected. Do not launch when installed but simply close the installer.
3. Locate the `eclipse.ini` file in the installation (for `2022-12` it is located under `Eclipse.app/Contents/Eclipse/` but different versions may have it elsewhere) and ensure that `-Xmx16g` is set (normally this is a much lower number). You may also add your name with the `-Duser.name=XXX` option which can be useful (i.e. for Git interaction).
4. Create a start script (see below) under `~/Dev/Eclipse` and call it `eclipse` (don't forget to set the permissions to `a+x`). It is advisable to add `export ~/Dev/Eclipse:$PATH` to your `~/.bash_profile` to make it easier to run `eclipse`.
5. Run `eclipse` from the command line and that should open the IDE. You will be asked for a workspace, choose a sensible location (i.e. something under `~/Dev/Workspaces/`).
6. Select **Eclipse** > **Settings...** and choose **Team** > **Git**. Set the Default repository folder to `${workspace_loc}`.
7. Setup the standard Maven install run configurations under **Run** > **Run Configurations...**. Call one *Standard Install* with goals `clean install` and use `${project_loc}` for the *Base directory*. Copy this to *Standard Install (No tests)* and add the parameter `tests.skip` with the value of `true` (which will not run any unit or integration tests). It is recommended that you set these as favorites (set in the *Display in favorites menu* section under the *Common* tab).
8. Select **Eclipse** > **Settings...** and apply the following (this ensures that formatting is standardised):
    1. Under **Java** > **Code Style** import the code formatter and code template files from the `doc/ide` directory (of this project).
    2. Scanning through the various editor settings and ensure that when editing XML files that the line length is at least 400 and that 2 spaces are used for indentation (and **not** tabs).

Your Eclipse installation and workspace is now ready to accept the project.

**Setup script**

Called `eclipse` with `chmod a+x` privileges:

```bash
#!/bin/bash

export GIT_SSH=/usr/bin/ssh
open ~/Dev/Eclipse/jee-XXXX-YY/Eclipse.app
```

Noting one should replace the `XXXX-YY` with the eclipse version.

#### IDE (VS Code)

This can be installed from [VS Code download](https://code.visualstudio.com/download). Once running you should install the following extensions:

1. Debugger for Java (Microsoft)
2. Extension Pack for Java (Microsoft)
3. Language Support for Java (Red Hat)
4. Project Manager for Java (Microsoft)
5. Test Runner for Java (Microsoft)

## Importing the projects

### Eclipse

*If you are not familar with Git and Eclipse then [Eclipse Git Tutorial](https://www.vogella.com/tutorials/EclipseGit/article.html) provides a nice introduction.*

Begin by changing to the Git perspective in Eclipse (on the top-right corner of the IDE is a small box with a plus sign, click on that and select Git). Now proceed as follows:

1. Select the *Clone Git repository...* action (the icon that looks like a yellow cloud with a green arrow over it).
2. Paste `git@bitbucket.org:jerry-effacy/jui-stack.git` into the *URI* and select *Next*.
3. Check the `develop` and `master` branches to import and select *Next*.
4. Select `develop` as the initial branch and select *Finish* to start the import.

This will clone the project into the workspace but does not import the project into Eclipse. To do this:

1. Switch over to the work perspective (the icon in the top-right corder of the IDE that looks like a pair of nested folders with a bean floating above it).
2. Create a general project named `jui-stack`. The project will pick-up the cloned `jui-stack` and import it.
3. Select the project in the project explorer and run the **Standard Install** run configuration (as created during the Eclipse installation). This will ensure that project is in a buildable state and, more importantly, ensure all the Maven dependencies are resolved.
4. Right click on the project and select **Configure** > **Configure and Detect Nested Projects...**. All the sub-modules projects should be selected so you should only need to click *Finish*. The projects should now appear as Eclipse projects (rather than folders) under `jui-stack`.
5. Perform a clean and build of all the projects (in Eclipse) and ensure there are no errors.

Note that if you add Maven dependencies you can update the project(s) by right selecting each project you want to update and choosing **Maven** > **Update Project...**.

This completes the project import read for development. It is highly recommended that you begin by running up the testing application as described in [`jui-playground`](./jui-playground/).

### VS Code

To install:

1. Open or create a workspace into which you want to install the project.
2. In the search bar at the top of the VS Code application search for `>git:Clone` and run.
3. In the prompt enter `git@bitbucket.org:jerry-effacy/jui-stack.git` and install as a new project.

The various module should import as separate Java projects.

## Release management

*This only applies if you are going to release versions of the library to an artefact repository.*

Performing a release follows the standard Maven process for deploying a multi-module project (so there should be no surprises).

### Release process

The assumptions made prior to performing a deployment are:

1. The correct version snapshot is present in the parent project `pom.xml` declared under the `revision` property (see [Versions](#versions) below).
2. The code being deployed is in the `develop` branch (see [Branching](#branching) below).
3. The code is in a state to be deployed (there is a control for this but the understanding should be that it is in the correct state).
4. The person or system running the deployment has suitable credentials in place to deploy the artefacts to the artefact repository (see [Artefact repository](#artefact-repository) below).
5. There should be no conflicts merging into `master` (with the exception of the version in the POM file).

With the above assumptions the process for deployment are (this is assumed to be executed on the command line):

1. Clone the project `git clone git@bitbucket.org:jerry-effacy/jui-stack.git jui-stack; cd jui-stack` (for consistency avoid using an existing clone).
2. Switch to the develop branch `git checkout develop; git pull`.
3. Run a local install (the control) `mvn clean install` (if this fails the abort the deployment and clean up).
4. Switch back to the master and merge `git checkout master; git merge develop` (if there are conflicts beyond the revision number in the pom file, the abort).
5. Edit the `pom.xml` and remove `-SNAPSHOT` from the version declared under the `revision` property (if there was a conflict then simply resolve with the correct version of the revision property).
6. Add the pom file and commit with `git add pom.xml; git commit -m "Merge and updated revision"`.
7. Run the deployment with `mvn clean deploy` (if this fails the abort the deployment).
8. Push the changes with `git push`.
9. Tag the commit with `git tag version/---` (where `---` is the version).
10. Push the tag with `git push origin version/---`.
11. Switch back to `develop` with `git checkout develop`.
12. Merge master back into `develop` with `git merge master` (this should avoid later merge problems by bringing `develop` up to `master`).
13. Update the `pom.xml` to assign the new snapshot revision in the `revision` property (i.e. if the released version was `0.2.1.M` and the next version will be `0.2.2.M` then set the revision to `0.2.2.M-SNAPSHOT`).
14. Commit the change with `git add pom.xml; git commit -m "Updated snapshot"; git push`.
15. Clean up by removing the cloned repository with `cd ..; rm -rf jui-stack`.

It is good practice to then verify that the artefacts have been deployed to the artefact repository. For convenience a compact version of the above is presented below:

```bash
git clone git@bitbucket.org:jerry-effacy/jui-stack.git jui-stack; cd jui-stack
git checkout develop; git pull
mvn clean install
git checkout master; git merge develop
[edit pom.xml (fix or update revision)]
git add pom.xml; git commit -m "Updated revision"
mvn clean deploy
git push
git tag version/---
git push origin version/---
git checkout develop
[edit pom.xml (update revision)]
git add pom.xml; git commit -m "Updated snapshot"; git push
cd ..; rm -rf jui-stack
```

### Versions

We adhere to a relatively simple version scheme where each version adheres to the form `X.Y.Z.M[n]` where `X` is the major version (where incompatibilities with prior versions may exist), `Y` is the minor versions (significant changes from the last minor version but still compatible), `Z` is the sub-minor version (small changes) and `n` is a patch release off the main version (this is indexed from `0` and the case where this is `0` it is omitted). An example version is `0.2.1.M` being the *main* release of `0.2.1` and `0.2.1.M1` would be the first *patch* release of `0.2.1.M`. 

During development the version declared in the POM is a *snapshot* and is of the form `X.Y.Z.M[n]-SNAPSHOT` where `X.Y.Z.M[n]` is the target version for the next release.

### Branching

Branching follows a relatively simple model suitable for small-scale development:

1. The `master` branch is reserved for releases so is only updated and tagged on successful release (see [Release process](#release-process) above). Note that this constraint is viable regardless of the branching model employed.
2. Version tags are applied to the `master` branch upon release and are of the form `version/X.Y.Z.M[n]` where `X.Y.Z.M[n]` is the release version (see [Versions](#versions) above).
3. Pre-release code is committed to the `develop` branch and only this branch is merged into `master` during the release process.

The `develop` branch also serves as (the name implies) the branch to commit changes into. Separate branches may be maintained on a case-by-case basis and should be named in accordance with intent: `feature/---` for a feature, `bugfix/---` for a bug-fix and `hotfix/X.Y.Z.Mn` for a patch (this latter one should always be branched off the `version/X.Y.Z.M[n-1]` tag). Developers may maintain their own personal branches `developer/<developer-name>/sub-branch` (noting the `sub-branch` which must be present should default `develop`, this gives flexibility to create multiple scoped branches).

### Artefact repository

The current artefact repository is `s3://maven.effacy.com/maven2` (private access). To deploy you will need to update your Maven `~/.m2/settings.xml` settings file as follows:

```xml
<settings>
  <localRepository>${user.home}/.m2/repository</localRepository>
  <servers>
    <server>
      <id>effacy-dist</id>
      <username>${env.EFFACY_USERNAME}</username>
      <password>${env.EFFACY_PASSWORD}</password>
    </server>
  </servers>
  <profiles>
    <profile>
      <id>default</id>
      <repositories>
        <repository>
          <id>effacy-dist</id>
          <name>Effacy Maven 2 Repo</name>
          <url>s3://maven.effacy.com/maven2</url>
        </repository>
      </repositories>
    </profile>
  </profiles>
  <activeProfiles>
    <activeProfile>default</activeProfile>
  </activeProfiles>
</settings>
```

You will also need to store your Effacy Maven 2 Repository access keys in the environment variables `EFFACY_USERNAME` and `EFFACY_PASSWORD` respectively; this is most easily done in your `~/.bash_profile` (or `~/.zshrc`):

```txt
export EFFACY_USERNAME=XXX
export EFFACY_PASSWORD=XXX
```

(feel free to change the variable names as you see fit).


