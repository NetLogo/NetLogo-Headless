// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ Syntax, SyntaxJ }
import org.nlogo.api.Dump
import org.nlogo.nvm.{ Command, Context }

class _stdout extends Command {
  override def syntax =
    SyntaxJ.commandSyntax(Array(Syntax.WildcardType))
  override def perform(context: Context) {
    println(Dump.logoObject(args(0).report(context)))
    context.ip = next
  }
}
