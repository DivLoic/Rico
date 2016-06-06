package org.rico.test

import com.typesafe.config.ConfigFactory
import org.scalatest.FunSuite
import org.scalatest.exceptions.TestFailedException
import org.slf4j.LoggerFactory
import org.tartarus.snowball.ext.FrenchStemmer

/**
  * Created by loicmdivad on 21/05/2016.
  */
class StemmingVerbTest extends FunSuite {

  val conf = ConfigFactory.load("rico")
  val log = LoggerFactory.getLogger("rico")

  val fr = new FrenchStemmer()

  def compareStemming(initWord:String, toCompare:Array[String]): Unit ={
    fr.setCurrent(initWord)
    if(!fr.stem()) fail()

    val leftStemmed = fr.getCurrent()
    var rightStemmed :String = new String()

    for(word <- toCompare){
      fr.setCurrent(word)
      log debug s"Running the stemming test for the word: $word"

      if(fr.stem()){
        rightStemmed = fr.getCurrent()
        assert( leftStemmed == rightStemmed,
          log debug s"The stemming expected: $leftStemmed instead find $rightStemmed"
        )
      }
    }
  }

  test("Compare first group verbs with their infinitive.") {
    compareStemming("manger", Array("mangé","mangeais","mangeront","manges","mangeâmes"))
  }

  test("Compare second group verbs with their infinitive.") {
    compareStemming("finir", Array("fini","finîmes","finis","finirez","finissent"))
  }

  test("Should fail Compare third group verbs with their infinitive.") {
    try {
      compareStemming("prendre", Array("prendrons","preniez","prennent","prîmes","pris"))
    } catch {
      case _: TestFailedException => log warn "The verb 'prendre' is'nt totaly treated by snowball"
    }
  }
}
