// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import java.io.{ Reader => JReader }

import org.nlogo.core.{ NumberParser, StringEscaper, Token, TokenType }

import scala.util.{ matching, parsing },
  matching.Regex,
  parsing.{ combinator, input },
    combinator.{ PackratParsers, RegexParsers },
    input.{ Reader, StreamReader }

abstract class TokenLexer extends RegexParsers with PackratParsers {
  import TokenLexer.{ TokenParseResult, WrappedInput }

  private val validIdentifierChars = """\p{L}\p{Digit}_.?=*!<>:#\+/%$^'&\\-"""
  private val identifierChar = s"[$validIdentifierChars]"
  private val badChar = s"[^$validIdentifierChars]"
  private val stringPattern = """(?:\\\"|\\\\|\\[^"]|[^\r\n\\"])*""".r
  private val commentPattern = ";[^\\r\\n]*".r
  private val numberPattern = s"""-?\\.?[0-9]$identifierChar*""".r

  private val punctuation = Map(
    "," -> TokenType.Comma,
    "{" -> TokenType.OpenBrace,
    "}" -> TokenType.CloseBrace,
    "(" -> TokenType.OpenParen,
    ")" -> TokenType.CloseParen,
    "[" -> TokenType.OpenBracket,
    "]" -> TokenType.CloseBracket
  )

  private[lex] def tokenAndSkippedSpaceParser: PackratParser[TokenParseResult]

  private[lex] def token: PackratParser[TokenParseResult] =
    extensionLiteral | punct | comment| number | ident | string | illegalCharacter

  private def extensionLiteral: PackratParser[TokenParseResult] = completeLiteral | unclosedLiteral

  private def unclosedLiteral: PackratParser[TokenParseResult] =
    "{{" ~! ".*".r ~ "\\r|\\n".r.? ^^ {
      case _ ~ s ~ Some(_) =>
        TokenParseResult("", TokenType.Bad, "End of line reached unexpectedly")
      case _ =>
        TokenParseResult("", TokenType.Bad, "End of file reached unexpectedly")
    }

  private def completeLiteral: PackratParser[TokenParseResult] =
    literalText ^^ {
      l => TokenParseResult(l, TokenType.Extension, l)
    }

  private def literalText: PackratParser[String] =
    "{{" ~ rep1(literalText | """[^\}]|\}[^\}]""".r) ~ "}}" ^^ {
      case a ~ b ~ c => a + b.mkString + c
    }

  private def punct: PackratParser[TokenParseResult] =
    punctuation.keys.map(Regex.quote).mkString("[", "", "]").r ^^ {
      matchedPunct => TokenParseResult(matchedPunct, punctuation(matchedPunct), null)
    }

  private def comment: PackratParser[TokenParseResult] =
    commentPattern ^^ { TokenParseResult(_, TokenType.Comment, null) }

  private def number: PackratParser[TokenParseResult] =
    numberPattern ^^ {
      numberString => NumberParser.parse(numberString) match {
        case Left(error) => TokenParseResult(numberString, TokenType.Bad, error)
        case Right(literal) => TokenParseResult(numberString, TokenType.Literal, literal)
      }
    }

  private def ident: PackratParser[TokenParseResult] =
    s"$identifierChar+".r ^^ {
      s => TokenParseResult(s, TokenType.Ident, s.toUpperCase)
    }

  private def string: PackratParser[TokenParseResult] =
    "\"" ~! (finishedString | unfinishedString) ^^ { case (_ ~ t) => t }

  private def finishedString: PackratParser[TokenParseResult] =
    (stringPattern <~ "\"") ^^ {
      case matchedText =>
        try {
          val unescapedText = StringEscaper.unescapeString(matchedText)
          new TokenParseResult(s""""$matchedText"""", TokenType.Literal, unescapedText, unescapedText.length + 2)
        } catch {
          case ex: IllegalArgumentException => TokenParseResult(s""""$matchedText"""", TokenType.Bad, "Illegal character after backslash")
        }
    }

  private def unfinishedString: PackratParser[TokenParseResult] =
    stringPattern ^^ {
      matchedText => TokenParseResult(s""""$matchedText""", TokenType.Bad, "Closing double quote is missing")
    }

  private def illegalCharacter: PackratParser[TokenParseResult] =
    badChar.r ^^ {
      s => TokenParseResult(s, TokenType.Bad, "This non-standard character is not allowed.")
    }

  def apply(input: WrappedInput): (Token, WrappedInput) =
    handleParseResult(parse(tokenAndSkippedSpaceParser, input.input), input)

  private def handleParseResult(parseResult: ParseResult[TokenParseResult], input: WrappedInput): (Token, WrappedInput) =
    parseResult match {
      case Success(result, reader) =>
        val returnedTok = result.toToken(input.offset, input.filename)
        (returnedTok, TokenInputWrapper(reader, input.offset + result.totalChars, input.filename))
      case Error(msg, remainder) =>
        throw new Exception(s"Error: $msg")
      case Failure(msg, remainder) =>
        if (remainder.atEnd)
          (Token.Eof, TokenInputWrapper(remainder, remainder.offset, input.filename))
        else
          throw new Exception(s"Failure: $msg")
    }

  def wrapInput(reader: JReader, filename: String): WrappedInput =
    TokenInputWrapper(new PackratReader(StreamReader(reader)), 0, filename)

  case class TokenInputWrapper(input: Input, offset: Int, filename: String) extends WrappedInput{
    def atEnd: Boolean = input.atEnd
  }
}

object TokenLexer {
  class TokenParseResult(text: String,
                         tpe: TokenType,
                         value: AnyRef,
                         textSize: Int) {

    var leadingWhitespaces = 0
    var trailingWhitespaces = 0

    def totalChars = leadingWhitespaces + textSize + trailingWhitespaces

    def toToken(offset: Int, filename: String): Token =
      new Token(text, tpe, value)(offset + leadingWhitespaces, offset + leadingWhitespaces + textSize, filename)

    def consumedChars: Int = leadingWhitespaces + textSize + trailingWhitespaces
  }

  object TokenParseResult {
    def apply(text: String, tpe: TokenType, value: AnyRef) = new TokenParseResult(text, tpe, value, text.length)

    val Eof = TokenParseResult("", TokenType.Eof, "")
  }

  object StandardLexer extends TokenLexer {
    override def skipWhitespace: Boolean = false

    override private[lex] def tokenAndSkippedSpaceParser: PackratParser[TokenParseResult] =
      (whiteSpace.? ~ token.?) ^^ {
        case (ws ~ t) =>
          val tok = t.getOrElse(TokenParseResult.Eof)
          tok.leadingWhitespaces = ws.map(_.length).getOrElse(0)
          tok
      }
  }

  object TrailingWhitespaceSkippingLexer extends TokenLexer {
    override def skipWhitespace: Boolean = false

    override private[lex] def tokenAndSkippedSpaceParser: PackratParser[TokenParseResult] =
      whiteSpace.? ~ (token ~ whiteSpace.?).? ^^ {
        case leadingWs ~ Some(tok ~ trailingWs) =>
          tok.leadingWhitespaces = leadingWs.map(_.length).getOrElse(0)
          tok.trailingWhitespaces = trailingWs.map(_.length).getOrElse(0)
          tok
        case leadingWs ~ None =>
          val tok = TokenParseResult.Eof
          tok.leadingWhitespaces = leadingWs.map(_.length).getOrElse(0)
          tok
      }
  }

  trait WrappedInput {
    def atEnd: Boolean
    def offset: Int
    def filename: String
    def input: Reader[Char]
  }
}
