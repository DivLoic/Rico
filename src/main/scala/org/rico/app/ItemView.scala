package org.rico.app

import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import com.datastax.spark.connector._
import com.typesafe.scalalogging.slf4j.Logger
import org.apache.spark.mllib.linalg.Vectors
import org.slf4j.LoggerFactory

/**
  * Created by loicmdivad on 30/05/2016.
  */
object ItemView {

  /**
    * Perform all the verification before use args: <br/>
    * args should contain only one element <br/>
    * args element should allow .toInt conversion <br/>
    * otherwise insureParams stop the programme
    * @param args
    */
  def insureParams(args: Array[String], log:Logger):Unit = try {
    assert(args.length equals 1); args(0).toInt
  } catch {
    case _ : java.lang.AssertionError => log error s"Incorrect Number of param." ; System.exit(1)
    case _ : java.lang.NumberFormatException => log error s"Incorrect Item ID." ; System.exit(1)
    case _ : java.lang.Exception => log error s"An Exception occurs while parcing args." ; System.exit(1)
    //case _ => log error s"An Exception occurs while parcing args " ; System.exit(1)
  }

  def main(args: Array[String]) {
    val conf = ConfigFactory.load("rico")
    val log  = Logger(LoggerFactory.getLogger("console"))
    //val log LoggerFactory.getLogger(getClass)

    insureParams(args, log)
    val ITEMID = args(0)

    val sparkConf = new SparkConf()
      .setAppName("[rico] - tfidf")
      .set("spark.cassandra.connection.host", conf.getString("cassandra.host"))
      .set("spark.cassandra.connection.port", conf.getString("cassandra.port"))

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    //TODO: refactor this in a function
    val row:CassandraRow = sc.cassandraTable(conf.getString("cassandra.keyspace"), "termvectors")
      .select("indices","values")
      .where(s"course_id = $ITEMID").first()

    val indices = row.get[List[Int]]("indices").toArray
    val values = row.get[List[Double]]("values").toArray
    //TODO: Configure the vector size
    val target = Vectors.sparse(1048576, indices, values)

    val rdd = sc.cassandraTable(conf.getString("cassandra.keyspace"), "termvectors")

    val tfidfRdd = rdd.map { x => (
        x.getInt("course_id"),
        Vectors.sparse(
          1048576,
          x.getList[Int]("indices").toArray,
          x.getList[Double]("values").toArray)
      )
    }

    val score = tfidfRdd.map { w => ( w._1, Vectors.sqdist(target, w._2 ) ) }.sortBy(_._2)
    val scoreDf = score.toDF("id", "distance")

    scoreDf.show(10)


  }
}
