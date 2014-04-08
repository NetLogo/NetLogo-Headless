// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Syntax
import org.nlogo.nvm.{ Command, Context }

class _changetopology extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.BooleanType, Syntax.BooleanType))
  override def perform(context: Context) {
    workspace.changeTopology(
      argEvalBooleanValue(context, 0),
      argEvalBooleanValue(context, 1))
    context.ip = next
  }
}