// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

trait Instruction extends TokenHolder {
  def syntax: Syntax
  var token: Token = null
  def displayName = token.text.toUpperCase
  override def toString = getClass.getSimpleName
}

trait Reporter extends Instruction

trait Command extends Instruction
