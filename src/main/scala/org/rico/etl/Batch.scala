package org.rico.etl

import java.text.Normalizer

import org.apache.spark.sql.{DataFrame, SQLContext}

import scala.util.matching.Regex

/**
  * Created by LoicMDIVAD on 02/05/2016.
  */
class Batch(driver:String, host:String, port:String, schema:String, pwd:String) extends Serializable {

  //TODO: add a user configuration & pass it to sqlSelect
  def this() = this("", "localhost", "3306", "kokoroe", "root")

  /**
    *
    * @param text
    * @return
    */
  def cleanWords(text :String) :String = {
    def format(t: String, reg :String = "[^a-zA-Z ]"): String = {
      val ascii = Normalizer.normalize(t, Normalizer.Form.NFD)
      val keepPattern = new Regex(reg)
      keepPattern.replaceAllIn(ascii, Regex.quoteReplacement(""))
        .toLowerCase.trim
    }

    text match {
      case null => new String()
      case _ => format(text.toString)
    }
  }

  /**
    *
    * @param sqlContext
    * @param table
    * @return
    */
  def sqlSelect(sqlContext :SQLContext, table :String) :DataFrame = {
    sqlContext.read.format("jdbc")
        .option("url", s"jdbc:mysql://$host:$port/$schema")
        .option("driver", driver)
        .option("dbtable", table)
        .option("user", "root")
        .option("password", "kokoroepwd").load()
  }

}
