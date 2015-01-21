// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import org.nlogo.core, core.{ Token, TokenType },
  TokenLexer.{ StandardLexer, TrailingWhitespaceSkippingLexer, WrappedInput }

// caller's responsibility to check for TokenType.Bad!

object Tokenizer extends core.TokenizerInterface {
  def tokenizeString(source: String, filename: String = ""): Iterator[Token] =
    tokenize(new java.io.StringReader(source), filename)

  def tokenize(reader: java.io.Reader, filename: String = ""): Iterator[Token] =
    new TokenLexIterator(StandardLexer, reader, filename).map(_._1)

  def tokenizeWithOffset(reader: java.io.Reader, filename: String = ""): Iterator[(Token, Int)] =
    new TokenLexIterator(StandardLexer, reader, filename).map {
      case (t, r) => (t, r.offset)
    }

  def tokenizeSkippingTrailingWhitespace(reader: java.io.Reader, filename: String = ""): Iterator[(Token, Int)] = {
    var lastOffset = 0
    new TokenLexIterator(TrailingWhitespaceSkippingLexer, reader, filename).map {
      case (t, i) =>
        val r = (t, i.offset - lastOffset - (t.end - t.start))
        lastOffset = i.offset
        r
    }
  }

  private class TokenLexIterator(lexer: TokenLexer, reader: java.io.Reader, filename: String)
    extends Iterator[(Token, WrappedInput)] {
    private var lastToken = Option.empty[Token]
    private var lastInput = lexer.wrapInput(reader, filename)

    override def hasNext: Boolean = ! lastToken.contains(Token.Eof)

    override def next(): (Token, WrappedInput) = {
      val (t, i) = lexer(lastInput)
      lastToken = Some(t)
      lastInput = i
      (t, i)
    }
  }
}
