// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package middle

import org.nlogo.core
import org.nlogo.api.Fail._

class ReferenceVisitor extends DefaultAstVisitor {
  override def visitStatement(stmt: Statement) {
    super.visitStatement(stmt)
    val index = stmt.coreCommand.syntax.right.indexWhere(_ == core.Syntax.ReferenceType)
    if(index != -1) {
      stmt.args(index).asInstanceOf[ReporterApp].coreReporter match {
        case referenceable: core.Referenceable =>
          stmt.nvmCommand.reference = referenceable.makeReference
          stmt.removeArgument(index)
        case _ =>
          exception("Expected a patch variable here.", stmt.args(index))
      }
    }
  }
}
