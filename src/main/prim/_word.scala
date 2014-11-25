// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.{ Syntax, prim },
  prim.Pure
import org.nlogo.api.Dump
import org.nlogo.nvm.{ Context, Reporter, CustomGenerated }

class _word extends Reporter with Pure with CustomGenerated {

  override def returnType =
    Syntax.StringType

  override def report(context: Context): String =
    args.map(arg => Dump.logoObject(arg.report(context)))
      .mkString

}
