// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo
package org.nlogo.shape

import org.nlogo.api.GraphicsInterface
import org.nlogo.core.Shape
import java.awt.{Point, Color, Rectangle => AwtRectangle}
import java.util.StringTokenizer

@SerialVersionUID(0L)
object Rectangle {
  def parseRectangle(text: String): Rectangle = {
    val tokenizer: StringTokenizer = new StringTokenizer(text)
    tokenizer.nextToken
    val color: String = tokenizer.nextToken
    val b1: Boolean = tokenizer.nextToken == "true"
    val b2: Boolean = tokenizer.nextToken == "true"
    val x1: Int = Integer.valueOf(tokenizer.nextToken).intValue
    val y1: Int = Integer.valueOf(tokenizer.nextToken).intValue
    val x2: Int = Integer.valueOf(tokenizer.nextToken).intValue
    val y2: Int = Integer.valueOf(tokenizer.nextToken).intValue
    if (x1 == x2 && y1 == y2) {
      return null
    }
    val rect: Rectangle = new Rectangle(new Point(x1, y1), new Point(x2, y2), java.awt.Color.decode(color))
    rect.setFilled(b1)
    rect.setMarked(b2)
    rect
  }
}

@SerialVersionUID(0L)
class Rectangle(color: Color) extends Element(color) with Cloneable {

  def this(start: Point, end: Point, color: Color) {
    this(color)
    upperLeft = new Point(start)
    upperRight = new Point(end.x, start.y)
    lowerLeft = new Point(start.x, end.y)
    lowerRight = new Point(end)
  }

  protected var upperLeft: Point = null
  protected var upperRight: Point = null
  protected var lowerRight: Point = null
  protected var lowerLeft: Point = null
  protected var xmin: Int = 0
  protected var xmax: Int = 0
  protected var ymin: Int = 0
  protected var ymax: Int = 0
  private var modifiedPoint: String = null

  def getX: Int =
    upperLeft.x

  def getY: Int =
    upperLeft.y

  def getWidth: Int = {
    lowerRight.x - upperLeft.x
  }

  def getHeight: Int = {
    lowerRight.y - upperLeft.y
  }

  def getCorners: Array[Point] = {
    Array[Point](upperLeft, lowerRight)
  }

  def setFilled(fill: Boolean) {
    filled = fill
  }

  override def clone: AnyRef = {
    val newRect: Rectangle = super.clone.asInstanceOf[Rectangle]
    newRect.upperLeft = newRect.upperLeft.clone.asInstanceOf[Point]
    newRect.upperRight = newRect.upperRight.clone.asInstanceOf[Point]
    newRect.lowerLeft = newRect.lowerLeft.clone.asInstanceOf[Point]
    newRect.lowerRight = newRect.lowerRight.clone.asInstanceOf[Point]
    newRect
  }

  def getBounds: AwtRectangle = {
    setMaxsAndMins
    new AwtRectangle(xmin, ymin, xmax - xmin, ymax - ymin)
  }

  def contains(p: Point): Boolean = getBounds.contains(p)

  def modify(start: Point, last: Point) {
    val width: Int = StrictMath.abs(start.x - last.x)
    val height: Int = StrictMath.abs(start.y - last.y)
    upperLeft.x = StrictMath.min(start.x, last.x)
    upperLeft.y = StrictMath.min(start.y, last.y)
    upperRight.x = upperLeft.x + width
    upperRight.y = upperLeft.y
    lowerRight.x = upperLeft.x + width
    lowerRight.y = upperLeft.y + height
    lowerLeft.x = upperLeft.x
    lowerLeft.y = upperLeft.y + height
  }

  def reshapeElement(oldPoint: Point, newPoint: Point) {
    if (modifiedPoint == "upperLeft") {
      upperLeft = newPoint
      lowerLeft.x = newPoint.x
      upperRight.y = newPoint.y
    }
    if (modifiedPoint == "upperRight") {
      upperRight = newPoint
      lowerRight.x = newPoint.x
      upperLeft.y = newPoint.y
    }
    if (modifiedPoint == "lowerRight") {
      lowerRight = newPoint
      upperRight.x = newPoint.x
      lowerLeft.y = newPoint.y
    }
    if (modifiedPoint == "lowerLeft") {
      lowerLeft = newPoint
      upperLeft.x = newPoint.x
      lowerRight.y = newPoint.y
    }
    xmin = upperLeft.x
    xmax = upperRight.x
    ymin = upperLeft.y
    ymax = lowerLeft.y
  }

