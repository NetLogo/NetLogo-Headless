// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

/**
 * The default AST tree-walker. This simply visits each node of the
 * tree, and visits any children of each node in turn. Subclasses can
 * implement pre-order or post-order traversal, or a wide range of other
 * strategies.
 */
trait AstVisitor {
  def visitProcedureDefinition(proc: ProcedureDefinition) {
    visitStatements(proc.statements)
  }
  def visitCommandBlock(block: CommandBlock) {
    visitStatements(block.statements)
  }
  def visitExpression(exp: Expression) {
    exp match {
      case app: ReporterApp =>
        visitReporterApp(app)
      case cb: CommandBlock =>
        visitCommandBlock(cb)
      case rb: ReporterBlock =>
        visitReporterBlock(rb)
    }
  }
  def visitReporterApp(app: ReporterApp) {
    app.args.foreach(visitExpression)
  }
  def visitReporterBlock(block: ReporterBlock) {
    visitReporterApp(block.app)
  }
  def visitStatement(stmt: Statement) {
    stmt.args.foreach(visitExpression)
  }
  def visitStatements(stmts: Statements) {
    stmts.stmts.foreach(visitStatement)
  }
}
