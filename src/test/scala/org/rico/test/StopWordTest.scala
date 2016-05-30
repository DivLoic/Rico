package org.rico.test

import org.scalatest.FunSuite
import org.apache.lucene.analysis.{Analyzer, StopAnalyzer, TokenStream}
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.analysis.fr.FrenchAnalyzer
import org.apache.lucene.util.Version
import java.io.StringReader


/**
  * Created by loicmdivad on 26/05/2016.
  */
class StopWordTest extends FunSuite {

  val ens:Analyzer = new StopAnalyzer(Version.LUCENE_36)
  val fa:Analyzer = new FrenchAnalyzer(Version.LUCENE_36)

  def didFilter(blackList:Seq[String], current:String): Unit = {
    assert(!blackList.contains(current))
  }

  test("Should filter english stop words") {
    val text = "Lucene is simple yet powerful java based search library."
    val tsm:TokenStream = ens.tokenStream("rico", new StringReader(text))
    val term:CharTermAttribute = tsm.addAttribute(classOf[CharTermAttribute])

    tsm.reset()
    while(tsm.incrementToken){
      didFilter(Seq("is"), term.toString)
    }
  }

  test("Should filter french stop words") {
    val text = "Tracfin est un Service de renseignement rattaché au " +
      "Ministère des Finances et des Comptes publics."
    val tsm:TokenStream = fa.tokenStream(null, new StringReader(text))
    val term:CharTermAttribute = tsm.addAttribute(classOf[CharTermAttribute])

    tsm.reset()

    while(tsm.incrementToken){
      didFilter(
        Seq("est", "un", "de", "au", "des", "et"),
        term.toString
      )
    }
  }

  test("Should filter the very common words") {
    val text = "TensorFlow est un outil une librairie pour le deeplearning développé par Google"
    val tsm = fa.tokenStream(null, new StringReader(text))
    val term:CharTermAttribute = tsm.addAttribute(classOf[CharTermAttribute])

    val passing = Iterator.iterate((tsm, term)){case (a,b) => (a,b)}
      .takeWhile(_._1.incrementToken)
      .map(_._2.toString)

    val stopped = Seq("est","un","par","pour").toSet
    val intersect = passing.toSet.intersect(stopped)
    assertResult (0) (intersect.size)
  }
}
