// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import scala.collection.mutable
import org.nlogo.agent.AgentSet
import org.nlogo.api.LogoException
import org.nlogo.core.{ LogoList, Syntax }
import org.nlogo.nvm.{ ArgumentTypeException, Context, EngineException, Reporter, ReporterTask }

class _sortby extends Reporter {

  // see issue #172
  private val Java7SoPicky =
    "Comparison method violates its general contract!"

  override def report(context: Context): LogoList = {
    val task = argEvalReporterTask(context, 0)
    if(task.formals.size > 2)
      throw new EngineException(
        context, this, task.missingInputs(2))
    val obj = args(1).report(context)
    val input = obj match {
      case list: LogoList =>
        // must copy the list, because Collections.sort() works in place - ST 7/31/04, 1/12/06
        list
      case agents: AgentSet =>
        val list = mutable.MutableList[AnyRef]()
        val it = agents.shufflerator(context.job.random)
        while(it.hasNext)
          list += it.next()
        list
      case _ =>
        throw new ArgumentTypeException(
          context, this, 0, Syntax.ListType | Syntax.AgentsetType, obj)
    }
    try {
      val sorted = input.sortWith(sortOrder(context, task))
      LogoList.fromIterator(sorted.iterator)
    }
    catch {
      case e: IllegalArgumentException if e.getMessage == Java7SoPicky =>
        throw new EngineException(
          context, this, "predicate is not a strictly-less-than or strictly-greater than relation")
      case e: WrappedLogoException => throw e.ex
    }
  }

  def sortOrder(context: Context, task: ReporterTask): ((AnyRef, AnyRef) => Boolean) = {
    def die(o: AnyRef) =
      throw new ArgumentTypeException(
        context, _sortby.this, 0, Syntax.BooleanType, o)

    def apply(o1: AnyRef, o2: AnyRef): Boolean =
      try task.report(context, Array(o1, o2)) match {
            case b: java.lang.Boolean =>
              if(b.booleanValue) true
              else task.report(context, Array(o2, o1)) match {
                case b: java.lang.Boolean =>
                  if(b.booleanValue) false
                  else false
                case o => die(o)
              }
            case o => die(o)
      }
      catch {
        case ex: LogoException =>
          throw new WrappedLogoException(ex)
      }

    apply _
  }

  class WrappedLogoException(val ex: LogoException) extends RuntimeException

}
