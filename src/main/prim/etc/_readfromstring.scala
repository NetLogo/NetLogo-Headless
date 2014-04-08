// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Syntax
import org.nlogo.api.CompilerException
import org.nlogo.nvm.{ Context, EngineException, Reporter }

class _readfromstring extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.StringType),
      ret = Syntax.ReadableType)
  override def report(context: Context): AnyRef =
    try workspace.readFromString(argEvalString(context, 0))
    catch {
      case e: CompilerException =>
        throw new EngineException(context, this, e.getMessage)
    }
}