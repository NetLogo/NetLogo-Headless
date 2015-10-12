// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo
package org.nlogo.prim.etc

import
  java.lang.{ Double => JDouble }

import
  org.nlogo.{ api, core, nvm },
    api.Color,
    core.{ LogoList, Syntax },
    nvm.{ ArgumentTypeException, Context, EngineException, Reporter }

final class _extracthsb extends Reporter {

  def report(context: Context): AnyRef =
    report_1(context, args(0).report(context))

  def report_1(context: Context, obj: AnyRef): LogoList =
    obj match {
      case list: LogoList =>
        list.toList match {
          case (h: JDouble) :: (s: JDouble) :: (b: JDouble) :: Nil =>
            Color.hsbToRGBList(h, s, b)
          case x :: y :: z :: Nil =>
            throw new EngineException(context, this, s"$displayName an rgb list must contain only numbers")
          case _ =>
            throw new EngineException(context, this, s"$displayName an rgb list must have 3 elements")
        }
      case color: JDouble =>
        val c = if (color < 0 || color >= 140) Color.modulateDouble(color) else color.toDouble
        Color.getHSBListByARGB(Color.getARGBbyPremodulatedColorNumber(c))
      case _ =>
        throw new ArgumentTypeException(context, this, 1, Syntax.ListType | Syntax.NumberType, obj)
    }

  def report_2(context: Context, color: Double): LogoList = {
    val c = if (color < 0 || color >= 140) Color.modulateDouble(color) else color
    Color.getHSBListByARGB(Color.getARGBbyPremodulatedColorNumber(c))
  }

  def report_3(context: Context, list: LogoList): LogoList =
    list.toList match {
      case (h: JDouble) :: (s: JDouble) :: (b: JDouble) :: Nil =>
        Color.hsbToRGBList(h, s, b)
      case x :: y :: z :: Nil =>
        throw new EngineException(context, this, s"$displayName an rgb list must contain only numbers")
      case _ =>
        throw new EngineException(context, this, s"$displayName an rgb list must have 3 elements")
    }

}
