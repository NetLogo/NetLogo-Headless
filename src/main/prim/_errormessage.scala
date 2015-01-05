// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.LogoException
import org.nlogo.core.Let
import org.nlogo.nvm.{ Context, Reporter }

/**
 * Gets the error message from the LetMap.
 * Used in conjunction with <code>carefully</code>.
 *
 * @see _carefully
 */
class _errormessage extends Reporter {
  // MethodRipper won't let us call a public method from perform_1() - ST 7/20/12
  private[this] var _let: Let = null
  def let = _let
  def let_=(let: Let) { _let = let }

  override def report(context: Context): String =
    report_1(context)
  def report_1(context: Context): String =
    context.getLet(_let).asInstanceOf[LogoException].getMessage
}
