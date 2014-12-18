// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.{ Syntax, Let }
import org.nlogo.nvm.{ Command, Context }

// This isn't rejiggered yet because of the extra, unevaluated argument. (I say "yet" because this
// shouldn't be that hard to work around.) - ST 2/6/09

class _let(private[this] val _let: Let) extends Command {

  def let: Let = _let

  override def perform(context: Context) {
    context.let(_let, args(1).report(context))
    context.ip = next
  }
}
