// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package front

import org.nlogo.api
import org.nlogo.api.FrontEndProcedure
import org.nlogo.{ core, nvm },
  nvm.Procedure

import scala.collection.immutable.ListMap

class ASTBackifier(program: api.Program,
                   extensionManager: api.ExtensionManager,
                   _procedures: api.FrontEndInterface.ProceduresMap) {

  type ProceduresWithDefinitions = Iterable[(FrontEndProcedure, core.ProcedureDefinition)]

  val procedures: ListMap[String, nvm.Procedure] = _procedures.map {
    case (k, p: FrontEndProcedure) => k -> fromApiProcedure(p)
  }

  val backifier = new Backifier(program, extensionManager, procedures)

  def backifyAll(proceduresAndDefinitions: ProceduresWithDefinitions): (Seq[ProcedureDefinition], api.Program) = {
    val procdefs = proceduresAndDefinitions.map {
      case (x: FrontEndProcedure, y: core.ProcedureDefinition) => backify(x, y)
    }.toSeq
    (procdefs, program)
  }

  def backify(procedure: org.nlogo.api.FrontEndProcedure, pd: core.ProcedureDefinition): ProcedureDefinition =
    new ProcedureDefinition(procedures(procedure.name), backify(pd.statements))

  def backify(expr: core.Expression): Expression =
    expr match {
      case cb: core.CommandBlock => backify(cb)
      case rb: core.ReporterBlock => backify(rb)
      case ra: core.ReporterApp => backify(ra)
    }

  def backify(stmts: core.Statements): Statements = {
    val result = new Statements(stmts.file)
    stmts.stmts.map(backify).foreach(result.addStatement)
    result
  }

  def backify(stmt: core.Statement): Statement = {
    val result =
      new Statement(stmt.command, backifier(stmt.command),
        stmt.start, stmt.end, stmt.file)
    stmt.args.map(backify).foreach(result.addArgument)
    result
  }

  def backify(cb: core.CommandBlock): CommandBlock =
    new CommandBlock(backify(cb.statements),
      cb.start, cb.end, cb.file)

  def backify(rb: core.ReporterBlock): ReporterBlock =
    new ReporterBlock(backify(rb.app),
      rb.start, rb.end, rb.file)

  def backify(ra: core.ReporterApp): ReporterApp = {
    val result =
      new ReporterApp(ra.reporter, backifier(ra.reporter),
        ra.start, ra.end, ra.file)
    ra.args.map(backify).foreach(result.addArgument)
    result
  }

  private def fromApiProcedure(frontEndProcedure: api.FrontEndProcedure): Procedure = {
    frontEndProcedure match {
      case nvmProcedure: Procedure => nvmProcedure
      case p =>
        val proc = new Procedure(
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
}
