package org.rico.app

import System.{currentTimeMillis => timeMs}

import com.datastax.spark.connector._
import breeze.linalg.{SparseVector, squaredDistance}
import org.slf4j.Logger
import breeze.linalg.functions._
import org.apache.spark.SparkContext

/**
  * Created by loicmdivad on 31/05/2016.
  */
class Rico(var start:Long = -1) extends Serializable {

  /**
    * Start the timer at the first call <br/>
    * Then log the elapsed time <br/>
    * @param l :Logger from slf4j
    */
  def time(l:Logger):Unit = this.start match {
    case -1L => this.start = timeMs; l info "Rico timer start !"
    case x if x > -1L => l info s"Rico timer, elapsed time: ${(timeMs.toDouble - this.start)/1000D} s."
    case _ => l warn "/!\\ Unable to measure the Elapsed time. /!\\"
  }

  /**
    * Build a Serializable task
    * Take CassandraRow and map it to a breeze.linalg.SparseVector
    * @param size :Int number of index for the vector 2**20
    * @return func :(CassandraRow => SparseVector[Double])
    */
  def mapBreeze(size:Int=1048576):(CassandraRow => SparseVector[Double]) = {

    def toBreeze(row:CassandraRow): SparseVector[Double] = {
      new SparseVector[Double](
        row.get[List[Int]]("indices").toArray,
        row.get[List[Double]]("values").toArray,
        size
      )
    }
    toBreeze
  }

  /**
    * Perform a select all where to fetch records for the user.
    * @param sc :SparkContext
    * @param keySpace :String from conf.getString
    * @param table :String
    * @param uid :String User id
    * @return
    */
  def fetchCourseIds(sc:SparkContext, keySpace:String, table:String, uid:Int) = {
    sc.cassandraTable(keySpace, table)
      .select("course_id")
      .where(s"user_id = $uid")
      .map(_.getInt(0))
      .collect()
  }

  case class UnknowDistanceName(smth:String) extends Exception

  /**
    * Return a Breeze distance function, throw UnknowDistanceName
    * if the key recommender.distance is badly set
    * @param conf :String from conf.getString
    * @return func :(SparseVector[Double],SparseVector[Double]) => Double)
    */
  def distFactory(conf:String):((SparseVector[Double],SparseVector[Double])=>Double) = {

    def func(v1: SparseVector[Double], v2: SparseVector[Double]) = conf match {
      case "cosine" => cosineDistance(v1, v2)
      case "squared" => squaredDistance(v1, v2)
      case "chebyshev" => chebyshevDistance(v1,v2)
      case "manhattan" => manhattanDistance(v1,v2)
      case "euclidean" => euclideanDistance(v1,v2)
      case "tanimoto" => tanimotoDistance(v1,v2)
      case _ => throw new UnknowDistanceName("La distance dans le fichier rico.conf incorrecte.")
    }
    func
  }

  /**
    * Perform all the verification before use args: <br/>
    * args should contain only one element <br/>
    * args element should allow .toInt conversion <br/>
    * otherwise insureParams stop the programme
    * @param args :List[String] from the main function
    */
  def insureParams(args: Array[String], log:Logger):Unit = try {
    assert(args.length equals 1); args(0).toInt
  } catch {
    case _ : java.lang.AssertionError => log error s"Incorrect Number of param." ; System.exit(1)
    case _ : java.lang.NumberFormatException => log error s"Incorrect Item ID." ; System.exit(1)
    case _ : java.lang.Exception => log error s"An Exception occurs while parcing args." ; System.exit(1)
  }

  def ribbon(txt:String):Unit = println("%"*25 +s"          $txt          "+ "%"*25)

}
