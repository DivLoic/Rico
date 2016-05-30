package org.rico.test

import com.typesafe.config.ConfigFactory
import org.scalatest.FunSuite
import org.rico.etl.Transformer
import org.tartarus.snowball.ext._
import org.apache.lucene.analysis.fr.FrenchAnalyzer
import org.apache.lucene.analysis.no.NorwegianAnalyzer
import org.apache.lucene.analysis.sv.SwedishAnalyzer
import org.apache.lucene.analysis.en.EnglishAnalyzer


/**
  * Created by loicmdivad on 26/05/2016.
  */
class TransformerTest extends FunSuite {

  val conf = ConfigFactory.load("rico")
  val trf = new Transformer(conf.getString("lang.stemmer"))

  /*
  test the instantiation of snowball stemmer
  for a given language name.
   */
  test("Should build the correct snowball stemmer") {
    assert(trf.stemmerFactory("SwedishStemmer").isInstanceOf[SwedishStemmer])
    assert(trf.stemmerFactory("NorwegianStemmer").isInstanceOf[NorwegianStemmer])
    assert(trf.stemmerFactory("EnglishStemmer").isInstanceOf[EnglishStemmer])
    assert(trf.stemmerFactory("FrenchStemmer").isInstanceOf[FrenchStemmer])
    assert(trf.stemmerFactory(conf.getString("lang.stemmer")).isInstanceOf[FrenchStemmer])
  }

  /*
  test the instantiation of lucene analyzer
  for a given language name.
   */
  test("Should build the correct lucene Analyser") {
    assert(trf.analyzerFactory("fr", "FrenchAnalyzer").isInstanceOf[FrenchAnalyzer])
    assert(trf.analyzerFactory("en", "EnglishAnalyzer").isInstanceOf[EnglishAnalyzer])
    assert(trf.analyzerFactory("no", "NorwegianAnalyzer").isInstanceOf[NorwegianAnalyzer])
    assert(trf.analyzerFactory("sv", "SwedishAnalyzer").isInstanceOf[SwedishAnalyzer])
    assert(trf.analyzerFactory(
      conf.getString("lang.sigle"),
      conf.getString("lang.analyzer")
    ).isInstanceOf[FrenchAnalyzer])
  }

  /*
  test the behavior of the function create with the Transformer
   */
  test("Should stem the words with a Function from the Transformer"){
    val func = trf.doStem()
    assertResult ("mang") (func("mangé"))
    assertResult ("nécessit") (func("nécessiteront"))
    assertResult ("envoi") (func("envoyées"))
    assertResult (func("finance")) (func("financement"))
  }

  test("Should avoid nullPointerException while stopping words"){
    val func = trf.doStop()
    assertResult (0) (func(null).size)
  }
}
