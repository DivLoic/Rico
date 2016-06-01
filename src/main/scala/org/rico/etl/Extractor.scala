package org.rico.etl

import org.apache.spark.sql.{DataFrame, SQLContext}

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
}
