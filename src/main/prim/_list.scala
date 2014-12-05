// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.{Pure, LogoList, Syntax, prim}
import org.nlogo.api.{ LogoException, LogoListBuilder }
import org.nlogo.nvm.{ Reporter, Context, CustomGenerated }

class _list extends Reporter with Pure with CustomGenerated {
  override def returnType =
    Syntax.ListType
  override def report(context: Context): LogoList = {
    val builder = new LogoListBuilder
    var i = 0
    while(i < args.length) {
      builder.add(args(i).report(context))
      i += 1
    }
    builder.toLogoList
  }
}
