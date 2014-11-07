// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package front

import org.nlogo.{ api, core },
  api.FrontEndProcedure,
  core.StructureDeclarations.Procedure

class RawProcedure(val procedureDeclaration: Procedure, val displayNameOption: Option[String]) extends FrontEndProcedure {
  val isReporter: Boolean = procedureDeclaration.isReporter
  val nameToken: core.Token = procedureDeclaration.tokens.tail.head
  val argTokens: Seq[core.Token] = procedureDeclaration.inputs.map(_.token)

  args = procedureDeclaration.inputs.map(_.name).toVector

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
