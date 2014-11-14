// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import org.scalatest.FunSuite

class WidgetTests extends FunSuite {
  test("Chooser handles choices with lists") {
    val l = new LogoList {
      val toList = List(1, 2, 3).map(_.toDouble).asInstanceOf[List[AnyRef]]
      override def toString: String = toList.toString()
    }

    val chooser = Chooser(
      display = "FOOBAR",
      varName = "FOOBAR",
      choices = List(ChooseableList(l), ChooseableDouble(4.toDouble)))
    assertResult("[[1.0 2.0 3.0] 4.0]")(chooser.constraint(1))
  }
}
