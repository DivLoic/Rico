package org.rico.etl

import org.tartarus.snowball.SnowballProgram
import org.tartarus.snowball.ext.FrenchStemmer

/**
  * Created by loicmdivad on 26/05/2016.
  */
class Transformer(stemmer : SnowballProgram) extends Serializable {

  def stopWords(text:String) = ???

  def doStem() :(String => String) = {

    def stemmingFunction(word:String):String = {
      // TODO: Find away to configure this
      val stm = new FrenchStemmer()
      stm.setCurrent(word)
      stm.stem()
      stm.getCurrent
    }

    return stemmingFunction
  }

}
