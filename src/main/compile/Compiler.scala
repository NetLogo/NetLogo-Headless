// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

import org.nlogo.{ core, api, nvm },
  nvm.Procedure.{ ProceduresMap, NoProcedures },
  org.nlogo.api.Femto

// One design principle here is that calling the compiler shouldn't have any side effects that are
// visible to the caller; it should only cause results to be constructed and returned.  There is a
// big exception to that principle, though, which is that the ExtensionManager gets side-effected
// as we load and unload extensions. - ST 2/21/08, 1/21/09, 12/7/12

object Compiler extends nvm.CompilerInterface {

  val frontEnd = Femto.get[FrontEndInterface](
    "org.nlogo.compile.front.FrontEnd")
  val middleEnd = Femto.scalaSingleton[MiddleEndInterface](
    "org.nlogo.compile.middle.MiddleEnd")
  val backEnd = Femto.scalaSingleton[BackEndInterface](
    "org.nlogo.compile.back.BackEnd")

  // used to compile the Code tab, including declarations
  def compileProgram(source: String, program: api.Program,
      extensionManager: api.ExtensionManager, flags: nvm.CompilerFlags): nvm.CompilerResults =
    compile(source, None, program, false, NoProcedures, extensionManager, flags)

  // used to compile a single procedures only, from outside the Code tab
  def compileMoreCode(source: String, displayName: Option[String], program: api.Program,
      oldProcedures: ProceduresMap, extensionManager: api.ExtensionManager,
      flags: nvm.CompilerFlags): nvm.CompilerResults =
    compile(source, displayName, program, true, oldProcedures, extensionManager, flags)

  private def compile(source: String, displayName: Option[String], oldProgram: api.Program, subprogram: Boolean,
      oldProcedures: ProceduresMap, extensionManager: api.ExtensionManager,
      flags: nvm.CompilerFlags): nvm.CompilerResults = {
    val (topLevelDefs, structureResults) =
      frontEnd.frontEnd(source, displayName, oldProgram, subprogram, oldProcedures, extensionManager)
    val fmb = FrontMiddleBridge(structureResults, extensionManager, oldProcedures, topLevelDefs)
    val bridged = bridge(fmb)
    val allDefs = middleEnd.middleEnd(bridged, flags)
    backEnd.backEnd(allDefs, structureResults.program, source, extensionManager.profilingEnabled, flags)
  }

  def makeLiteralReporter(value: AnyRef): nvm.Reporter =
    Literals.makeLiteralReporter(value)

  case class FrontMiddleBridge(
    structureResults: StructureResults,
    extensionManager: api.ExtensionManager,
    oldProcedures: ProceduresMap,
    topLevelDefs: Seq[core.ProcedureDefinition]
  )

  def bridge(fmb: FrontMiddleBridge): Seq[ProcedureDefinition] = {
    import fmb._
    val newProcedures = structureResults.procedures.mapValues(fromApiProcedure).toMap
    val backifier = new middle.Backifier(
      structureResults.program, extensionManager, oldProcedures ++ newProcedures)
    val astBackifier = new middle.ASTBackifier(backifier)
    (newProcedures.values, topLevelDefs)
      .zipped
      .map(astBackifier.backify)
      .toSeq
  }

  private def fromApiProcedure(p: api.FrontEndProcedure): nvm.Procedure = {
    val proc = new nvm.Procedure(
      isReporter = p.isReporter,
      name = p.name,
      nameToken = p.nameToken,
      argTokens = p.argTokens,
      _displayName = if (p.displayName == "") None else Some(p.displayName)
    )
    proc.topLevel = p.topLevel
    proc.args = p.args
    proc
  }

}
