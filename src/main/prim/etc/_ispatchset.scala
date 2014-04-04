// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.AgentKind
import org.nlogo.agent.{ AgentSet, Patch }
import org.nlogo.nvm.{ Context, Pure, Reporter }

class _ispatchset extends Reporter with Pure {
  override def report(context: Context): java.lang.Boolean =
    Boolean.box(
      args(0).report(context) match {
        case set: AgentSet =>
          set.kind == AgentKind.Patch
        case _ =>
          false
      })
}
