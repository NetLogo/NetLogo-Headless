// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ Syntax, SyntaxJ }
import org.nlogo.api.Numbers
import org.nlogo.agent.Turtle
import org.nlogo.nvm.{ Context, Reporter }

class _dx extends Reporter {
  override def syntax =
    SyntaxJ.reporterSyntax(Syntax.NumberType, "-T--")
  override def report(context: Context): java.lang.Double =
    Double.box(report_1(context))
  def report_1(context: Context): Double = {
    val result = context.agent.asInstanceOf[Turtle].dx
    if (StrictMath.abs(result) < Numbers.Infinitesimal)
      0 else result
  }
}
