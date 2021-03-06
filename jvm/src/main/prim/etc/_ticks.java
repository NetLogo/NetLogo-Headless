// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.I18N;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Reporter;

public final strictfp class _ticks extends Reporter {

  @Override
  public Double report(final Context context)
      throws EngineException {
    return report_1(context);
  }

  public double report_1(final Context context)
      throws EngineException {
    double result = world.ticks();
    if (result == -1) {
      throw new EngineException(
          context, this, I18N.errorsJ().get("org.nlogo.prim.etc.$common.tickCounterNotStarted"));
    }
    return result;
  }
}
