// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import FrontEndInterface.ProceduresMap

trait CompilerUtilitiesInterface {
  def readFromString(source: String): AnyRef
  def readFromString(source: String, world: World, extensionManager: ExtensionManager): AnyRef
  def readNumberFromString(source: String, world: World, extensionManager: ExtensionManager): AnyRef
  @throws(classOf[java.io.IOException])
  def readFromFile(currFile: File, world: World, extensionManager: ExtensionManager): AnyRef
  def isReporter(s: String, program: Program, procedures: ProceduresMap, extensionManager: ExtensionManager): Boolean
}
