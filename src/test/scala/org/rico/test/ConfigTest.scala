package org.rico.test

import com.typesafe.config.ConfigFactory
import org.scalatest.FunSuite

/**
  * Created by loicmdivad on 16/05/2016.
  */

class ConfigTest extends FunSuite {

  val ricoConf = ConfigFactory.load("test")

  test("It should pass anyway") {
    assert(Set.empty.size == 0)
  }

  test("Configuration should match values") {
    assert(ricoConf.getString("caseone.foo") == "bar")
    assert(ricoConf.getDouble("caseone.version") == 1.0)
  }

  test("Configuration should match types") {
    assert(ricoConf.getLong("casetwo.pi").isInstanceOf[Long])
  }

  test("Configuration bring a correct sentence") {
    val title = ricoConf.getString("testcase")
    assert(title.split(" ").size == 9)
  }

}