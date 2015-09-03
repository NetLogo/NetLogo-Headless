import org.scalajs.sbtplugin.cross.{ CrossProject, CrossType }
import org.scalastyle.sbt.ScalastylePlugin.scalastyleTarget

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
  scalaVersion := "2.11.7",
  // don't cross-build for different Scala versions
  crossPaths := false,
  scalastyleTarget in Compile := {
    (target in root).value / s"scalastyle-result-${name.value}.xml"
  }
)

lazy val jvmSettings = Seq(
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
  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oT"),
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "2.2.1" % "test",
    // Using a 1.12.2 until fix is available for https://github.com/rickynils/scalacheck/issues/173
    "org.scalacheck" %% "scalacheck" % "1.12.2" % "test"
  )
)

lazy val testSettings = scalatestSettings ++ Seq(
  libraryDependencies ++= Seq(
    "org.jmock" % "jmock" % "2.8.1" % "test",
    "org.jmock" % "jmock-legacy" % "2.8.1" % "test",
    "org.jmock" % "jmock-junit4" % "2.8.1" % "test",
    "org.reflections" % "reflections" % "0.9.10" % "test",
    "org.slf4j" % "slf4j-nop" % "1.7.12" % "test"
  )
)

lazy val publicationSettings =
  bintrayPublishSettings ++
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
  sources in (Compile, doc) ++= (sources in (parserJVM, Compile)).value,
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
  name := "parser",
  version := "0.0.1",
  unmanagedSourceDirectories in Compile += file(".").getAbsoluteFile / "parser-core" / "src" / "main"
)

lazy val root = project.in(file("."))

lazy val sharedResources = (project in file ("shared")).
  settings(commonSettings: _*).
  settings(unmanagedResourceDirectories in Compile += baseDirectory.value / "resources" / "main")

// this project exists only to allow parser-core to be scalastyled
lazy val parserCore = (project in file("parser-core")).
  settings(commonSettings: _*).
  settings(skip in (Compile, compile) := true)

lazy val macros = (project in file("macro")).
  dependsOn(sharedResources).
  settings(commonSettings: _*).
  settings(libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value)

lazy val parser = CrossProject("parser", file("."),
  new CrossType {
    override def projectDir(crossBase: File, projectType: String): File =
      crossBase / s"parser-$projectType"
    override def sharedSrcDir(projectBase: File, conf: String): Option[File] =
      Some(projectBase / "parser-core" / "src" / conf)
  }).
  settings(commonSettings: _*).
  settings(parserSettings: _*).
  jsConfigure(_.dependsOn(sharedResources % "compile-internal->compile")).
  jsConfigure(_.dependsOn(macros % "compile-internal->compile;test-internal->compile")).
  jsSettings(publicationSettings: _*).
  jsSettings(
      name := "parser-js",
      ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) },
      resolvers += Resolver.sonatypeRepo("releases"),
      testFrameworks += new TestFramework("utest.runner.Framework"),
      libraryDependencies ++= {
      import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport.toScalaJSGroupID
        Seq(
          "org.scala-js" %%%! "scala-parser-combinators" % "1.0.2",
          "com.lihaoyi"  %%%! "utest" % "0.3.1"
      )}).
  jvmConfigure(_.dependsOn(sharedResources)).
  jvmSettings(jvmSettings: _*).
  jvmSettings(scalatestSettings: _*).
  jvmSettings(
      mappings in (Compile, packageBin) ++= mappings.in(sharedResources, Compile, packageBin).value,
      mappings in (Compile, packageSrc) ++= mappings.in(sharedResources, Compile, packageSrc).value,
      libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4"
    )

lazy val parserJVM = parser.jvm
lazy val parserJS  = parser.js

lazy val jvmBuild = (project in file ("jvm")).
  dependsOn(parserJVM % "test-internal->test;compile-internal->compile").
  settings(FastMediumSlow.settings: _*).
  settings(commonSettings: _*).
  settings(jvmSettings: _*).
  settings(testSettings: _*).
  settings(FastMediumSlow.settings: _*).
  settings(docOptions: _*).
  settings(Depend.settings: _*).
  settings(publicationSettings: _*).
  settings(
    version := "5.2.0",
    isSnapshot := true,
    libraryDependencies += "org.ow2.asm" % "asm-all" % "5.0.4",
    (fullClasspath in Runtime) ++= (fullClasspath in Runtime in parserJVM).value,
    mainClass in Compile := Some("org.nlogo.headless.Main"),
    onLoadMessage := "",
    name := "NetLogoHeadless",
    // Used by the publish-versioned plugin
    isSnapshot := true,
    version := "5.2.0",
    mappings in (Compile, packageBin) ++= mappings.in(parserJVM, Compile, packageBin).value,
    mappings in (Compile, packageSrc) ++= mappings.in(parserJVM, Compile, packageSrc).value,
    unmanagedResourceDirectories in Compile += baseDirectory.value / "resources" / "main",
    unmanagedResourceDirectories in Test += baseDirectory.value / "resources" / "test"
  )
