# Replacement class

*THIS IS NOT OPERATIONAL UNTIL WE INCORPORATE THE GWT DEV PROJECT OR PACKAGE THE JAR ALTERNATIVELY*

These are classes that replace those in the GWT Dev library, either to modify behaviour or to gain access to otherwise locked away information.

1. `ModuleDef` adds `getInheritedModules()` to return a list of the module names that have been imported (used for logging).