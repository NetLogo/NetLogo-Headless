// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc;

import org.nlogo.agent.PatchException;
import org.nlogo.api.AgentException;
import org.nlogo.api.Dump;
import org.nlogo.api.I18N;
import org.nlogo.api.TypeNames;
import org.nlogo.nvm.Command;
import org.nlogo.nvm.Context;
import org.nlogo.nvm.EngineException;
import org.nlogo.nvm.Referencer;

public final strictfp class _diffuse
    extends Command implements Referencer {

  private int _vn;
  @Override
  public int vn() {
    return _vn;
  }

  @Override
  public void vn_$eq(int vn) {
    _vn = vn;
  }

  public String toString() {
    if (world != null) {
      return super.toString() + ":" + world.patchesOwnNameAt(_vn);
    } else {
      return super.toString();
    }
  }

  @Override
  public void perform(final Context context) {
    double amount = argEvalDoubleValue(context, 0);
    if (amount < 0.0 || amount > 1.0) {
      throw new EngineException
          (context, this, I18N.errorsJ().getN("org.nlogo.prim.$common.paramOutOfBounds", amount));
    }
    try {
      world.diffuse(amount, _vn);
    } catch (AgentException ex) {
      throw new EngineException(context, this, ex.getMessage());
    } catch (PatchException ex) {
      Object value = ex.patch().getPatchVariable(_vn);
      throw new EngineException
          (context, this,
              ex.patch() + " should contain a number in the " + world.patchesOwnNameAt(_vn) +
                  " variable, but contains " +
                  (value == org.nlogo.api.Nobody$.MODULE$
                      ? "NOBODY"
                      : "the " + TypeNames.name(value) + " " + Dump.logoObject(value)) +
                  " instead");
    }
    context.ip = next;
  }
}
