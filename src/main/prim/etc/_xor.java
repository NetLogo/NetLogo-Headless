// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;
import org.nlogo.nvm.Reporter;

public final strictfp class _xor
    extends Reporter
    implements org.nlogo.core.prim.Pure {
  @Override
  public Object report(final org.nlogo.nvm.Context context) {
    boolean b1 = argEvalBooleanValue(context, 0);
    boolean b2 = argEvalBooleanValue(context, 1);
    if (b1) {
      return b2 ? Boolean.FALSE : Boolean.TRUE;
    } else {
      return b2 ? Boolean.TRUE : Boolean.FALSE;
    }
  }

}
