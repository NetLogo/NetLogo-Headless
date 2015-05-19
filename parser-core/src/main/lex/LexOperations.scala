// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import scala.annotation.tailrec
import org.nlogo.core.TokenType

object LexOperations {
  type LexPredicate = Char => DetectorStates
  type TokenGenerator = String => Option[(String, TokenType, AnyRef)]

  def characterMatching(f: Char => Boolean): LexPredicate =
    aSingle(c => if (f(c)) Accept else Error)

  def zeroOrMore(f: Char => Boolean): LexPredicate =
    { c => if (f(c)) Accept else Finished }

  def oneOrMore(f: Char => Boolean): LexPredicate =
    chain(characterMatching(f), { c => if (f(c)) Accept else Finished })

  def aSingle(a: LexPredicate): LexPredicate =
    withFeedback(false) {
      case (hasRunOnce, _) if hasRunOnce => (true, Finished)
      case (hasRunOnce, c)               => (true, a(c))
    }

  def anyOf(predicates: LexPredicate*): LexPredicate =
    withFeedback(predicates) {
      case (preds,            c) if preds.isEmpty => (preds, Finished)
      case (activePredicates, c)                  =>
        val results             = activePredicates.map(_(c))
        val remainingPredicates = (activePredicates zip results).filter(_._2 == Accept).map(_._1)
        (remainingPredicates, results.reduce(_ or _))
    }

  def chain(ds: LexPredicate*): LexPredicate = {
    @tailrec def attemptMatch(matchers: Seq[LexPredicate], c: Char): (Seq[LexPredicate], DetectorStates) =
      matchers.head(c) match {
        case Finished if matchers.tail.nonEmpty => attemptMatch(matchers.tail, c)
        case Finished                           => (Seq(), Finished)
        case state                              => (matchers, state)
      }
    withFeedback(ds) {
      case (matchers, c) => attemptMatch(matchers, c)
    }
  }

  sealed trait DetectorStates {
    def continue: Boolean
    def or(d: DetectorStates): DetectorStates
  }

  case object Accept extends DetectorStates {
    val continue = true
    override def or(d: DetectorStates): DetectorStates = Accept
  }

  case object Finished extends DetectorStates {
    val continue = false
    override def or(d: DetectorStates): DetectorStates = {
      d match {
        case Accept => Accept
        case _ => Finished
      }
    }
  }

  case object Error extends DetectorStates {
    val continue = false
    override def or(d: DetectorStates): DetectorStates = d
  }

  def withFeedback[A](i: A)(f: (A, Char) => (A, DetectorStates)): LexPredicate = {
    var feedback: A = i
    def feedingBack(c: Char): DetectorStates = {
      val (newFeedback, ret) = f(feedback, c)
      feedback = newFeedback
      ret
    }
    feedingBack
  }

  object PrefixConversions {
    import language.implicitConversions

    implicit def charToLexPredicate(literalChar: Char): LexPredicate =
      withFeedback(false) {
        case (hasRunOnce, _) if hasRunOnce => (true, Finished)
        case (hasRunOnce, c)               => (true, if (c == literalChar) Accept else Error)
      }

    implicit def stringToLexPredicate(literalString: String): LexPredicate =
      withFeedback(literalString) {
        case ("", char) => ("", Finished)
        case (s, char)  => (s.tail, if (s.head == char) Accept else Error)
      }
  }
}
