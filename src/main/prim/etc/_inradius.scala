// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import
  java.util.{ List => JList }

import
  scala.annotation.strictfp

import
  org.nlogo.{ agent, api, core, nvm },
    agent.{ Agent, AgentSet },
    api.I18N,
    core.AgentKind,
    nvm.{ Context, EngineException, Reporter }

@strictfp
final class _inradius extends InRadiusReporter {
  override protected lazy val findAgentsInRadius = world.inRadiusOrCone.inRadiusSimple _
}

@strictfp
final class _inradiusboundingbox extends InRadiusReporter {
  override protected lazy val findAgentsInRadius = world.inRadiusOrCone.inRadius _
}

sealed trait InRadiusReporter extends Reporter {

  protected def findAgentsInRadius: (Agent, AgentSet, Double, Boolean) => JList[Agent]

  override def syntax = {
    import org.nlogo.core.Syntax, Syntax._
    Syntax.reporterSyntax(
      agentClassString = "-TP-",
      precedence       = NormalPrecedence + 2,
      left             = TurtlesetType | PatchsetType,
      right            = List(NumberType),
      ret              = TurtlesetType | PatchsetType
    )
  }

  override def report(context: Context): AnyRef = {

    val sourceSet = argEvalAgentSet(context, 0)
    val radius    = argEvalDoubleValue(context, 1)

    if (sourceSet.kind == AgentKind.Link)
      throw new EngineException(context, this, I18N.errorsJ.get("org.nlogo.prim.etc.$common.expectedTurtleOrPatchButGotLink"))

    if (radius < 0)
      throw new EngineException(context, this, I18N.errorsJ.getN("org.nlogo.prim.etc.$common.noNegativeRadius", displayName))

    val result = findAgentsInRadius(context.agent, sourceSet, radius, true)
    AgentSet.fromArray(sourceSet.kind, result.toArray(new Array[Agent](result.size)))

  }

}
