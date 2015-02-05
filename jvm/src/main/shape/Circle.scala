// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo
package org.nlogo.shape

import java.awt.geom.Ellipse2D

import org.nlogo.api.GraphicsInterface
import java.awt.{ Color, Point, Rectangle => AwtRectangle }
import java.util.StringTokenizer

import org.nlogo.core.Shape

@SerialVersionUID(0L)
object Circle {
  def parseCircle(text: String): Circle = {
    val tokenizer: StringTokenizer = new StringTokenizer(text)
    tokenizer.nextToken
    val color: String = tokenizer.nextToken
    val b1: Boolean = tokenizer.nextToken == "true"
    val b2: Boolean = tokenizer.nextToken == "true"
    val x1: Int = Integer.valueOf(tokenizer.nextToken).intValue
    val y1: Int = Integer.valueOf(tokenizer.nextToken).intValue
    val diam: Int = Integer.valueOf(tokenizer.nextToken).intValue
    if (diam == 0) {
      return null
    }
    val circle: Circle = new Circle(x1, y1, diam, java.awt.Color.decode(color))
    circle.setFilled(b1)
    circle.setMarked(b2)
    circle
  }
}

@SerialVersionUID(0L)
class Circle(color: Color) extends Element(color) with Cloneable {
  import Math.round

  private var x: Int = 0
  private var y: Int = 0
  private var xDiameter: Int = 0
  private var yDiameter: Int = 0

  def this(center: Point, circum: Point, color: Color) {
    this(color)
    val radius: Double = distance(center, circum)
    x = center.x - round(radius).toInt
    y = center.y - round(radius).toInt
    xDiameter = round(2.0 * radius).toInt
    yDiameter = xDiameter
  }

  def this(x: Int, y: Int, xDiameter: Int, color: Color) {
    this(color)
    this.x = x
    this.y = y
    this.xDiameter = xDiameter
    yDiameter = xDiameter
  }

  def getOrigin: Point = {
    new Point(x + round(xDiameter / 2), y + round(yDiameter / 2))
  }

  def setFilled(fill: Boolean) {
    filled = fill
  }

  def getBounds: AwtRectangle = {
    new AwtRectangle(x, y, xDiameter, yDiameter)
  }

  def modify(center: Point, circum: Point) {
    val radius: Double = distance(center, circum)
    x = center.x - round(radius).toInt
    y = center.y - round(radius).toInt
    xDiameter = round(2.0 * radius).toInt
    yDiameter = xDiameter
  }

  def reshapeElement(oldPoint: Point, newPoint: Point) {
    val change: Double = distance(getOrigin, newPoint)
    x = getOrigin.x - change.toInt
    y = getOrigin.y - change.toInt
    xDiameter = change.toInt * 2
    yDiameter = change.toInt * 2
  }

  def moveElement(xOffset: Int, yOffset: Int) {
    x += xOffset
    y += yOffset
  }

  def getHandles: Array[Point] = {
    val top = new Point(x + (xDiameter / 2), y)
    val left = new Point(x, y + (yDiameter / 2))
    val right = new Point(x + xDiameter, y + (yDiameter / 2))
    val bottom = new Point(x + (xDiameter / 2), y + yDiameter)
    Array[Point](top, left, right, bottom)
  }

  def contains(p: Point): Boolean = {
    val check: Ellipse2D.Double = new Ellipse2D.Double(x, y, xDiameter, yDiameter)
    check.contains(p.x, p.y)
  }

  def draw(g: GraphicsInterface, turtleColor: Color, scale: Double, angle: Double) {
    g.setColor(getColor(turtleColor))
    if (filled) {
      g.fillCircle(x, y, xDiameter, yDiameter, scale, angle)
    }
    else {
      g.drawCircle(x, y, xDiameter, yDiameter, scale, angle)
    }
  }

  def rotateLeft() = {
    val oldX: Int = x
    x = y
    y = Shape.Width - oldX - yDiameter
    val oldXDiameter: Int = xDiameter
    xDiameter = yDiameter
    yDiameter = oldXDiameter
  }

  def rotateRight() = {
    val oldX: Int = x
    x = Shape.Width - y - xDiameter
    y = oldX
    val oldXDiameter: Int = xDiameter
    xDiameter = yDiameter
    yDiameter = oldXDiameter
  }

  def flipHorizontal() = {
    x = Shape.Width - x - xDiameter
  }

  def flipVertical() = {
    y = Shape.Width - y - yDiameter
  }

  def toReadableString: String = {
    "Type: Circle, color: " + c + ",\n bounds: " + getBounds
  }

  override def toString: String = {
    "Circle " + c.getRGB + " " + filled + " " + marked + " " + x + " " + y + " " + xDiameter
  }

  def setModifiedPoint(modified: Point) {
  }
}
