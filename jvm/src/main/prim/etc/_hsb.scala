// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo
package org.nlogo.prim.etc

import
  org.nlogo.{ api, core, nvm },
    api.Color,
    core.LogoList,
    nvm.{ Context, Reporter }

final class _hsb extends Reporter {

  def report(context: Context): AnyRef =
    report_1(context, argEvalDoubleValue(context, 0), argEvalDoubleValue(context, 1), argEvalDoubleValue(context, 2))

  def report_1(context: Context, h: Double, s: Double, b: Double): LogoList =
    Color.hsbToRGBList(h, s, b)

}
