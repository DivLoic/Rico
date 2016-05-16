package org.rico.etl

import com.typesafe.config.ConfigFactory
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.SparkContext._
import org.apache.spark.sql.SQLContext

/**
  * Created by loicmdivad on 16/05/2016.
  */
object Tfidf {

  def main(args: Array[String]): Unit = {

    val ricoConf = ConfigFactory.load("rico")

    val sparkConf = new SparkConf()
      .setAppName("Rico tfidf construction")
      .setMaster(s"spark://${ricoConf.getString("spark.master")}")

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)

    val batch = new Batch(
      ricoConf.getString("sql.driver"),
      ricoConf.getString("sql.host"),
      ricoConf.getString("sql.port"),
      ricoConf.getString("sql.schema"),
      ricoConf.getString("sql.password")
    )

    val df = batch.sqlSelect(sqlContext, "courses_translations")
      .select("course_id", "title", "experience", "program", "material")
      .filter(s"locale = ${ricoConf.getString("lang")}")

    df.show(10)
  }
}
