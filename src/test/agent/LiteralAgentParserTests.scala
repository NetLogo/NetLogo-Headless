// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.util.MockSuite
import org.nlogo.api.{ Dump, LogoList, ExtensionManager, World => APIWorld, CompilerException }
import org.nlogo.core.{ Token, TokenType },
  TokenType._
import org.nlogo.core.Token
import org.scalatest.FunSuite

class LiteralAgentParserTests extends FunSuite with MockSuite {

  def defaultWorld = {
    val world = new World
    world.createPatches(-10, 10, -10, 10)
    world.realloc()
    world
  }

  object tokenDSL {
    def `{`: Token             = Token("{", OpenBrace, null)(0, 0, "test")
    def `}`: Token             = Token("}", CloseBrace, null)(0, 0, "test")
    def `[`: Token             = Token("[", OpenBracket, null)(0, 0, "test")
    def `]`: Token             = Token("]", CloseBracket, null)(0, 0, "test")
    def id(str: String): Token = Token(str, Ident, str.toUpperCase)(0, 0, "test")
    def lit(v: Int): Token     = Token(v.toString, Literal, Double.box(v.toDouble))(0, 0, "test")
    def eof: Token             = Token("eof", Eof, null)(0, 0, "test")

    def tokenIterator(ts: Token*): Iterator[Token] = ts.iterator ++ Iterator(eof)
  }

  def dummyLiteralParser(t: Token, i: Iterator[Token]): AnyRef = {
    t.tpe match {
      case Literal => t.value
      case OpenBracket => LogoList.fromIterator(i.takeWhile(_.tpe != CloseBracket).map(_.value))
      case _ => throw new Exception("dummyLiteralParser doesn't handle this token!")
    }
  }

  def toLiteral(toks: Iterator[Token],
                      world: APIWorld = defaultWorld,
                      extensionManager: ExtensionManager = null): AnyRef = {
    val literalAgentParser = new LiteralAgentParser(world, dummyLiteralParser)
    toks.next() // discard `{`
    literalAgentParser(toks)
  }

  def testError(toks: Iterator[Token], error: String, world: APIWorld = defaultWorld) = {
    val e = intercept[CompilerException] {
      toLiteral(toks, world)
    }
    assertResult(error)(e.getMessage)
  }

  import tokenDSL._

  test("badAgent") {
    val input = tokenIterator(`{`, id("foobar"), `}`)
    testError(input, "FOOBAR is not an agentset")
  }

  test("parsePatch") {
    val input = tokenIterator(`{`, id("patch"), lit(1), lit(3), `}`)
    val result = toLiteral(input).asInstanceOf[Patch]
    assertResult("(patch 1 3)")(Dump.logoObject(result))
  }

  test("parseTurtle") {
    val result = toLiteral(tokenIterator(`{`, id("turtle"), lit(3), `}`)).asInstanceOf[Turtle]
    assertResult("(turtle 3)")(Dump.logoObject(result))
  }

  test("parseTurtles") {
    val input = tokenIterator(`{`, id("turtles"), lit(1), lit(2), lit(3), `}`)
    val result = toLiteral(input).asInstanceOf[AgentSet]
    assertResult("{turtles 1 2 3}")(Dump.agentset(result, true))
  }

  test("parsePatches") {
    val input = tokenIterator(`{`, id("patches"), `[`, lit(1), lit(2), `]`, `[`, lit(3), lit(4), `]`, `}`)
    val result = toLiteral(input).asInstanceOf[AgentSet]
    assertResult("{patches [1 2] [3 4]}")(Dump.agentset(result, true))
  }

  test("parseAllTurtles") {
    val input = tokenIterator(`{`, id("all-turtles"), `}`)
    val result = toLiteral(input).asInstanceOf[AgentSet]
    assertResult("{all-turtles}")(Dump.agentset(result, true))
  }

  test("parseAllPatches") {
    val input = tokenIterator(`{`, id("all-patches"), `}`)
    val result = toLiteral(input).asInstanceOf[AgentSet]
    assertResult("{all-patches}")(Dump.agentset(result, true))
  }
}
