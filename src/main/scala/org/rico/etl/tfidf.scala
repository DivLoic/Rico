package org.rico.etl

import scala.math.BigDecimal
import com.typesafe.config.ConfigFactory
import org.apache.spark.mllib.feature.{HashingTF, IDF}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{DataFrame, SQLContext}
import org.apache.spark.sql.functions._
import com.datastax.spark.connector._
import org.apache.spark.rdd.RDD
import org.rico.app.Rico
import org.slf4j.LoggerFactory

/**
  * Created by loicmdivad on 16/05/2016.
  */
object Tfidf {

  val app = new Rico()
  val conf = ConfigFactory.load("rico")
  val log  = LoggerFactory.getLogger(getClass)

  val ext = new Extractor(
    conf.getString("sql.driver"),
    conf.getString("sql.host"),
    conf.getString("sql.port"),
    conf.getString("sql.user"),
    conf.getString("sql.schema")
  )

  val trfm = new Transformer(
    conf.getString("lang.stemmer"),
    conf.getString("lang.analyzer"),
    conf.getString("lang.sigle")
  )

  case class tfidfVector(course_id:Int, title:String, indices:List[Int], values:List[Double])

  /**
    * Since int are represeted with java.math.BigDecimal this <br/>
    * user difined function is used to convert dataFrame column from BigDecimal to int
    * @return :Unit
    */
  def udfToInt = udf[Int, java.math.BigDecimal](new BigDecimal(_).toInt)

  def udfDoc   = udf((col1: String, col2: String) => { s"$col1 $col2" })

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf()
      .setAppName("[rico] - tfidf")
      .set("spark.cassandra.connection.host", conf.getString("cassandra.host"))
      .set("spark.cassandra.connection.port", conf.getString("cassandra.port"))

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    val luceneFormat = trfm.doStop()

    app.time(log)
    // load from Mysql
    log info "Recupération des courss"
    val sqlDf = ext.sqlSelect(sqlContext, "courses_translations", conf.getString("sql.password"))
      .filter(s"locale = '${conf.getString("lang.sigle")}'")
      .select("course_id", "title", "experience", "program", "material")

    // fetching the universes dataframe
    log info "Aggragation des univers"
    val univers = unverseRetriver(sqlContext).toDF("course_u", "universe")

    // fetching the tags dataframe
    log info "Aggragation des tags"
    val tags = tagRetriever(sqlContext).toDF("course_t", "tags")

    // java.math.BigDecimal -> scala.Int
    val df = sqlDf.withColumn("course_id", udfToInt(sqlDf("course_id")))

    val hashing = new HashingTF()

    val tfRdd =  df.join(tags, df("course_id") === tags("course_t"), "left")
      .join(univers, df("course_id") === univers("course_u"), "left")
      .withColumn("document", udfDoc($"experience", $"tags"))
      .withColumn("document", udfDoc($"title", $"document"))
      .withColumn("document", udfDoc($"document", $"universe"))
      .select("course_id","title","document")
      .map {
        x => ( x.getInt(0), x.getString(1), hashing.transform( luceneFormat(x.getString(2))) )
    }

    // FIT THE MODEL
    val idf = new IDF().fit(tfRdd.map(x => x._3))

    val tfidfRdd = tfRdd
        .map { x => ( x._1, x._2, idf.transform(x._3) ) }
        .map { x => new tfidfVector(x._1, x._2, x._3.toSparse.indices.toList, x._3.toSparse.values.toList ) }

    // Fix the database sparsity for test db
    val tfidfC = tfidfRdd.filter(_.indices.size > 30)

    tfidfC.saveToCassandra(
      conf.getString("cassandra.keyspace"), "termvectors",
      SomeColumns("course_id", "title", "indices", "values")
    )

    app.time(log)
  }

  def tagRetriever(ctx:SQLContext): RDD[(Int, String)] = {
    val c_tags = ext.sqlSelect(ctx,"courses_tags",conf.getString("sql.password"))
    val t_tags = ext.sqlSelect(ctx,"tags",conf.getString("sql.password"))

    ext.castColumns(List(("course_id", udfToInt)),
      c_tags.join(t_tags, c_tags("tag_id") === t_tags("id"), "inner"))
      .filter("official = True")
      .select("course_id", "name")
      .map(t => (t.getInt(0), t.getString(1)))
      .reduceByKey((a,b) => s"$a, $b")
  }

  def unverseRetriver(ctx:SQLContext): RDD[(Int, String)] = {

    val dftu = ext.sqlSelect(ctx,"universes_translations",conf.getString("sql.password"))
    val c_univ = ext.sqlSelect(ctx,"courses_universes",conf.getString("sql.password"))
    val t_univ = dftu.withColumn("universe", dftu("universe_id")).drop(dftu("universe_id"))

    ext.castColumns(List(("course_id", udfToInt)),
      c_univ.join(t_univ, c_univ("universe_id") === t_univ("universe"), "inner"))
      .filter(s"locale = '${conf.getString("lang.sigle")}'")
      .select("course_id", "name", "description")
      .map(t => (t.getInt(0), t.getString(1) +" "+ t.getString(2)))
  }
}
