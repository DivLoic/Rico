package org.rico.test

import com.typesafe.config.ConfigFactory
import org.scalatest.FunSuite
import org.rico.etl.Transformer
import org.tartarus.snowball.ext.FrenchStemmer

/**
  * Created by loicmdivad on 26/05/2016.
  */
class TransformerTest extends FunSuite {

  val conf = ConfigFactory.load("rico")
  val trf = new Transformer(new FrenchStemmer())

  test("Should stem the words with a Transform instance"){
    val func = trf.doStem()
    assert(func("mangé") equals "mang")
  }

  test("Should stop the common words with a Transform instance"){
    //assert("" == trf.stop(""))
  }
}
