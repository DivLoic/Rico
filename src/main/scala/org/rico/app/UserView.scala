package org.rico.app

import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import org.slf4j.LoggerFactory
import com.datastax.spark.connector._

/**
  * Created by loicmdivad on 30/05/2016.
  */
object UserView {

  val app  = new Rico()
  val conf = ConfigFactory.load("rico")
  val log  = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]) {

    val USERID = 4

    val sparkConf = new SparkConf()
      .setAppName(s"[rico] - User: $USERID")
      .set("spark.cassandra.connection.host", conf.getString("cassandra.host"))
      .set("spark.cassandra.connection.port", conf.getString("cassandra.port"))

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    val reviews = sc.cassandraTable(conf.getString("cassandra.keyspace"), "reviews_by_user")
      .select("user_id", "course_id").where(s"user_id = $USERID").map{
      x => (x.getInt(0), x.getInt(1))
    }.toDF("user_id", "course_id")

    val tfidf = sc.cassandraTable(conf.getString("cassandra.keyspace"), "termvectors")
      .map{ x => (x.getInt("course_id"), x.getList[Int]("indices"), x.getList[Double]("values"))
      }.toDF("id", "indices", "values")

    reviews.join(tfidf, $"course_id" === $"id", "left").show() //(tfidf)



  }
}
