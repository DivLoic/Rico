package org.rico.etl

import java.io.StringReader

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.analysis.{Analyzer, TokenStream}
import org.apache.lucene.util.Version
import org.tartarus.snowball.SnowballProgram

/**
  * Created by loicmdivad on 26/05/2016.
  * @param langStemmer :String from conf (key lang.stemmer)
  * @param langAnalyzer :String from conf (key lang.analyzer)
  * @param lang :String from conf (key lang.sigle)
  */
class Transformer(langStemmer:String="EnglishStemmer",
                  langAnalyzer:String="EnglishAnalyzer",
                  lang:String="en") extends Serializable {

  /**
    * Return a function table to split, filter stem words in a sentence of  paragraph<br/>
    * it's only take a string as param and return a Seq (String => Seq[String])<br/>
    * All element are lowercase
    * @return list of stemmed words without stop words
    */
  def doStop():(String => Seq[String]) = {

    def stopStemNormalize(text:String): Seq[String] = text match {
      case null => Seq[String]()
      case _ =>
        val analyzer = analyzerFactory(this.lang, this.langAnalyzer)
        val tsm  :TokenStream = analyzer.tokenStream(null, new StringReader(text))
        val term :CharTermAttribute = tsm.addAttribute(classOf[CharTermAttribute])

        Iterator.iterate((tsm, term)){case (a,b) => (a,b)}
          .takeWhile(_._1.incrementToken)
          .map(_._2.toString).toList
    }
    stopStemNormalize
  }

  /**
    * Return a function able to stem one word with the lucene/snowball rules <br/>
    * It can pass in a map, filter or any spark transformation without any serialization <br/>
    * @return func: (String) => (String) able to stem :language: words
    */
  def doStem():(String => String) = {

    def stemmingFunction(word:String):String = {
      val stm = stemmerFactory(this.langStemmer)
      stm.setCurrent(word)
      stm.stem()
      stm.getCurrent
    }
    stemmingFunction
  }

  /**
    * Take stemmer language like ''FrenchStemmer'' from org.tartarus.snowball.ext <br/>
    * package and return an intance as SnowballProgram <br/>
    * @param lang class name like SwedishStemmer, FrenchStemmer etc...
    * @return a SnowballProgram implementation
    */
  def stemmerFactory(lang:String):SnowballProgram = {
    val tartarus = "org.tartarus.snowball.ext"
    Class.forName(s"$tartarus.$lang").newInstance().asInstanceOf[SnowballProgram]
  }

  /**
    * Take a package name and a class name and return a Analyzer
    * @param lang single of language like fr, en, sv etc ...
    * @param langAnalyzer name of class like EnglishAnalyzer, SwedishAnalyzer etc ...
    * @return an instance of Analyzer corresponding langage used
    */
  def analyzerFactory(lang:String, langAnalyzer:String): Analyzer = {
    val lucene = "org.apache.lucene.analysis"
    Class.forName(s"$lucene.$lang.$langAnalyzer")
      .getDeclaredConstructor(classOf[Version])
      .newInstance(Version.LUCENE_36).asInstanceOf[Analyzer]
  }

}
