// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core,
org.nlogo.core.{AstTransformer, Femto, ExtensionManager, DummyExtensionManager, FrontEndInterface, FrontEndProcedure, Program}

object FrontEnd extends FrontEnd {
  val tokenizer: core.TokenizerInterface =
    Femto.scalaSingleton("org.nlogo.lex.Tokenizer")
  val tokenMapper = new TokenMapper(
    "/system/tokens.txt", "org.nlogo.core.prim.")
}

class FrontEnd extends FrontEndMain
    with FrontEndInterface

trait FrontEndMain {

  import FrontEndInterface.ProceduresMap
  import FrontEnd.tokenizer

  // entry points

  def frontEnd(
        source: String,
        displayName: Option[String] = None,
        program: Program = core.Program.empty(),
        subprogram: Boolean = true,
        oldProcedures: FrontEndInterface.ProceduresMap = core.FrontEndInterface.NoProcedures,
        extensionManager: ExtensionManager = new DummyExtensionManager)
      : FrontEndInterface.FrontEndResults = {
    val structureResults = StructureParser.parseAll(
      tokenizer, source, displayName, program, subprogram, oldProcedures, extensionManager)
    def parseProcedure(procedure: FrontEndProcedure): core.ProcedureDefinition = {
      val rawTokens = structureResults.procedureTokens(procedure.name)
      val usedNames =
        StructureParser.usedNames(structureResults.program,
          oldProcedures ++ structureResults.procedures) ++
        procedure.args.map(_ -> "local variable here")
      // on LetNamer vs. Namer vs. LetScoper, see comments in LetScoper
      val namedTokens = {
        val letNamedTokens = LetNamer(rawTokens.iterator)
        val namer =
          new Namer(structureResults.program,
            oldProcedures ++ structureResults.procedures,
            extensionManager)
        val namedTokens = namer.process(letNamedTokens, procedure)
        val letScoper = new LetScoper(usedNames)
        letScoper(namedTokens.buffered)
      }
      ExpressionParser(procedure, namedTokens)
    }
    var topLevelDefs = structureResults.procedures.values.map(parseProcedure).toSeq

    topLevelDefs = transformers.foldLeft(topLevelDefs) {
      case (defs, transform) => defs.map(transform.visitProcedureDefinition)
    }

    val letVerifier = new LetVerifier
    topLevelDefs.foreach(letVerifier.visitProcedureDefinition)

    new AgentTypeChecker(topLevelDefs).check()  // catch agent type inconsistencies

    val verifier = new TaskVariableVerifier
    topLevelDefs.foreach(verifier.visitProcedureDefinition)

    (topLevelDefs, structureResults)
  }

  private def transformers: Seq[AstTransformer] = {
    Seq(
      new TaskSpecializer,
      new TaskVariableVerifier
    )
  }
}
