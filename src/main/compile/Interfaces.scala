// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

import org.nlogo.{ core, api, nvm },
  nvm.Procedure.ProceduresMap

trait FrontMiddleBridgeInterface {
  def apply(
    structureResults: api.StructureResults,
    extensionManager: api.ExtensionManager,
    oldProcedures: ProceduresMap,
    topLevelDefs: Seq[core.ProcedureDefinition]
  ): Seq[ProcedureDefinition]
}

trait MiddleEndInterface {
  def middleEnd(defs: Seq[ProcedureDefinition], flags: nvm.CompilerFlags): Seq[ProcedureDefinition]
}

trait BackEndInterface {
  def backEnd(defs: Seq[ProcedureDefinition], program: api.Program, source: String,
      profilingEnabled: Boolean, flags: nvm.CompilerFlags): nvm.CompilerResults
}
