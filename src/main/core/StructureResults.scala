// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import FrontEndInterface._
import org.nlogo.core.StructureDeclarations.Procedure
import org.nlogo.core.StructureResults.StructureResultsProceduresMap

import scala.collection.immutable.ListMap

case class StructureResults(program: Program,
                        procedures: ProceduresMap = NoProcedures,
                        procedureTokens: Map[String, Iterable[Token]] = Map(),
                        includes: Seq[Token] = Seq(),
                        extensions: Seq[Token] = Seq(),
                        proceduresUnderCompilation: StructureResultsProceduresMap = ListMap())


object StructureResults {
  type StructureResultsProceduresMap = ListMap[String, FrontEndProcedure with Procedure]
  val empty = StructureResults(Program.empty())
}
