package org.rico.etl

import java.io.StringReader

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.analysis.{Analyzer, TokenStream}
import org.apache.lucene.util.Version
import org.tartarus.snowball.SnowballProgram

/**
  *
  * Created by loicmdivad on 26/05/2016.
  * @param langStemmer
  * @param langAnalyzer
  * @param lang
  */
class Transformer(langStemmer:String="EnglishStemmer",
                  langAnalyzer:String="EnglishAnalyzer",
                  lang:String="en") extends Serializable {

  // TODO: Complete doc of stopWords
  /**
    *
    * @return
    */
  def doStop():(String => Seq[String]) = {

    def stopStemNormalize(text:String): Seq[String] = {
      text match {
        case null => Seq[String]()
        case _ => {
          val analyzer = analyzerFactory(this.lang, this.langAnalyzer)
          val tsm  :TokenStream = analyzer.tokenStream(null, new StringReader(text))
          val term :CharTermAttribute = tsm.addAttribute(classOf[CharTermAttribute])

          Iterator.iterate((tsm, term)){case (a,b) => (a,b)}
            .takeWhile(_._1.incrementToken)
            .map(_._2.toString).toList
        }
      }

    }
    stopStemNormalize
  }

  /**
    * Return a function that able to stem one word with the lucene/snowball rules <br/>
    * It can pass in a map, filter or any spark transformation without any serialization <br/>
 *
    * @return func: (String) => (String)
    */
  def doStem() :(String => String) = {

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
    *
    * @param lang :String class name like SwedishStemmer, FrenchStemmer etc...
    * @return a SnowballProgram implementation
    */
  def stemmerFactory(lang:String):SnowballProgram = {
    val tartarus = "org.tartarus.snowball.ext"
    Class.forName(s"$tartarus.$lang").newInstance().asInstanceOf[SnowballProgram]
  }

  //TODO: Complete doc of buidAnalyzer
  /**
    *
    * @param lang
    * @param langAnalyzer
    * @return
    */
  def analyzerFactory(lang:String, langAnalyzer:String): Analyzer = {
    val lucene = "org.apache.lucene.analysis"
    Class.forName(s"$lucene.$lang.$langAnalyzer")
      .getDeclaredConstructor(classOf[Version])
      .newInstance(Version.LUCENE_36).asInstanceOf[Analyzer]
  }

}
