// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.api.Dump;
import org.nlogo.core.I18N;
import org.nlogo.core.LogoList;
import org.nlogo.core.Pure;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

import java.util.Iterator;

public final strictfp class _min extends Reporter implements Pure {

  @Override
  public Object report(Context context) {
    return report_1(context, argEvalList(context, 0));
  }

  public double report_1(Context context, LogoList list) {
    double winner = 0;
    Double boxedWinner = null;

    for (Iterator<Object> i = list.javaIterator(); i.hasNext();) {
      Object elt = i.next();
      if (elt instanceof Double) {
        Double boxedValue = (Double) elt;
        double value = boxedValue.doubleValue();
        if (boxedWinner == null || value < winner) {
          winner = value;
          boxedWinner = boxedValue;
        }
      }
    }
    if (boxedWinner == null) {
      throw new EngineException(context, this,
              I18N.errorsJ().getN("org.nlogo.prim._min.cantFindMinOfListWithNoNumbers", Dump.logoObject(list)));
    }
    return boxedWinner;
  }
}
