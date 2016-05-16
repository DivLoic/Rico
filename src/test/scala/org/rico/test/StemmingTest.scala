package org.rico.test

import org.scalatest.FunSuite
import org.tartarus.snowball.ext.{englishStemmer, frenchStemmer}

/**
  * Created by loicmdivad on 16/05/2016.
  */
class StemmingTest extends FunSuite {

  val fr = new frenchStemmer()
  val en = new englishStemmer()

  test("It should pass anyway"){
    assert(fr.isInstanceOf[frenchStemmer])
  }

  test("It should remove plural form"){
    fr.setCurrent("Ils")
    if(fr.stem()) assert(fr.getCurrent == "il")

    fr.setCurrent("ont")
    if(fr.stem()) assert(fr.getCurrent == "ont")

    fr.setCurrent("aimé")
    if(fr.stem()) assert(fr.getCurrent == "aim")

    fr.setCurrent("les cours")
    if(fr.stem()) assert(fr.getCurrent == "les cour")
  }

  test("It should remove mark of the paste") {
    fr.setCurrent("J'ai")
    if(fr.stem()) assert(fr.getCurrent == "J'ai")

    fr.setCurrent("été")
    if(fr.stem()) assert(fr.getCurrent == "été")

    fr.setCurrent("récompensée")
    if(fr.stem()) assert(fr.getCurrent == "récompens")
  }

  test("It should ") {
    fr.setCurrent("nous")
    if(fr.stem()) assert(fr.getCurrent == "nous")

    fr.setCurrent("eûmes")
    if(fr.stem()) assert(fr.getCurrent == "eûm")

    fr.setCurrent("disputaillé")
    if(fr.stem()) assert(fr.getCurrent == "disputaill")
  }

  test("chu verbe choir futur antérieur") {
    fr.setCurrent("nous")
    if(fr.stem()) assert(fr.getCurrent == "nous")

    fr.setCurrent("aurons")
    if(fr.stem()) assert(fr.getCurrent == "auron")

    fr.setCurrent("chu")
    if(fr.stem()) assert(fr.getCurrent == "chu")
  }

  test("It shoul deal with verbe") {

    fr.setCurrent("nous mangerons")
    if(fr.stem()) assert(fr.getCurrent == "nous mang")

    fr.setCurrent("tu mangeras")
    if(fr.stem()) assert(fr.getCurrent == "tu mang")

    fr.setCurrent("habiter")
    if(fr.stem()) assert(fr.getCurrent == "habit")

    fr.setCurrent("ils habitaient")
    if(fr.stem()) assert(fr.getCurrent == "ils habit")

    fr.setCurrent("vous choirez")
    if(fr.stem()) assert(fr.getCurrent == "vous choir")

  }
}