// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ AgentSet, Turtle, Patch }
import org.nlogo.core.Syntax
import org.nlogo.nvm.{ Reporter, Context }

class _neighbors4 extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.PatchsetType,
      agentClassString = "-TP-")
  override def report(context: Context): AgentSet =
    report_1(context)
  def report_1(context: Context): AgentSet =
    (context.agent match {
      case t: Turtle => t.getPatchHere
      case p: Patch => p
    }).getNeighbors4
}