// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang

import java.io.File
import org.nlogo.headless.test.{Finder => TestFinder, ExtensionTests, ModelTests, ReporterTests, CommandTests,
                                AbstractFixture, TestMode, Reporter, Command, Declaration, Open, LanguageTest}
import org.scalatest.{ FunSuite, Tag }

import
  org.nlogo.{ api, core, util },
    api.FileIO.file2String,
    core.{ Model, Resource },
    util.SlowTest

trait Finder extends TestFinder {
  override def withFixture[T](name: String)(body: AbstractFixture => T): T =
    Fixture.withFixture(name) { fixture =>
      System.setProperty("netlogo.extensions.dir", "jvm/extensions")
      body(fixture)
    }
}

class TestCommands extends CommandTests with Finder
class TestReporters extends ReporterTests with Finder
class TestModels extends ModelTests with Finder
class TestExtensions extends ExtensionTests with Finder
