import sbt._
import Keys._

object FastMediumSlow {
  lazy val fast   = taskKey[Unit]("fast tests")
  lazy val medium = taskKey[Unit]("medium tests")
  lazy val slow   = taskKey[Unit]("slow tests")
  lazy val language = taskKey[Unit]("language tests")
  lazy val crawl = taskKey[Unit]("extremely slow tests")

  lazy val settings = Seq(
    (fast in Test) := {
      (testOnly in Test).toTask(" -- -l org.nlogo.util.SlowTestTag -l org.nlogo.util.LanguageTestTag").value
    },
    (medium in Test) := {
      (testOnly in Test).toTask(" -- -l org.nlogo.util.SlowTestTag").value
    },
    (language in Test) := {
      (testOnly in Test).toTask(" -- -n org.nlogo.util.LanguageTestTag").value
    },
    (crawl in Test) := {
      (testOnly in Test).toTask(" -- -n org.nlogo.util.SlowTestTag").value
    },
    (slow in Test) := {
      (testOnly in Test).toTask(" -- -n org.nlogo.util.LanguageTestTag -n org.nlogo.util.SlowTestTag").value
    })
}
