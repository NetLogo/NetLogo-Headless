// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.prim.Pure
import org.nlogo.nvm.{ Reporter, Context }

class _constboolean(_primitiveValue: Boolean) extends Reporter with Pure {

  private[this] val value = Boolean.box(_primitiveValue)
  val primitiveValue = _primitiveValue

  override def toString =
    super.toString + ":" + primitiveValue

  override def report(context: Context): java.lang.Boolean =
    report_1(context)

  def report_1(context: Context): java.lang.Boolean =
    value

  def report_2(context: Context): Boolean =
    _primitiveValue

}
