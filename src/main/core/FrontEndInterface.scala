// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import org.nlogo.api.{DummyExtensionManager, ExtensionManager}
import org.nlogo.core.FrontEndInterface.{NoProcedures, ProceduresMap}

trait FrontEndProcedure {
  def procedureDeclaration: StructureDeclarations.Procedure
  def syntax: Syntax
  def name: String
  def isReporter: Boolean
  def displayName: String
  def filename: String
  def nameToken: Token
  def argTokens: Seq[Token]
  var args = Vector[String]()
  var topLevel = false
  def dump: String
}

object FrontEndInterface {
  // use ListMap so procedures come out in the order they were defined (users expect errors in
  // earlier procedures to be reported first) - ST 6/10/04, 8/3/12
  import scala.collection.immutable.ListMap
  type ProceduresMap = ListMap[String, FrontEndProcedure]
  val NoProcedures: ProceduresMap = ListMap()
  type FrontEndResults = (Seq[ProcedureDefinition], StructureResults)
}

trait FrontEndInterface {
  def frontEnd(
        source: String,
        displayName: Option[String] = None,
        program: Program = Program.empty(),
        subprogram: Boolean = true,
        oldProcedures: ProceduresMap = NoProcedures,
        extensionManager: ExtensionManager = new DummyExtensionManager)
      : FrontEndInterface.FrontEndResults
}
