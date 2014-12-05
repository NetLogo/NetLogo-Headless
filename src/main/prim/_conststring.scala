// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.Pure
import org.nlogo.nvm.{ Reporter, Context }

class _conststring(value: String) extends Reporter with Pure {
  override def toString =
    super.toString + ":\"" + value + "\""
  override def report(context: Context): String =
    report_1(context)
  def report_1(context: Context): String =
    value
}
