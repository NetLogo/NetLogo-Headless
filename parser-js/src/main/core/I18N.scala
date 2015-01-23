// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

object I18N {

  case class Prefix(name: String)

  class BundleKind(name: String) {

    // TODO: Abstract user preferences so that JVM NetLogo and JS NetLogo can work separately
    //
    // TODO: Provide a way for javascript to load up resource files, separate from JVM NetLogo
    def apply(key: String)(implicit prefix: Prefix) = get(prefix.name + "." + key)
    def get(key: String) = getN(key)
    def getN(key: String, args: AnyRef*) = {
      s"key ${args.mkString(", ")}"
    }
  }

  lazy val errors = new BundleKind("Errors")
}
