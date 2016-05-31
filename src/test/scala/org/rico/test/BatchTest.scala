package org.rico.test

/**
  * Created by LoicMDIVAD on 05/05/2016.
  */
import org.rico.etl.Batch
import org.scalatest._

class ®BatchTest extends FunSuite {

  val batch :Batch = new Batch()

  test("It should pass anyway"){
    assert(Set.empty.size == 0)
  }

  test("Text should be cleaned up!") {

    assert(batch.cleanWords("Hi, you!") == "hi you")
    assert(batch.cleanWords(" No under_score!") == "no underscore")
    assert(batch.cleanWords("it's 4 O'clock") == "its  oclock")
    assert(batch.cleanWords(" The Elephant's 4 cats. ") == "the elephants  cats")

  }

  test("Text should be cleaned alson in french") {

    assert(batch.cleanWords("rendez-vous") == "rendezvous")
    assert(batch.cleanWords("rez-de-chaussé") == "rezdechausse")

  }

  test("French accent should be kept") {

    assert(batch.cleanWords("Loïc & éloïse ") == "loic  eloise")

  }

}
