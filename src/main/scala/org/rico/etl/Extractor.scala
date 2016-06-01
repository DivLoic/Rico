package org.rico.etl

import org.apache.spark.sql.{DataFrame, SQLContext, UserDefinedFunction}

/**
  * Created by loicmdivad on 31/05/2016.
  */
class Extractor (driver:String, host:String, port:String, schema:String) {

  /**
    * 
    * @param sqlContext
    * @param table
    * @param password
    * @return
    */
  def sqlSelect(sqlContext :SQLContext, table :String, password:String) :DataFrame = {
    sqlContext.read.format("jdbc")
      .option("url", s"jdbc:mysql://$host:$port/$schema")
      .option("driver", driver)
      .option("dbtable", table)
      .option("user", password)
      .option("password", "kokoroepwd").load()
  }

  /**
    *
    * @param cols
    * @param df
    * @return
    */
  def columns(cols:List[(String,UserDefinedFunction)], df:DataFrame): DataFrame = cols match {
    case h :: tail => columns(tail, df.withColumn(h._1, h._2(df(h._1))))
    case h :: nil => df.withColumn(h._1, h._2(df(h._1)))
    case _ => df
  }
}
