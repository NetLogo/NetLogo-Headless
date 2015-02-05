// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo
package org.nlogo.shape

import org.nlogo.api.GraphicsInterface
import java.awt.Color
import java.util.{ List => JList, ArrayList => JArrayList }
import java.util.Observable
import org.nlogo.core.Shape

@SerialVersionUID(0L)
object VectorShape {
  private object Recolorable extends Enumeration {
    type Recolorable = Value
    val UNKNOWN, TRUE, FALSE = Value
  }

  private[shape] def getString(v: Array[String], index: Int): String = {
    if ((null != v) && (v.length > index)) {
      return v(index)
    }
    ""
  }

  def parseShape(shapes: Array[String], version: String, shape: VectorShape, startingIndex: Int): Int = {
    var index = startingIndex
    // Read in the name and rotatability of a shape
    shape.setName(getString(shapes, index))
    index+= 1
    if (shape.getName.contains("StarLogoT")) {
      // oops, it's not really shapes, it's the version line of a
      // StarLogoT model... so ignore it
      throw new IllegalStateException("found StarLogoT version instead of shape")
    }
    shape.setRotatable(getString(shapes, index).equals("true"))
    index += 1
    val rgb = Integer.valueOf(getString(shapes, index)).intValue();
    index += 1
    shape.setEditableColorIndex(rgb)
    // Read in the elements of that shape
    while (0 != getString(shapes, index).length()) {
      shape.addElement(getString(shapes, index))
      index += 1
    }
    index
  }

  def getDefaultShape: VectorShape = {
    val result: VectorShape = new VectorShape
    result.setName(org.nlogo.core.ShapeList.DefaultShapeName)
    result.setRotatable(true)
    result.setEditableColorIndex(0)
    result.addElement("Polygon -7500403 true true 150 5 40 250 150 205 260 250")
    return result
  }

  val TURTLE_WIDTH: Int = 25
  val CLOSE_ENOUGH: Int = 10
  val NUM_GRID_LINES: Int = 20
}

@SerialVersionUID(0L)
class VectorShape extends Observable with org.nlogo.core.Shape with Cloneable with java.io.Serializable with DrawableShape {
  protected var name: String = ""

  def setName(name: String) {
    this.name = name
  }

  def getName: String = {
    name
  }

  protected var editableColorIndex: Int = 0
  protected var elementList: JList[Element] = new JArrayList[Element]
  protected var rotatable: Boolean = true

  def setEditableColorIndex(editableColorIndex: Int) {
    this.editableColorIndex = editableColorIndex
  }

  def getEditableColorIndex: Int = {
    editableColorIndex
  }

  override def clone: AnyRef = {
    var newShape: VectorShape = null
    try {
      newShape = super.clone.asInstanceOf[VectorShape]
    } catch {
      case ex: CloneNotSupportedException => throw new IllegalStateException(ex)
    }
    newShape.elementList = new JArrayList[Element]
    import scala.collection.JavaConversions._
    for (e <- elementList) {
      newShape.elementList.add(e.clone.asInstanceOf[Element])
    }
    newShape
  }

  def setOutline() = {
    import scala.collection.JavaConversions._
    for (e <- elementList) {
      e.setFilled(false)
    }
  }

  def getElements: JList[Element] = {
    elementList
  }

  def setRotatable(rotatable: Boolean) {
    this.rotatable = rotatable
  }

  def isRotatable: Boolean = {
    rotatable
  }

  private var _fgRecolorable: VectorShape.Recolorable.Recolorable = VectorShape.Recolorable.UNKNOWN

  def fgRecolorable: Boolean = {
    if (_fgRecolorable eq VectorShape.Recolorable.UNKNOWN) {
      _fgRecolorable = VectorShape.Recolorable.FALSE
      val editableColor: Color = new Color(org.nlogo.api.Color.getARGBByIndex(editableColorIndex))
      val n: Int = elementList.size
      for (i <- 0 until elementList.size()) {
        if (element(i).getColor == editableColor) {
          _fgRecolorable = VectorShape.Recolorable.TRUE
          return true
        }
      }
    }
    _fgRecolorable == VectorShape.Recolorable.TRUE
  }

