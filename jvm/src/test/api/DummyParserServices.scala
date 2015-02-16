// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.io.IOException

import org.nlogo.core,
  core.{Program, File, LiteralImportHandler, FrontEndInterface, CompilerUtilitiesInterface, CompilerException, LogoList, Nobody},
    FrontEndInterface.ProceduresMap
// just enough functionality to make the tests pass

class DummyParserServices extends CompilerUtilitiesInterface {
  private def unsupported = throw new UnsupportedOperationException
  def readFromString(s: String): AnyRef =
    try { s.toDouble: java.lang.Double }
    catch {
      case ex: NumberFormatException =>
        s match {
          case "true" => true: java.lang.Boolean
          case "false" => false: java.lang.Boolean
          case "nobody" => Nobody
          case _ if s.head == '[' && s.charAt(1) == '[' =>  LogoList(readFromString(s.tail.init))
          case _ if s.head == '[' =>  LogoList(s.tail.init.split(" ").map(readFromString): _*)
          case _ if s.head == '"' && s.last == '"' =>  s.tail.init
          case _ => throw new CompilerException(
            s + " not a constant recognized by DummyParserServices", 0, s.size, "")
        }
    }
  def readNumberFromString(source: String) = source

  override def readFromString(source: String, importHandler: LiteralImportHandler): AnyRef = unsupported

  override def isReporter(s: String, program: Program, procedures: ProceduresMap, extensionManager: core.ExtensionManager): Boolean = unsupported

  @throws(classOf[IOException])
  override def readFromFile(currFile: File, importHandler: LiteralImportHandler): AnyRef = unsupported

  override def isReporter(s: String): Boolean = unsupported

  override def readNumberFromString(source: String, importHandler: LiteralImportHandler): AnyRef = unsupported
}
