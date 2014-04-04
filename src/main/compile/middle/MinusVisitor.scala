// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package middle

import org.nlogo.prim, Fail._

class MinusVisitor extends DefaultAstVisitor {
  override def visitReporterApp(rApp: ReporterApp) {
    super.visitReporterApp(rApp)
    rApp.reporter match {
      case _: prim._minus if rApp.args.size == 1 =>
        val r = new prim._unaryminus
        r.token(rApp.reporter.token)
        rApp.reporter = r
      case _ =>
    }
  }
}
