# Running the JUI Code Server in GitHub Codespaces

This guide describes how to set up a JUI project to use the [JUI code server](docs/app_codeserver.md) when developing inside a GitHub Codespace. Once configured, the development cycle (run app, click **Dev Mode On**, recompile in browser) works the same way it does on a developer's local machine.

If you have not used the code server before, read [Code server](docs/app_codeserver.md) first. This document only covers the Codespaces-specific additions.

The instructions use the `jui-playground` module as the running example. Because the playground is a JUI project like any other, the same steps apply to any JUI project that is built and run with Maven.

## Why Codespaces is different

On a developer's local machine the application and the code server both bind to `localhost`. The browser sees both at the same hostname and reaches them by port (e.g. `localhost:8080` for the app and `localhost:9876` for the code server).

In a Codespace those ports are not directly reachable. The Codespaces proxy forwards each listening port to its own HTTPS subdomain, so:

| Local | Codespaces |
|-------|------------|
| `http://localhost:8080/` | `https://<codespace>-8080.<domain>/` |
| `http://localhost:9876/` | `https://<codespace>-9876.<domain>/` |

where `<codespace>` is `$CODESPACE_NAME` and `<domain>` is `$GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN` (typically `app.github.dev`). Both URLs are HTTPS, both omit the original port, and the application and code server are on different origins.

The code server bakes self-referencing URLs into two outputs:

1. The `includeSourceMapUrl` stamped into compiled JS (used by the browser to fetch source maps).
2. The redirect script that replaces the original `.nocache.js` when **Dev Mode On** is active.

These URLs must point at the public Codespaces address of the code server, not at `localhost:9876`. To support this the code server accepts a `-publicUrl` argument naming its externally-visible URL, and the `jui-maven-plugin` auto-derives this value when it detects it is running in a Codespace.

## Setup

### 1. Forward the ports in `devcontainer.json`

In the `.devcontainer/devcontainer.json` of the project (create one if it does not exist), declare both the application port and the code server port. The code server port should be marked `public` so cross-origin script loads from the application page are not blocked by Codespaces' authentication cookie:

```json
{
  "forwardPorts": [8080, 9876],
  "portsAttributes": {
    "8080": {
      "label": "App",
      "visibility": "public"
    },
    "9876": {
      "label": "JUI Code Server",
      "visibility": "public"
    }
  }
}
```

Both ports are set to `public` because the application page (served from one origin) loads scripts and source maps from the code server origin. Private ports require an auth cookie that browsers will not send across origins in this configuration.

> If you prefer to keep your application port private, only the code server port (`9876`) must be public. The application port can remain private as long as you reach it through the regular Codespaces port-forwarding flow.

If you are adding this to an existing Codespace, rebuild the container (**Codespaces: Rebuild Container** from the command palette) so the new port-forwarding rules take effect.

### 2. Configure the `codeserver` Maven profile

