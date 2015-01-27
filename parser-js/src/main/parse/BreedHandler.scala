// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{BreedIdentifierHandler, Instruction, Primitive, Program, Token, TokenType}

// go thru our breed prim handlers, if one triggers, return the result
class BreedHandler(program: Program) extends NameHandler {
  val reflectionPool = TokenClasses.poolForPackage[Instruction]("org.nlogo.core.prim")

  override def apply(token: Token) = {
    BreedIdentifierHandler.process(token, program) map {
      case (className, breedName, tokenType) =>
        (tokenType, reflectionPool(s"org.nlogo.core.prim.$className")(breedName))
    }
  }
}
