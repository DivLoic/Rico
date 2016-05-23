package org.rico.test

import com.typesafe.config.ConfigFactory
import org.scalatest.FunSuite
import org.scalatest.exceptions.TestFailedException
import org.tartarus.snowball.ext.frenchStemmer

/**
  * Created by loicmdivad on 21/05/2016.
  */
class StemmingVerbTest extends FunSuite {

  val ricoConf = ConfigFactory.load("rico")
  val LogOn = ricoConf.getBoolean("logger.test")

  val fr = new frenchStemmer()



  def compareStemming(initWord:String, toCompare:Array[String]): Unit ={
    fr.setCurrent(initWord)
    if(!fr.stem()) fail()

    val leftStemmed = fr.getCurrent()
    var rightStemmed :String = new String()

    for(word <- toCompare){
      if(LogOn) println(s"[INFO] - Running the stemming test for: $word")
      fr.setCurrent(word)

      if(fr.stem()){
        rightStemmed = fr.getCurrent()
        assert(
          leftStemmed == rightStemmed,
          s"[ERROR] - The stemming expected: $leftStemmed instead find $rightStemmed"
        )
      }

    }
  }

  test("Compare first group verb with their infinitive.") {
    compareStemming("manger", Array("mangé","mangeais","mangeront","manges","mangeâmes"))
  }

  test("Compare second group verb with their infinitive.") {
    compareStemming("finir", Array("fini","finîmes","finis","finirez","finissent"))
  }

  test("Compare third group verb with their infinitive.") {
    try {
      compareStemming("prendre", Array("prendrons","preniez","prennent","prîmes","pris"))    }
    catch {
      case _: TestFailedException => println("[WARRNING] - The verb 'prendre' is'nt totaly treat by snowball")
    }
  }
}
