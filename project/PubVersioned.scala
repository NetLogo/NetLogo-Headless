import sbt._, sbt.Def, Keys.{ `package` => packageKey, _ }

object PubVersioned {
  val settings = Seq(version <<= Def.setting {
    if (! isSnapshot.value) {
      version.value
    } else {
      val isClean  = Process("git diff --quiet --exit-code HEAD").! == 0
      val dirtyStr = if (isClean) "" else "-dirty"
      val sha      = Process("git rev-parse HEAD").lines.head take 7
      s"${version.value}-$sha$dirtyStr"
    }
  })
}

