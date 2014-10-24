// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Syntax
import org.nlogo.api.Nobody
import org.nlogo.agent.{ Turtle, LinkManager }
import org.nlogo.nvm.{ Reporter, Context, EngineException }

class _inlinkfrom(val breedName: String) extends Reporter {

  def this() = this(null)

  override def toString =
    super.toString + ":" + breedName

  override def report(context: Context): AnyRef = {
    val target = argEvalTurtle(context, 0)
    val breed =
      if (breedName == null) world.links
      else world.getLinkBreed(breedName)
    for(err <- LinkManager.mustNotBeUndirected(breed))
      throw new EngineException(context, this, err)
    val link = world.linkManager.findLinkFrom(
      target, context.agent.asInstanceOf[Turtle], breed, true)
    if (link == null)
      Nobody
    else
      link
  }

}
