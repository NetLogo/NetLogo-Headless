// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Syntax
import org.nlogo.nvm.{ AssemblerAssistant, Command, Context, CustomAssembled }

class _loop extends Command with CustomAssembled {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.CommandBlockType))
  override def perform(context: Context) {
    // we get custom-assembled out of existence
    throw new IllegalStateException()
  }
  override def assemble(a: AssemblerAssistant) {
    a.comeFrom()
    a.block()
    a.goTo()
  }
}