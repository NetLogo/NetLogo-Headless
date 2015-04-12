// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import utest._
import org.nlogo.core.{DummyExtensionManager, FrontEndInterface, Program, CompilerException, Femto, StructureResults}

import org.nlogo._

object StructureParserTests extends TestSuite {

  def tests = TestSuite {
    val tokenizer: core.TokenizerInterface =
      Femto.scalaSingleton[org.nlogo.core.TokenizerInterface]("org.nlogo.lex.Tokenizer")

    def assertResult[A](expected: A)(actual: A) = {
      assert(expected == actual)
    }

    def compile(source: String): StructureResults =
      new StructureParser(tokenizer.tokenizeString(source).map(Namer0),
        None, core.StructureResults.empty)
        .parse(false)

    def expectError(source: String, error: String) {
      val e = intercept[CompilerException] {
        compile(source)
      }
      assertResult(error)(e.getMessage.takeWhile(_ != ','))
    }

    "empty"- {
      val results = compile("")
      assert(results.procedures.isEmpty)
      assert(results.procedureTokens.isEmpty)
      assertResult("globals []\n" +
        "interfaceGlobals []\n" +
        "turtles-own [WHO COLOR HEADING XCOR YCOR SHAPE LABEL LABEL-COLOR BREED HIDDEN? SIZE PEN-SIZE PEN-MODE]\n" +
        "patches-own [PXCOR PYCOR PCOLOR PLABEL PLABEL-COLOR]\n" +
        "links-own [END1 END2 COLOR LABEL LABEL-COLOR HIDDEN? BREED THICKNESS SHAPE TIE-MODE]\n" +
        "breeds \n" +
        "link-breeds \n")(results.program.dump)
    }

    "globals"- {
      val results = compile("globals [foo bar]")
      assertResult("globals [FOO BAR]")(
        results.program.dump.split("\n").head)
    }

    "turtles-own"- {
      val results = compile("turtles-own [foo bar]")
      assertResult("turtles-own [WHO COLOR HEADING XCOR YCOR SHAPE LABEL LABEL-COLOR BREED HIDDEN? SIZE PEN-SIZE PEN-MODE FOO BAR]")(
        results.program.dump.split("\n").drop(2).head)
    }

    "breeds"- {
      val results = compile("breed [mice mouse] breed [frogs]")
      assertResult("breeds MICE = Breed(MICE, MOUSE, , false)" +
        "FROGS = Breed(FROGS, TURTLE, , false)")(
          results.program.dump.split("\n").drop(5).take(2).mkString)
    }

    "mice-own"- {
      val results = compile("breed [mice mouse] mice-own [fur teeth]")
      assertResult("breeds MICE = Breed(MICE, MOUSE, FUR TEETH, false)")(
        results.program.dump.split("\n").drop(5).head)
    }

    "command procedure"- {
      val results = compile("to go fd 1 end")
      assertResult(1)(results.procedures.size)
      val proc = results.procedures("GO")
      assertResult(false)(proc.isReporter)
      assertResult("procedure GO:[]{OTPL}:\n")(proc.dump)
    }

    "two command procedures"- {
      val results = compile("globals [g] to foo print 5 end to bar print g end")
      assertResult("globals [G]")(
        results.program.dump.split("\n").head)
      assertResult(2)(results.procedures.size)
      assertResult("procedure FOO:[]{OTPL}:\n")(results.procedures("FOO").dump)
      assertResult("procedure BAR:[]{OTPL}:\n")(results.procedures("BAR").dump)
    }

    "command procedure with empty args"- {
      val results = compile("to go [] fd 1 end")
      assertResult(1)(results.procedures.size)
      val proc = results.procedures("GO")
      assertResult(false)(proc.isReporter)
      assertResult("procedure GO:[]{OTPL}:\n")(proc.dump)
    }

    "command procedure with some args"- {
      val results = compile("to go [a b c] fd 1 end")
      assertResult(1)(results.procedures.size)
      val proc = results.procedures("GO")
      assertResult(false)(proc.isReporter)
      assertResult("procedure GO:[A B C]{OTPL}:\n")(proc.dump)
    }

    "reporter procedure"- {
      val results = compile("to-report foo report 0 end")
      assertResult(1)(results.procedures.size)
      val proc = results.procedures("FOO")
      assertResult(true)(proc.isReporter)
      assertResult("reporter procedure FOO:[]{OTPL}:\n")(proc.dump)
    }

    "includes"- {
      val results = compile("__includes [\"foo.nls\"]")
      assertResult(0)(results.procedures.size)
      assertResult(1)(results.includes.size)
      assertResult("foo.nls")(results.includes.head.value.asInstanceOf[String])
    }

    /// allow breeds to share variables
    "breeds may share variables"- {
      val results = compile("undirected-link-breed [edges edge]\n" +
        "breed [nodes node]\n" +
        "breed [foos foo]\n" +
        "edges-own [lweight]\n" +
        "nodes-own [weight]\n" +
        "foos-own [weight]")
      val dump = results.program.dump
      assert(dump.containsSlice(
        "breeds NODES = Breed(NODES, NODE, WEIGHT, false)\n" +
          "FOOS = Breed(FOOS, FOO, WEIGHT, false)\n" +
          "link-breeds EDGES = Breed(EDGES, EDGE, LWEIGHT, false)"))
    }

    "declarations1"- {
      val results = compile("extensions [foo] globals [g1 g2] turtles-own [t1 t2] patches-own [p1 p2]")
      assert(results.procedures.isEmpty)
      assertResult("globals [G1 G2]\n" +
        "interfaceGlobals []\n" +
        "turtles-own [WHO COLOR HEADING XCOR YCOR SHAPE LABEL LABEL-COLOR BREED HIDDEN? SIZE PEN-SIZE PEN-MODE T1 T2]\n" +
        "patches-own [PXCOR PYCOR PCOLOR PLABEL PLABEL-COLOR P1 P2]\n" +
        "links-own [END1 END2 COLOR LABEL LABEL-COLOR HIDDEN? BREED THICKNESS SHAPE TIE-MODE]\n" +
        "breeds \n" +
        "link-breeds \n")(results.program.dump)
      assertResult("FOO")(results.extensions.map(_.value).mkString)
    }

    "declarations2"- {
      val results = compile("breed [b1s b1] b1s-own [b11 b12] breed [b2s b2] b2s-own [b21 b22]")
      assert(results.procedures.isEmpty)
      assertResult("globals []\n" +
        "interfaceGlobals []\n" +
        "turtles-own [WHO COLOR HEADING XCOR YCOR SHAPE LABEL LABEL-COLOR BREED HIDDEN? SIZE PEN-SIZE PEN-MODE]\n" +
        "patches-own [PXCOR PYCOR PCOLOR PLABEL PLABEL-COLOR]\n" +
        "links-own [END1 END2 COLOR LABEL LABEL-COLOR HIDDEN? BREED THICKNESS SHAPE TIE-MODE]\n" +
        "breeds B1S = Breed(B1S, B1, B11 B12, false)\n" +
        "B2S = Breed(B2S, B2, B21 B22, false)\n" +
        "link-breeds \n")(results.program.dump)
    }

    "missing procedure name"- {
      // ticket #1183
      expectError("to", "identifier expected")
      expectError("to-report", "identifier expected")
    }

    "missing open bracket after globals"- {
      expectError("globals schmobals", "opening bracket expected")
    }
    "bad top level keyword"- {
      expectError("schmobals", "keyword expected")
    }
    "missing close bracket after globals"- {
      expectError("globals [", "closing bracket expected")
    }
    "missing close bracket in globals"- {
      expectError("globals [g turtles-own [t]",
        "closing bracket expected")
    }
    "attempt primitive as variable"- {
      expectError("globals [turtle]",
        "There is already a primitive reporter called TURTLE")
    }
    "redeclaration of globals"- {
      expectError("globals [] globals []",
        "Redeclaration of GLOBALS")
    }
    "redeclaration of turtles-own"- {
      expectError("turtles-own [] turtles-own []",
        "Redeclaration of TURTLES-OWN")
    }
    "redeclaration of breed-own"- {
      expectError("breed [hunters hunter] hunters-own [fear] hunters-own [loathing]",
        "Redeclaration of HUNTERS-OWN")
    }
    "redeclaration of extensions"- {
      expectError("extensions [foo] extensions [bar]",
        "Redeclaration of EXTENSIONS")
    }

    // https://github.com/NetLogo/NetLogo/issues/348
    def testTaskVariableMisuse(source: String) {
      expectError(source,
        "Names beginning with ? are reserved for use as task inputs")
    }
    "task variable as procedure name"- {
      testTaskVariableMisuse("to ?z end")
    }
    "task variable as procedure input"- {
      testTaskVariableMisuse("to x [?y] end")
    }
    "task variable as agent variable"- {
      testTaskVariableMisuse("turtles-own [?a]")
    }

    "missing close bracket in last declaration"- {
      expectError("turtles-own [",
        "closing bracket expected")
    }

    "breed singular clash with global"- {
      // ticket #446
      expectError("breed [frogs frog] globals [frog]",
        "There is already a breed called FROG")
    }

    // https://github.com/NetLogo/NetLogo/issues/414
    "missing end 1"- {
      expectError("to foo to bar",
        "END expected")
    }
    "missing end 2"- {
      expectError("to foo fd 1",
        "END expected")
    }
    "missing end 3"- {
      expectError("to foo",
        "END expected")
    }
    "missing end 4"- {
      expectError("to foo [",
        "closing bracket expected")
    }
    "missing close bracket in formals 1"- {
      expectError("to foo [ end",
        "closing bracket expected")
    }
    "missing close bracket in formals 2"- {
      expectError("to foo [ to",
        "closing bracket expected")
    }
    "declaration after procedure"- {
      expectError("to foo end globals []",
        "TO or TO-REPORT expected")
    }

    def compileAll(src: String): StructureResults = {
      StructureParser.parseAll(
        tokenizer, src, None, Program.empty(), false, FrontEndInterface.NoProcedures, new DummyExtensionManager)
    }

    def expectParseAllError(src: String, error: String) = {
      val e = intercept[CompilerException] {
        compileAll(src)
      }
      assertResult(error)(e.getMessage.takeWhile(_ != ','))
    }

    "invalid included file"- {
      expectParseAllError( """__includes [ "foobar.nlogo" ]""", "Included files must end with .nls")
    }

    "nonexistent included file"- {
      expectParseAllError( """__includes [ "foobar.nls" ]""", "Could not find foobar.nls")
    }
  }
}
