// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.{FrontEndInterface, Program, LiteralImportHandler, Token}
import FrontEndInterface.ProceduresMap

import org.nlogo.core.{Program, LiteralImportHandler, Token}

trait CompilerUtilitiesInterface {
  def readFromString(source: String): AnyRef

  def readNumberFromString(source: String): AnyRef

  def readFromString(source: String, importHandler: LiteralImportHandler): AnyRef

  def readNumberFromString(source: String, importHandler: LiteralImportHandler): AnyRef

  @throws(classOf[java.io.IOException])
  def readFromFile(currFile: File, importHandler: LiteralImportHandler): AnyRef

  def isReporter(s: String, program: Program, procedures: ProceduresMap, extensionManager: ExtensionManager): Boolean
}
