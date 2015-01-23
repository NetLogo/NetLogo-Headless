// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import java.io.{Reader => JReader, BufferedReader}

import org.nlogo.core.{ NumberParser, StringEscaper, Token, TokenType }
import org.nlogo.lex.TokenLexer.WrappedInput
import TokenLexer.WrappedInput
import LexOperations._

import scala.annotation.tailrec
import scala.collection.immutable.HashSet

class TokenLexer {
  import LexOperations.PrefixConversions._

  private val identifierPunctuation = """_.?=*!<>:#+/%$^'&\\-"""
  private val digits = "0123456789"
  private val identifierChars = {
    UnicodeInformation.letterCharacters.foldLeft(HashSet[Int]())(_ ++ _.toSet) ++
      (digits.map(_.toInt).toSet ++ identifierPunctuation.map(_.toInt).toSet)
  }

  private def validIdentifierChar(c: Char): Boolean = identifierChars.contains(c)

  private val punctuation = Map(
    "," -> TokenType.Comma,
    "{" -> TokenType.OpenBrace,
    "}" -> TokenType.CloseBrace,
    "(" -> TokenType.OpenParen,
    ")" -> TokenType.CloseParen,
    "[" -> TokenType.OpenBracket,
    "]" -> TokenType.CloseBracket
  )

  def apply(input: WrappedInput): (Token, WrappedInput) = {
    val (wsCount, remainder) = fastForwardWhitespace(input)
    if (input.hasNext) {
      val r = Seq(extensionLiteral, punct, comment, numericLiteral, string, ident, illegalCharacter)
        .foldLeft((Option.empty[Token], input)) {
        case ((Some(token), remaining), (prefixDetector, tokenizer)) => (Some(token), remaining)
        case ((None, remaining), (prefixDetector, tokenizer)) =>
          remaining.assembleToken(prefixDetector, tokenizer)
            .map(o => (Some(o._1), o._2))
            .getOrElse((None, remaining))
      }
      (r._1.get, r._2)
    } else
      (Token.Eof, input)
  }

  def fastForwardWhitespace(input: WrappedInput): (Int, WrappedInput) = {
    val (spaces, remainder) = input.longestPrefix({
      c => if (Character.isWhitespace(c)) Accept else Finished
    })
    (spaces.length, remainder)
  }

  class DoubleBracePairMatcher extends LexPredicate {
    var lastChar = Option.empty[Char]
    var nesting = 0
    val detectEnd = withFeedback[(Option[Char], Int)]((lastChar, nesting)) {
        case ((_, n), _) if n < 0 => ((None, n), Error)
        case ((_, n), c) if c == '\r' || c == '\n' => ((Some(c), n), Finished)
        case ((Some('}'), 1), '}') => ((None, 0), Finished)
        case ((Some('{'), n), '{') => ((None, n + 1), Accept)
        case ((Some('}'), n), '}') => ((None, n - 1), Accept)
        case ((_, n), c)           => ((Some(c), n), Accept)
        case _ => ((None, 0), Error)
      }

    def apply(c: Char): DetectorStates =
      detectEnd(c)
  }

  def extensionLiteral: (LexPredicate, TokenGenerator) = {
    val innerMatcher = new DoubleBracePairMatcher
    (chain(innerMatcher, anyOf('}', '\n', '\r')), tokenizeExtensionLiteral(innerMatcher))
  }

  def punct: (LexPredicate, TokenGenerator) =
    (characterMatching(c => punctuation.isDefinedAt(c.toString)),
      p => punctuation.get(p).map(tpe => (p, tpe, null)))

  def string: (LexPredicate, TokenGenerator) =
    (chain('"', stringLexer, '"') , tokenizeString)

  def stringLexer: LexPredicate = {
    withFeedback[Option[Char]](Some('"')) {
      case (Some('\\'), '"') => (Some('"'), Accept)
      case (_, '"') => (Some('"'), Finished)
      case (_, c) => (Some(c), Accept)
    }
  }

  def comment: (LexPredicate, TokenGenerator) =
    (chain(';', zeroOrMore(c => c != '\r' && c != '\n')),
      {s => Some((s, TokenType.Comment, null))})

  def numericLiteral: (LexPredicate, TokenGenerator) =
    (chain(
      characterMatching(c => Character.isDigit(c) || c == '.' || c == '-'),
      zeroOrMore(c => validIdentifierChar(c))),
      tokenizeLiteral)

  def ident: (LexPredicate, TokenGenerator) =
    (oneOrMore(validIdentifierChar), tokenizeIdent)

