// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Syntax
import org.nlogo.agent.Link
import org.nlogo.nvm.{ Command, Context }

class _showlink extends Command {
  switches = true
  override def perform(context: Context) {
    context.agent.asInstanceOf[Link].hidden(false)
    context.ip = next
  }
}
