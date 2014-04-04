// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.AgentKind
import org.nlogo.nvm, nvm.{ Reporter, Context }

class _observervariable(_vn: Int) extends Reporter with nvm.Referenceable {

  override def toString =
    super.toString + ":" +
      Option(world).map(_.observerOwnsNameAt(vn)).getOrElse(vn.toString)

  // MethodRipper won't let us call a public method from report_1()
  // so we must keep vn and _vn separate - ST 9/22/12
  def vn = _vn

  override def report(context: Context): AnyRef =
    world.observer.getVariable(_vn)

  def report_1(context: Context): AnyRef =
    world.observer.getVariable(_vn)

}
