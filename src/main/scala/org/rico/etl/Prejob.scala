package org.rico.etl

import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import com.datastax.spark.connector._
import org.apache.spark.sql.functions._
import org.slf4j.LoggerFactory

/**
  * Created by loicmdivad on 31/05/2016.
  */
object Prejob {

  val conf = ConfigFactory.load("rico")
  val log  = LoggerFactory.getLogger(getClass)

  case class Booking(user_id:Int, course_id:Int, create_at:java.util.Date)

  /**
    * Since int are represeted with java.math.BigDecimal this <br/>
    * user difined function is used to convert dataFrame column from BigDecimal to int
    *
    * @return :Unit
    */
  def udfToInt  = udf[Int, java.math.BigDecimal](new BigDecimal(_).toInt)

  def udfToDate = udf[Long, java.sql.Timestamp](_.getTime)

  def main(args: Array[String]) {

    val sparkConf = new SparkConf()
      .setAppName("[rico] - Prejob")
      .set("spark.cassandra.connection.host", conf.getString("cassandra.host"))
      .set("spark.cassandra.connection.port", conf.getString("cassandra.port"))

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)

    val ext = new Extractor(
      conf.getString("sql.driver"),
      conf.getString("sql.host"),
      conf.getString("sql.port"),
      conf.getString("sql.schema")
    )

    // load from Mysql
    val sqlDf = ext.sqlSelect(sqlContext, "bookings", conf.getString("sql.password"))
      .select("user_id", "course_id", "create_at")

    val bookingsDf = sqlDf
      .withColumn("user_id", udfToInt(sqlDf("user_id")))
      .withColumn("course_id", udfToInt(sqlDf("course_id")))
      .withColumn("create_at", udfToDate(sqlDf("create_at")))

    // dataframe -> case rdd -> save to cassandra
    bookingsDf.map( x => new Booking(x.getInt(0), x.getInt(1), new java.util.Date(x.getLong(2))))
      .saveToCassandra( conf.getString("cassandra.keyspace"), "bookings_by_user",
      SomeColumns("user_id","course_id","create_at")
    )

    //sqlDf.show(30)

  }
}
