// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.PlotPenInterface
import scala.collection.immutable

trait PlotManagerInterface {
  def nextName: String
  def publish(action: PlotAction)
  def currentPlot: Option[PlotInterface]
  def setCurrentPlot(name: String)
  def hasPlot(name: String): Boolean
  def getPlotNames: Seq[String]
}

trait PlotInterface {
  def name: String
  def getPen(pen: String): Option[PlotPenInterface]
  def currentPen: Option[PlotPenInterface]
  def currentPenByName: String
  def currentPenByName_=(pen: String)
  def legendIsOpen_=(open: Boolean)
  var state: PlotState
  def plot(y: Double)
  def plot(x: Double, y: Double)
  def histogramActions(pen: PlotPenInterface, values: Seq[Double]): immutable.Seq[PlotAction]
}

case class PlotState(
  autoPlotOn: Boolean = true,
  xMin: Double = 0,
  xMax: Double = 10,
  yMin: Double = 0,
  yMax: Double = 10
)
