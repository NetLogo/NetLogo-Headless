// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

// has to be in this package because that's where ProcedureDefinition is - ST 8/27/13

import org.nlogo.{ api, nvm },
  api.FrontEndInterface.ProceduresMap

trait FrontEndInterface extends api.FrontEndInterface {
  def frontEnd(source: String, oldProcedures: ProceduresMap = api.FrontEndInterface.NoProcedures,
      program: api.Program = api.Program.empty()): (Seq[ProcedureDefinition], api.Program)
  def frontEndHelper(source: String, displayName: Option[String], program: api.Program, subprogram: Boolean,
      oldProcedures: ProceduresMap, extensionManager: api.ExtensionManager)
    : (Seq[ProcedureDefinition], api.Program)
}

trait MiddleEndInterface {
  def middleEnd(defs: Seq[ProcedureDefinition], flags: nvm.CompilerFlags): Seq[ProcedureDefinition]
}

trait BackEndInterface {
  def backEnd(defs: Seq[ProcedureDefinition], program: api.Program, source: String,
      profilingEnabled: Boolean, flags: nvm.CompilerFlags): nvm.CompilerResults
}
