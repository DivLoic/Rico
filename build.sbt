name := "Rico"

version := "1.0"

scalaVersion := "2.10.6"

libraryDependencies  ++= Seq(

  // Logging and Configuration
  "com.typesafe" % "config" % "1.2.1",

  // Unit test freamwork
  "org.scalatest" % "scalatest_2.10" % "2.0" % "test",

  // Spark dependencies
  "org.apache.spark" % "spark-core_2.10" % "1.4.1",
  "org.apache.spark" % "spark-sql_2.10" % "1.4.1",
  "org.apache.spark" % "spark-mllib_2.10" % "1.4.1",

  // Cassandra / Cassandra - Spark connector
  "com.datastax.spark" % "spark-cassandra-connector_2.10" % "1.4.1",

  // stemming libraries
  "org.apache.lucene" % "lucene-snowball" % "3.0.3",
  "org.apache.lucene" % "lucene-analyzers" % "3.6.2"

)