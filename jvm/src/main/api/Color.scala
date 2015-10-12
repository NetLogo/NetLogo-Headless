// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import
  java.lang.{ Double => JDouble }

import java.text.DecimalFormat
import java.awt.{ Color => JColor }

import org.nlogo.core.{ Color => CColor, ColorConstants, LogoList, I18N },
  ColorConstants._

object Color extends CColor {

  private val MaxHue        = 360.0f
  private val MaxSaturation = 100.0f
  private val MaxBrightness = 100.0f

  private val AWT_Cache =
    for(i <- (0 until MaxColor * 10).toArray)
    yield new JColor(
      getARGBbyPremodulatedColorNumber(i / 10.0))

  val BaseColors = LogoList(
    (0 to 13).map(n => Double.box(n * 10 + 5)): _*)

  def getColor(color: AnyRef): JColor = {
    color match {
      case d: java.lang.Double =>
        AWT_Cache((d.doubleValue * 10).toInt)
      case list: LogoList if list.size == 3 =>
        new JColor(list.get(0).asInstanceOf[Number].intValue,
                           list.get(1).asInstanceOf[Number].intValue,
                           list.get(2).asInstanceOf[Number].intValue)
      case list: LogoList if list.size == 4 =>
        new JColor(list.get(0).asInstanceOf[Number].intValue,
                           list.get(1).asInstanceOf[Number].intValue,
                           list.get(2).asInstanceOf[Number].intValue,
                           list.get(3).asInstanceOf[Number].intValue)
    }
  }

  def hsbToRGBList(h: Double, s: Double, b: Double): LogoList = {
    val argb  = hsbToARGBNumber(h.toFloat, s.toFloat, b.toFloat)
    val (_, red, green, blue) = argbNumToTuple(argb)
    LogoList.fromVector(Vector(red, green, blue).map(x => Double.box(x)))
  }

  // given a color in ARGB, function returns a string in the "range" of
  // "red - 5" to "magenta + 5" representing the color in NetLogo's color scheme
  // input: ARGB
  // output: ["red - 5" to "magenta + 5"]
  def getClosestColorNameByARGB(argb: Int): String = {
    val formatter = new DecimalFormat("###.####")
    getClosestColorNumberByARGB(argb) match {
      case Black =>
        getColorNameByIndex(14)
      case White =>
        getColorNameByIndex(15)
      case closest =>
        val baseColorNumber = findCentralColorNumber(closest).toInt
        val difference = closest - baseColorNumber
        val baseColorName = getColorNameByIndex((baseColorNumber - 5) / 10)
        if (difference == 0)
          baseColorName
        else if (difference > 0)
          baseColorName + " + " + formatter.format(StrictMath.abs(difference))
        else
          baseColorName + " - " + formatter.format(StrictMath.abs(difference))
    }
  }

  // given a color in the HSB spectrum, function returns a value
  // that represents the color in NetLogo's color scheme
  // inputs: clamped to [0.0-1.0]
  // output: [0.0-139.9]
  def getClosestColorNumberByHSB(h: Float, s: Float, b: Float) = {
    getClosestColorNumberByARGB(hsbToARGBNumber(h, s, b))
  }

  ///

  def getRGBListByARGB(argb: Int): LogoList =
    getRGBAListByARGB(argb).butLast

  def getRGBAListByARGB(argb: Int): LogoList = {
    val (a, r, g, b) = argbNumToTuple(argb)
    LogoList.fromVector(Vector(r, g, b, a).map(x => approximate(x.toFloat)))
  }

  def getHSBListByARGB(argb: Int): LogoList = {

    val (_, red, green, blue) = argbNumToTuple(argb)

    val hsb = new Array[Float](3)
    JColor.RGBtoHSB(red, green, blue, hsb)

    val h = approximate(hsb(0) * MaxHue)
    val s = approximate(hsb(1) * MaxSaturation)
    val b = approximate(hsb(2) * MaxBrightness)

    LogoList(h, s, b)

  }

  def getComplement(color: JColor): JColor = {
    val rgb = color.getRGBColorComponents(null)
    new JColor(
      (rgb(0) + 0.5f) % 1.0f,
      (rgb(1) + 0.5f) % 1.0f,
      (rgb(2) + 0.5f) % 1.0f)
  }

  @throws(classOf[AgentException])
  def validRGBList(rgb: LogoList, allowAlpha: Boolean) {
    def validRGB(c: Int) {
      if (c < 0 || c > 255)
        throw new AgentException(I18N.errors.get(
          "org.nlogo.agent.Agent.rgbValueError"))
    }
    if (rgb.size == 3 || (allowAlpha && rgb.size == 4))
      try {
        var i = 0
        while (i < rgb.size) {
          validRGB(rgb.get(i).asInstanceOf[java.lang.Double].intValue)
          i += 1
        }
        return
      }
      catch { case e: ClassCastException =>
        // just fall through and throw the error below
        org.nlogo.api.Exceptions.ignore(e)
      }
    val key = "org.nlogo.agent.Agent." +
      (if (allowAlpha) "rgbListSizeError.3or4"
       else "rgbListSizeError.3")
    throw new AgentException(I18N.errors.get(key))
  }

  private def hsbToARGBNumber(h: Float, s: Float, b: Float): Int = {
    val attenuate = (x: Float, maxValue: Float) => (0f max x min maxValue) / maxValue
    JColor.HSBtoRGB(attenuate(h, MaxHue), attenuate(s, MaxSaturation), attenuate(b, MaxBrightness))
  }

  // 3 is just enough digits of precision so that passing the resulting values through the hsb
  // prim will reconstruct the original number (or rather the floor of the original number to the
  // nearest 0.1) - ST 10/25/05
  private def approximate(x: Float): JDouble =
    Double.box(Approximate.approximate(x, 3))

  private def argbNumToTuple(argb: Int): (Int, Int, Int, Int) = {
    val a = (argb >> 24) & 0xff
    val r = (argb >> 16) & 0xff
    val g = (argb >>  8) & 0xff
    val b =  argb        & 0xff
    (a, r, g, b)
  }

}
