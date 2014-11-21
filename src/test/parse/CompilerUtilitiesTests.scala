// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.scalatest.FunSuite
import org.nlogo.{ core, api },
org.nlogo.api.{ExtensionManager, World, FrontEndProcedure},
  core.{StructureDeclarations, Syntax, Token},
  StructureDeclarations.Procedure

class CompilerUtilitiesTests extends FunSuite {

  /// helpers

  val src = "globals [glob1] " +
    "to foo end to-report bar [] report 5 end"

  // It's a bit depressing we have to go through the rigmarole of making
  // dummy Procedure objects, but isReporter requires us to supply a
  // ProceduresMap. - ST 7/17/13
  val (proceduresMap, program) = {
    def dummyProcedure(p: core.ProcedureDefinition): FrontEndProcedure =
      new FrontEndProcedure {
        override def procedureDeclaration: Procedure = null
        override def dump: String = ""
        override def nameToken: Token = p.procedure.name.token
        override def syntax: Syntax =
          core.Syntax.reporterSyntax(
            right = List.fill(argTokens.size)(core.Syntax.WildcardType),
            ret = core.Syntax.WildcardType
          )
        override def displayName: String = p.procedure.name.name
        override def isReporter: Boolean = p.procedure.isReporter
        override def filename: String = "FrontEndExtrasTests.scala"
        override def name: String = p.procedure.name.name
        override def argTokens: Seq[Token] = p.procedure.inputs.map(_.token)
      }
    val (procedures, structureResults) = FrontEnd.frontEnd(src)
    val proceduresMap =
      collection.immutable.ListMap(
        procedures.map { p =>
          val prc = dummyProcedure(p)
          prc.name -> prc
        }: _*)
    (proceduresMap, structureResults.program)
  }

  def compilerUtilities: api.CompilerUtilitiesInterface = CompilerUtilities

  def isReporter(s: String) =
    compilerUtilities.isReporter(s, program,
      proceduresMap, new api.DummyExtensionManager)

  /// tests for isReporter

  val reporters = Seq("3", "[]", "[", "((5))", "timer", "glob1", "bar")
  val nonReporters = Seq("", ";", " ; ", "ca", "((ca))", "foo",
                         "5984783478344387487348734", "gkhjfghkjfhjkg")

  for(x <- reporters)
    test("is a reporter: '" + x + "'") {
      assert(isReporter(x))
    }
  for(x <- nonReporters)
    test("isn't a reporter: '" + x + "'") {
      assert(!isReporter(x))
    }
}
