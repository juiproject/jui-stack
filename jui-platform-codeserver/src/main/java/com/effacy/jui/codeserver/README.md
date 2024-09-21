# Implementation

## Redirection mechanism

As a matter of course an application page will incorporate a JUI module by making reference to the bootstrap JS (where `{module}` is the module name):

```html
<script src="/{module}/{module}.nocache.js"></script>
```

This pulls in subsequent compilation assets (including the contents of any of the `public` directories of any of the modules used in the application) as needed and calls the associated module load method on an instance of its entry point class. Note that the compilation assets referred to are all retrieved from `/{module}/` (these are written to the expanded WAR by the compiler during the build process so a treated as static web resources).

For development we would prefer not to have to perform regular (aka time consuming) builds but rather incrementall compile changes as we go and see the effects as soon as we can. To achieve this the *code server* provides an alternative mechanism for delivering these resources via a separate web server running on a unique port (`9876` by default). The immediate challenge with this approach is how to direct the default mode (as described above) to use this alternative location. This is what we refer to as the *redirection mechanism*.

This mechanism is triggered by a specially configured bookmark that runs some JavaScript when clicked. This JavaScript creates a SCRIPT element in the body of the page that references `dev_mode_on.js` served by the *code server*.

This JavaScript will initiate a compilation as needed, otherwise presents a UI to allow the user to perform some actions (such as initiate a re-compilation and handle its response). In terms of redirection this script directs towards establishing a consistent state of compilation then places a special key in the browsers `sessionStorage` that the bootstrap JavaScript is able to detect and use to redirect appropriately to the *code server* (the specifics are deferred to the underlying linking mechanism but one approach employed is for the bootstrap to detect the key that matches its associated module then reload the bootstrap from the *code server*).

*This does mean that prior to being able to employ the code server there must be at least a bootstrap JS in place. This is most easily achieved by performing an initial project build and using that to kick the process off.*

Finally to disable the mechanism another specially configured bookmark is used that runs some JavaScript to clear the relevent module hook keys in the browsers `sessionStorage` and reloads the page.

## Server endpoints

Described below are each of the endpoint (URL patterns) that the code server is able to process. Some of these are used to present the code server UI (for human interaction) while others related to serving the compilation assets and supporting source maps (for in-browser debugging).

These are implemented in `CodeServerController` and make use of a suitable `ICompilationService` implementation for access to the underlying data. Note that the server does perform safety filtering of the resource references to prevent back-references and problematic content being injected (though less of a risk since the code server is designed to run locally during development).

### / (root)

The front page. Returns a populated `fontpage.html` file with instructions and links to the modules.

*This is only used as part of the Code Server UI.*

### /favicon.ico

The favourite icon. This serves up the `favicon.ico` file.

*This is only used as part of the Code Server UI.*

### /dev_mode_on.js

When the **Dev Mode On** is clicked a SCRIPT element is created and that is set to retrieve this JS. This is run and presents the in-page Code Server UI to initiate a re-compilation (and track its progress including the handling of any error state).

This serves up the `dev_mode_on.js` file as JavaScript and injects configuration data (i.e. list of module names) under the variable `window.__gwt_codeserver_config`.

### /progress

Obtain the progress on the current build job in progress. The response is a JSON message:

```json
{
    "status": "...",       /* The status, i.e. 'ok', 'compiling', 'error', etc */
    "jobId": "...",        /* Unique reference to the compilation job */
    "message": "...",      /* Any message supporting the status */
    "inputModule": "...",  /* The name of the module being compiled */
    "bindings": { ... }    /* Properties passed to the compilation request */
}
```

This is used by `dev_mode_on.js` when tracking the progress of a requested compilation. It expects the status to be `ok` or `compiling` (anything else is considered an error).

*This is only used as part of the Code Server UI.*

### /clean/{module}

To mark the module for a full compilation. The response is a JSON message:

```json
{
    "status": "...",       /* The status, one of 'ok' or 'error' */
    "message": "..."       /* Any message supporting the status */
}
```

*This is only used as part of the Code Server UI.*

### /recompile/{module}

Trigger a re-compilation. The response is a JSON message:

```json
{
    "status": "...",       /* The status, one of 'ok' or 'failed' */
    "moduleNames": [ ... ] /* The modules being compiled */
}
```

Noting that although this specifies a module to recompile the compiler may just compile everything so the module names of those under compilation is passed back.

This is used by `dev_mode_on.js` to initiate a re-compilation and passing `?clean=true` will couple with an initial clean of the cache (i.e. a full rather than incremental build).

*This is only used as part of the Code Server UI.*

### /recompile-requester/{module}

This is a JavaScript file that is pulled in to initiate a recompilation, track progress through and perform error handling (so it is similar to `dev_mode_on.js` but kicks off a recompilation immediately). This is called by the default bootstrap JavaScript which is returned when the code server has not yet processed a compilation.

