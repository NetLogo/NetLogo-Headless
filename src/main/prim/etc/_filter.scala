// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Syntax
import org.nlogo.api.{ LogoList, LogoListBuilder }
import org.nlogo.nvm.{ ArgumentTypeException, Context, EngineException, Reporter }

class _filter extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.ReporterTaskType, Syntax.ListType),
      ret = Syntax.ListType)

  def report(context: Context): LogoList = {
    val task = argEvalReporterTask(context, 0)
    val list = argEvalList(context, 1)
    if(task.formals.size > 1)
      throw new EngineException(
        context, this, task.missingInputs(1))
    val builder = new LogoListBuilder
    for (item <- list)
      task.report(context, Array(item)) match {
        case b: java.lang.Boolean =>
          if (b.booleanValue)
            builder.add(item)
        case obj =>
          throw new ArgumentTypeException(
            context, this, 0, Syntax.BooleanType, obj)
      }
    val result = builder.toLogoList
    if (result.size == list.size) list
    else result
  }

}