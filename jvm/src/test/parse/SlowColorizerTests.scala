// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.scalatest.FunSuite
import org.nlogo.util.SlowTest

class SlowColorizerTests extends FunSuite with SlowTest {

  // very long Code tabs shouldn't blow the stack.
  // slow, hence SlowTest
  test("don't blow stack") {
    val longCode = io.Source.fromFile("models/test/Really Long Code.nls").mkString
    assertResult(1010916)(
      Colorizer.toHtml(longCode).size)
  }
}
