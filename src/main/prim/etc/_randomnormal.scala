// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Syntax
import org.nlogo.api.I18N
import org.nlogo.nvm.{ Context, EngineException, Reporter }

class _randomnormal extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.NumberType),
      ret = Syntax.NumberType)
  override def report(context: Context): java.lang.Double =
    Double.box(
      report_1(context,
               argEvalDoubleValue(context, 0),
               argEvalDoubleValue(context, 1)))
  def report_1(context: Context, mean: Double, sdev: Double): Double = {
    if (sdev < 0.0)
      throw new EngineException(
          context, this, I18N.errors.get(
            "org.nlogo.prim.etc._randomNormal.secondInputNotNegative"))
    validDouble(mean + sdev * context.job.random.nextGaussian)
  }
}
