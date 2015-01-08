val root = project in file (".") configs(FastMediumSlow.configs: _*)

scalaVersion := "2.11.5"

mainClass in Compile := Some("org.nlogo.headless.Main")

// show test failures again at end, after all tests complete.
// T gives truncated stack traces; change to G if you need full.
testOptions in Test += Tests.Argument("-oT")

onLoadMessage := ""

ivyLoggingLevel := UpdateLogging.Quiet

logBuffered in testOnly in Test := false

name := "NetLogoHeadless"

organization := "org.nlogo"

licenses += ("GPL-2.0", url("http://opensource.org/licenses/GPL-2.0"))

// Used by the publish-versioned plugin
isSnapshot := true

version := "5.2.0"

///
/// building
///

scalacOptions ++=
  "-deprecation -unchecked -feature -Xcheckinit -encoding us-ascii -target:jvm-1.7 -Xlint -Xfatal-warnings"
  .split(" ").toSeq

javacOptions ++=
  "-g -deprecation -encoding us-ascii -Werror -Xlint:all -Xlint:-serial -Xlint:-fallthrough -Xlint:-path -source 1.8 -target 1.8"
  .split(" ").toSeq

libraryDependencies ++= Seq(
  "org.ow2.asm" % "asm-all" % "5.0.3",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.2"
)

// JMock isn't declaring its dependency upon Hamcrest in 2.6.0, but hopefully
// it will fix this in future versions, and then we can get rid of the Hamcrest
// dependency --JAB (10/2/14)
libraryDependencies ++= Seq(
  "org.jmock" % "jmock" % "2.6.0" % "test",
  "org.jmock" % "jmock-legacy" % "2.6.0" % "test",
  "org.jmock" % "jmock-junit4" % "2.6.0" % "test",
  "org.hamcrest" % "hamcrest-core" % "1.3" % "test",
  "org.scalacheck" %% "scalacheck" % "1.12.0" % "test",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.reflections" % "reflections" % "0.9.9" % "test",
  "org.slf4j" % "slf4j-nop" % "1.7.7" % "test"
)

scalaSource in Compile := baseDirectory.value / "src" / "main"

scalaSource in Test := baseDirectory.value / "src" / "test"

javaSource in Compile := baseDirectory.value / "src" / "main"

javaSource in Test := baseDirectory.value / "src" / "test"

resourceDirectory in Compile := baseDirectory.value / "resources" / "main"

resourceDirectory in Test := baseDirectory.value / "resources" / "test"

///
/// packaging and publishing
///

// don't cross-build for different Scala versions
crossPaths := false

publishArtifact in Test := true

///
/// Scaladoc
///

val netlogoVersion = taskKey[String]("from api.Version")

netlogoVersion := {
  (testLoader in Test).value
    .loadClass("org.nlogo.api.Version")
    .getMethod("version")
    .invoke(null).asInstanceOf[String]
    .stripPrefix("NetLogo ")
}

scalacOptions in (Compile, doc) ++= {
  val version = netlogoVersion.value
  Seq("-encoding", "us-ascii") ++
    Opts.doc.title("NetLogo") ++
    Opts.doc.version(version) ++
    Opts.doc.sourceUrl("https://github.com/NetLogo/NetLogo/blob/" +
                       version + "/src/main€{FILE_PATH}.scala")
}

// compensate for issues.scala-lang.org/browse/SI-5388
doc in Compile := {
  val path = (doc in Compile).value
  for (file <- Process(Seq("find", path.toString, "-name", "*.html")).lines)
    IO.write(
      new File(file),
      IO.read(new File(file)).replaceAll("\\.java\\.scala", ".java"))
  path
}

///
/// plugins
///

org.scalastyle.sbt.ScalastylePlugin.Settings

///
/// get stuff from project/*.scala
///

FastMediumSlow.settings

bintrayPublishSettings

PublishVersioned.settings

bintray.Keys.repository in bintray.Keys.bintray := "NetLogoHeadlessMaven"

bintray.Keys.bintrayOrganization in bintray.Keys.bintray := Some("netlogo")

Depend.settings
