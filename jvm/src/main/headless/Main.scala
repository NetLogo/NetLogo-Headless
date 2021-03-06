// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.core.model.ModelReader
import org.nlogo.core.WorldDimensions
import org.nlogo.api.{ APIVersion, FileIO, Version }
import org.nlogo.workspace.AbstractWorkspace.setHeadlessProperty
import org.nlogo.nvm.LabInterface.Settings

object Main {
  class CancelException extends RuntimeException
  def main(args: Array[String]): Unit =
    try {
      setHeadlessProperty()
      parseArgs(args).foreach(runExperiment)
    }
    catch { case _: CancelException => } // ignore
  def runExperiment(settings: Settings) {
    def newWorkspace = {
      val w = HeadlessWorkspace.newInstance
      w.open(settings.model)
      w
    }
    val lab = HeadlessWorkspace.newLab
    lab.load(ModelReader.parseModel(FileIO.file2String(settings.model), newWorkspace.compiler.utilities)
      .behaviorSpace.mkString("", "\n", "\n"))
    lab.run(settings, newWorkspace _)
  }
  private def parseArgs(args: Array[String]): Option[Settings] = {
    var model: Option[String] = None
    var minPxcor: Option[String] = None
    var maxPxcor: Option[String] = None
    var minPycor: Option[String] = None
    var maxPycor: Option[String] = None
    var setupFile: Option[java.io.File] = None
    var experiment: Option[String] = None
    var tableWriter: Option[java.io.PrintWriter] = None
    var spreadsheetWriter: Option[java.io.PrintWriter] = None
    var threads = Runtime.getRuntime.availableProcessors
    var suppressErrors = false
    val it = args.iterator
    def die(msg: String) { Console.err.println(msg) ; throw new CancelException}
    def path2writer(path: String) =
      if(path == "-")
        new java.io.PrintWriter(Console.out) {
          // don't close Console.out - ST 6/9/09
          override def close() { } }
      else
        new java.io.PrintWriter(new java.io.FileWriter(path.trim))
    while(it.hasNext) {
      val arg = it.next()
      def requireHasNext() {
        if (!it.hasNext)
          die("missing argument after " + arg)
      }
      if(arg == "--version")
        { println(Version.version); return None }
      else if(arg == "--extension-api-version")
        { println(APIVersion.version); return None }
      else if(arg == "--builddate")
        { println(Version.buildDate); return None }
      else if(arg == "--fullversion")
        { println(Version.fullVersion); return None }
      else if(arg == "--model")
        { requireHasNext(); model = Some(it.next()) }
      else if(arg == "--min-pxcor")
        { requireHasNext(); minPxcor = Some(it.next()) }
      else if(arg == "--max-pxcor")
        { requireHasNext(); maxPxcor = Some(it.next()) }
      else if(arg == "--min-pycor")
        { requireHasNext(); minPycor = Some(it.next()) }
      else if(arg == "--max-pycor")
        { requireHasNext(); maxPycor = Some(it.next()) }
      else if(arg == "--setup-file")
        { requireHasNext(); setupFile = Some(new java.io.File(it.next())) }
      else if(arg == "--experiment")
        { requireHasNext(); experiment = Some(it.next()) }
      else if(arg == "--table")
        { requireHasNext(); tableWriter = Some(path2writer(it.next())) }
      else if(arg == "--spreadsheet")
        { requireHasNext(); spreadsheetWriter = Some(path2writer(it.next())) }
      else if(arg == "--threads")
        { requireHasNext(); threads = it.next().toInt }
      else if(arg == "--suppress-errors")
        { suppressErrors = true }
      else
        die("unknown argument: " + arg)
    }
    if(model == None)
      die("you must specify --model")
    if(setupFile == None && experiment == None)
      die("you must specify either --setup-file or --experiment (or both)")
    val dimStrings = List(minPxcor, maxPxcor, minPycor, maxPycor)
    if(dimStrings.exists(_.isDefined) && dimStrings.exists(!_.isDefined))
      die("if any of min/max-px/ycor are specified, all four must be specified")
    val dims =
      if(dimStrings.forall(!_.isDefined))
        None
      else
        Some(new WorldDimensions(minPxcor.get.toInt, maxPxcor.get.toInt,
                                 minPycor.get.toInt, maxPycor.get.toInt))
    Some(new Settings(model.get, setupFile, experiment, tableWriter,
                      spreadsheetWriter, dims, threads, suppressErrors))
  }
}
