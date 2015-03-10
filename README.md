## NetLogo-Headless

The NetLogo Headless project is broken into a number of modules, explained in the sections below.
The end result of these modules is two artifacts `NetLogoHeadless.jar` and `netlogo-parser_sjs.jar`.
`NetLogoHeadless.jar` bundles all the code for NetLogoHeadless.
`netlogo-parser_sjs.jar` is a scalajs jar that allows users to use NetLogo from scalajs.

Projects:
* [macros](#macros)
* [jvmBuild](#jvm-build)
* [parserJs](#parser-js)
* [parserJvm](#parser-jvm)
* [sharedResources](#shared-resources)

The dependencies are as follows

```
  sharedResources
    ^          ^
    |          |
parserJvm    macros
    |          |
jvmBuild    parserJs
```


SBT works as it normally does.
You can prefix tasks with the project name, like:
```sbt
> parserJvm/compile
[info] Updating {file:~/NetLogo-Headless/}parserJvm...
```
or you can switch "into" a project and use the commands without prefix:
```sbt
> project parserJvm
> compile
[info] Updating {file:~/NetLogo-Headless/}parserJvm...
```

### Macros

This project provides a number of features for parserJs that would otherwise be unavailable.
These include resource-loading and character detection.
Please note that this project is not needed by parser-js at runtime, and runtime dependencies between this project and parser-js are undesirable.

### JVM Build

This project bundles all of NetLogoHeadless into one jar.
It depends on (and should include files from) sharedResources and parserJvm.

### Parser JS

This project bundles the parser files into a scala.js jar.
It depends on macros for compile-time values of files that will not be available to it at runtime.
It merges sources from `parser-core` (also used by Parser JVM) with the sources in `parser-js`.

### Parser JVM

This project bundles the parser files into a scala jar.
It could be used anywhere that a NetLogo parser (without code generation) is required.
It merges source from `parser-core` (also used by Parser JS) with the sources in `parser-jvm`.

### Shared Resources

This project contains resources that are shared among all projects.
These are primarily static text files, but this project also handles I18N normalization and bundling.
