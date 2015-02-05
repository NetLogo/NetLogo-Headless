// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless.test

import java.io.File
import org.nlogo.util.SlowTest
import org.scalatest.{ FunSuite, Tag }

import
  org.nlogo.{ api, core },
    api.FileIO.file2String,
    core.{ Model, Resource }

/// top level entry points

abstract class CommandTests extends Finder {
  override def files =
    TxtsInResources("commands")
}
abstract class ReporterTests extends Finder {
  override def files =
    TxtsInResources("reporters")
}
abstract class ModelTests extends Finder {
  override def files =
    TxtsInDir("models/test")
}
abstract class ExtensionTests extends Finder {
  override def files = new Iterable[(String, String)] {
    override def iterator = {
      def filesInDir(parent: File): Iterable[File] =
        parent.listFiles.flatMap{f =>
          if(f.isDirectory)
            filesInDir(f)
          else
            List(f).filter(_.getName == "tests.txt")}
      filesInDir(new File("jvm" + File.separator + "extensions"))
        .iterator
        .map(f => (suiteName(f), file2String(f.getAbsolutePath)))
    }
  }
}


/// common infrastructure

// don't use FixtureSuite here because we may need two fixtures, not just
// one, and FixtureSuite assumes one - ST 8/7/13
trait Finder extends FunSuite with SlowTest {
  def files: Iterable[(String, String)]
  def suiteName(f: File): String =
    if (f.getName == "tests.txt")
      f.getParentFile.getName
    else
      f.getName.stripSuffix(".txt")
  case class TxtsInDir(dir: String) extends Iterable[(String, String)] {
    override def iterator =
      new File(dir).listFiles
        .filter(_.getName.endsWith(".txt"))
        .filterNot(_.getName.containsSlice("SDM"))
        .filterNot(_.getName.containsSlice("HubNet"))
        .iterator
        .map(f => (suiteName(f), file2String(f.getAbsolutePath)))
  }
  case class TxtsInResources(path: String) extends Iterable[(String, String)] {
    import org.reflections._
    import collection.JavaConverters._
    override def iterator =
      new Reflections(path, new scanners.ResourcesScanner())
        .getResources(java.util.regex.Pattern.compile(".*\\.txt"))
        .asScala.toSeq.sorted.iterator
        .map(s =>
          (s.stripPrefix(path + "/").stripSuffix(".txt"),
           Resource.asString("/" + s)))
  }
  // parse tests first, then run them
  for (t <- files.flatMap(Function.tupled(parseFile)))
    // by tagging each test with both its suite name and its full name,
    // we support both e.g. `tc Lists` and `tc Lists::Remove`
    test(t.fullName, new Tag(t.suiteName){}, new Tag(t.fullName){}) {
      for (mode <- t.modes)
        if (shouldRun(t, mode))
          runTest(t, mode)
    }

  def withFixture[T](name: String)(body: AbstractFixture => T): T

  def runTest(t: LanguageTest, mode: TestMode) {
    withFixture(s"${t.fullName} ($mode)") {
      fixture =>
        val nonDecls = t.entries.filterNot(_.isInstanceOf[Declaration])
        val decls =
          t.entries.collect{case d: Declaration => d.source}
            .mkString("\n").trim
        if (nonDecls.forall(!_.isInstanceOf[Open]))
          fixture.open(new Model(
            code = decls,
            widgets = StandardWidgets))
        else
          assert(t.entries.forall(!_.isInstanceOf[Declaration]))
        nonDecls.foreach{
          case Open(path) =>
            fixture.open(path)
          case command: Command =>
            fixture.runCommand(command, mode)
          case reporter: Reporter =>
            fixture.runReporter(reporter, mode)
          case _ =>
            throw new IllegalStateException
        }
    }
  }
  def parseFile(suiteName: String, contents: String): List[LanguageTest] = {
    def preprocessStackTraces(s: String) =
      s.replace("\\\n  ", "\\n")
    Parser.parse(suiteName, preprocessStackTraces(contents))
  }
  // on the core branch the _3D tests are gone, but extensions tests still have them since we
  // didn't branch the extensions, so we still need to filter those out - ST 1/13/12
  def shouldRun(t: LanguageTest, mode: TestMode) =
    !t.testName.endsWith("_3D") && {
      import api.Version.useGenerator
      if (t.testName.startsWith("Generator"))
        useGenerator
      else if (t.testName.startsWith("NoGenerator"))
        !useGenerator
      else true
    }
  val StandardWidgets = {
    import core.{ Plot, Pen, View }
    List(
      View.square(5),
      Plot(display = "plot1", pens = List(Pen(display = "pen1"), Pen(display = "pen2"))),
      Plot(display = "plot2", pens = List(Pen(display = "pen1"), Pen(display = "pen2"))))
  }
}