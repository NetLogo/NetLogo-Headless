// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core
package prim

//scalastyle:off number.of.types
case class _and() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.BooleanType,
      right = List(Syntax.BooleanType),
      ret = Syntax.BooleanType,
      precedence = Syntax.NormalPrecedence - 6)
}
case class _any() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.AgentsetType),
      ret = Syntax.BooleanType)
}
case class _ask() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.AgentsetType | Syntax.AgentType, Syntax.CommandBlockType),
      agentClassString = "OTPL",
      blockAgentClassString = Option("?"))
}
case class _askconcurrent() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.AgentsetType, Syntax.CommandBlockType),
      agentClassString = "OTPL",
      blockAgentClassString = Option("?"))
}
case class _bk() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.NumberType),
      agentClassString = "-T--")
}
case class _breed(breedName: String) extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.TurtlesetType)
}
case class _breedvariable(name: String) extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.WildcardType | Syntax.ReferenceType,
      agentClassString = "-T--")
}
case class _call(proc: FrontEndProcedure) extends Command {
  agentClassString = proc.agentClassString

  def name: String = proc.name
  override def syntax = proc.syntax
  override def toString =
    s"_call($name)"
}
case class _callreport(proc: FrontEndProcedure) extends Reporter {
  agentClassString = proc.agentClassString

  def name: String = proc.name
  override def syntax = proc.syntax
  override def toString =
    s"_call($name)"
}
case class _carefully() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.CommandBlockType, Syntax.CommandBlockType))
}
case class _commandtask(minArgCount: Int = 0) extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.CommandTaskType)

  def copy(minArgCount: Int = minArgCount): _commandtask = {
    val ct = new _commandtask(minArgCount)
    ct.agentClassString = agentClassString
    ct.token = token
    ct
  }
}
case class _const(value: AnyRef) extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      ret = value match {
        case b: java.lang.Boolean => Syntax.BooleanType
        case d: java.lang.Double => Syntax.NumberType
        case l: LogoList => Syntax.ListType
        case s: String => Syntax.StringType
        case _ => Syntax.WildcardType
      })
}
case class _count() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.AgentsetType),
      ret = Syntax.NumberType)
}
case class _createorderedturtles(breedName: String) extends Command {
  def this() = this("")
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.NumberType, Syntax.CommandBlockType | Syntax.OptionalType),
      agentClassString = "O---",
      blockAgentClassString = Option("-T--"))
}
case class _createturtles(breedName: String) extends Command {
  def this() = this("")
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.NumberType, Syntax.CommandBlockType | Syntax.OptionalType),
      agentClassString = "O---",
      blockAgentClassString = Option("-T--"))
}
case class _done() extends Command {
  override def syntax = Syntax.commandSyntax()
}
case class _equal() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.WildcardType,
      right = List(Syntax.WildcardType),
      ret = Syntax.BooleanType,
      precedence = Syntax.NormalPrecedence - 5)
}
case class _errormessage() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.StringType)
}
case class _extern(syntax: Syntax) extends Command {
  override def toString =
    s"_extern(${token.text.toUpperCase})"
}
case class _externreport(syntax: Syntax) extends Reporter {
  override def toString =
    s"_externreport(${token.text.toUpperCase})"
}
case class _fd() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.NumberType),
      agentClassString = "-T--")
}
case class _greaterthan() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.NumberType | Syntax.StringType | Syntax.AgentType,
      right = List(Syntax.NumberType | Syntax.StringType | Syntax.AgentType),
      ret = Syntax.BooleanType,
      precedence = Syntax.NormalPrecedence - 4)
}
case class _hatch(breedName: String) extends Command {
  def this() = this("")
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.NumberType, Syntax.CommandBlockType | Syntax.OptionalType),
      agentClassString = "-T--",
      blockAgentClassString = Option("-T--"))
}
case class _inradius() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.TurtlesetType | Syntax.PatchsetType,
      right = List(Syntax.NumberType),
      ret = Syntax.TurtlesetType | Syntax.PatchsetType,
      precedence = Syntax.NormalPrecedence + 2,
      agentClassString = "-TP-")
}
case class _jump() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.NumberType),
      agentClassString = "-T--")
}
case class _lessthan() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.NumberType | Syntax.StringType | Syntax.AgentType,
      right = List(Syntax.NumberType | Syntax.StringType | Syntax.AgentType),
      ret = Syntax.BooleanType,
      precedence = Syntax.NormalPrecedence - 4)
}
case class _let(let: Let) extends Command {
  def this() = this(null)
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.WildcardType, Syntax.WildcardType))
}
case class _letname() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.WildcardType)
}
case class _letvariable(let: Let) extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.WildcardType)
}
case class _linkbreedvariable(name: String) extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.WildcardType | Syntax.ReferenceType,
      agentClassString = "---L")
}
case class _linkvariable(vn: Int) extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.WildcardType | Syntax.ReferenceType,
      agentClassString = "---L")
}
case class _list() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.RepeatableType | Syntax.WildcardType),
      ret = Syntax.ListType,
      defaultOption = Some(2),
      minimumOption = Some(0))
}
case class _minus() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.NumberType,
      right = List(Syntax.NumberType),
      ret = Syntax.NumberType,
      precedence = Syntax.NormalPrecedence - 3)
}
case class _neighbors() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.PatchsetType,
      agentClassString = "-TP-")
}
case class _neighbors4() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.PatchsetType,
      agentClassString = "-TP-")
}
case class _nobody() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.NobodyType)
}
case class _not() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.BooleanType),
      ret = Syntax.BooleanType)
}
case class _notequal() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.WildcardType,
      right = List(Syntax.WildcardType),
      ret = Syntax.BooleanType,
      precedence = Syntax.NormalPrecedence - 5)
}
case class _observervariable(vn: Int) extends Reporter with Referenceable {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.WildcardType | Syntax.ReferenceType)
  def makeReference =
    new Reference(AgentKind.Observer, vn, this)
}
case class _of() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.ReporterBlockType,
      right = List(Syntax.AgentType | Syntax.AgentsetType),
      ret = Syntax.WildcardType,
      precedence = Syntax.NormalPrecedence + 1,
      isRightAssociative = true,
      agentClassString = "OTPL",
      blockAgentClassString = Option("?"))
}
case class _oneof() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.AgentsetType | Syntax.ListType),
      ret = Syntax.WildcardType)
}
case class _or() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.BooleanType,
      right = List(Syntax.BooleanType),
      ret = Syntax.BooleanType,
      precedence = Syntax.NormalPrecedence - 6)
}
case class _other() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.AgentsetType),
      ret = Syntax.AgentsetType)
}
case class _patchat() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType, Syntax.NumberType),
      ret = Syntax.PatchType | Syntax.NobodyType,
      agentClassString = "-TP-")
}
case class _patches() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.PatchsetType)
}
case class _patchvariable(vn: Int) extends Reporter with Referenceable {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.WildcardType | Syntax.ReferenceType,
      agentClassString = "-TP-")
  def makeReference =
    new Reference(AgentKind.Patch, vn, this)
}
case class _procedurevariable(vn: Int, name: String) extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.WildcardType)
}
case class _random() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.NumberType)
}
case class _repeat() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.NumberType, Syntax.CommandBlockType))
}
case class _reportertask(minArgCount: Int = 0) extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.ReporterTaskType)

  def copy(minArgCount: Int = minArgCount): _reportertask = {
    val rt = new _reportertask(minArgCount)
    rt.agentClassString = agentClassString
    rt.token = token
    rt
  }
}
case class _return() extends Command {
  override def syntax =
    Syntax.commandSyntax()
  // for use in error messages
  override def displayName =
    "END"
}
case class _sentence() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.RepeatableType | Syntax.WildcardType),
      ret = Syntax.ListType,
      defaultOption = Some(2),
      minimumOption = Some(0))
}
case class _set() extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.WildcardType, Syntax.WildcardType))
}
case class _sprout(breedName: String) extends Command {
  def this() = this("")
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.NumberType, Syntax.CommandBlockType | Syntax.OptionalType),
      agentClassString = "--P-",
      blockAgentClassString = Option("-T--"))
}
case class _sum() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.ListType),
      ret = Syntax.NumberType)
}
// This is used only for parsing and removed from the AST before compilation
case class _task() extends Reporter {
  override def syntax = {
    val anyTask = Syntax.CommandTaskType | Syntax.ReporterTaskType
    Syntax.reporterSyntax(
      right = List(anyTask),
      ret = anyTask)
  }
}
case class _taskvariable(vn: Int) extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.WildcardType)
}
case class _turtle() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.TurtleType | Syntax.NobodyType)
}
case class _turtles() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.TurtlesetType)
}
case class _turtleorlinkvariable(varName: String) extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.WildcardType | Syntax.ReferenceType,
      agentClassString = "-T-L")
}
case class _turtlevariable(vn: Int) extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.WildcardType | Syntax.ReferenceType,
      agentClassString = "-T--")
}
case class _unaryminus() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.NumberType)
}
case class _with() extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.AgentsetType,
      right = List(Syntax.BooleanBlockType),
      ret = Syntax.AgentsetType,
      precedence = Syntax.NormalPrecedence + 2,
      isRightAssociative = false,
      agentClassString = "OTPL",
      blockAgentClassString = Option("?"))
}
case class _word() extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.RepeatableType | Syntax.WildcardType),
      ret = Syntax.StringType,
      defaultOption = Some(2),
      minimumOption = Some(0))
}
//scalastyle:on number.of.types
