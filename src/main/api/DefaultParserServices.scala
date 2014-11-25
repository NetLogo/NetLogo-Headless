// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.{CompilerUtilitiesInterface, FrontEndInterface, Program}

// We use this in contexts where we want to do compiler stuff (not full compilation) like
// colorization but it's OK to assume that we are 2D not 3D and no extensions are loaded.  The
// HubNet client is one such context; also various testing contexts; also when reading
// BehaviorSpace XML. - ST 2/23/09, 3/4/09

class DefaultParserServices(utils: CompilerUtilitiesInterface) extends ParserServices {
  def readNumberFromString(source: String) =
    utils.readNumberFromString(source)
  def readFromString(source: String) =
    utils.readFromString(source)
  def isReporter(s: String) =
    utils.isReporter(s, Program.empty(),
      FrontEndInterface.NoProcedures,
      new DummyExtensionManager)
}
