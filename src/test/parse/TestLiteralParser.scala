// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.scalatest.{Tag, Documenter, FunSuite}
import org.nlogo.api.{Dump, ExtensionObject, ExtensionManager, LogoList,
                      World, CompilerUtilitiesInterface, CompilerException},
  CompilerUtilitiesInterface.AgentParserCreator
import org.nlogo.util.MockSuite

class TestLiteralParser extends FunSuite with MockSuite {

  lazy val defaultWorld: World = mock[World]

  val dummyAlternateParser: AgentParserCreator = {
    w => readLitPrefix => toks => {
      // if we are in here, it means something has gone wrong
      throw new Exception("LiteralParser attempted to parse a literal agent or agentset unexpectedly")
    }
  }

  def compilerUtilities(apc: AgentParserCreator) = new CompilerUtilities(apc)

  import org.nlogo.core.Token
  def tokenizeString(input: String): Iterator[Token] =
    FrontEnd.tokenizer.tokenizeString(input).map(Namer0)

  def toLiteral(input: String,
                 world: World = defaultWorld,
                 extensionManager: ExtensionManager = null,
                 agentParserCreator: AgentParserCreator = dummyAlternateParser): AnyRef = {
    val compilerUtils = compilerUtilities(agentParserCreator)
    compilerUtils.literalParser(world, extensionManager, compilerUtils.agentParserCreator(world))
      .getLiteralValue(tokenizeString(input))
  }

  def toLiteralList(input: String, world: World = defaultWorld): LogoList = {
    val compilerUtils = compilerUtilities(dummyAlternateParser)
    val tokens = FrontEnd.tokenizer.tokenizeString(input).map(Namer0)
    val (result, _) = compilerUtils.literalParser(world, null, compilerUtils.agentParserCreator(world)).parseLiteralList(tokens.next(), tokens)
    result
  }

  def testError(input: String, error: String, world: World = defaultWorld) {
    val e = intercept[CompilerException] {
      toLiteral(input, world)
    }
    assertResult(error)(e.getMessage)
  }

  // everything here is a mockTest because we're mocking out api.World
  mockTest("booleanTrue") { assertResult(java.lang.Boolean.TRUE)(toLiteral("true")) }
  mockTest("booleanFalse") { assertResult(java.lang.Boolean.FALSE)(toLiteral("false")) }
  mockTest("literalInt") { assertResult(Double.box(4))(toLiteral("4")) }
  mockTest("literalIntWhitespace") { assertResult(Double.box(4))(toLiteral("  4\t")) }
  mockTest("literalIntParens") { assertResult(Double.box(4))(toLiteral(" (4)\t")) }
  mockTest("literalIntParens2") { assertResult(Double.box(4))(toLiteral(" ((4)\t)")) }
  mockTest("literalIntBadParens") { testError("((4)", "Expected a closing parenthesis.") }
  mockTest("literalIntBadParens2") { testError("((4)))", "Extra characters after literal.") }
  mockTest("largeLiteral1") { testError("9999999999999999999999999999999999999999999999999", "Illegal number format") }
  mockTest("largeLiteral2") { testError("-9999999999999999999999999999999999999999999999999", "Illegal number format") }
  mockTest("largeLiteral3") { testError("9007199254740993", "9007199254740993 is too large to be represented exactly as an integer in NetLogo") }
  mockTest("largeLiteral4") { testError("-9007199254740993", "-9007199254740993 is too large to be represented exactly as an integer in NetLogo") }
  mockTest("literalString") { assertResult("hi there")(toLiteral("\"hi there\"")) }
  mockTest("literalList") { assertResult("[1 2 3]")(Dump.logoObject(toLiteralList("[1 2 3]"))) }
  mockTest("literalList2") { assertResult("[1 [2] 3]")(Dump.logoObject(toLiteralList("[1 [2] 3]"))) }
  mockTest("literalList3") { assertResult("[[1 2 3]]")(Dump.logoObject(toLiteralList("[[1 2 3]]"))) }
  mockTest("literalList4") { assertResult("[1 hi true]")(Dump.logoObject(toLiteralList("[1 \"hi\" true]"))) }
  mockTest("literalList5") { assertResult("[[1 hi true]]")(Dump.logoObject(toLiteral("([([1 \"hi\" true])])"))) }
  mockTest("parseLiteralList") { assertResult("[1 2 3]")(Dump.logoObject(toLiteralList("[1 2 3]"))) }
  mockTest("parseLiteralList2a") { assertResult("[1 [2] 3]")(Dump.logoObject(toLiteralList("[1 [2] 3]"))) }
  mockTest("parseLiteralList2b") { assertResult("[[1] [2] [3]]")(Dump.logoObject(toLiteralList("[[1] [2] [3]]"))) }
  mockTest("parseLiteralList3") { assertResult("[[1 2 3]]")(Dump.logoObject(toLiteralList("[[1 2 3]]"))) }
  mockTest("parseLiteralList4") { assertResult("[1 hi true]")(Dump.logoObject(toLiteralList("[1 \"hi\" true]"))) }

  mockTest("agent and agentset literals surrounded by brackets") {
    val validatingAgentSetParser: AgentParserCreator = {
      w => readLitPrefix => toks => {
        assert(toks.next().value == "AGENT-PARSEABLE")
        assert(toks.next().text  == "}")
        "foobarbaz"
      }
    }
    val result = toLiteral("{agent-parseable}", agentParserCreator = validatingAgentSetParser)
    assertResult(result)("foobarbaz")
  }

  mockTest("agent and agentset literals surrounded by brackets with null world") {
    testError("{all-turtles}", "Can only have literal agents and agentsets if importing.", world = null)
  }

  mockTest("badLiteral") { testError("foobar", "Expected a literal value.") }
  mockTest("badLiteralReporter") { testError("round", "Expected a literal value.") }

  mockTest("extension literal") {
    val manager = mock[ExtensionManager]
    expecting {
      one(manager).readExtensionObject("foo", "", "bar baz")
    }
    when {
      val input = "{{foo: bar baz}}"
      val result = toLiteral(input, extensionManager = manager)
      assert(result.isInstanceOf[ExtensionObject])
    }
  }

}
