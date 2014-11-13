// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

import org.nlogo.{core, api},
  api.FrontEndInterface.{ ProceduresMap, NoProcedures }

case class StructureResults(program: api.Program,
                        procedures: ProceduresMap = NoProcedures,
                        procedureTokens: Map[String, Iterable[core.Token]] = Map(),
                        includes: Seq[core.Token] = Seq(),
                        extensions: Seq[core.Token] = Seq())

object StructureResults {
  val empty: StructureResults = StructureResults(api.Program.empty())
}
