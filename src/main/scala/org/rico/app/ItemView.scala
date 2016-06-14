package org.rico.app

import org.apache.spark.{SparkConf, SparkContext}
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
  val log  = LoggerFactory.getLogger("rico")

  def main(args: Array[String]) {

    app.insureParams(args, log)
    val ITEMID = args(0)

    val sparkConf = new SparkConf()
      .setAppName(s"[rico] - Item: $ITEMID")
      .set("spark.cassandra.connection.host", conf.getString("cassandra.host"))
      .set("spark.cassandra.connection.port", conf.getString("cassandra.port"))

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    def toBreezeSparseVect = app.mapBreeze()
    def ricoDistance = app.distFactory(conf.getString("recommender.distance"))

    app.time(log)

    log info s"Querying the course nb°$ITEMID from Cassandra"
    val row:CassandraRow = sc.cassandraTable(conf.getString("cassandra.keyspace"), "termvectors")
      .select("title", "indices","values")
      .where(s"course_id = $ITEMID").first()


    val title = row.getString("title")

    log info s"Building a the course vector nb°$ITEMID : $title."
    val target = toBreezeSparseVect(row)

    log info s"Querying all the courses vectors table."
    val rdd = sc.cassandraTable(conf.getString("cassandra.keyspace"), "termvectors")

    val tfidfRdd = rdd.map {
      v => (
        v.getInt("course_id"),
        v.getString("title"),
        toBreezeSparseVect(v)
      )
    }

    val bctTarget = sc.broadcast(target)

    log info s"Scoring with the ${conf.getString("recommender.distance")} distance function ..."
    val score = tfidfRdd.map { w => ( w._1, w._2, ricoDistance(bctTarget.value, w._3) ) }
      .sortBy(_._3).zipWithIndex.filter {
        case (_, idx) => idx < conf.getInt("recommender.nbresult") + 1
      }.keys

    val scoreDf = score.toDF("id", "title", "distance")

    app.ribbon("START OF THE PREDICTION")
    Future.show(scoreDf, conf.getInt("recommender.nbresult"), false)
    app.ribbon("END OF THE PREDICTION")

    app.time(log)
  }
}
