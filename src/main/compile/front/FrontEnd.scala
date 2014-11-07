// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package front

import org.nlogo.{ core, api, agent, nvm, parse }
import org.nlogo.api.Femto

object FrontEnd extends FrontEnd {
  val tokenizer: core.TokenizerInterface =
    Femto.scalaSingleton("org.nlogo.lex.Tokenizer")
  val tokenMapper = new parse.TokenMapper(
    "/system/tokens.txt", "org.nlogo.core.prim.")
  // well this is pretty ugly.  LiteralParser and LiteralAgentParser call each other,
  // so they're hard to instantiate, but we "tie the knot" using lazy val. - ST 5/3/13
  def literalParser(world: api.World, extensionManager: api.ExtensionManager): parse.LiteralParser = {
    lazy val literalParser =
      new parse.LiteralParser(world, extensionManager, parseLiteralAgentOrAgentSet)
    lazy val parseLiteralAgentOrAgentSet: Iterator[core.Token] => AnyRef =
      new agent.LiteralAgentParser(
          world, literalParser.readLiteralPrefix _, Fail.cAssert _, Fail.exception _)
        .parseLiteralAgentOrAgentSet _
    literalParser
  }
}

class FrontEnd extends FrontEndMain
    with FrontEndInterface with FrontEndExtras

trait FrontEndMain {

  import nvm.FrontEndInterface.ProceduresMap
  import FrontEnd.tokenizer

  // entry points

  def frontEnd(source: String, oldProcedures: ProceduresMap = nvm.FrontEndInterface.NoProcedures,
      program: api.Program = api.Program.empty()): (Seq[ProcedureDefinition], nvm.StructureResults) =
    frontEndHelper(source, None, program, true,
      oldProcedures, new api.DummyExtensionManager)

  def frontEndHelper(source: String, displayName: Option[String], program: api.Program, subprogram: Boolean,
      oldProcedures: ProceduresMap, extensionManager: api.ExtensionManager)
    : (Seq[ProcedureDefinition], nvm.StructureResults) = {
    val structureResults = StructureParser.parseAll(
      tokenizer, source, displayName, program, subprogram, oldProcedures, extensionManager)
    val backifier = new Backifier(structureResults.program, extensionManager,
      oldProcedures ++ structureResults.procedures)
    def parseProcedure(procedure: nvm.Procedure): ProcedureDefinition = {
      val rawTokens = structureResults.tokens(procedure)
      val usedNames =
        StructureParser.usedNames(structureResults.program,
          structureResults.procedures ++ oldProcedures) ++
        procedure.args.map(_ -> "local variable here")
      // on LetNamer vs. Namer vs. LetScoper, see comments in LetScoper
      val namedTokens = {
        val letNamedTokens = parse.LetNamer(rawTokens.iterator)
        val namer =
          new Namer(structureResults.program,
            oldProcedures ++ structureResults.procedures,
            extensionManager)
        val namedTokens = namer.process(letNamedTokens, procedure)
        val letScoper = new parse.LetScoper(usedNames)
        letScoper(namedTokens.buffered)
      }
      new ASTBackifier(backifier)
        .backify(procedure, ExpressionParser(namedTokens))
    }
    val procdefs = structureResults.procedures.values.map(parseProcedure).toVector
    (procdefs, structureResults)
  }

}
