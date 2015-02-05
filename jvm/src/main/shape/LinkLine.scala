// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo
package org.nlogo.shape

import org.nlogo.api.GraphicsInterface
import java.awt.geom.AffineTransform
import java.awt.geom.QuadCurve2D
import java.awt.{ Shape => JShape }
import java.util.StringTokenizer
import java.awt.Color

@SerialVersionUID(0L)
object LinkLine {
  val dashChoices: Array[Array[Float]] = Array(
    Array(0.0f, 1.0f),
    Array(1.0f, 0.0f),
    Array(2.0f, 2.0f),
    Array(4.0f, 4.0f),
    Array(4.0f, 4.0f, 2.0f, 2.0f))

  private def getDashIndex(d2: Array[Float]): Int = {
    var success: Boolean = false;
    {
      var i: Int = 0
      while (i < dashChoices.length) {
        {
          if (d2.length == dashChoices(i).length) {
            {
              var j: Int = 0
              while (j < d2.length) {
                {
                  success = d2(j) == dashChoices(i)(j)
                }
                ({
                  j += 1; j - 1
                })
              }
            }
            if (success) {
              return i
            }
          }
        }
        ({
          i += 1; i - 1
        })
      }
    }
    return 1
  }

  def parseLine(shapes: Array[String], version: String, line: LinkLine, index: Int): Int = {
    val tokenizer: StringTokenizer = new StringTokenizer(shapes(index))
    line.xcor = tokenizer.nextToken.toDouble
    line.isVisible = tokenizer.nextToken.toInt != 0
    if (line.isVisible) {
      val d: Array[Float] = (for (i <- 0 until tokenizer.countTokens) yield tokenizer.nextToken().toFloat).toArray
      line.dashes = dashChoices(getDashIndex(d))
    }
    else line.dashes = dashChoices(0)
    index + 1
  }
}

@SerialVersionUID(0L)
class LinkLine(var xcor: Double = 0,
               var isVisible: Boolean = false,
               var dashes:Array[Float] = LinkLine.dashChoices(1)) extends java.io.Serializable with Cloneable {

  def dashIndex: Int = {
    for (i <- 0 until LinkLine.dashChoices.length) {
      if (dashes eq LinkLine.dashChoices(i)) {
        return i
      }
    }
    1
  }

  def this() = this(0, false, LinkLine.dashChoices(1))

  def this(xcor: Double, isVisible: Boolean) = {
    this(xcor, isVisible,
      dashes = LinkLine.dashChoices(if (isVisible) 1 else 0))
  }

  def isStraightPlainLine: Boolean = {
    isVisible && (xcor == 0) && (dashes.length == 2) && (dashes(0) == 1) && (dashes(1) == 0)
  }

  def setVisible(isVisible: Boolean) {
    this.isVisible = isVisible
  }

  def paint(g: GraphicsInterface, color: Color, cellSize: Double, strokeWidth: Float, shape: JShape) {
    g.setColor(color)
    g.setStroke(strokeWidth, dashes)
    g.draw(shape)
  }

  def getShape(x1: Double, y1: Double, x2: Double, y2: Double, curviness: Double, size: Double, cellSize: Double, stroke: Float): JShape = {
    val ycomp: Double = (x1 - x2) / size
    val xcomp: Double = (y2 - y1) / size
    val trans: AffineTransform = AffineTransform.getTranslateInstance(xcomp * xcor * stroke, ycomp * xcor * stroke)
    val midX: Double = ((x1 + x2) / 2) + (curviness * xcomp)
    val midY: Double = ((y1 + y2) / 2) + (curviness * ycomp)
    trans.createTransformedShape(new QuadCurve2D.Double(x1, y1, midX, midY, x2, y2))
  }

  def getDashes: Array[Float] = dashes

  def setDashiness(dashes: Array[Float]) {
    this.dashes = dashes
  }

  def setDashes(str: String) {
    val tokenizer: StringTokenizer = new StringTokenizer(str)
    dashes = (for (i <- 0 until tokenizer.countTokens()) yield tokenizer.nextToken.toFloat).toArray
  }

  def dashinessString: String = dashes.mkString(" ")

  override def clone: AnyRef = {
    var line: LinkLine = null
    try {
      line = super.clone.asInstanceOf[LinkLine]
    } catch {
      case ex: CloneNotSupportedException => throw new IllegalStateException(ex)
    }
    line.dashes = dashes
    line
  }

  override def toString: String = {
    s"$xcor ${if (isVisible) "1" else "0"} $dashinessString"
  }

  def toReadableString: String = {
    s"Link Line with xcor = $xcor $isVisible $dashinessString"
  }
}
