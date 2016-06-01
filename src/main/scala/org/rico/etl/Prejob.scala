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

  case class Wish(user_id:Int, course_id:Int, create_at:java.util.Date, status:Int)
  case class Booking(user_id:Int, course_id:Int, create_at:java.util.Date, status:Int)
  case class Review(user_id:Int, course_id:Int, rating:Double, create_at:java.util.Date, status:Int)

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

    // load Bookings from Mysql
    val bkgDf = ext.sqlSelect(sqlContext, "bookings", conf.getString("sql.password"))
      .select("user_id", "course_id", "create_at", "status")

    val bookingsDf = bkgDf
      .withColumn("user_id", udfToInt(bkgDf("user_id")))
      .withColumn("course_id", udfToInt(bkgDf("course_id")))
      .withColumn("create_at", udfToDate(bkgDf("create_at")))

    // dataframe -> case rdd -> save to cassandra
    bookingsDf.map( x => new Booking(x.getInt(0), x.getInt(1), new java.util.Date(x.getLong(2)), x.getInt(3)))
      .saveToCassandra(conf.getString("cassandra.keyspace"), "bookings_by_user",
        SomeColumns("user_id","course_id","create_at","status")
      )

    // load Wishlists from Mysql
    val wslDf = ext.sqlSelect(sqlContext, "wishlists", conf.getString("sql.password"))
      .select("user_id", "course_id", "create_at", "status")

    val wishlistDf = wslDf
      .withColumn("user_id", udfToInt(wslDf("user_id")))
      .withColumn("course_id", udfToInt(wslDf("course_id")))
      .withColumn("create_at", udfToDate(wslDf("create_at")))

    wishlistDf.map( x => new Wish(x.getInt(0), x.getInt(1), new java.util.Date(x.getLong(2)), x.getInt(3)))
      .saveToCassandra(conf.getString("cassandra.keyspace"), "wishes_by_user",
          SomeColumns("user_id","course_id","create_at","status")
      )

    // load Reviews from Mysql
    val rvwDf = ext.sqlSelect(sqlContext, "reviews", conf.getString("sql.password"))
      .select("user_id", "course_id", "avgRating", "create_at", "status")

    val reviewDf = rvwDf
      .withColumn("user_id", udfToInt(rvwDf("user_id")))
      .withColumn("course_id", udfToInt(rvwDf("course_id")))
      .withColumn("create_at", udfToDate(rvwDf("create_at")))

    reviewDf.map( x => new Review(x.getInt(0),x.getInt(1),x.getDouble(2),new java.util.Date(x.getLong(3)),x.getInt(4)))
      .saveToCassandra(conf.getString("cassandra.keyspace"), "reviews_by_user",
        SomeColumns("user_id","course_id","rating","create_at","status")
      )

  }
}
