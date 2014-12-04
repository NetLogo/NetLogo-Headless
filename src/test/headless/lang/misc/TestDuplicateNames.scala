// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang
package misc

import org.nlogo.core.CompilerException
import org.nlogo.{ api, nvm }
import org.nlogo.util.SlowTest  // depends on array extension

// Checking for error messages generated by compiling procedures.  (LanguageTest only knows how to
// check for errors that come from commands and reporters.).  Also, we want to do lots of similar
// tests, so rather than copy-and-paste them in a .txt file, we generate the tests on the fly.

class TestDuplicateNames extends FixtureSuite with SlowTest {

  def testBadName(name: String, error: String, declarations: String = "") {
    def check(source: String, note: String) {
      test(s"$name - $note") { fixture =>
        import fixture._
        val ex = intercept[CompilerException] {
          declare(source)
        }
        assertResult(error)(ex.getMessage)
      }
    }
    check(s"$declarations\nto $name end", "as procedure name")
    check(s"$declarations\nto foo [$name] end", "as input name")
  }

  testBadName("fd",
    "There is already a primitive command called FD")
  testBadName("turtles",
    "There is already a primitive reporter called TURTLES")
  testBadName("???",
    "Names beginning with ? are reserved for use as task inputs")
  testBadName("kitten",
    "There is already a breed called KITTEN", "breed [kittens kitten]")
  testBadName("kittens",
    "There is already a breed called KITTENS", "breed [kittens kitten]")
  testBadName("turtles-at",
    "There is already a primitive reporter called TURTLES-AT")
  testBadName("shell",
    "There is already a TURTLES-OWN variable called SHELL",
    "turtles-own [shell]")
  testBadName("silliness",
    "There is already a KITTENS-OWN variable called SILLINESS",
    "breed [kittens kitten] kittens-own [silliness]")
  testBadName("weight",
    "There is already a EDGES-OWN variable called WEIGHT",
    "undirected-link-breed [edges edge] " +
      "breed [nodes node] " +
      "breed [monsters monster] " +
      "edges-own [weight] " +
      "nodes-own [weight] " +
      "monsters-own [weight]")
  testBadName("end1",
    "There is already a link variable called END1")
  testBadName("size",
    "There is already a turtle variable called SIZE")
  testBadName("color", // well, is actually both turtle and link variable - ST 5/16/03
    "There is already a turtle variable called COLOR")
  testBadName("pcolor",
    "There is already a patch variable called PCOLOR")
  testBadName("kittens-at",
    "There is already a breed reporter called KITTENS-AT",
    "breed [kittens kitten]")
  testBadName("sprout-kittens",
    "There is already a breed command called SPROUT-KITTENS",
    "breed [kittens kitten]"
  )

  // at least we get errors on these, but the messages aren't great
  // https://github.com/NetLogo/NetLogo/issues/352
  testBadName("array:set",
    "There is already a _extern called ARRAY:SET",
    "extensions [array]")

}
