lazy val netlogoVersion = taskKey[String]("from api.Version")

lazy val commonSettings = Seq(
  organization := "org.nlogo",
  licenses += ("GPL-2.0", url("http://opensource.org/licenses/GPL-2.0")),
  scalacOptions ++=
    "-deprecation -unchecked -feature -Xcheckinit -encoding us-ascii -target:jvm-1.7 -Xlint -Xfatal-warnings"
      .split(" ").toSeq,
  scalaSource in Compile := baseDirectory.value / "src" / "main",
  scalaSource in Test := baseDirectory.value / "src" / "test",
  ivyLoggingLevel := UpdateLogging.Quiet,
  logBuffered in testOnly in Test := false,
  onLoadMessage := "",
  // don't cross-build for different Scala versions
  crossPaths := false
)

lazy val jvmSettings = Seq(
  scalaVersion := "2.11.6",
  javacOptions ++=
    "-g -deprecation -encoding us-ascii -Werror -Xlint:all -Xlint:-serial -Xlint:-fallthrough -Xlint:-path -source 1.8 -target 1.8"
    .split(" ").toSeq,
  javaSource in Compile := baseDirectory.value / "src" / "main",
  javaSource in Test := baseDirectory.value / "src" / "test",
  publishArtifact in Test := true
)

lazy val scalatestSettings = Seq(
  // show test failures again at end, after all tests complete.
  // T gives truncated stack traces; change to G if you need full.
  testOptions in Test += Tests.Argument("-oT"),
  libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"
)

// JMock isn't declaring its dependency upon Hamcrest in 2.6.0, but hopefully
// it will fix this in future versions, and then we can get rid of the Hamcrest
// dependency --JAB (10/2/14)
<<<<<<< HEAD
libraryDependencies ++= Seq(
  "org.jmock" % "jmock" % "2.6.0" % "test",
  "org.jmock" % "jmock-legacy" % "2.6.0" % "test",
  "org.jmock" % "jmock-junit4" % "2.6.0" % "test",
  "org.hamcrest" % "hamcrest-core" % "1.3" % "test",
  "org.scalacheck" %% "scalacheck" % "1.12.0" % "test",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.reflections" % "reflections" % "0.9.9" % "test",
  "org.slf4j" % "slf4j-nop" % "1.7.10" % "test"
=======
lazy val testSettings = scalatestSettings ++ Seq(
  libraryDependencies ++= Seq(
    "org.jmock" % "jmock" % "2.6.0" % "test",
    "org.jmock" % "jmock-legacy" % "2.6.0" % "test",
    "org.jmock" % "jmock-junit4" % "2.6.0" % "test",
    "org.hamcrest" % "hamcrest-core" % "1.3" % "test",
    "org.scalacheck" %% "scalacheck" % "1.12.0" % "test",
    "org.reflections" % "reflections" % "0.9.9" % "test",
    "org.slf4j" % "slf4j-nop" % "1.7.9" % "test"
  )
>>>>>>> 53e3154... build.sbt builds jvmBuild, shared, macros, parserJvm, parserJs
)

lazy val publicationSettings =
  bintrayPublishSettings ++
  PublishVersioned.settings ++
  Seq(
    bintray.Keys.repository in bintray.Keys.bintray := "NetLogoHeadless",
    bintray.Keys.bintrayOrganization in bintray.Keys.bintray := Some("netlogo")
  )

lazy val docOptions = Seq(
  netlogoVersion := {
    (testLoader in Test).value
      .loadClass("org.nlogo.api.Version")
      .getMethod("version")
      .invoke(null).asInstanceOf[String]
      .stripPrefix("NetLogo ")
  },
  scalacOptions in (Compile, doc) ++= {
    Seq("-encoding", "us-ascii") ++
    Opts.doc.title("NetLogo") ++
    Opts.doc.version(version.value) ++
    Opts.doc.sourceUrl("https://github.com/NetLogo/NetLogo/blob/" +
      version.value + "/src/main€{FILE_PATH}.scala")
  },
  sources in (Compile, doc) ++= (sources in (parserJvm, Compile)).value,
  // compensate for issues.scala-lang.org/browse/SI-5388
  doc in Compile := {
    val path = (doc in Compile).value
    for (file <- Process(Seq("find", path.toString, "-name", "*.html")).lines)
      IO.write(
        new File(file),
        IO.read(new File(file)).replaceAll("\\.java\\.scala", ".java"))
    path
  }
)

lazy val parserSettings: Seq[Setting[_]] = Seq(
  isSnapshot := true,
  name := "NetLogo-Parser",
  version := "0.0.1",
  unmanagedSourceDirectories in Compile += file(".").getAbsoluteFile / "parser-core" / "src" / "main"
)

lazy val root = project.in(file("."))

lazy val sharedResources = (project in file ("shared")).
  settings(commonSettings: _*).
  settings(unmanagedResourceDirectories in Compile += baseDirectory.value / "resources" / "main")

// this project exists only to allow parser-core to be scalastyled
lazy val parserCore = (project in file("parser-core")).
  settings(commonSettings: _*)

lazy val parserJvm = (project in file ("parser-jvm")).
  dependsOn(sharedResources).
  settings(commonSettings: _*).
  settings(parserSettings: _*).
  settings(jvmSettings: _*).
  settings(scalatestSettings: _*).
  settings(
    mappings in (Compile, packageBin) ++= mappings.in(sharedResources, Compile, packageBin).value,
    mappings in (Compile, packageSrc) ++= mappings.in(sharedResources, Compile, packageSrc).value,
    libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.3"
  )

lazy val macros = (project in file("macro")).
  dependsOn(sharedResources).
  settings(commonSettings: _*).
  settings(
    scalaVersion := "2.11.2",
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value
  )

lazy val parserJs = (project in file ("parser-js")).
  dependsOn(sharedResources % "compile-internal->compile").
  dependsOn(macros % "compile-internal->compile;test-internal->compile").
  settings(commonSettings: _*).
  settings(parserSettings: _*).
  settings(scalaJSSettings: _*).
  settings(PubVersioned.settings: _*).
  settings(publicationSettings: _*).
  settings(utest.jsrunner.Plugin.utestJsSettings: _*).
  settings(
    scalaVersion := "2.11.2",
    testFrameworks += new TestFramework("utest.jsrunner.JsFramework"),
    libraryDependencies += "org.scalajs" %%% "scala-parser-combinators" % "1.0.2"
  )


lazy val jvmBuild = (project in file ("jvm")).
  dependsOn(parserJvm % "test-internal->test;compile-internal->compile").
  configs(FastMediumSlow.configs: _*).
  settings(commonSettings: _*).
  settings(jvmSettings: _*).
  settings(testSettings: _*).
  settings(FastMediumSlow.settings: _*).
  settings(docOptions: _*).
  settings(Depend.settings: _*).
  settings(publicationSettings: _*).
  settings(
    libraryDependencies += "org.ow2.asm" % "asm-all" % "5.0.3",
    mainClass in Compile := Some("org.nlogo.headless.Main"),
    onLoadMessage := "",
    name := "NetLogoHeadless",
    // Used by the publish-versioned plugin
    isSnapshot := true,
    version := "5.2.0",
    mappings in (Compile, packageBin) ++= mappings.in(parserJvm, Compile, packageBin).value,
    mappings in (Compile, packageSrc) ++= mappings.in(parserJvm, Compile, packageSrc).value,
    unmanagedResourceDirectories in Compile += baseDirectory.value / "resources" / "main",
    unmanagedResourceDirectories in Test += baseDirectory.value / "resources" / "test"
