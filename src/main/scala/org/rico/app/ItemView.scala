package org.rico.app

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.linalg.Vectors
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SQLContext
import com.datastax.spark.connector._
import org.slf4j.LoggerFactory

/**
  * Created by loicmdivad on 30/05/2016.
  */
object ItemView {

  val app  = new Rico()
  val conf = ConfigFactory.load("rico")
  val log  = LoggerFactory.getLogger(getClass)

  /**
    * Perform all the verification before use args: <br/>
    * args should contain only one element <br/>
    * args element should allow .toInt conversion <br/>
    * otherwise insureParams stop the programme
    *
    * @param args
    */
  def insureParams(args: Array[String]):Unit = try {
    assert(args.length equals 1); args(0).toInt
  } catch {
    case _ : java.lang.AssertionError => log error s"Incorrect Number of param." ; System.exit(1)
    case _ : java.lang.NumberFormatException => log error s"Incorrect Item ID." ; System.exit(1)
    case _ : java.lang.Exception => log error s"An Exception occurs while parcing args." ; System.exit(1)
  }

  def main(args: Array[String]) {

    insureParams(args)
    val ITEMID = args(0)

    val sparkConf = new SparkConf()
      .setAppName(s"[rico] - Item: $ITEMID")
      .set("spark.cassandra.connection.host", conf.getString("cassandra.host"))
      .set("spark.cassandra.connection.port", conf.getString("cassandra.port"))

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    //TODO: refactor this in a function
    //TODO: deal with java.lang.UnsupportedOperationException: empty collection
    val row:CassandraRow = sc.cassandraTable(conf.getString("cassandra.keyspace"), "termvectors")
      .select("indices","values")
      .where(s"course_id = $ITEMID").first()

    val indices = row.get[List[Int]]("indices").toArray
    val values = row.get[List[Double]]("values").toArray
    //TODO: Configure the vector size
    val target = Vectors.sparse(conf.getInt("recommender.vectsize"),indices, values)

    val rdd = sc.cassandraTable(conf.getString("cassandra.keyspace"), "termvectors")

    val tfidfRdd = rdd.map { x => (
        x.getInt("course_id"),
        x.getString("title"),
        Vectors.sparse(
          conf.getInt("recommender.vectsize"),
          x.getList[Int]("indices").toArray,
          x.getList[Double]("values").toArray)
      )
    }

    //TODO: try other dist function
    val score = tfidfRdd.map { w => ( w._1, w._2, Vectors.sqdist(target, w._3 ) ) }.sortBy(_._3)
    val scoreDf = score.toDF("id", "title", "distance")

    scoreDf.show(conf.getInt("recommender.nbresult"))
    app.afterResult()

  }
}
