// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo
package org.nlogo.shape

import org.nlogo.api.GraphicsInterface
import org.nlogo.core.Shape
import java.awt.{ Color, Point, Rectangle => AwtRectangle }
import java.util.{ List => JList, ArrayList => JArrayList }

// Note: currently I think this is used only as an abstract superclass
// for Polygon.  Neither Steph nor I really knows why -- not sure if
// it's just a historical thing or if it actually makes sense that way.
//  - ST 6/11/04
// If this were to become an element type in its own right, then this
// is the method that would be used to draw it:
// drawArc( int x, int y, int width, int height, int startAngle, int arcAngle )
// except this doesn't allow for rotation.  Presumably Graphics2D has a similar
// method that would draw a rotated arc? - SAB/ST 6/11/04
@SerialVersionUID(0L)
abstract class Curve(color: Color) extends Element(color) with Cloneable {
  var xcoords = Array[Integer]()
  var ycoords = Array[Integer]()
  private var xmin: Int = 0
  private var xmax: Int = 0
  private var ymin: Int = 0
  private var ymax: Int = 0

  def setFilled(fill: Boolean) {
  }

  def this(start: Point, next: Point, color: Color) {
    this(color)
    xcoords :+ Integer.valueOf(start.x)
    ycoords :+ Integer.valueOf(start.y)
    xcoords :+ Integer.valueOf(next.x)
    ycoords :+ Integer.valueOf(next.y)
    xmin = start.x
    xmax = start.x
    ymin = start.y
    ymax = start.y
    updateBounds(next)
  }

  def getBounds: AwtRectangle = {
    createRect(new Point(xmin, ymin), new Point(xmax, ymax))
  }

  def modify(start: Point, next: Point) {
    xcoords :+ Integer.valueOf(next.x)
    ycoords :+ Integer.valueOf(next.y)
    updateBounds(next)
  }

  // this is a transition step
  def getAElt(i: Int, a: Array[Integer]): Int = {
    a(i).intValue()
  }

  def draw(g: GraphicsInterface, turtleColor: Color, scale: Double, angle: Double) {
    val xArray: Array[Int] = new Array[Int](xcoords.size)
    val yArray: Array[Int] = new Array[Int](xcoords.size)
    for (i <- 0 until xcoords.size) {
      xArray(i) = getAElt(i, xcoords)
      yArray(i) = getAElt(i, ycoords)
    }
    g.setColor(getColor)
    g.drawPolyline(xArray, yArray, xcoords.size)
  }

  def rotateLeft() = {
    for (i <- 0 until xcoords.size) {
      val temp: Int = getAElt(i, xcoords)
      xcoords(i) = Integer.valueOf(getAElt(i, ycoords))
      ycoords(i) = Integer.valueOf(Shape.Width - temp)
    }
  }

  def rotateRight() = {
    for (i <- 0 until xcoords.size) {
      val temp: Int = getAElt(i, xcoords)
      xcoords(i) = Integer.valueOf(Shape.Width - getAElt(i, ycoords))
      ycoords(i) = Integer.valueOf(temp)
    }
  }

  def flipHorizontal() = {
    for (i <- 0 until xcoords.size) {
      xcoords(i) = Integer.valueOf(Shape.Width - getAElt(i, xcoords))
    }
  }

  def flipVertical() = {
    for (i <- 0 until ycoords.size)  {
      ycoords(i) = Integer.valueOf(Shape.Width - getAElt(i, ycoords))
    }
  }

  private def updateBounds(newPoint: Point) {
    xmin = StrictMath.min(xmin, newPoint.x)
    xmax = StrictMath.max(xmax, newPoint.x)
    ymin = StrictMath.min(ymin, newPoint.y)
    ymax = StrictMath.max(ymax, newPoint.y)
  }

  def toReadableString: String = {
    "Type: Curve, color: " + c + ",\n bounds: " + getBounds
  }
}
