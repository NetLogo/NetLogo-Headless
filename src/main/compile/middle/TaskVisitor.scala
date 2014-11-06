// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package middle

import org.nlogo.{ core, api, nvm, prim },
  Fail._

// This replaces _taskvariable with _letvariable everywhere.  And we need
//   to know which Let object to connect each occurrence to.
// There are two cases, command tasks and reporter tasks:
// - In the command task case, LambdaLifter already made the task body into
//   its own procedure, so we never see _commandtask, so we look up the
//   right Let in the enclosing procedure.
// - In the reporter task case, we walk the tree and always keep track of
//   the nearest enclosing _reportertask node, so we can find our Let there.

class TaskVisitor extends DefaultAstVisitor {
  private var task = Option.empty[prim._reportertask]
  private var procedure = Option.empty[nvm.Procedure]
  override def visitProcedureDefinition(procdef: ProcedureDefinition) {
    procedure = Some(procdef.procedure)
    super.visitProcedureDefinition(procdef)
  }
  override def visitReporterApp(expr: ReporterApp) {
    expr.nvmReporter match {
      case l: prim._reportertask =>
        val old = task
        task = Some(l)
        super.visitReporterApp(expr)
        task = old
      case lv: prim._taskvariable =>
        task match {
          case None =>
            cAssert(procedure.get.isTask,
                    api.I18N.errors.get("compiler.TaskVisitor.notDefined"), expr)
            val formal: core.Let = procedure.get.getTaskFormal(lv.varNumber)
            val plv = new prim._letvariable
            expr.nvmReporter = plv
            plv.let = formal
            expr.nvmReporter.token = lv.token
          case Some(l: prim._reportertask) =>
            val formal: core.Let = l.getFormal(lv.varNumber)
            val plv = new prim._letvariable
            expr.nvmReporter = plv
            plv.let = formal
            expr.nvmReporter.token = lv.token
        }
      case _ =>
        super.visitReporterApp(expr)
    }
  }
}
