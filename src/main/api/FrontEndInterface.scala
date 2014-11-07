// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo
package api

import org.nlogo.api.FrontEndInterface.{ ProceduresMap, NoProcedures }
import org.nlogo.core.{Syntax, Token}

trait FrontEndProcedure {
  def syntax: core.Syntax
  def name: String
  def isReporter: Boolean
  def displayName: String
  def filename: String
  def nameToken: core.Token
  def argTokens: Seq[core.Token]
  var args = Vector[String]()
  var topLevel = false
  def dump: String
}

object FrontEndInterface {
  // use ListMap so procedures come out in the order they were defined (users expect errors in
  // earlier procedures to be reported first) - ST 6/10/04, 8/3/12
  import scala.collection.immutable.ListMap
  type ProceduresMap = ListMap[String, api.FrontEndProcedure]
  val NoProcedures: ProceduresMap = ListMap()
}

trait FrontEndInterface {
  def readFromString(source: String): AnyRef
  def readFromString(source: String, world: World, extensionManager: ExtensionManager): AnyRef
  def readNumberFromString(source: String, world: World, extensionManager: ExtensionManager): AnyRef
  @throws(classOf[java.io.IOException])
  def readFromFile(currFile: org.nlogo.api.File, world: World, extensionManager: ExtensionManager): AnyRef
  def isReporter(s: String, program: Program, procedures: ProceduresMap, extensionManager: ExtensionManager): Boolean
}
