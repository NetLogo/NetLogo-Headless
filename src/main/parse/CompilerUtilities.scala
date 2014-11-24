// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.{ core, api, agent },
  api.CompilerUtilitiesInterface,
  agent.LiteralAgentParser

object CompilerUtilities extends CompilerUtilitiesInterface {

  import api.FrontEndInterface.ProceduresMap
  import FrontEnd.tokenizer

  // well this is pretty ugly.  LiteralParser and LiteralAgentParser call each other,
  // so they're hard to instantiate, but we "tie the knot" using lazy val. - ST 5/3/13
  val literalParser: (api.World, api.ExtensionManager) => LiteralParser = {(world, extensionManager) =>
    import core.Token
    val agentParserCreator =
      (f: (Token, Iterator[Token]) => AnyRef) => new agent.LiteralAgentParser(world, f)
    new LiteralParser(world, extensionManager, agentParserCreator)
  }

  // In the following 3 methods, the initial call to NumberParser is a performance optimization.
  // During import-world, we're calling readFromString over and over again and most of the time
  // the result is a number.  So we try the fast path through NumberParser first before falling
  // back to the slow path where we actually tokenize. - ST 4/7/11

  def readFromString(source: String): AnyRef =
    core.NumberParser.parse(source).right.getOrElse(
      new LiteralParser(null, null, null)
        .getLiteralValue(tokenizer.tokenizeString(source)
          .map(Namer0)))

  def readFromString(source: String, world: api.World, extensionManager: api.ExtensionManager): AnyRef =
    core.NumberParser.parse(source).right.getOrElse(
      literalParser(world, extensionManager)
        .getLiteralValue(tokenizer.tokenizeString(source)
          .map(Namer0)))

  def readNumberFromString(source: String, world: api.World, extensionManager: api.ExtensionManager): java.lang.Double =
    core.NumberParser.parse(source).right.getOrElse(
      literalParser(world, extensionManager)
        .getNumberValue(tokenizer.tokenizeString(source)
          .map(Namer0)))

  @throws(classOf[java.io.IOException])
  def readFromFile(currFile: api.File, world: api.World, extensionManager: api.ExtensionManager): AnyRef = {
    val tokens: Iterator[core.Token] =
      new TokenReader(currFile, tokenizer)
        .map(Namer0)
    val result =
      literalParser(world, extensionManager)
        .getLiteralFromFile(tokens)
    // now skip whitespace, so that the model can use file-at-end? to see whether there are any
    // more values left - ST 2/18/04
    // org.nlogo.util.File requires us to maintain currFile.pos ourselves -- yuck!!! - ST 8/5/04
    var done = false
    while(!done) {
      currFile.reader.mark(1)
      currFile.pos += 1
      val i = currFile.reader.read()
      if(i == -1 || !Character.isWhitespace(i)) {
        currFile.reader.reset()
        currFile.pos -= 1
        done = true
      }
    }
    result
  }

  // used by CommandLine
  def isReporter(s: String, program: api.Program, procedures: ProceduresMap, extensionManager: api.ExtensionManager) =
    try {
      val sp = new StructureParser(
        tokenizer.tokenizeString("to __is-reporter? report " + s + "\nend")
          .map(Namer0),
        None, api.StructureResults(program, procedures))
      val results = sp.parse(subprogram = true)
      val namer =
        new Namer(program, procedures ++ results.procedures, extensionManager)
      val proc = results.procedures.values.head
      val tokens = namer.process(results.procedureTokens(proc.name).iterator, proc)
      tokens.toStream
        .drop(1)  // skip _report
        .dropWhile(_.tpe == core.TokenType.OpenParen)
        .headOption
        .exists(isReporterToken)
    }
    catch { case _: api.CompilerException => false }

  private def isReporterToken(token: core.Token): Boolean = {
    import core.TokenType._
    token.tpe match {
      case OpenBracket | Literal | Ident =>
        true
      case Reporter =>
        !token.value.isInstanceOf[core.prim._letvariable]
      case _ =>
        false
    }
  }

}
