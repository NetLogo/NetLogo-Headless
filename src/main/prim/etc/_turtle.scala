// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Syntax
import org.nlogo.api.Nobody
import org.nlogo.agent.Turtle
import org.nlogo.nvm.{ Reporter, Context, EngineException }

class _turtle extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.TurtleType | Syntax.NobodyType)

  override def report(context: Context): AnyRef =
    report_1(context, argEvalDoubleValue(context, 0))

  def report_1(context: Context, idDouble: Double): AnyRef = {
    val id = validLong(idDouble)
    if (id != idDouble)
      throw new EngineException(
        context, this, idDouble + " is not an integer")
    val turtle = world.getTurtle(id)
    if (turtle == null) Nobody
    else turtle
  }

}