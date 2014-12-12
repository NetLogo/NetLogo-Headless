// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.FrontEndProcedure
import org.nlogo.{ api, core },

  core.StructureDeclarations.Procedure

class RawProcedure(_procedureDeclaration: Procedure, val displayNameOption: Option[String]) extends
  Procedure(_procedureDeclaration.ident, _procedureDeclaration.isReporter, _procedureDeclaration.inputs, _procedureDeclaration.tokens) with FrontEndProcedure {

  override def procedureDeclaration: Procedure = this

  val nameToken: core.Token = _procedureDeclaration.tokens.tail.head
  val argTokens: Seq[core.Token] = _procedureDeclaration.inputs.map(_.token)

  args = _procedureDeclaration.inputs.map(_.name).toVector

  def displayName: String = displayNameOption.getOrElse("")

  def syntax: core.Syntax = {
    val right = List.fill(argTokens.size)(core.Syntax.WildcardType)
    if (isReporter)
      core.Syntax.reporterSyntax(right = right, ret = core.Syntax.WildcardType)
    else
      core.Syntax.commandSyntax(right = right)
  }

  val name = nameToken.value.asInstanceOf[String]

  def dump: String = {
    val buf = new StringBuilder
    if (isReporter)
      buf ++= "reporter "
    buf ++= s"procedure $name"
    buf ++= ":"
    buf ++= args.mkString("[", " ", "]")
    buf ++= "{OTPL}:\n"
    // excludes code and children segments, since this doesn't have those
    buf.toString()
  }

  def filename: String = nameToken.filename
}
