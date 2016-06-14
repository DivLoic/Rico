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
  val log  = LoggerFactory.getLogger("rico")

  def main(args: Array[String]) {

    app.insureParams(args, log)
    val USERID = args(0).toInt

    val sparkConf = new SparkConf()
      .setAppName(s"[rico] - User: $USERID")
      .set("spark.cassandra.connection.host", conf.getString("cassandra.host"))
      .set("spark.cassandra.connection.port", conf.getString("cassandra.port"))

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    app.time(log)

    val wishes = app.fetchCourseIds(sc, conf.getString("cassandra.keyspace"), "wishes_by_user", USERID)
    val reviews = app.fetchCourseIds(sc, conf.getString("cassandra.keyspace"), "reviews_by_user", USERID)
    val bookings = app.fetchCourseIds(sc, conf.getString("cassandra.keyspace"), "bookings_by_user", USERID)

    val courseid = wishes ++ reviews ++ bookings

    log info s"Number of records for the targeted user: ${courseid.size}."
    if(courseid.size == 0){
      log error s"Aucune info (Booking, review, whishe) pour l'utilisateur $USERID"
      log error s"Arret du programme"
      sc.stop(); System.exit(1)
    }

    def toBreezeSparseVect = app.mapBreeze()
    def ricoDistance = app.distFactory(conf.getString("recommender.distance"))
    log info s"Scoring with the ${conf.getString("recommender.distance")} distance function ..."

    val tfidfRdd = sc.cassandraTable(conf.getString("cassandra.keyspace"), "termvectors")
      .map(v => (v.getInt("course_id"), v.getString("title"), toBreezeSparseVect(v)))

    tfidfRdd.cache()

    val target = tfidfRdd.filter(x => courseid contains x._1)
      .map ( v => v._3 ).reduce( ( a , b ) => a + b )

    val bctTarget = sc.broadcast(target)

    val score = tfidfRdd.map { w => (w._1, w._2, ricoDistance(bctTarget.value, w._3)) }
      .sortBy(_._3).zipWithIndex.filter {
      case (_, idx) => idx < conf.getInt("recommender.nbresult") + 1
    }.keys

    val scoreDf = score.toDF("id", "title", "distance")

    log warn s"Recommendation for User nbr: $USERID."
    app.ribbon("START OF THE PREDICTION")
    Future.show(scoreDf, conf.getInt("recommender.nbresult"), false)
    app.ribbon("END OF THE PREDICTION")

    app.time(log)

  }
}
