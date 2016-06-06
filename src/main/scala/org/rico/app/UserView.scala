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

    val USERID = 4

    val sparkConf = new SparkConf()
      .setAppName(s"[rico] - User: $USERID")
      .set("spark.cassandra.connection.host", conf.getString("cassandra.host"))
      .set("spark.cassandra.connection.port", conf.getString("cassandra.port"))

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    app.time(log)

    val reviews = sc.cassandraTable(conf.getString("cassandra.keyspace"), "reviews_by_user")
      .select("course_id")
      .where(s"user_id = $USERID")
      .map(_.getInt(0))
      .collect()

    //TODO: Vérifier la renormalisation
    log info s"Number of reviews  for the targeted user: ${reviews.size}."

    def toBreezeSparseVect = app.mapBreeze()
    def ricoDistance = app.distFactory(conf.getString("recommender.distance"))
    log info s"Scoring starting with the ${conf.getString("recommender.distance")} distance function"

    val tfidfRdd = sc.cassandraTable(conf.getString("cassandra.keyspace"), "termvectors")
      .map(v => (v.getInt("course_id"), v.getString("title"), toBreezeSparseVect(v)))

    tfidfRdd.cache()

    val target = tfidfRdd.filter(x => reviews contains x._1)
      .map ( v => v._3 ).reduce( ( a , b ) => a + b )

    val targetRdd = sc.broadcast(target)

    val score = tfidfRdd.map { w => ( w._1, w._2, ricoDistance(targetRdd.value, w._3) ) }.sortBy(_._3)
    val scoreDf = score.toDF("id", "title", "distance")

    log warn s"Recommendation for User nbr: $USERID."
    app.ribbon("START OF THE PREDICTION")
    scoreDf.show(conf.getInt("recommender.nbresult"))//.foreach(println)
    app.ribbon("END OF THE PREDICTION")
    app.time(log)

  }
}
