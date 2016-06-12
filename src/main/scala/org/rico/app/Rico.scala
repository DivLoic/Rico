package org.rico.app

import System.{currentTimeMillis => timeMs}

import com.datastax.spark.connector.CassandraRow
import breeze.linalg.{SparseVector, squaredDistance}
import org.slf4j.Logger
import breeze.linalg.functions._

/**
  * Created by loicmdivad on 31/05/2016.
  */
class Rico(var start:Long = -1) extends Serializable {

  /**
    *
    * @param l
    */
  def time(l:Logger):Unit = this.start match {
    case -1L => this.start = timeMs; l info "Rico timer start !"
    case x if x > -1L => l info s"Rico timer, elapsed time: ${(timeMs.toDouble - this.start)/1000D} s."
    case _ => l warn "/!\\ Unable to measure the Elapsed time. /!\\"
  }

  /**
    *
    * @param size
    * @return
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

  case class UnknowDistanceName(smth:String) extends Exception

  /**
    *
    * @param conf
    * @return
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

  def doScore(): Unit ={

    def scoring(): Unit ={

    }

    scoring
  }

  def ribbon(txt:String):Unit = println("%"*25 +s"          $txt          "+ "%"*25)

  def afterResult():Unit = {println(
    """%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
      |%                                    END OF THE PREDICTION                                       %
      |%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    """.stripMargin
  )}
}
