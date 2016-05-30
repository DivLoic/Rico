package org.rico.etl

import scala.math.BigDecimal
import com.typesafe.config.ConfigFactory
import org.apache.spark.mllib.feature.{HashingTF, IDF}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.functions._
import com.datastax.spark.connector._

/**
  * Created by loicmdivad on 16/05/2016.
  */
object Tfidf {

  /**
    * Since int are represeted with java.math.BigDecimal this <br/>
    * user difined function is used to convert dataFrame column from BigDecimal to int
    *
    * @return :Unit
    */
  def udfToInt = udf[Int, java.math.BigDecimal](new BigDecimal(_).toInt)

  case class tfidfVector(course_id:Int, indices:List[Int], values:List[Double])

  def main(args: Array[String]): Unit = {

    val conf = ConfigFactory.load("rico")

    val sparkConf = new SparkConf()
      .setAppName("[rico] - tfidf")
      .set("spark.cassandra.connection.host", conf.getString("cassandra.host"))
      .set("spark.cassandra.connection.port", conf.getString("cassandra.port"))
      //.setMaster(s"spark://${ricoConf.getString("spark.master")}")

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)

    val batch = new Batch(
      conf.getString("sql.driver"),
      conf.getString("sql.host"),
      conf.getString("sql.port"),
      conf.getString("sql.schema"),
      conf.getString("sql.password")
    )

    val trfm = new Transformer(
      conf.getString("lang.stemmer"),
      conf.getString("lang.analyzer"),
      conf.getString("lang.sigle")
    )

    val luceneFormat = trfm.doStop()

    // load from Mysql
    val sqlDf = batch.sqlSelect(sqlContext, "courses_translations")
      .filter(s"locale = '${conf.getString("lang.sigle")}'")
      .select("course_id", "title", "experience", "program", "material")

    // java.math.BigDecimal -> scala.Int
    val df = sqlDf.withColumn("course_id", udfToInt(sqlDf("course_id")))

    val hashing = new HashingTF()

    //TODO: Delete the following reminder
    //batch.cleanWords(x.getString(2)).split(" ").toSeq
    val tfRdd =  df.map { // TODO: Modify with the transformers sotp words
      x => ( x.getInt(0) , hashing.transform( luceneFormat(x.getString(2))) )
    }

    val idf = new IDF().fit(tfRdd.map(x => x._2))

    val tfidfRdd = tfRdd
        .map { x => ( x._1, idf.transform(x._2) ) }
        .map { x => new tfidfVector(x._1, x._2.toSparse.indices.toList, x._2.toSparse.values.toList ) }

    // Fix the database sparsity for test db
    val tfidfC = tfidfRdd.filter(_.indices.size > 30)

    tfidfC.saveToCassandra(
      conf.getString("cassandra.keyspace"), "termvectors",
      SomeColumns("course_id", "indices", "values")
    )
  }
}
