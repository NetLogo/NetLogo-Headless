// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.Syntax
import org.nlogo.api.Let
import org.nlogo.nvm.{ ReporterTask, Context, Reporter }

class _reportertask extends Reporter {

  val formals = collection.mutable.ArrayBuffer[Let]()

  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.WildcardType),
      ret = Syntax.ReporterTaskType)

  override def report(c: Context): AnyRef =
    ReporterTask(body = args(0),
                 formals = formals.reverse.dropWhile(_==null).reverse.toArray,
                 lets = c.allLets,
                 locals = c.activation.args)

  def getFormal(n: Int): Let = {
    while(formals.size < n)
      formals += Let()
    formals(n - 1)
  }

}