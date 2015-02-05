// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo
package org.nlogo.shape

import org.nlogo.api.GraphicsInterface
import java.awt.{ Color, Point, Polygon => AwtPolygon, Rectangle => AwtRectangle }
import java.util.{ List => JList, ArrayList => JArrayList }
import java.util.StringTokenizer

@SerialVersionUID(0L)
object Polygon {
  def parsePolygon(text: String): Polygon = {
    val tokenizer: StringTokenizer = new StringTokenizer(text)
    tokenizer.nextToken
    val color: String = tokenizer.nextToken
    val b1: Boolean = tokenizer.nextToken == "true"
    val b2: Boolean = tokenizer.nextToken == "true"
    val xs: JList[Integer] = new JArrayList[Integer]
    val ys: JList[Integer] = new JArrayList[Integer]
    var lastx: Integer = null
    var lasty: Integer = null
    while (tokenizer.hasMoreTokens) {
      val newx: Integer = Integer.valueOf(tokenizer.nextToken)
      val newy: Integer = Integer.valueOf(tokenizer.nextToken)
      if (!((newx == lastx) && (newy == lasty))) {
        xs.add(newx)
        ys.add(newy)
      }
      lastx = newx
      lasty = newy
    }
    if (xs.size < 2) {
      return null
    }
    val polygon: Polygon = new Polygon(xs, ys, java.awt.Color.decode(color))
    polygon.setFilled(b1)
    polygon.setMarked(b2)
    polygon
  }
}

@SerialVersionUID(0L)
class Polygon(color: Color) extends Curve(color) with Cloneable {
  def getXcoords: List[Integer] = xcoords.toList

  def getYcoords: List[Integer] = ycoords.toList

  private var modifiedPointIndex: Int = 0

  def this(xcoords: JList[Integer], ycoords: JList[Integer], c: Color) {
    this(c)
    import scala.collection.JavaConverters._
    this.xcoords = xcoords.asScala.toArray
    this.ycoords = ycoords.asScala.toArray
  }

  override def setFilled(fill: Boolean) {
    filled = fill
  }

  def this(start: Point, color: Color) {
    this(color)
    notCompleted = true
    xcoords :+ Integer.valueOf(start.x)
    ycoords :+ Integer.valueOf(start.y)
    xcoords :+ Integer.valueOf(start.x)
    ycoords :+ Integer.valueOf(start.y)
  }

  override def clone: AnyRef = {
    val newPoly: Polygon = super.clone.asInstanceOf[Polygon]
    newPoly.xcoords = Array[Integer](newPoly.xcoords: _*)
    newPoly.ycoords = Array[Integer](newPoly.ycoords: _*)
    newPoly
  }

  override def getBounds: AwtRectangle = {
    val xArray: Array[Int] = new Array[Int](xcoords.size)
    val yArray: Array[Int] = new Array[Int](xcoords.size)
    for (i <- 0 until xcoords.length) {
      xArray(i) = getAElt(i, xcoords)
      yArray(i) = getAElt(i, ycoords)
    }
    System.out.println("Max ycoord: " + max(yArray) + ", Min ycoord: " + min(yArray))
    new AwtPolygon(xArray, yArray, xcoords.size).getBounds
  }

  def addNewPoint(newPoint: Point) {
    xcoords :+ Integer.valueOf(newPoint.x)
    ycoords :+ Integer.valueOf(newPoint.y)
    latestIndex += 1
  }

  def modifyPoint(newPoint: Point) {
    xcoords(latestIndex) = Integer.valueOf(newPoint.x)
    ycoords(latestIndex) = Integer.valueOf(newPoint.y)
  }

  override def modify(start: Point, end: Point) {
    xcoords(latestIndex) = Integer.valueOf(end.x)
    ycoords(latestIndex) = Integer.valueOf(end.y)
  }

  def reshapeElement(oldPoint: Point, newPoint: Point) {
    xcoords(modifiedPointIndex) = Integer.valueOf(newPoint.x)
    ycoords(modifiedPointIndex) = Integer.valueOf(newPoint.y)
  }

  def moveElement(xOffset: Int, yOffset: Int) = {
    for (i <- 0 until xcoords.length)
    {
      xcoords(i) = Integer.valueOf(xcoords(i).intValue + xOffset)
      ycoords(i) = Integer.valueOf(ycoords(i).intValue + yOffset)
    }
  }

  def getHandles: Array[Point] = {
    val handles: Array[Point] = new Array[Point](xcoords.size)
    for (i <- 0 until xcoords.length) {
      handles(i) = new Point(xcoords(i).intValue, ycoords(i).intValue)
    }
    handles
  }

  def contains(p: Point): Boolean = {
    val check: AwtPolygon = new AwtPolygon(xcoords.map(_.intValue), ycoords.map(_.intValue), xcoords.length)
    check.contains(p)
  }

  override def draw(g: GraphicsInterface, turtleColor: Color, scale: Double, angle: Double) {
    if (notCompleted)
      super.draw(g, null, scale, angle)
    else {
      val xArray: Array[Int] = new Array[Int](xcoords.size)
      val yArray: Array[Int] = new Array[Int](xcoords.size)
      for (i <- 0 until xcoords.size) {
        xArray(i) = getAElt(i, xcoords)
        yArray(i) = getAElt(i, ycoords)
      }
      g.setColor(getColor(turtleColor))
      if (filled)
        g.fillPolygon(xArray, yArray, xcoords.size)
      else
        g.drawPolygon(xArray, yArray, xcoords.size)
    }
  }

  def finishUp() = {
    xcoords = xcoords.dropRight(3)
    ycoords = ycoords.dropRight(3)
    /*
    xcoords.remove(latestIndex)
    ycoords.remove(latestIndex)
    xcoords.remove(latestIndex - 1)
    ycoords.remove(latestIndex - 1)
    xcoords.remove(latestIndex - 2)
    ycoords.remove(latestIndex - 2)
    */
    notCompleted = false
  }

  def selfClose() = {
    xcoords = xcoords.dropRight(1)
    ycoords = ycoords.dropRight(1)
    /*
    xcoords.remove(latestIndex)
    ycoords.remove(latestIndex)
    */
    notCompleted = false
  }

  override def toReadableString: String = {
    "Polygon - color: " + c + ",\n          bounds: " + getBounds
  }

  override private[shape] def shouldSave: Boolean = {
    xcoords.size >= 2
  }

  override def toString: String = {
    var ret: String = ""
    ret += "Polygon " + c.getRGB + " " + filled + " " + marked;
    for (i <- 0 until xcoords.size) {
      ret += " " + getAElt(i, xcoords) + " " + getAElt(i, ycoords)
    }
    ret
  }

  var latestIndex: Int = 1
  var notCompleted: Boolean = false

  def setModifiedPoint(modified: Point) {
    for (i <- 0 until xcoords.size) {
      if (xcoords(i) == Integer.valueOf(modified.x) && ycoords(i) == Integer.valueOf(modified.y)) {
        modifiedPointIndex = i
      }
    }
  }
}

