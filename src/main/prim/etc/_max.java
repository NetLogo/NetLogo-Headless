// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.Dump;
import org.nlogo.core.I18N;
import org.nlogo.api.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.core.prim.Pure;
import org.nlogo.nvm.Reporter;

public final strictfp class _max extends Reporter implements Pure {
  @Override
  public Object report(Context context) {
    LogoList list = argEvalList(context, 0);
    double winner = 0;
    Double boxedWinner = null;
    for (Object elt : list) {
      if (elt instanceof Double) {
        Double boxedValue = (Double) elt;
        double value = boxedValue.doubleValue();
        if (boxedWinner == null || value > winner) {
          winner = value;
          boxedWinner = boxedValue;
        }
      }
    }
    if (boxedWinner == null) {
      throw new EngineException(context, this,
          I18N.errorsJ().getN("org.nlogo.prim._max.cantFindMaxOfListWithNoNumbers", Dump.logoObject(list)));
    }
    return boxedWinner;
  }
}
