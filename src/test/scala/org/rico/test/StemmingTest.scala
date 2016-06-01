package org.rico.test

import org.scalatest.FunSuite
import org.tartarus.snowball.SnowballProgram
import org.tartarus.snowball.ext.{EnglishStemmer, FrenchStemmer}

/**
  * Created by loicmdivad on 16/05/2016.
  */
class StemmingTest extends FunSuite {

  val fr:SnowballProgram = new FrenchStemmer()
  val en:SnowballProgram = new EnglishStemmer()

  test("It should pass anyway"){
    assert(fr.isInstanceOf[FrenchStemmer])
  }

  test("It should remove plural form"){
    fr.setCurrent("Ils")
    if(fr.stem()) assert(fr.getCurrent equals "il")

    fr.setCurrent("ont")
    if(fr.stem()) assert(fr.getCurrent equals "ont")

    fr.setCurrent("aimé")
    if(fr.stem()) assert(fr.getCurrent equals "aim")

    fr.setCurrent("les cours")
    if(fr.stem()) assert(fr.getCurrent equals "les cour")
  }

  test("It should remove the mark of the paste") {
    fr.setCurrent("J'ai")
    if(fr.stem()) assert(fr.getCurrent equals "J'ai")

    fr.setCurrent("été")
    if(fr.stem()) assert(fr.getCurrent equals "été")

    fr.setCurrent("récompensée")
    if(fr.stem()) assert(fr.getCurrent equals "récompens")
  }

  test("It should remove the mark of plural") {
    fr.setCurrent("nous")
    if(fr.stem()) assert(fr.getCurrent equals "nous")

    fr.setCurrent("eûmes")
    if(fr.stem()) assert(fr.getCurrent equals "eûm")

    fr.setCurrent("disputaillé")
    if(fr.stem()) assert(fr.getCurrent equals "disputaill")
  }

  test("chu verbe choir futur antérieur") {
    fr.setCurrent("nous")
    if(fr.stem()) assert(fr.getCurrent equals "nous")

    fr.setCurrent("aurons")
    if(fr.stem()) assert(fr.getCurrent equals "auron")

    fr.setCurrent("chu")
    if(fr.stem()) assert(fr.getCurrent equals "chu")
  }

  test("It should deal with verbe") {

    fr.setCurrent("nous mangerons")
    if(fr.stem()) assert(fr.getCurrent equals "nous mang")

    fr.setCurrent("tu mangeras")
    if(fr.stem()) assert(fr.getCurrent equals "tu mang")

    fr.setCurrent("habiter")
    if(fr.stem()) assert(fr.getCurrent equals "habit")

    fr.setCurrent("ils habitaient")
    if(fr.stem()) assert(fr.getCurrent equals "ils habit")

    fr.setCurrent("vous choirez")
    if(fr.stem()) assert(fr.getCurrent equals "vous choir")

  }
}