// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

import org.nlogo.{ api, nvm },
  api.Femto

object Scaffold {

  val frontEnd = Femto.scalaSingleton[api.FrontEndInterface](
    "org.nlogo.parse.FrontEnd")
  val bridge = Femto.scalaSingleton[FrontMiddleBridgeInterface](
    "org.nlogo.compile.middle.FrontMiddleBridge")

  def apply(source: String): Seq[ProcedureDefinition] = {
    val (coreDefs, results) = frontEnd.frontEnd(source)
    bridge(
      results,
      new api.DummyExtensionManager,
      nvm.Procedure.NoProcedures,
      coreDefs
    )
  }

}
