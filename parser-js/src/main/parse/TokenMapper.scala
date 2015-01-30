// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{Command, Reporter, TokenHolder}

//TODO: These constructor parameters are unused, it would be better to have this as an object
class TokenMapper(location: String, prefix: String) {
  def getCommand(s: String): Option[TokenHolder] =
    commands.get(s.toUpperCase).map(_())
  def getReporter(s: String): Option[TokenHolder] =
    reporters.get(s.toUpperCase).map(_())

  val reporters:Map[String, () => TokenHolder] = TokenClasses.compiledReporters[TokenHolder]("org.nlogo.core.prim")
  val commands:Map[String, () => TokenHolder] = TokenClasses.compiledCommands[TokenHolder]("org.nlogo.core.prim")

  def allCommandNames = commands.keySet
  def allReporterNames = reporters.keySet
  // for integration testing
  def allCommandClassNames = commands.values.toSet
  def allReporterClassNames = reporters.values.toSet

  def checkInstructionMaps() {
    commands.keySet.foreach(getCommand)
    reporters.keySet.foreach(getReporter)
  }
}
