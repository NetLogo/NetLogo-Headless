// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.{ Syntax, Let }
import org.nlogo.nvm.{ ReporterTask, Context, Reporter }

class _reportertask(val argCount: Int) extends Reporter {

  val taskFormals = Array.fill(argCount)(Let())

  override def report(c: Context): AnyRef =
    ReporterTask(body = args(0),
                 formals = taskFormals,
                 lets = c.allLets,
                 locals = c.activation.args)

  def getFormal(n: Int): Let = taskFormals(n - 1)
}
