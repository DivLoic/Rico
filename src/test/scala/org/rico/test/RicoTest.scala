package org.rico.test

import breeze.linalg.{SparseVector, norm, squaredDistance}
import breeze.linalg.functions._
import com.datastax.spark.connector.CassandraRow
import org.apache.spark.mllib.linalg.Vectors
import org.rico.app.Rico
import org.scalatest.FunSuite

/**
  * Created by loicmdivad on 04/06/2016.
  */
class RicoTest extends FunSuite {

  val rico = new Rico()

  val indices1 = Array(0,2,4)
  val indices2 = Array(1,2,3)

  val values1 = Array(1.0, 2.0, 1.0)
  val values2 = Array(1.0, -2.0, 1.0)

  val v1 = new SparseVector[Double](Array(2,3,4,5),Array(9d,3d,5d,7d),10)
  val v2 = new SparseVector[Double](Array(2,4,6,8),Array(8d,3d,5d,2d),10)

  test("Should compare mllib & breeze dist function") {

    val mllibVect1 = Vectors.sparse(5, indices1, values1)
    val mllibVect2 = Vectors.sparse(5, indices2, values2)

    val breeze1 = new SparseVector[Double](indices1, values1,5)
    val breeze2 = new SparseVector[Double](indices2, values2,5)

    assert(Vectors.sqdist(mllibVect1, mllibVect2) equals squaredDistance(breeze1, breeze2))
  }

  test("Shoud convert cassandra row to vector") {

    val breeze1 = new SparseVector[Double](indices1, values1,5)

    val cr = new CassandraRow(IndexedSeq("course_id", "title", "indices", "values"),
      IndexedSeq(1.asInstanceOf[AnyRef], "the title", indices1.toList, values1.toList))

    def mapBreeze = rico.mapBreeze(5)
    val vect = mapBreeze(cr)

    assertResult (5) (vect.length)
    assertResult (3) (vect.activeSize)
    assertResult (math.sqrt(6)) (norm(vect))
    assertResult (0) (squaredDistance(vect, breeze1))
  }

  test("Should build a breeze distance function") {
    def d1 = rico.distFactory("cosine")
    assertResult (d1(v1,v2)) (cosineDistance(v1,v2))

    def d2 = rico.distFactory("squared")
    assertResult (d2(v1,v2)) (squaredDistance(v1,v2))

    def d3 = rico.distFactory("chebyshev")
    assertResult (d3(v1,v2)) (chebyshevDistance(v1,v2))

    def d4 = rico.distFactory("manhattan")
    assertResult (d4(v1,v2)) (manhattanDistance(v1,v2))

    def d5 = rico.distFactory("euclidean")
    assertResult (d5(v1,v2)) (euclideanDistance(v1,v2))

    def d6 = rico.distFactory("tanimoto")
    assertResult (d6(v1,v2)) (tanimotoDistance(v1,v2))
  }


}
