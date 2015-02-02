// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

object I18N {

  case class Prefix(name: String)

  class BundleKind(errorStrings: Map[String, String]) {
    def apply(key: String)(implicit prefix: Prefix) = get(prefix.name + "." + key)
    def get(key: String) = getN(key)
    def getN(key: String, args: AnyRef*) = {
      val templateString = errorStrings.getOrElse(key, throw new IllegalArgumentException(
        "coding error, bad translation key: " + key + " for Errors"))
      (args zip (0 until args.length)).foldLeft(templateString) {
        case (templatedString, (substitution: String, i: Int)) =>
          templatedString.replaceAll("\\{" + i.toString + "\\}", substitution)
      }
    }
  }

  lazy val errors = new BundleKind(I18NBundle.errorBundle)
}
