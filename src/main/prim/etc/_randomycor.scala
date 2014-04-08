// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ Syntax }
import org.nlogo.nvm.{ Context, Reporter }

class _randomycor extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.NumberType)
  override def report(context: Context): java.lang.Double =
    Double.box(report_1(context))
  def report_1(context: Context): Double = {
    val min = world.minPycor - 0.5
    val max = world.maxPycor + 0.5
    min + context.job.random.nextDouble * (max - min)
  }
}