  def markRecolorableElements(editableColor: Color, editableColorIndex: Int) {
    this.editableColorIndex = editableColorIndex
    _fgRecolorable = VectorShape.Recolorable.FALSE
    for (i <- 0 until elementList.size) {
      if (element(i).getColor == editableColor) {
        element(i).setMarked(true)
        _fgRecolorable = VectorShape.Recolorable.TRUE
      } else {
        element(i).setMarked(false)
      }
    }
  }

  def isTooSimpleToCache: Boolean = {
    elementList.size match {
      case 0 => true
      case 1 =>
        !element(0).isInstanceOf[Polygon]
      case _ =>
        false
    }
  }

  protected def element(i: Int): Element = {
    elementList.get(i)
  }

  def remove(element: Element) {
    if (elementList.remove(element)) {
      setChanged()
      notifyObservers()
    }
  }

  def changed() = {
    setChanged()
    notifyObservers()
  }

  def removeLast() = {
    if (!elementList.isEmpty) {
      elementList.remove(elementList.size - 1)
      setChanged()
      notifyObservers()
    }
  }

  def removeAll() = {
    if (!elementList.isEmpty) {
      elementList.clear()
      setChanged()
      notifyObservers()
    }
  }

  def add(element: Element) {
    elementList.add(element)
    element match {
      case rectangle: Rectangle => rectangle.setMaxsAndMins()
      case _ =>
    }
    setChanged()
    notifyObservers()
  }

  def addAtPosition(index: Int, element: Element) = {
    elementList.add(index, element)
    setChanged()
    notifyObservers()
  }

  def rotateLeft() = {
    for (i <- 0 until elementList.size) {
      element(i).rotateLeft()
    }
  }

  def rotateRight() = {
    for (i <- 0 until elementList.size) {
      element(i).rotateRight()
    }
  }

  def flipHorizontal() = {
    for (i <- 0 until elementList.size) {
      element(i).flipHorizontal()
    }
  }

  def flipVertical() = {
    for (i <- 0 until elementList.size) {
      element(i).flipVertical()
    }
  }

  def paint(g: GraphicsInterface, turtleColor: Color, x: Double, y: Double, size: Double, cellSize: Double, angle: Int, lineThickness: Double) {
    var rotationAngle = angle
    g.push()
    val scale: Double = size * cellSize
    if (!isRotatable)
      rotationAngle = 0
    try {
      if (rotationAngle != 0) {
        g.rotate(rotationAngle / 180.0 * StrictMath.PI, x, y, scale)
      }
      g.translate(x, y)
      g.scale(scale, scale, Shape.Width)
      g.setStrokeFromLineThickness(lineThickness, scale, cellSize, Shape.Width)
      for (i <- 0 until elementList.size()) {
        element(i).draw(g, turtleColor, scale, rotationAngle)
      }
    } finally {
      g.pop()
    }
  }

  def paint(g: GraphicsInterface, turtleColor: Color, x: Int, y: Int, cellSize: Double, angle: Int) {
    paint(g, turtleColor, x, y, 1, cellSize, angle, 0.0f)
  }

  def toReadableString: String = {
    var ret: String = "Shape " + name + ":\n"
    for (i <- 0 until elementList.size()) {
      ret += elementList.get(i).toString
    }
    ret
  }

  override def toString: String = {
    var ret: String = name + "\n" + rotatable + "\n" + editableColorIndex
    for (i <- 0 until elementList.size()) {
      val elt: Element = elementList.get(i)
      if (elt.shouldSave) {
        ret += "\n" + elt.toString
      }
    }
    ret
  }

  def addElement(line: String) {
    var element: Element = null
    if (line.startsWith("Line")) {
      element = org.nlogo.shape.Line.parseLine(line)
    }
    else if (line.startsWith("Rectangle")) {
      element = org.nlogo.shape.Rectangle.parseRectangle(line)
    }
    else if (line.startsWith("Circle")) {
      element = org.nlogo.shape.Circle.parseCircle(line)
    }
    else if (line.startsWith("Polygon")) {
      element = org.nlogo.shape.Polygon.parsePolygon(line)
    }
    else {
      throw new IllegalStateException("Invalid shape format in file: " + line)
    }
    if (element != null) {
      add(element)
    }
  }
}
