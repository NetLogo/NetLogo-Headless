// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.{ LogoList, I18N, Syntax }
import org.nlogo.api.{ AgentException, LogoListBuilder }
import org.nlogo.nvm.{ Reporter, Context, EngineException , ArgumentTypeException }
import org.nlogo.agent.{ Agent, AgentSet }

class _breedvariableof(name: String) extends Reporter {

  override def toString =
    super.toString + ":" + name

  override def report(context: Context): AnyRef =
    args(0).report(context) match {
      case agent: Agent =>
        if (agent.id == -1)
          throw new EngineException(
            context, this, I18N.errors.getN(
              "org.nlogo.$common.thatAgentIsDead", agent.classDisplayName))
        try agent.getBreedVariable(name)
        catch { case ex: AgentException =>
          throw new EngineException(context, this, ex.getMessage) }
      case sourceSet: AgentSet =>
        val result = new LogoListBuilder
        try {
          val iter = sourceSet.shufflerator(context.job.random)
          while(iter.hasNext)
            result.add(iter.next().getBreedVariable(name))
          result.toLogoList
        }
        catch { case ex: AgentException =>
          throw new EngineException(context, this, ex.getMessage) }
      case x =>
        throw new ArgumentTypeException(
          context, this, 0, Syntax.TurtlesetType | Syntax.TurtleType, x)
    }

}
