// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package front

import org.scalatest.FunSuite
import org.nlogo.{ core, api, nvm },
  api.{ CompilerException, DummyExtensionManager, Program }

// This is where ExpressionParser gets most of its testing.  (It's a lot easier to test it as part
// of the overall front end than it would be to test in strict isolation.)

class FrontEndTests extends FunSuite {

  val PREAMBLE = "to __test "
  val POSTAMBLE = "\nend"

  /// helpers
  def compile(source: String, preamble: String = PREAMBLE): Seq[core.Statements] =
    FrontEnd.frontEnd(preamble + source + POSTAMBLE) match {
      case (procs, _) =>
        procs.map(_.statements)
    }

  /**
   * utility method useful for testing that start()
   * and end() return right answers everywhere
   */
  def statementsToString(ss: Seq[core.Statements], source: String) =
    (for (stmts <- ss) yield {
      val visitor = new PositionsCheckVisitor(source)
      visitor.visitStatements(stmts)
      visitor.buf.toString
    }).mkString
  /// helper
  def testStartAndEnd(source: String, preorderDump: String) {
    assertResult(preorderDump)(statementsToString(compile(source), source))
  }
  // preorder traversal
  class PositionsCheckVisitor(source: String) extends core.AstVisitor {
    val buf = new StringBuilder()
    override def visitCommandBlock(node: core.CommandBlock) { visit(node); super.visitCommandBlock(node) }
    override def visitReporterApp(node: core.ReporterApp) { visit(node); super.visitReporterApp(node) }
    override def visitReporterBlock(node: core.ReporterBlock) { visit(node); super.visitReporterBlock(node) }
    override def visitStatement(node: core.Statement) { visit(node); super.visitStatement(node) }
    override def visitStatements(node: core.Statements) {
      if (node.stmts.isEmpty)
        buf.append(node.getClass.getSimpleName + " '' ")
      else visit(node)
      super.visitStatements(node)
    }
    def visit(node: core.AstNode) {
      val start = node.start - PREAMBLE.length
      val end = node.end - PREAMBLE.length
      val text =
        try "'" + source.substring(start, end) + "'"
        catch { case _: StringIndexOutOfBoundsException =>
          "out of bounds: " + ((start, end)) }
      buf.append(node.getClass.getSimpleName + " " + text + " ")
    }
  }

  def runTest(input: String, result: String, preamble: String = PREAMBLE) {
    assertResult(result)(compile(input, preamble).mkString)
  }
  def runFailure(input: String, message: String, start: Int, end: Int) {
    doFailure(input, message, start, end)
  }
  def doFailure(input: String, message: String, start: Int, end: Int) {
    val e = intercept[CompilerException] { compile(input) }
    assertResult(message)(e.getMessage)
    assertResult(start + PREAMBLE.length)(e.start)
    assertResult(end + PREAMBLE.length)(e.end)
  }

