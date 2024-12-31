# Release

## Maven Central

### Local workstation

The profile `release` has been configured to perform a deployment to Maven Central via the command:

```bash
VERSION=<version>
mvn -Drevision=$VERSION -Prelease deploy
```

This will (by default) create a component (assuming all validations pass) accessible in the [Sonatype Portal](https://central.sonatype.com) which can then be manually deployed.

#### Preparation

To perform a release from your local environment requires some prepartion:

1. Create and make available a GPG key (see [GPG](https://central.sonatype.org/publish/requirements/gpg/)).
2. Register the password for the GPG key in your `settings.xml` (see note below).
3. Register the Sonatype token and password in your `settings.xml` (see [Publish to Central](https://central.sonatype.org/publish/publish-portal-maven/) for details; in our case the server is configured under the name `central`).

In reference to (2) above, the following can be used to store the GPG passphrase in your `settings.xml`:

```xml
...
<server>
  <id>gpg.passphrase</id>
  <passphrase>...</passphrase>
</server>
...
```

### Github actions

See the `release.yml` script.