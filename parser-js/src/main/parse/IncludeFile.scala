// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.ExtensionManager

object IncludeFile {
  def apply(extensionManager: ExtensionManager, suppliedPath: String): Option[(String, String)] = {
    None
  }
}
