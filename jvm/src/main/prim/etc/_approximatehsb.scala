// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo
package org.nlogo.prim.etc

import
  org.nlogo.{ api, core, nvm },
    api.Color,
    core.Pure,
    nvm.{ Context, Reporter }

final class _approximatehsb extends Reporter with Pure {

  def report(context: Context): AnyRef = {
    val reportedValue =
      report_1(context, argEvalDoubleValue(context, 0), argEvalDoubleValue(context, 1), argEvalDoubleValue(context, 2))
    Double.box(validDouble(reportedValue))
  }

  def report_1(context: Context, h: Double, s: Double, b: Double): Double =
    Color.getClosestColorNumberByHSB(h.toFloat, s.toFloat, b.toFloat)

}
