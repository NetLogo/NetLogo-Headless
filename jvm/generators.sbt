///
/// JFlex
///

sourceGenerators in Compile += Def.task[Seq[File]] {
  val src = (sourceManaged in Compile).value
  val base = baseDirectory.value
  val s = streams.value
  val cache =
    FileFunction.cached(s.cacheDirectory / "lexers", inStyle = FilesInfo.hash, outStyle = FilesInfo.hash) {
      in: Set[File] =>
        Set(flex(s.log.info(_), base, src, "ImportLexer"))
    }
  cache(Set(base / "project" / "flex" / "warning.txt",
            base / "project" / "flex" / "ImportLexer.flex")).toSeq
}.taskValue

def flex(log: String => Unit, base: File, dir: File, kind: String): File = {
  val project = base / "project" / "flex"
  val result = dir / (kind + ".java")
  log("generating " + result)
  jflex.Main.main(Array("--quiet", (project / (kind + ".flex")).asFile.toString))
  IO.write(result,
    IO.read(project / "warning.txt") +
    IO.read(project / (kind + ".java")))
  (project / (kind + ".java")).asFile.delete()
  result
}
