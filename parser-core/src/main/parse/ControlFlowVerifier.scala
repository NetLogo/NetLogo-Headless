// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import
  org.nlogo.core.{ AstVisitor, CommandBlock, Fail, I18N,
                   prim, ProcedureDefinition, ReporterBlock, Statement },
    Fail._,
    prim.etc.{ _report, _stop }

import
  scala.collection.mutable.Stack

class ControlFlowVerifier extends AstVisitor {

  sealed trait CurrentContext
  case object ReporterContext extends CurrentContext
  case object CommandContext  extends CurrentContext
  case object BlockContext    extends CurrentContext

  var contextStack = Stack[CurrentContext]()

  override def visitProcedureDefinition(proc: ProcedureDefinition) = {
    if (proc.procedure.isReporter)
      contextStack.push(ReporterContext)
    else
      contextStack.push(CommandContext)
    super.visitProcedureDefinition(proc)
    contextStack.pop()
  }

  override def visitStatement(statement: Statement) = {
    (contextStack.head, statement.command) match {
      case (ReporterContext, _: _stop) =>
        exception(
          I18N.errors.getN("org.nlogo.prim.etc._stop.notAllowedInsideToReport", "STOP"),
          statement)
      case (_,               _: _report) if contextStack.contains(CommandContext) =>
        exception(
          I18N.errors.getN("org.nlogo.prim._report.canOnlyUseInToReport", "REPORT"),
          statement)
      case (ctx,             _: _report) if ctx != ReporterContext =>
        exception(
          I18N.errors.getN("org.nlogo.prim._report.mustImmediatelyBeUsedInToReport", "REPORT"),
          statement)
      case _ if (statement.command.syntax.introducesContext) =>
        contextStack.push(BlockContext)
        super.visitStatement(statement)
        contextStack.pop()
      case _ =>
        super.visitStatement(statement)
    }
  }
}