  /// now, the actual tests
  test("DoParseSimpleCommand") {
    runTest("__ignore round 0.5", "_ignore()[_round()[_const(0.5)[]]]")
  }
  test("DoParseCommandWithInfix") {
    runTest("__ignore 5 + 2", "_ignore()[_plus()[_const(5.0)[], _const(2.0)[]]]")
  }
  test("DoParseTwoCommands") {
    runTest("__ignore round 0.5 fd 5",
      "_ignore()[_round()[_const(0.5)[]]] " +
      "_fd()[_const(5.0)[]]")
  }
  test("DoParseBadCommand1") {
    runFailure("__ignore 1 2 3", "Expected command.", 11, 12)
  }
  test("DoParseBadCommand2") {
    runFailure("__ignore", "__IGNORE expected 1 input.", 0, 8)
  }
  test("DoParseReporterOnly") {
    runFailure("round 1.2", "Expected command.", 0, 5)
  }
  test("WrongArgumentType") {
    runFailure("__ignore count 0", "COUNT expected this input to be an agentset, but got a number instead", 15, 16)
  }
  test("missingCloseBracket") {
    // You could argue that it ought to point to the second bracket and not the first, but this is
    // fine. - ST 1/22/09
    runFailure("crt 10 [ [", "No closing bracket for this open bracket.", 7, 8)
  }
  test("missing name after let") {
    // here the error is at TokenType.Eof - ST 9/29/14
    runFailure("let", "Expected variable name here",
      core.Token.Eof.start - PREAMBLE.size,
      core.Token.Eof.end - PREAMBLE.size)
  }
  // https://github.com/NetLogo/NetLogo/issues/348
  test("let of task variable") {
    runFailure("foreach [1] [ let ? 0 ]",
      "Names beginning with ? are reserved for use as task inputs",
      18, 19)
  }
  test("DoParseMap") {
    runTest("__ignore map [round ?] [1.2 1.7 3.2]",
      "_ignore()[_map()[_reportertask()[_round()[_taskvariable(1)[]]], _const([1.2, 1.7, 3.2])[]]]")
  }
  test("DoParseMapShortSyntax") {
    runTest("__ignore map round [1.2 1.7 3.2]",
      "_ignore()[_map()[_reportertask()[_round()[_taskvariable(1)[]]], _const([1.2, 1.7, 3.2])[]]]")
  }
  test("DoParseForeach") {
    runTest("foreach [1 2 3] [__ignore ?]",
      "_foreach()[_const([1.0, 2.0, 3.0])[], _commandtask()[[_ignore()[_taskvariable(1)[]]]]]")
  }
  test("DoParseParenthesizedCommand") {
    runTest("(__ignore 5)",
      "_ignore()[_const(5.0)[]]")
  }
  test("DoParseParenthesizedCommandAsFromEvaluator") {
    runTest("__observercode (__ignore 5) __done",
      "_observercode()[] " +
      "_ignore()[_const(5.0)[]] " +
      "_done()[]")
  }
  test("ParseExpressionWithInfix") {
    runTest("__ignore 5 + 2",
      "_ignore()[_plus()[_const(5.0)[], _const(2.0)[]]]")
  }
  test("ParseExpressionWithInfix2") {
    runTest("__ignore 5 + 2 * 7",
      "_ignore()[_plus()[_const(5.0)[], _mult()[_const(2.0)[], _const(7.0)[]]]]")
  }
  test("ParseExpressionWithInfix3") {
    runTest("__ignore 5 + 2 * 7 - 2",
      "_ignore()[_minus()[_plus()[_const(5.0)[], _mult()[_const(2.0)[], _const(7.0)[]]], _const(2.0)[]]]")
  }
  test("ParseExpressionWithInfixAndPrefix") {
    runTest("__ignore round 5.2 + log 64 2 * log 64 2 - random 2",
      "_ignore()[_minus()[_plus()[_round()[_const(5.2)[]], _mult()[_log()[_const(64.0)[], _const(2.0)[]], _log()[_const(64.0)[], _const(2.0)[]]]], _random()[_const(2.0)[]]]]")
  }
  test("ParseConstantInteger") {
    runTest("__ignore 5",
      "_ignore()[_const(5.0)[]]")
  }
  test("ParseConstantList") {
    runTest("__ignore [5]",
      "_ignore()[_const([5.0])[]]")
  }
  test("ParseConstantListWithSublists") {
    runTest("__ignore [[1] [2]]",
      "_ignore()[_const([[1.0], [2.0]])[]]")
  }
  test("ParseConstantListInsideTask1") {
    runTest("__ignore n-values 10 [[]]",
      "_ignore()[_nvalues()[_const(10.0)[], _reportertask()[_const([])[]]]]")
  }
  test("ParseConstantListInsideTask2") {
    runTest("__ignore n-values 10 [[5]]",
      "_ignore()[_nvalues()[_const(10.0)[], _reportertask()[_const([5.0])[]]]]")
  }
  test("ParseCommandTask1") {
    runTest("__ignore task [print ?]",
      "_ignore()[_task()[_commandtask()[[_print()[_taskvariable(1)[]]]]]]")
  }
  test("ParseCommandTask2") {
    runTest("__ignore task [print 5]",
      "_ignore()[_task()[_commandtask()[[_print()[_const(5.0)[]]]]]]")
  }
  test("ParseCommandTask3") {
    // it would be nice if this resulted in a CompilerException instead
    // of failing at runtime - ST 2/6/11
    runTest("__ignore runresult task [__ignore 5]",
      "_ignore()[_runresult()[_task()[_commandtask()[[_ignore()[_const(5.0)[]]]]]]]")
  }
  test("ParseDiffuse") {
    runTest("diffuse pcolor 1",
      "_diffuse()[_patchvariable(2)[], _const(1.0)[]]")
  }

  // in SetBreed2, we are checking that since no singular form of `fish`
  // is provided and it defaults to `turtle`, that the primitive `turtle`
  // isn't mistaken for a singular form and parsed as `_breedsingular` - ST 4/12/14
  test("SetBreed1") {
    runTest("__ignore turtle 0",
      "_ignore()[_turtle()[_const(0.0)[]]]")
  }
  test("SetBreed2") {
    runTest("__ignore turtle 0",
      "_ignore()[_turtle()[_const(0.0)[]]]")
  }

