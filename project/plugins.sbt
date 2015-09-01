scalacOptions += "-deprecation"

// so we can use native2ascii on Linux.
unmanagedJars in Compile += {
  // use JAVA_HOME not the java.home
  // system property because the latter may have "/jre" tacked onto it.
  val home = javaHome.value.getOrElse(file(System.getenv("JAVA_HOME")))
  home / "lib" / "tools.jar"
}

libraryDependencies +=
  "de.jflex" % "jflex" % "1.4.3"

libraryDependencies +=
  "classycle" % "classycle" % "1.4.1" from
    "http://ccl.northwestern.edu/devel/classycle-1.4.1.jar"

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.6.0")

addSbtPlugin("org.ensime" % "ensime-sbt" % "0.1.7")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.5.1")

resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
    url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
        Resolver.ivyStylePatterns)

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.5")

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.1.2")

// prevents noise from bintray stuff
libraryDependencies +=
  "org.slf4j" % "slf4j-nop" % "1.6.0"

resolvers += Resolver.url(
  "publish-versioned-plugin-releases",
    url("http://dl.bintray.com/content/netlogo/publish-versioned"))(
        Resolver.ivyStylePatterns)

addSbtPlugin("org.nlogo" % "publish-versioned-plugin" % "2.0")
