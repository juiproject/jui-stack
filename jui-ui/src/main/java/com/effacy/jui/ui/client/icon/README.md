# Overview

Bindings to make use of the [FontAwesome](https://github.com/FortAwesome/Font-Awesome) icon library (the free version). See the link for license attribution.

**Note** the free version does not include all versions of each icon, in particular the solid versions have a greater representation than the regular ones. As such the default font is the solid one.

# Updating

You should regularly update the icon pack to represent the lastest version:

1. Download the latest (free) version from [FontAwesome](https://github.com/FortAwesome/Font-Awesome).
2. Extract the package and copy the `woff2` files from the `webfonts` directory to the resource package `com.effacy.jui.ui.client.icon` of this project.
3. Copy across the `XXX.css` files, that match the font files, to the resource package `com.effacy.jui.ui.client.icon` and for each remove the `font-face` block at the top (these are built in this class to correctly reference the font). If there are any new sizings or fonts then create new entries in the `FontCSS` class (using the `CssResource.Font` annotation). You may need to rename these to match what is there (i.e. lately the `fa-` prefix has been dropped).
4. Copy over the `fontawesome.css` file.
5. Update the version number.
6. Run `FontAwesome.FontAwesomeGenerator` to generate an output of methods and replace those in `FontAwesome`.