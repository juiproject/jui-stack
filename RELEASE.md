# Release process

## Preamble

Prior to describing the release process itself we make a few notes on some important (and relevant) concepts.

### Versions

We adhere to a relatively simple version scheme where each version adheres to the form `X.Y.Z.[(SP|RC)n]` where `X` is the major version (where incompatibilities with prior versions may exist), `Y` is the minor versions (significant changes from the last minor version but still compatible), `Z` is the sub-minor version (small changes). The `n` is used when either of the qualifiers `SP` or `RC` are employed and is incremental from `1`.

The use of `SP` or `RC` are for the specific cases of a *service patch* (i.e. patch release off the *main* version) or a *release candidate* (in preparation for a main release).

An example version is `0.2.1.M` being the *main* release of `0.2.1` with `0.2.1.SP1` being the first *service patch* of `0.2.1`.

During development the version declared in the POM is the generic *snapshot* `LOCAL-SNAPSHOT`, so is devoid of any specify target version.

### Branches and tags

For the purposes of realeases, all *main* releases are made from a tag of the form `version/<version>` from the `main` branch. The same is for *release candidates* (as these are sequentially compatible with the *main* releases).

For service patch a bugfix branch `bugfix/<version>` is created off the `main` branch tag `version/<version>` and it tagged `version/<version>.SPn`.

## Maven Central

Here we discuss the process by which one releases artefacts to the [Maven Central Repository](https://mvnrepository.com/repos/central). This involves (for our purposes) deploying the project as a single component into the [Sonatype Portal](https://central.sonatype.com) from which it can be deployed manually (while the process is new for the project, later we will switch to automated deployments).

### Github releases

The preferred approach is to release through Github whereby the Github action [release.yml](.github/workflows/release.yml) performs the actual deployment using the version supplied through the release:

1. Select **Create new release**.
2. Ensure the `main` branch is selected.
3. Create a new tag of the form `version/<version>` where `<version>` is the version number (i.e. `0.1.0`). Note that the [version format](#versions) is enforced during release.
4. Give the release the title `v<version>` (i.e. `v0.1.0`).
5. Publish the release. This will result in the [release.yml](.github/workflows/release.yml) being run and a newly created component in the [Sonatype Portal](https://central.sonatype.com).
6. Login to the [Sonatype Portal](https://central.sonatype.org) and deploy the component.

#### Maintenace

This should be limited to maintaining the secrets used by [release.yml](.github/workflows/release.yml):

|Secret|Description|
|------|-----------|
|`CENTRAL_TOKEN_USERNAME`|The user token username as obtained from the [Sonatype Portal](https://central.sonatype.org).|
|`CENTRAL_TOKEN_PASSWORD`|The user token password as obtained from the [Sonatype Portal](https://central.sonatype.org).|
|`GPG_SIGNING_KEY_PASSWORD`|The GPG passphrase (see below).|
|`GPG_SIGNING_KEY`|The encoded GPG siging key (see below).|

For GPG signing (see [GPG keys](#gpg-keys) for a brief description) we require a key and passphrase. This can be created locally (see aforementioned link) then a suitabley encoded key created running:

```bash
gpg --armor --export-secret-keys <id>
```

where `<id>` can be obtained from (it is assumed one has been created and deployed to the `keys.openpgp.org` keyserver):

```bash
gpg --list-secret-keys <email>
```

and `<email>` is the email associated to the key (the `<id>` is long hex sequence).

#### Implementation details

The action [release.yml](.github/workflows/release.yml) is based on the guidance in [Setup Java](https://github.com/actions/setup-java/blob/v3.11.0/docs/advanced-usage.md#Publishing-using-Apache-Maven) performs the following:

1. Creates a `settings.xml` configured with the deployment location credentials and signing credentials for the GPG key (see [Local workstation](#local-workstation) below).
2. Makes the the GPG passphrase available.
3. Runs a Maven `deploy` on the project using the `release` profile and specifying the `revision` property as per the release.

The last step (3) is essentially no differen to running the deployment on ones local workstation. See [Local workstation] for detail on the Maven configuration.

### Local workstation

The profile `release` has been configured to perform a deployment to Maven Central via the command:

```bash
VERSION=<version>
mvn -Drevision=$VERSION -Prelease deploy
```

This will (by default) create a component (assuming all validations pass) accessible in the [Sonatype Portal](https://central.sonatype.com) which can then be manually deployed (to deploy automatically pass `-Drelease.auto=true`).

#### Preparation

*The following assumes access to the [Sonatype Portal](https://central.sonatype.com) from which a user token and password have been obtained (and the domain `effacy.com` has been verified).*

To perform a release from your local environment requires some prepartion:

1. Create and make available a GPG key (see [GPG](https://central.sonatype.org/publish/requirements/gpg/), this include deploying to the `keys.openpgp.org` keyserver).
2. Register the password for the GPG key in your `settings.xml` (see note below).
3. Register the Sonatype user token and password in your `settings.xml` (see [Publish to Central](https://central.sonatype.org/publish/publish-portal-maven/) for details; in our case the server is configured under the name `central`).

In reference to (2) above, the following can be used to store the GPG passphrase in your `settings.xml`:

```xml
...
<server>
  <id>gpg.passphrase</id>
  <passphrase>...</passphrase>
</server>
...
```

#### Maven configuration

Deployment to Maven Central is configured under the `release` profile and consists of the following plugins:

1. `maven-deploy-plugin` to create the checksum.
2. `maven-javadoc-plugin` to generate and attach javadoc.
3. `maven-source-plugin` to attach sources.
4. `maven-gpg-plugin` to sign using GPG (see [Github: Setup Java](https://github.com/actions/setup-java/blob/v3.11.0/docs/advanced-usage.md#Publishing-using-Apache-Maven)).
5. `central-publishing-maven-plugin` to deploy the generated artefacts.

The requirements are defined in [Sonatype: Publish](https://central.sonatype.org/register/central-portal/) with minor modifications as per [Github: Setup Java](https://github.com/actions/setup-java/blob/v3.11.0/docs/advanced-usage.md#Publishing-using-Apache-Maven) to ensure the setup works when using Github actions.

#### GPG keys

A full description on GPG key creation and distribution can be found at [GPG](https://central.sonatype.org/publish/requirements/gpg/). If a new (or personal) GPG key is required then it must not only be generated but distributed (i.e. to [keys.openpgp.org](https://keys.openpgp.org)). The aforementioned documentation does describe how to distribute keys in a generic fashion however experience is that distribution comes with some hooks and the generic approach may fail (for example one may need to verify ones email address). Its recommended to follow the distribution process as described explicitly for the given keyserver (i.e the [keys.openpgp.org usage guide](https://keys.openpgp.org/about/usage)).