  def illegalCharacter: (LexPredicate, TokenGenerator) = {
    // if we've gotten to this point, we have a bad character
    (aSingle(c => Accept), {s => Some((s, TokenType.Bad, "This non-standard character is not allowed.")) } )
  }

  private def tokenizeExtensionLiteral(innerMatcher: DoubleBracePairMatcher)(literalString: String): Option[(String, TokenType, AnyRef)] = {
    if (literalString.take(2) != "{{") {
      None
    } else if (literalString.last == '\n' || literalString.last == '\r') {
      Some(("", TokenType.Bad, "End of line reached unexpectedly"))
    } else if (literalString.foldLeft((0, Option.empty[Char])) {
          case ((nesting, lastChar), currentChar) =>
            (lastChar, currentChar) match {
              case (Some('{'), '{') => (nesting + 1, None)
              case (Some('}'), '}') => (nesting - 1, None)
              case _ => (nesting, Some(currentChar))
            }
        }._1 > 0)
      Some(("", TokenType.Bad, "End of file reached unexpectedly"))
    else
      Some((literalString, TokenType.Extension, literalString))
  }

  private def tokenizeLiteral(literalString: String): Option[(String, TokenType, AnyRef)] = {
    if (literalString.exists(Character.isDigit))
      NumberParser.parse(literalString) match {
        case Left (error) => Some((literalString, TokenType.Bad, error) )
        case Right (literal) => Some((literalString, TokenType.Literal, literal) )
      }
    else
      None
  }

  private def tokenizeIdent(identString: String): Option[(String, TokenType, AnyRef)] =
    Some((identString, TokenType.Ident, identString.toUpperCase))

  private def tokenizeString(stringText: String): Option[(String, TokenType, AnyRef)] = {
    try {
      if (stringText.last != '"' || stringText.takeRight(2) == "\\\"")
        Some((s"""$stringText""", TokenType.Bad, "Closing double quote is missing"))
      else {
        val unescapedText = StringEscaper.unescapeString(stringText.drop(1).dropRight(1))
        Some(( s"""$stringText""", TokenType.Literal, unescapedText))
      }
    } catch {
      case ex: IllegalArgumentException => Some(( s"""$stringText""", TokenType.Bad, "Illegal character after backslash"))
    }
  }

  def wrapInput(reader: JReader, filename: String): WrappedInput =
    new BufferedInputWrapper(reader, 0, filename)

  class BufferedInputWrapper(input: JReader, var offset: Int, val filename: String) extends WrappedInput {
    private val buffReader: BufferedReader = new BufferedReader(input, 10000)

    def nextChar: Option[Char] = {
      val readChar = buffReader.read()
      if (readChar == -1)
        None
      else
        Some(readChar.asInstanceOf[Char])
    }

    @tailrec private def longestPrefixTail(p: LexPredicate, acc: String): String =
      nextChar match {
        case Some(c) if p(c).continue => longestPrefixTail(p, acc + c)
        case _ => acc
      }

    override def hasNext: Boolean = {
      buffReader.mark(1)
      val r = buffReader.read() != -1
      buffReader.reset()
      r
    }

    override def assembleToken(p: LexPredicate, f: TokenGenerator): Option[(Token, WrappedInput)] = {
      val originalOffset = offset
      val (prefix, remainder) = longestPrefix(p)
      val r = prefix match {
        case "" => None
        case nonEmptyString => f(nonEmptyString).map {
          case (text, tpe, tval) => (new Token(text, tpe, tval)(originalOffset, offset, filename), this)
        }
      }
      r orElse {
        buffReader.reset()
        offset = originalOffset
        None
      }
    }

    override def longestPrefix(f: LexPredicate): (String, WrappedInput) = {
      buffReader.mark(10000)
      val (a, b) = (longestPrefixTail(f, ""), this)
      buffReader.reset()
      buffReader.skip(a.length) // we always go "one too far", so we have to backup
      offset += a.length
      (a, b)
    }
  }
}

object StandardLexer extends TokenLexer {}

object WhitespaceSkippingLexer extends TokenLexer {
  override def apply(input: WrappedInput): (Token, WrappedInput) = {
    val (t, endOfToken) = super.apply(input)
    val (_, beginningOfNextToken) = fastForwardWhitespace(endOfToken)
    (t, beginningOfNextToken)
  }
}

object TokenLexer {
  trait WrappedInput {
    def hasNext: Boolean
    def offset: Int
    def filename: String
    def longestPrefix(f: LexPredicate): (String, WrappedInput)
    def assembleToken(p: LexPredicate, f: TokenGenerator): Option[(Token, WrappedInput)]
  }
}
