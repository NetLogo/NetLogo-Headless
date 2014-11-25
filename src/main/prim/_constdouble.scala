// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.prim.Pure
import org.nlogo.nvm.{ Reporter, Context }

class _constdouble(_primitiveValue: Double) extends Reporter with Pure {

  private[this] val value = Double.box(_primitiveValue)
  val primitiveValue = _primitiveValue

  override def toString =
    super.toString + ":" + primitiveValue

  override def report(context: Context): java.lang.Double =
    report_1(context)

  def report_1(context: Context): java.lang.Double =
    value

  def report_2(context: Context): Double =
    _primitiveValue

}
