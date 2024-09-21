# Support

## Serving documentation locally

Documentation makes use of [Docsify](https://docsify.js.org/). This is bundled with `jui-playground` and its associated executable JAR but can also be spun up locally with a simple HTTP server (which is especially useful when you don't want to run up the playground).

The script `docs` achieves this but does require **python3** to be installed.

To launch run:

```bash
docs
```

and open http://localhost:3000/index.html#/.

Note that if you are ammending the documentation this is a good approach to view it as it will be seen. Sometimes changes appear not be be reflected on a refresh but this is often due to caching on the browser. Here you should try to perform a hard reload (for Chrome you can open the developer tools which activates the right click option on the refresh button).

## License

The script `license.py` is used to apply a license header to each Java source file. To use run the script from this directory (the example applies licenses to, in this case, the files under the `src` directory of the **jui-test** project):

```bash
python3 license.py ../jui-test/src
```

You can specify a different licence file (which consists of a license comment) as the only (optional) argument `-l`.