// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package back

import org.nlogo.{ api, nvm },
  api.Femto

object Scaffold {

  // cheating here - ST 8/27/13
  val frontEnd = Femto.get[FrontEndInterface](
    "org.nlogo.compile.front.FrontEnd")

  def apply(source: String): Seq[ProcedureDefinition] = {
    val (coreDefs, results) = frontEnd.frontEnd(source)
    val fmb = Compiler.FrontMiddleBridge(
      results,
      new api.DummyExtensionManager,
      nvm.Procedure.NoProcedures,
      coreDefs
    )
    Compiler.bridge(fmb)
  }

}
