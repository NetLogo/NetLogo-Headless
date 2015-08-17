// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

object LexerTestCases {

  case class LexFailure(text: String, start: Int, end: Int, error: String)
  case class LexSuccess(text: String, tokens: String, jsTokens: String)
  case class WsSkip(text: String, expectedTokenTexts: Seq[String], expectedSkips: Seq[Int])

  object LexSuccess {
    // in most cases, the tokens are exactly the same
    def apply(text: String, tokens: String): LexSuccess =
      LexSuccess(text, tokens, tokens)
  }

  val SuccessCases = Seq(
    LexSuccess("", ""),
    LexSuccess("\n", ""),
    LexSuccess(".5",         "Token(.5,Literal,0.5)"),
    LexSuccess("-1",         "Token(-1,Literal,-1.0)", "Token(-1,Literal,-1)"),
    LexSuccess("-.75",       "Token(-.75,Literal,-0.75)"),
    LexSuccess("foo",        "Token(foo,Ident,FOO)"),
    LexSuccess("_",          "Token(_,Ident,_)"),
    LexSuccess("\"foo\"",    "Token(\"foo\",Literal,foo)"),
    LexSuccess("""""""",     "Token(\"\",Literal,)"),
    LexSuccess(""""\"\""""", "Token(\"\\\"\\\"\",Literal,\"\")"),
    LexSuccess("\"\\\\\"",   "Token(\"\\\\\",Literal,\\)"),
    LexSuccess("round ?",    "Token(round,Ident,ROUND)Token(?,Ident,?)"),
    LexSuccess("a\rb",       "Token(a,Ident,A)" + "Token(b,Ident,B)"),
    LexSuccess("-.",         "Token(-.,Ident,-.)"),
    { val o ="\u00F6"  // lower case o with umlaut
      LexSuccess(o, s"Token($o,Ident,${o.toUpperCase})") },
    LexSuccess("\"foo\u216C\"", "Token(\"foo\u216C\",Literal,foo\u216C)"),
    LexSuccess("-WOLF-SHAPE-00013",
      "Token(-WOLF-SHAPE-00013,Ident,-WOLF-SHAPE-00013)"),
    LexSuccess("__ignore round 0.5",
      "Token(__ignore,Ident,__IGNORE)" +
        "Token(round,Ident,ROUND)" +
        "Token(0.5,Literal,0.5)"),
    LexSuccess("\n\n__ignore round 0.5",
      "Token(__ignore,Ident,__IGNORE)" +
        "Token(round,Ident,ROUND)" +
        "Token(0.5,Literal,0.5)"),
    LexSuccess("\r__ignore round 0.5",
      "Token(__ignore,Ident,__IGNORE)" +
        "Token(round,Ident,ROUND)" +
        "Token(0.5,Literal,0.5)"),
    { val tokens =
        """|Token([,OpenBracket,null)
           |Token(123,Literal,123.0)
           |Token(-456,Literal,-456.0)
           |Token("a",Literal,a)
           |Token(],CloseBracket,null)""".stripMargin.replaceAll("\n", "")
    LexSuccess("[123 -456 \"a\"]", tokens, tokens.replaceAll(".0", "")) },
    LexSuccess("{{array: 0}}", "Token({{array: 0}},Extension,{{array: 0}})"),
    LexSuccess("{{array: 2: {{array: 0}} {{array: 1}}}}",
      "Token({{array: 2: {{array: 0}} {{array: 1}}}},Extension," +
        "{{array: 2: {{array: 0}} {{array: 1}}}})")
  )

  val FailureCases = Seq(
    LexFailure("\"\\b\"",      0, 4,  "Illegal character after backslash"),
    LexFailure("\"\\\"",       0, 3,  "Closing double quote is missing"),
    LexFailure(""""abc""",     0, 4,  "Closing double quote is missing"),
    LexFailure("\"abc\n\"",    0, 4,  "Closing double quote is missing"),
    LexFailure("1.2.3",        0, 5,  "Illegal number format"),
    LexFailure("{{array: 1: ", 0, 12, "End of file reached unexpectedly"),
    LexFailure("{{",           0, 2,  "End of file reached unexpectedly"),
    LexFailure("{{\n",         0, 3,  "End of line reached unexpectedly"),
    LexFailure("{{ {{ }}",     0, 8,  "End of file reached unexpectedly"),
    // 216C is a Unicode character I chose pretty much at random.  it's a Roman numeral
    // for fifty, and *looks* just like an L, but is not a letter according to Unicode.
    LexFailure("foo\u216Cbar", 3, 4,  "This non-standard character is not allowed."),
    LexFailure("__ignore 3__ignore 4", 9, 18, "Illegal number format")
  )

  val WsSkippingCases = Seq(
    WsSkip("    123",     Seq("123"),        Seq(4)),
    WsSkip("123",         Seq("123"),        Seq(0)),
    WsSkip("123   ",      Seq("123"),        Seq(3)),
    WsSkip("  123   ",    Seq("123"),        Seq(5)),
    WsSkip("  123  456 ", Seq("123", "456"), Seq(4, 1)))
}
