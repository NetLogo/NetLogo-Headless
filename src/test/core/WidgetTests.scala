// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import org.scalatest.FunSuite

class WidgetTests extends FunSuite {
  test("Chooser handles choices with lists") {
    val l = new LogoList {
      val toList = List(1, 2, 3).map(_.toDouble).asInstanceOf[List[AnyRef]]
      override def toString: String = toList.toString()
    }

    val testChoices = List(l, 4.toDouble).asInstanceOf[List[AnyRef]]
    val chooser = Chooser(display = "FOOBAR", varName = "FOOBAR", choices = testChoices)
    assertResult(ChoiceConstraintSpecification(testChoices, 0))(chooser.constraint)
  }
}