The Maven plugin auto-derives the public URL whenever `CODESPACE_NAME` and `GITHUB_CODESPACES_PORT_FORWARDING_DOMAIN` are both present in the environment, which they always are inside a Codespace. **No configuration change is needed** for the standard `codeserver` profile to work in a Codespace — just follow the normal setup described in [Code server / Getting started](docs/app_codeserver.md#getting-started).

If you want to be explicit, or if you are running behind a different proxy and need to override the auto-derivation, add a `<publicUrl>` element:

```xml
<plugin>
  <groupId>com.effacy.jui</groupId>
  <artifactId>jui-maven-plugin</artifactId>
  <version>${release}</version>
  <configuration>
    <module>com.effacy.jui.playground.PlaygroundApp</module>
    <publicUrl>https://my-codespace-9876.app.github.dev</publicUrl>
    <!-- the rest of your configuration -->
  </configuration>
  ...
</plugin>
```

To suppress auto-derivation entirely (e.g. for testing the non-Codespaces flow from within a Codespace) pass an empty string or `-`:

```bash
mvn -Pcodeserver -Djui.publicUrl=-
```

### 3. Build and start the application

Run a full Maven build, then start the application as you would normally:

```bash
mvn install -DskipTests
mvn -pl jui-playground spring-boot:run
```

The first time the `8080` port is bound Codespaces will forward it; the **Ports** panel in VS Code will show a row for it. Open it in your browser — that gives you the application URL (`https://<codespace>-8080.<domain>/playground` for the playground).

### 4. Start the code server

In a second terminal:

```bash
mvn -Pcodeserver
```

In the startup log you will see a line like:

```
[INFO] Code server public URL: https://<codespace>-9876.app.github.dev
```

This confirms the plugin has detected the Codespace environment and passed `-publicUrl` to the code server. Once the **Ports** panel shows `9876`, open that URL in your browser. You should see the regular JUI Code Server front page.

### 5. Install the bookmarklets (first time only)

The front-page bookmarklets (**Dev Mode On**, **Dev Mode Off**) embed the URL of whichever page they were dragged from. Because you opened the code server front page via its Codespaces-forwarded HTTPS URL, the bookmarklets you drag to your bookmark bar will correctly target that URL. No manual editing required.

> If the Codespace is rebuilt and the codespace name changes, the embedded URL goes stale. Open the code server front page in the new Codespace and re-drag the bookmarklets to refresh them.

### 6. Recompile

Navigate to the application page (`https://<codespace>-8080.<domain>/playground`) and click **Dev Mode On**. From here the workflow is identical to the local-machine flow described in [Code server / First time running](docs/app_codeserver.md#first-time-running): pick a module, click **Compile**, and the page reloads with the freshly compiled code served from the code server origin.

## How it works under the hood

The `-publicUrl` argument is consumed by `CodeServer` and is used in three places where the code server would otherwise hardcode `http://<host>:<port>`:

1. `includeSourceMapUrl` — the absolute source-map URL baked into compiled JS so the browser can fetch maps.
2. The `sourceRoot` field written into served `.map` files, so the browser can resolve Java source paths relative to the code server origin.
3. The `serverUrl` variable in the dev-mode redirection stub (`stub.nocache.js`) that is injected into the page when **Dev Mode On** is active.

When `-publicUrl` is unset the code server falls back to its previous behaviour (using the configured `-bindAddress`/`-port`, or values from the inbound request). This means everything described here is backward-compatible: nothing changes for developers running on a local machine.

## Troubleshooting

### `Dev Mode On` produces a "Couldn't load … from Super Dev Mode server" dialog

The browser cannot reach the code server origin. Common causes:

- The code server port (`9876`) is forwarded as **private** rather than **public**. Set it to public in the **Ports** panel or in `devcontainer.json` (see step 1).
- The code server is not actually running. Check the terminal output for `mvn -Pcodeserver` and make sure the port appears in the **Ports** panel.

### Source maps don't resolve in the browser dev tools

- Confirm the **Sources** tab shows a node named after the codespace hostname (e.g. `<codespace>-9876.app.github.dev`). If it is `127.0.0.1:9876` then the source map base URL was not rewritten — verify the code server log shows `Code server public URL:` at startup. If not, the plugin did not pick up the Codespaces environment; check that `echo $CODESPACE_NAME` returns a value in the terminal you launched Maven from.
- A stale cache can also cause this. Open the code server front page and click **Clear server cache**, then recompile.

### The Codespace was rebuilt and bookmarklets no longer work

The codespace hostname changes when a Codespace is recreated, so any bookmarklets you dragged previously now point at the old hostname. Open the code server front page in the current Codespace and re-drag both bookmarklets to update them.

### `mvn install` fails with `Compiler exited with status 143`

Exit code 143 (SIGTERM) means the cgroup memory limit killed the GWT compile JVM. A standard 2-core / 8 GB Codespace has roughly 2.5–3 GB free once VS Code and the Java language server are running, which is not enough for a `-Xmx4g` GWT compile.

The playground's `compile` phase has been tuned down to `-Xmx2g` for exactly this reason and that comfortably fits. If you have configured your own JUI project with a larger heap, lower it:

```xml
<plugin>
  <groupId>com.effacy.jui</groupId>
  <artifactId>jui-maven-plugin</artifactId>
  <executions>
    <execution>
      <phase>compile</phase>
      <goals><goal>compile</goal></goals>
      <configuration>
        <module>my.app.Module</module>
        <jvmArgs>-Xmx2g,-Xss1024k</jvmArgs>
        ...
      </configuration>
    </execution>
  </executions>
</plugin>
```

If `-Xmx2g` still gets killed, either drop further (`-Xmx1536m`) or move to a 4-core / 16 GB Codespace machine type via **Codespaces** → **Change machine type**.

### I want to override the auto-derived URL

Pass the desired URL via `<publicUrl>` in the plugin configuration, or `-Djui.publicUrl=https://...` on the command line. To suppress auto-derivation entirely (and fall back to `http://<bindAddress>:<port>`), pass `-Djui.publicUrl=-`.
