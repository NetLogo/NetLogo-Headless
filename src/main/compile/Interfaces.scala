// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

// has to be in this package because that's where ProcedureDefinition is - ST 8/27/13

import org.nlogo.{ api, nvm },
  api.FrontEndInterface.ProceduresMap

object FrontEndInterface {
  type FrontEndResults = (Seq[ProcedureDefinition], StructureResults)
}
trait FrontEndInterface extends api.FrontEndInterface {
  def frontEnd(
        source: String,
        displayName: Option[String] = None,
        program: api.Program = api.Program.empty(),
        subprogram: Boolean = true,
        oldProcedures: api.FrontEndInterface.ProceduresMap = api.FrontEndInterface.NoProcedures,
        extensionManager: api.ExtensionManager = new api.DummyExtensionManager)
      : FrontEndInterface.FrontEndResults
}

trait MiddleEndInterface {
  def middleEnd(defs: Seq[ProcedureDefinition], flags: nvm.CompilerFlags): Seq[ProcedureDefinition]
}

trait BackEndInterface {
  def backEnd(defs: Seq[ProcedureDefinition], program: api.Program, source: String,
      profilingEnabled: Boolean, flags: nvm.CompilerFlags): nvm.CompilerResults
}
