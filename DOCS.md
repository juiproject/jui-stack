# Documentation

Documentation is located in the [docs](./docs/) directory and is based on [docsify](https://docsify.js.org/). It is deployed on a check-in to the `main` branch (using GitHub pages).

## Generation of a sitemap

This can be done by installing `docsify-sitemap`:

```bash
npm install -g docsify-sitemap
```

The running the following from the project root:

```bash
docsify-sitemap local -u https://juiproject.github.io/jui-stack/ -b docs -f ./docs/sitemap.xml -i
```

A subsequent step is to integrate this into the build process.
