// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.I18N;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;
import org.nlogo.nvm.EngineException;

public final strictfp class _importdrawing
    extends org.nlogo.nvm.Command {

  public _importdrawing() {
    switches = true;
  }

  @Override
  public void perform(final org.nlogo.nvm.Context context) {
    try {
      workspace.importDrawing
          (workspace.fileManager().attachPrefix
              (argEvalString(context, 0)));
    } catch (java.io.IOException ex) {
      throw new EngineException
          (context, _importdrawing.this,
              token().text() +
                  ": " + ex.getMessage());
    } catch (RuntimeException ex) {
      throw new EngineException
          (context, _importdrawing.this,
              token().text() +
                  ": " + ex.getMessage());
    }
    context.ip = next;
  }
}
