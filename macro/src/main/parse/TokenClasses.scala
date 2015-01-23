// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.{ Context => BlackBoxContext}

object TokenClasses {
  val FileEntries: Seq[(String, String, String)] =
    io.Source.fromFile("/Users/rgg284/IdeaProjects/NetLogo-Headless/resources/main/system/tokens.txt")
      .getLines()
      .toSeq
      .map {
      case l: String =>
        val List(tpe, primName, className) = l.split(' ').toList
        (tpe, primName, className)
    }

  def compiledReporters[T](packagePrefix: String): Map[String, () => T] = macro compileReporters[T]
  def compiledCommands[T](packagePrefix: String): Map[String, () => T] = macro compileCommands[T]

  def poolForPackage[T](packagePrefix: String): Map[String, (String => T)] = macro reflectingPool[T]

  def compileReporters[T: c.WeakTypeTag](c: BlackBoxContext)(packagePrefix: c.Tree): c.Tree = {
    import c.universe._
    packagePrefix match {
      case q"${packageName: String}" =>
        val mapElems = FileEntries.collect {
          case ("R", commandName, className) => (commandName, className)
        }.map {
          case (key, className) =>
            val klass = c.mirror.staticClass(s"$packageName.$className")
            q"$key -> (() => new $klass())"
        }.toList
        q"Map(..$mapElems)"
      case _ => c.abort(c.enclosingPosition, "Must supply a string literal to compileReporters")
    }
  }

  def compileCommands[T: c.WeakTypeTag](c: BlackBoxContext)(packagePrefix: c.Tree): c.Tree = {
    import c.universe._
    packagePrefix match {
      case q"${packageName: String}" =>
        val mapElems = FileEntries.collect {
          case ("C", commandName, className) => (commandName, className)
        }.map {
          case (key, className) =>
            val klass = c.mirror.staticClass(s"$packageName.$className")
            q"$key -> (() => new $klass())"
        }.toList
        q"Map(..$mapElems)"
      case _ => c.abort(c.enclosingPosition, "Must supply a string literal to compileReporters")
    }
  }

  def constructorMap(c: BlackBoxContext)(constructorMap: Map[String, String]): c.Tree = {
    import c.universe._
    val mapElems = constructorMap.map {
      case (key, className) =>
        val klass = c.mirror.staticClass(className)
        q"$key -> (() => new $klass())"
    }.toList
    q"Map(..$mapElems)"
  }

  def reflectingPool[T: c.WeakTypeTag](c: BlackBoxContext)(packagePrefix: c.Tree)(implicit expectedType: c.WeakTypeTag[T]): c.Tree = {
    import c.universe._
    packagePrefix match {
      case q"${ packageName: String }" =>
        try {
          val pkg = c.mirror.staticPackage(packageName).typeSignature.decls
          val subpkgs = pkg.filter(_.isPackage).flatMap(_.typeSignature.decls)
          val constructorClosures = (pkg ++ subpkgs).collect {
            case cp: ClassSymbol => (cp, cp.toType)
          }.filter {
            case (cp: ClassSymbol, t: Type) if cp.isClass
              && ! cp.isModule && ! cp.isTrait && ! cp.isAbstract
              && t <:< expectedType.tpe => true
            case _ => false
          }.filter {
            case (cp: ClassSymbol, t: Type) =>
              cp.typeSignature.decls.exists {
                case m: MethodSymbol if m.isConstructor =>
                  m.paramLists.exists(prms => prms.length == 1 && prms.head.typeSignature =:= typeOf[String])
                case _ => false
              }
          }.map {
            case (cp: ClassSymbol, _) =>
              q"""${cp.fullName} -> ((s: String) => new $cp(s))"""
          }
          q"Map(..$constructorClosures)"
        } catch {
          case ex: ScalaReflectionException => c.abort(c.enclosingPosition, s"Invalid package: $packageName")
        }
      case _ => c.abort(c.enclosingPosition, "Must supply a string literal to poolForPackage")
    }
  }
}
