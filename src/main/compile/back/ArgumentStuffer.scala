// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package back

import org.nlogo.nvm

/**
 * Fills the args arrays, in all of the Instructions anywhere in
 * the Procedure, with Reporters.
 */
private class ArgumentStuffer extends DefaultAstVisitor {
  override def visitStatement(stmt: Statement) {
    stmt.nvmCommand.args = gatherArgs(stmt.args)
    super.visitStatement(stmt)
  }
  override def visitReporterApp(app: ReporterApp) {
    app.nvmReporter.args = gatherArgs(app.args)
    super.visitReporterApp(app)
  }
  private def gatherArgs(expressions: Seq[Expression]): Array[nvm.Reporter] =
    expressions.flatMap{
      case app: ReporterApp => Some(app.nvmReporter)
      case block: ReporterBlock => Some(block.app.nvmReporter)
      case _ => None
    }.toArray
}