  def moveElement(xOffset: Int, yOffset: Int) {
    upperLeft.x += xOffset
    upperLeft.y += yOffset
    upperRight.x += xOffset
    upperRight.y += yOffset
    lowerLeft.x += xOffset
    lowerLeft.y += yOffset
    lowerRight.x += xOffset
    lowerRight.y += yOffset
  }

  def getHandles: Array[Point] = {
    val xcoords: Array[Int] = Array(upperLeft.x, upperRight.x, lowerRight.x, lowerLeft.x)
    val ycoords: Array[Int] = Array(upperLeft.y, upperRight.y, lowerRight.y, lowerLeft.y)
    (xcoords zip ycoords).map {
      case (x, y) => new Point(x, y)
    }.toArray
  }

  def setMaxsAndMins() = {
    val xcoords: Array[Int] = Array(upperLeft.x, upperRight.x, lowerRight.x, lowerLeft.x)
    val ycoords: Array[Int] = Array(upperLeft.y, upperRight.y, lowerRight.y, lowerLeft.y)
    xmin = min(xcoords)
    xmax = max(xcoords)
    ymin = min(ycoords)
    ymax = max(ycoords)
  }

  def draw(g: GraphicsInterface, turtleColor: Color, scale: Double, angle: Double) {
    g.setColor(getColor(turtleColor))
    if (filled)
      g.fillRect(upperLeft.x, upperLeft.y, upperRight.x - upperLeft.x, lowerLeft.y - upperLeft.y, scale, angle)
    else
      g.drawRect(upperLeft.x, upperLeft.y, upperRight.x - upperLeft.x, lowerLeft.y - upperLeft.y, scale, angle)
  }

  def rotateLeft() = {
    val temp: Point = lowerLeft
    lowerLeft = upperLeft
    upperLeft = upperRight
    upperRight = lowerRight
    lowerRight = temp
    var temp2: Int = 0
    temp2 = upperLeft.x
    upperLeft.x = upperLeft.y
    upperLeft.y = Shape.Width - temp2
    temp2 = upperRight.x
    upperRight.x = upperRight.y
    upperRight.y = Shape.Width - temp2
    temp2 = lowerLeft.x
    lowerLeft.x = lowerLeft.y
    lowerLeft.y = Shape.Width - temp2
    temp2 = lowerRight.x
    lowerRight.x = lowerRight.y
    lowerRight.y = Shape.Width - temp2
  }

  def rotateRight() = {
    val temp: Point = upperLeft
    upperLeft = lowerLeft
    lowerLeft = lowerRight
    lowerRight = upperRight
    upperRight = temp
    var temp2: Int = 0
    temp2 = upperLeft.x
    upperLeft.x = Shape.Width - upperLeft.y
    upperLeft.y = temp2
    temp2 = lowerLeft.x
    lowerLeft.x = Shape.Width - lowerLeft.y
    lowerLeft.y = temp2
    temp2 = upperRight.x
    upperRight.x = Shape.Width - upperRight.y
    upperRight.y = temp2
    temp2 = lowerRight.x
    lowerRight.x = Shape.Width - lowerRight.y
    lowerRight.y = temp2
  }

  def flipHorizontal() = {
    var temp: Point = upperLeft
    upperLeft = upperRight
    upperRight = temp
    temp = lowerLeft
    lowerLeft = lowerRight
    lowerRight = temp
    upperLeft.x = Shape.Width - upperLeft.x
    upperRight.x = Shape.Width - upperRight.x
    lowerLeft.x = Shape.Width - lowerLeft.x
    lowerRight.x = Shape.Width - lowerRight.x
  }

  def flipVertical() = {
    var temp: Point = upperLeft
    upperLeft = lowerLeft
    lowerLeft = temp
    temp = lowerRight
    lowerRight = upperRight
    upperRight = temp
    upperLeft.y = Shape.Width - upperLeft.y
    upperRight.y = Shape.Width - upperRight.y
    lowerLeft.y = Shape.Width - lowerLeft.y
    lowerRight.y = Shape.Width - lowerRight.y
  }

  def toReadableString: String =
    "Type: Rectangle, color: " + c + ",\n bounds: " + getBounds

  override def toString: String =
    "Rectangle " + c.getRGB + " " + filled + " " + marked + " " + upperLeft.x + " " + upperLeft.y + " " + lowerRight.x + " " + lowerRight.y

  def setModifiedPoint(modified: Point) {
    if (modified == upperLeft)
      modifiedPoint = "upperLeft"
    else if (modified == upperRight)
      modifiedPoint = "upperRight"
    else if (modified == lowerRight)
      modifiedPoint = "lowerRight"
    else if (modified == lowerLeft)
      modifiedPoint = "lowerLeft"
  }
}
