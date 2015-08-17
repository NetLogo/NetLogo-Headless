// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import utest._
import org.nlogo.core.{ Token, TokenType }

object TokenizerTests extends TestSuite {
  def tests = TestSuite{

    def tokenize(s: String) = {
      val result = Tokenizer.tokenizeString(s, "").toSeq
      assert(TokenType.Eof == result.last.tpe)
      result.dropRight(1)
    }

    def tokenizeSkippingWhitespace(s: String) = {
      val result = Tokenizer.tokenizeSkippingTrailingWhitespace(
        new java.io.StringReader(s), "").toSeq
      assert(TokenType.Eof == result.last._1.tpe)
      result.dropRight(1)
    }

    def tokenizeRobustly(s: String) = {
      val result = Tokenizer.tokenizeString(s, "").toList
      assert(TokenType.Eof == result.last.tpe)
      result.dropRight(1)
    }

    def firstBadToken(tokens: Seq[Token]) =
      tokens.find(_.tpe == TokenType.Bad)

    "test lexing of successful cases"-{
      LexerTestCases.SuccessCases foreach {
        case LexerTestCases.LexSuccess(text, _, expectedTokens) =>
          val tokens = tokenize(text)
          assert(expectedTokens == tokens.mkString)
      }
    }

    "test lexing of failure cases" - {
      LexerTestCases.FailureCases foreach {
        case LexerTestCases.LexFailure(text, start, end, error) =>
          val tokens = tokenizeRobustly(text)
          val badToken = firstBadToken(tokens).get
          assert(start == badToken.start)
          assert(end   == badToken.end)
          assert(error == badToken.value)
      }
    }

    "test lexing with whitespace skips" - {
      LexerTestCases.WsSkippingCases foreach {
        case LexerTestCases.WsSkip(text, expectedTexts, expectedSkips) =>
          val tokens = tokenizeSkippingWhitespace(text)
          assert(tokens.map(_._1.text) == expectedTexts)
          assert(tokens.map(_._2)      == expectedSkips)
      }
    }

    "ListOfArrays"-{
      val tokens = tokenize("[{{array: 0}} {{array: 1}}]")
      assert("Token([,OpenBracket,null)" +
        "Token({{array: 0}},Extension,{{array: 0}})" +
        "Token({{array: 1}},Extension,{{array: 1}})" +
        "Token(],CloseBracket,null)" ==
          tokens.mkString)
      assert(1 == tokens(1).start)
      assert(13 == tokens(1).end)
      assert(14 == tokens(2).start)
      assert(26 == tokens(2).end)
    }

    "ArrayOfArrays"-{
      val tokens = tokenize("{{array: 2: {{array: 0}} {{array: 1}}}}")
      val expected = "Token({{array: 2: {{array: 0}} {{array: 1}}}},Extension,{{array: 2: {{array: 0}} {{array: 1}}}})"
      assert(expected == tokens.mkString)
    }
  }
}
