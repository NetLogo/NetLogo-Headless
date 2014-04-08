// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.{ Syntax, AgentKind }
import org.nlogo.agent.{ Patch, AgentSetBuilder }
import org.nlogo.nvm.{ Reporter, Context }

class _patchrow extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.PatchsetType)

  override def report(context: Context): AnyRef = {
    val builder = new AgentSetBuilder(AgentKind.Patch, world.worldHeight)
    val yDouble = argEvalDoubleValue(context, 0)
    val y = yDouble.toInt
    if (y == yDouble && y >= world.minPycor && y <= world.maxPycor) {
      val xMax = world.maxPxcor
      var x = world.minPxcor
      while(x <= xMax) {
        builder.add(world.fastGetPatchAt(x, y))
        x += 1
      }
    }
    builder.build()
  }

}