  /// tests using testStartAndEnd
  test("StartAndEndPositions0") {
    testStartAndEnd("ca",
      "Statements 'ca' " +
      "Statement 'ca' ")
  }
  test("StartAndEndPositions1") {
    testStartAndEnd("__ignore 5",
      "Statements '__ignore 5' " +
      "Statement '__ignore 5' " +
      "ReporterApp '5' ")
  }
  test("StartAndEndPositions2") {
    testStartAndEnd("__ignore n-values 5 [world-width]",
      "Statements '__ignore n-values 5 [world-width]' " +
      "Statement '__ignore n-values 5 [world-width]' " +
      "ReporterApp 'n-values 5 [world-width]' " +
      "ReporterApp '5' " +
      "ReporterApp '[world-width]' " +
      "ReporterApp 'world-width' ")
  }
  test("StartAndEndPositions8") {
    testStartAndEnd("crt 1",
      "Statements 'crt 1' " +
      "Statement 'crt 1' " +
      "ReporterApp '1' " +
      "CommandBlock '' " +
      "Statements '' ")
  }
  test("StartAndEndPositions9") {
    testStartAndEnd("crt 1 [ ]",
      "Statements 'crt 1 [ ]' " +
      "Statement 'crt 1 [ ]' " +
      "ReporterApp '1' " +
      "CommandBlock '[ ]' " +
      "Statements '' ")
  }

  test("StartAndEndPositions10") {
    testStartAndEnd("ask turtles with [color = red ] [ fd 1 ]",
      "Statements 'ask turtles with [color = red ] [ fd 1 ]' " +
      "Statement 'ask turtles with [color = red ] [ fd 1 ]' " +
      "ReporterApp 'turtles with [color = red ]' " +
      "ReporterApp 'turtles' " +
      "ReporterBlock '[color = red ]' " +
      "ReporterApp 'color = red' " +
      "ReporterApp 'color' " +
      "ReporterApp 'red' " +
      "CommandBlock '[ fd 1 ]' " +
      "Statements 'fd 1' " +
      "Statement 'fd 1' " +
      "ReporterApp '1' ")
  }

  test("While") {
    testStartAndEnd("while [count turtles < 10] [ crt 1 ]",
      "Statements 'while [count turtles < 10] [ crt 1 ]' " +
      "Statement 'while [count turtles < 10] [ crt 1 ]' " +
      "ReporterBlock '[count turtles < 10]' " +
      "ReporterApp 'count turtles < 10' " +
      "ReporterApp 'count turtles' " +
      "ReporterApp 'turtles' " +
      "ReporterApp '10' " +
      "CommandBlock '[ crt 1 ]' " +
      "Statements 'crt 1' " +
      "Statement 'crt 1' " +
      "ReporterApp '1' " +
      "CommandBlock '' " +
      "Statements '' ")
  }

  // issue #417 (source positions for literal lists)
  test("literal list") {
    testStartAndEnd("print [1 2 3]",
      "Statements 'print [1 2 3]' " +
      "Statement 'print [1 2 3]' " +
      "ReporterApp '[1 2 3]' ")
  }

  /// duplicate name tests

  def duplicateName(s: String, err: String) = {
    val e = intercept[api.CompilerException] {
      FrontEnd.frontEnd(s)
    }
    assertResult(err)(e.getMessage)
  }

  test("LetSameNameAsCommandProcedure2") {
    duplicateName("to b let a 5 end  to a end",
      "There is already a procedure called A")
  }
  test("LetSameNameAsReporterProcedure2") {
    duplicateName("to b let a 5 end  to-report a end",
      "There is already a procedure called A")
  }
  test("LetNameSameAsEnclosingCommandProcedureName") {
    duplicateName("to bazort let bazort 5 end",
      "There is already a procedure called BAZORT")
  }
  test("LetNameSameAsEnclosingReporterProcedureName") {
    duplicateName("to-report bazort let bazort 5 report bazort end",
      "There is already a procedure called BAZORT")
  }
  test("SameLocalVariableTwice1") {
    duplicateName("to a1 locals [b b] end",
      "Nothing named LOCALS has been defined.")
  }
  test("SameLocalVariableTwice2") {
    duplicateName("to a2 [b b] end",
      "There is already a local variable called B here")
  }
  test("SameLocalVariableTwice3") {
    duplicateName("to a3 let b 5 let b 6 end",
      "There is already a local variable here called B")
  }
  test("SameLocalVariableTwice4") {
    duplicateName("to a4 locals [b] let b 5 end",
      "Nothing named LOCALS has been defined.")
  }
  test("SameLocalVariableTwice5") {
    duplicateName("to a5 [b] locals [b] end",
      "Nothing named LOCALS has been defined.")
  }
  test("SameLocalVariableTwice6") {
    duplicateName("to a6 [b] let b 5 end",
      "There is already a local variable here called B")
  }

}
