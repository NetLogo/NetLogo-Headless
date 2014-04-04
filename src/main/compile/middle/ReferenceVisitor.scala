// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package middle

import org.nlogo.{ core, nvm, prim }, Fail._

class ReferenceVisitor extends DefaultAstVisitor {
  override def visitStatement(stmt: Statement) {
    super.visitStatement(stmt)
    stmt.command match {
      case referencer: nvm.Referencer =>
        val index =
          referencer.syntax.right.indexWhere(
            _ == core.Syntax.ReferenceType)
        val rApp = stmt.args(index).asInstanceOf[ReporterApp]
        rApp.reporter match {
          case refable: nvm.Referenceable =>
            referencer.vn = refable.vn
            stmt.removeArgument(index)
          case _ =>
            exception("Expected a patch variable here.", rApp)
        }
      case _ =>
    }
  }
}