### /log/{module}

The module complication log formatted in HTML.

*This is only used as part of the Code Server UI.*

### /sourcemaps/{module}/{resource}

Retrieves the symbol map asset specified by the (locally referenced) resource. This may include the source map itself (of the form `XXX_sourcemap.json`), a specific source file (i.e. `com/effacy/jui/core/client/IProcessable.java`) or a directory or file listing (the directory listing off the empty resource lists all packges otherwise a file listing is returned of classes when given a package reference ending in `/`).

Note that any source map files (of type `SourceMapDescriptor.Type.MAP`) are scrubbed for the string `$sourceroot_goes_here$` which is replaced with the URL prefix for the sourcemaps in the module (allowing for correct retrieval).

*The directory and file listings are only used as part of the Code Server UI and make use of the `directorylist.html` and `filelist.html` templates for formatting. The UI adds the `?html` query to the source requests and these are served up in nicely formatted HTML.*

### /{module}/

Module summary page. This displays a nicely formatted list of the assets available via `/{module}/{resource}` along with links to additional resources (such as the source code via the source maps and the compilation log). Formatting make use of the `modulepage.html` template.

*This is only used as part of the Code Server UI.*

### /{module}/{resource}

Retrieves the web asset specified by the resource (this URL pattern is the same pattern used in a deployment of the application in question where the assets would have been written into the WAR). These include the compiled (and linked) files generated by the compiler which includes files contained in the the `public` directory or any referenced module (note that this is an output of the compiler so these is no special logic involved here).

Headers are also assigned to prevent caching of assets.

*Note that generally compiled JS files will include the `//# sourceMappingURL=` comment to inform the browser where to find the associated source map.*

# GWT

1. The default bootstrap checks for the existence of `__gwtDevModeHook:{module}` in `sessionStorage` and if found will reload the bootstrap file from the location specified within (i.e. the code server).
2. On the first load since the codeserver started there will be no compiled assets (unless it is configured to precompile) to return (including no compiled bootstrap file). To amelorate this situation when the codeserver starts it creates an initial minimal *compilation* that consists of:
     1. Copying over the static assets from the `public` directories (in case they are used pre-compilation).
     2. Creates a stub bootstrap JS (given by `stub.nocache.js`) that will perform a request against `/recompile-requester/{module}` (see endpoints) which starts a compilation against `/recompile/{module}` and when complete reloads knowing that compilation assets are in place.
3. On reload (and any subsequent reload) the default bootstrap detects the session variable and reloads from the codeserver. The new bootstrap then proceeds as normal but using the codeserver as the source of assets.

# Directories

## Working directory

The  `workingDir` is the working directory created in `CodeServerConfig` for the code server (i.e. `/var/folders/sq/2bn4nbq53_q7px8p5fz8mm1h0000gn/T/jui-codeserver-7184822954126043554.tmp`). All generated content resides under this.

## Module compilation

Managed by the `OutboxDir` class and on a per-module basis. This creates a directory `{workingDir}/{module}` and under that compilation directories `compile-{n}` (one for each (re-)compilation that occurs in a session). When started if it finds compilation directories it deletes them.

## Compile directory

Used by `Recompiler` to determine when to drop the compilation assets. Returned by `OutboxDir` on demand. Consists of:

1. `war` the WAR directory which contains a directory named as the module and contains the link artefacts as well:
    1. `WEB-INF/deploy` directory for rpc maps (which are not used).
    2. Various JS files arising from linking.
    3. All the files under the `public` directory of each module.
2. `gen` that contains generated files during rebind.
3. `extras` that contains various extras including the source and symbol maps:
    1. `cssResources/` CSS maps?
    2. `rpcPolicyManifest/` not used (since we don't use GWT RPC)
    3. `soycReport/` reports?
    4. `symbolMaps/` symbol map and source map files.
4. `compile.log` the compile log.

Note that source files themselves are expected to be resolved from the classpath with the exception of the generated one which reside in the `gen` directory above.

# Configuration

During startup:

1. A GWT compiler is instantiated.
2. Iterates over each declared module. For each of these:
     a. An `OutboxDir` is created under the `workingDir` using the module name.
     b. A `Recompiler` is created passing the outbox directory.
     c. These are bundled into an `Outbox` and added to the `OutboxTable`.
3. A job running infrastructure is initialised with the GWT compiler.

# Compilation process

Occurs in `Recompiler`:

1. Call to `compile(Job)` passing a job instance to report against.
2. Create a new compilation directory (via `Outbox`).
3. Performs the compiliation (see below).
4. Store the location of the compilation for serving.
5. Report completion (logs).

The compilation:

1. Call to `doCompile()` passing the directory and job.
2. Create fresh set of compiler options including directory and module name.
3. Verify that only one permutation is configured when performing an incremental build. Fail otherwise.
4. Performs the compilation by calling `Compiler.compile(...)`.