package org.rico.app

import org.apache.spark.SparkContext
import com.typesafe.config.{Config, ConfigFactory}
import com.datastax.spark.connector._
import org.slf4j.LoggerFactory
import spark.jobserver.{SparkJob, SparkJobInvalid, SparkJobValid, SparkJobValidation}

import scala.util.Try

/**
  * Created by loicmdivad on 07/06/2016.
  */
object ItemViewService extends SparkJob {

  val app  = new Rico()
  val conf = ConfigFactory.load("rico")
  val log  = LoggerFactory.getLogger(getClass)

  override def validate(sc: SparkContext, config: Config): SparkJobValidation = {
    Try(config.getInt("param.itemid"))
      .map(x => SparkJobValid)
      .getOrElse(SparkJobInvalid("No input.string config param"))
  }

  override def runJob(sc: SparkContext, config: Config): Any = {

    val ITEMID = config.getInt("param.itemid")

    def toBreezeSparseVect = app.mapBreeze()
    def ricoDistance = app.distFactory("cosine")

    val row:CassandraRow = sc.cassandraTable("kokoroe", "termvectors")
      .select("indices","values")
      .where(s"course_id = $ITEMID").first()

    val target = toBreezeSparseVect(row)

    val rdd = sc.cassandraTable("kokoroe", "termvectors")

    val tfidfRdd = rdd.map { x => (
      x.getInt("course_id"),
      x.getString("title"),
      toBreezeSparseVect(x))
    }

    val brcTarget = sc.broadcast(target)

    val score = tfidfRdd.map { w => ( w._1, w._2, ricoDistance(brcTarget.value, w._3 ) ) }.sortBy(_._3)

    score.take(10)
  }
}
