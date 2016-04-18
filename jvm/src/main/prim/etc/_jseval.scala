// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ Command, Context }

class _jseval extends Command {
  override def perform(context: Context) {
    context.ip = next
  }
}
