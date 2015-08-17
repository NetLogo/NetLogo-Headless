// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import org.scalatest.FunSuite
import org.nlogo.core.{ Token, TokenType }

class TokenizerTests extends FunSuite {

  def tokenize(s: String): Seq[Token] = {
    val result = Tokenizer.tokenizeString(s, "").toSeq
    assertResult(TokenType.Eof)(result.last.tpe)
    result.dropRight(1)
  }
  def tokenizeSkippingWhitespace(s: String) = {
    val result = Tokenizer.tokenizeSkippingTrailingWhitespace(
      new java.io.StringReader(s), "").toSeq
    assertResult(TokenType.Eof)(result.last._1.tpe)
    result.dropRight(1)
  }
  def tokenizeRobustly(s: String): List[Token] = {
    val result = Tokenizer.tokenize(new java.io.StringReader(s)).toList
    assertResult(TokenType.Eof)(result.last.tpe)
    result.dropRight(1)
  }
  def firstBadToken(tokens: Seq[Token]) =
    tokens.find(_.tpe == TokenType.Bad)

  LexerTestCases.SuccessCases foreach {
    case LexerTestCases.LexSuccess(text, expectedTokens, _) =>
      test(s"""Tokenize: '$text'""") {
        val tokens = tokenize(text)
        assertResult(expectedTokens)(tokens.mkString)
      }
  }

  LexerTestCases.FailureCases foreach {
    case LexerTestCases.LexFailure(text, start, end, error) =>
      test(s"""Tokenize as error: '$text'""") {
        val tokens = tokenizeRobustly(text)
        val badToken = firstBadToken(tokens).get
        assertResult(start)(badToken.start)
        assertResult(end)(badToken.end)
        assertResult(error)(badToken.value)
      }
  }

  LexerTestCases.WsSkippingCases foreach {
    case LexerTestCases.WsSkip(text, expectedTokenTexts, expectedSkips) =>
      test(s"""Skips whitespace correctly: '$text'""") {
        val tokens = tokenizeSkippingWhitespace(text)
        assertResult(expectedTokenTexts)(tokens.map(_._1.text))
        assertResult(expectedSkips     )(tokens.map(_._2))
      }
  }

  test("ListOfArrays") {
    val tokens = tokenize("[{{array: 0}} {{array: 1}}]")
    assertResult("Token([,OpenBracket,null)" +
                 "Token({{array: 0}},Extension,{{array: 0}})" +
                 "Token({{array: 1}},Extension,{{array: 1}})" +
                 "Token(],CloseBracket,null)")(
      tokens.mkString)
    assertResult(1)(tokens(1).start)
    assertResult(13)(tokens(1).end)
    assertResult(14)(tokens(2).start)
    assertResult(26)(tokens(2).end)
  }
}
