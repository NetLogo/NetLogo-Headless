// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package front

import org.nlogo.{ core, nvm }

class ASTBackifier(backifier: Backifier) {

  def backify(procedure: nvm.Procedure, pd: core.ProcedureDefinition): ProcedureDefinition =
    new ProcedureDefinition(procedure, backify(pd.statements))

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

}
