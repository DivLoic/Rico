name := "Rico"

version := "1.0"

scalaVersion := "2.10.6"

val excludeMac = ExclusionRule(organization = "org.scalamacros")
val excludeChu = ExclusionRule(organization = "com.chuusai")

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

  // Numerical Processing
  "org.scalanlp" %% "breeze" % "0.12",

  // stemming libraries
  "org.apache.lucene" % "lucene-snowball" % "3.0.3",
  "org.apache.lucene" % "lucene-analyzers" % "3.6.2",

  // REST service
  "spark.jobserver" %% "job-server-api" % "0.6.0" % "provided",
  "spark.jobserver" %% "job-server-extras" % "0.6.0" % "provided" excludeAll(excludeMac, excludeChu)

)

resolvers += "Job Server Bintray" at "https://dl.bintray.com/spark-jobserver/maven"