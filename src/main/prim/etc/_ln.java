// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.core.I18N;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.core.Pure;
import org.nlogo.nvm.Reporter;

public final strictfp class _ln extends Reporter implements Pure {

  @Override
  public Object report(Context context) {
    return report_1(context, argEvalDoubleValue(context, 0));
  }

  public double report_1(Context context, double d) {
    if (d <= 0) {
      throw new EngineException(context, this,
          I18N.errorsJ().getN("org.nlogo.prim.etc.$common.cantTakeLogarithmOf", d));
    }
    return validDouble(StrictMath.log(d));
  }
}
