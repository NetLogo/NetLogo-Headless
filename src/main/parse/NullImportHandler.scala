// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.{ api, agent, core },
  core.{ LiteralImportHandler, Token },
  api.Fail._

object NullImportHandler extends LiteralImportHandler {
  private val ERR_ILLEGAL_AGENT_LITERAL = "Can only have literal agents and agentsets if importing."

  override def parseExtensionLiteral(token: Token): AnyRef = {
    cAssert(false, ERR_ILLEGAL_AGENT_LITERAL, token)
    ""
  }

  override def parseLiteralAgentOrAgentSet(tokens: Iterator[Token], parser: LiteralImportHandler.Parser): AnyRef = {
    cAssert(false, ERR_ILLEGAL_AGENT_LITERAL, tokens.next())
    ""
  }
}
