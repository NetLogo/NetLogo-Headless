// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.Syntax
import org.nlogo.api.{ Dump, I18N, Nobody }
import org.nlogo.agent.AgentSet
import org.nlogo.nvm.{ Reporter, Context, EngineException }

class _oneofwith extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.AgentsetType, Syntax.BooleanBlockType),
      ret = Syntax.AgentType | Syntax.NobodyType,
      agentClassString = "OTPL",
      blockAgentClassString = "?")

  override def report(context: Context): AnyRef =
    report_1(context, argEvalAgentSet(context, 0), args(1))

  def report_1(context: Context, sourceSet: AgentSet, arg1: Reporter): AnyRef = {
    val freshContext = new Context(context, sourceSet)
    arg1.checkAgentSetClass(sourceSet, context)
    val iter = sourceSet.shufflerator(context.job.random)
    while(iter.hasNext) {
      val tester = iter.next()
      freshContext.evaluateReporter(tester, arg1) match {
        case b: java.lang.Boolean =>
          if (b)
            return tester
        case x =>
          throw new EngineException(
            context, this, I18N.errors.getN(
              "org.nlogo.prim.$common.withExpectedBooleanValue",
              Dump.logoObject(tester), Dump.logoObject(x)))
      }
    }
    Nobody
  }

}