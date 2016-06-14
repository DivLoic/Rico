package org.rico.etl

import org.apache.spark.sql.{DataFrame, SQLContext, UserDefinedFunction}

/**
  * Created by loicmdivad on 31/05/2016.
  */
class Extractor (driver:String, host:String, port:String, user:String, schema:String) {

  def this() = this("", "localhost", "3306", "root", "kokoroe")

  /**
    * Take a context, a table name and a password and return a spark dataframe
    * (org.apache.spark.sql.DataFrame)
    * @param sqlContext :SQLContext build from sc
    * @param table :String name of the table to load
    * @param password :String password from conf.getString(sql.password)
    * @return a dataframe corresponding to the sql table
    */
  def sqlSelect(sqlContext :SQLContext, table :String, password:String) :DataFrame = {
    sqlContext.read.format("jdbc")
      .option("url", s"jdbc:mysql://$host:$port/$schema")
      .option("driver", driver)
      .option("dbtable", table)
      .option("user", user)
      .option("password", password).load()
  }

  /**
    * Convert the type of multiple column from a dataframe
    * @param cols list of pairs (column, udf) like: (col1, udf1),(col2, udf2),(col3, udf3)
    * @param df dataframe to where type modification need to be applied
    * @return a new dataframe with corrects column types
    */
  def castColumns(cols:List[(String,UserDefinedFunction)], df:DataFrame): DataFrame = cols match {
    case h :: tail => castColumns(tail, df.withColumn(h._1, h._2(df(h._1))))
    case Nil => df
  }
}
