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
object Restore {

  val conf = ConfigFactory.load("rico")
  val log  = LoggerFactory.getLogger(getClass)

  case class Wish(user_id:Int, course_id:Int, create_at:java.util.Date, status:Int)
  case class Booking(user_id:Int, course_id:Int, create_at:java.util.Date, status:Int)
  case class Review(user_id:Int, course_id:Int, rating:Double, create_at:java.util.Date, status:Int)

  /**
    * Since int are represented with java.math.BigDecimal this <br/>
    * user defined function is used to convert dataFrame column from BigDecimal to int.
    * @return :Unit
    */
  def udfInt  = udf[Int, java.math.BigDecimal](new BigDecimal(_).toInt)

  /**
    * Since Date are mapped with java.sql.Date this <br/>
    * user defined function is used to conver Date in Long.
    * @return
    */
  def udfLong = udf[Long, java.sql.Timestamp](_.getTime)

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
      conf.getString("sql.user"),
      conf.getString("sql.schema")
    )

    /* mysql to cassandra: bookings_by_user */

    val bkgDf = ext.sqlSelect(sqlContext, "bookings", conf.getString("sql.password"))  // jdbc connection
      .select("user_id", "course_id", "create_at", "status")

    ext.castColumns(List(("user_id",udfInt),("course_id",udfInt),("create_at",udfLong)),bkgDf) // covert column types

      .map(x => new Booking(x.getInt(0),x.getInt(1),new java.util.Date(x.getLong(2)),x.getInt(3))) // map to case rdd

      .saveToCassandra(conf.getString("cassandra.keyspace"), "bookings_by_user",
        SomeColumns("user_id", "course_id", "create_at", "status")
      )

    /* mysql to cassandra: whishlist_by_user */

    val wslDf = ext.sqlSelect(sqlContext, "wishlists", conf.getString("sql.password"))
      .select("user_id", "course_id", "create_at", "status")

    ext.castColumns(List(("user_id",udfInt),("course_id",udfInt),("create_at",udfLong)), wslDf)

      .map( x => new Wish(x.getInt(0), x.getInt(1), new java.util.Date(x.getLong(2)), x.getInt(3)))

      .saveToCassandra(conf.getString("cassandra.keyspace"), "wishes_by_user",
          SomeColumns("user_id","course_id","create_at","status")
      )

    /* mysql to cassandra: review_by_user */

    val rvwDf = ext.sqlSelect(sqlContext, "reviews", conf.getString("sql.password"))
      .select("user_id", "course_id", "avgRating", "create_at", "status")

    ext.castColumns(List(("user_id",udfInt),("course_id",udfInt),("create_at",udfLong)), rvwDf)

      .map( x => new Review(x.getInt(0),x.getInt(1),x.getDouble(2),new java.util.Date(x.getLong(3)),x.getInt(4)))

      .saveToCassandra(conf.getString("cassandra.keyspace"), "reviews_by_user",
        SomeColumns("user_id","course_id","rating","create_at","status")
      )

  }
}
