import org.apache.spark.mllib.feature.HashingTF
import org.apache.spark.mllib
// sparse vector constructor from mllib
val v1 = new mllib.linalg.SparseVector (3, Array(0,2), Array(0.5, 0.25))
val v2 = new breeze.linalg.SparseVector(Array(0,2), Array(0.5, 0.25), 3)

val tf = new HashingTF()
tf.transform("a à")
