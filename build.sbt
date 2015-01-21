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
  scalaVersion := "2.11.5",
  javacOptions ++=
    "-g -deprecation -encoding us-ascii -Werror -Xlint:all -Xlint:-serial -Xlint:-fallthrough -Xlint:-path -source 1.8 -target 1.8"
    .split(" ").toSeq,
  javaSource in Compile := baseDirectory.value / "src" / "main",
  javaSource in Test := baseDirectory.value / "src" / "test"
)

// JMock isn't declaring its dependency upon Hamcrest in 2.6.0, but hopefully
// it will fix this in future versions, and then we can get rid of the Hamcrest
// dependency --JAB (10/2/14)
lazy val jvmDeps = Seq(
  libraryDependencies ++= Seq(
  "org.jmock" % "jmock" % "2.6.0" % "test",
  "org.jmock" % "jmock-legacy" % "2.6.0" % "test",
  "org.jmock" % "jmock-junit4" % "2.6.0" % "test",
  "org.hamcrest" % "hamcrest-core" % "1.3" % "test",
  "org.scalacheck" %% "scalacheck" % "1.12.0" % "test",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.reflections" % "reflections" % "0.9.9" % "test",
  "org.slf4j" % "slf4j-nop" % "1.7.7" % "test"
  ),
  libraryDependencies ++= Seq(
    "org.ow2.asm" % "asm-all" % "5.0.3",
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.3"
  ),
  resourceDirectory in Compile := baseDirectory.value / "resources" / "main",
  resourceDirectory in Test := baseDirectory.value / "resources" / "test",
publishArtifact in Test := true
)


lazy val publicationSettings =
  bintrayPublishSettings ++
  PublishVersioned.settings ++
  Seq(
    bintray.Keys.repository in bintray.Keys.bintray := "NetLogoHeadlessMaven",
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
    val version = netlogoVersion.value
    Seq("-encoding", "us-ascii") ++
    Opts.doc.title("NetLogo") ++
    Opts.doc.version(version) ++
    Opts.doc.sourceUrl("https://github.com/NetLogo/NetLogo/blob/" +
      version + "/src/main€{FILE_PATH}.scala")
  },
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

lazy val jvmBuild = (project in file ("jvm")).
  configs(FastMediumSlow.configs: _*).
  settings(commonSettings: _*).
  settings(jvmSettings: _*).
  settings(jvmDeps: _*).
  settings(FastMediumSlow.settings: _*).
  settings(docOptions: _*).
  settings(Depend.settings: _*).
  settings(publicationSettings: _*).
  settings(
    mainClass in Compile := Some("org.nlogo.headless.Main"),
    // show test failures again at end, after all tests complete.
    // T gives truncated stack traces; change to G if you need full.
    testOptions in Test += Tests.Argument("-oT"),
    onLoadMessage := "",
    name := "NetLogoHeadless",
    // Used by the publish-versioned plugin
    isSnapshot := true,
    version := "5.2.0"
  )


///
/// building
///

///
/// packaging and publishing
///


///
/// Scaladoc
///

///
/// plugins
///
