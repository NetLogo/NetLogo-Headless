// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core

object ShapeList {
  val DefaultShapeName = "default"
  def isDefaultShapeName(name: String) =
    name == DefaultShapeName
  def sortShapes(unsortedShapes: Seq[Shape]): Seq[Shape] =
    collection.mutable.ArrayBuffer(unsortedShapes: _*)
      .sortBy(_.getName)
}

class ShapeList(val kind: core.AgentKind, _shapes: Seq[Shape]) {

  def this(kind: core.AgentKind) = this(kind, Seq())

  private val shapes = collection.mutable.HashMap[String, Shape]()

  _shapes.foreach(add)

  import ShapeList._

  def shape(name: String): Shape =
    shapes.get(name).getOrElse(shapes(DefaultShapeName))

  /** Returns vector of the list of shapes available to the current model */
  def getShapes: Seq[Shape] = {
    // leave out the default shape for now; we will add it later so that it is at the top of the list
    val currentShapes =
      shapes.values.toSeq.filterNot(s => isDefaultShapeName(s.getName))
    // make sure that the shape with the name DefaultShapeName is at the top of the list.
    shapes(DefaultShapeName) +: sortShapes(currentShapes)
  }

  /** Returns a set of the names of all available shapes */
  def getNames: Set[String] =
    shapes.keySet.toSet

  /** Returns true when a shape with the given name is already available to the current model */
  def exists(name: String) =
    shapes.contains(name)

  /** Clears the list of shapes currently available */
  def replaceShapes(newShapes: Iterable[Shape]) {
    shapes.clear()
    addAll(newShapes)
  }

  /** Adds a new shape to the ones currently available for use */
  def add(newShape: Shape): Shape = {
    val replaced = shapes.get(newShape.getName).orNull
    shapes(newShape.getName) = newShape
    replaced
  }

  /** Adds a collection of shapes to the ones currently available for use */
  def addAll(collection: Iterable[Shape]) {
    collection.foreach(add)
  }

  /** Removes a shape from those currently in use */
  def removeShape(shapeToRemove: Shape) = {
    val removed = shapes.get(shapeToRemove.getName).orNull
    shapes -= shapeToRemove.getName
    removed
  }
}
