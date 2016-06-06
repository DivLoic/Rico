# Rico
###### Final project 2016 from @ISEP

### Subject
>The isep lab has been working on a generic recommender system, able to recommend
>things like food, journey or training. The main concern of this project is to design a scalable version of it.

### Technologies
- [X] Apache Spark
- [X] Apache Cassandra
- [ ] Apache Zeppelin
- [ ] Spark jobServer

### Prerequisites
Here is the tools & versions used for the project.
- git
- jdk   : 1.7.0_80
- sbt   : 0.13.9
- scala : 2.10.6
- spark (1.4.1), cassandra (2.2.5), zeppelin, mysql or
[The gowalla VM demo](https://github.com/natalinobusa/gowalla-spark-demo)


### Configuration
I. Environment
Add to your `~/.bashrc` the following lines:
```sh
export SPARK_PKGS="com.datastax.spark:spark-cassandra-connector_2.10:1.4.1"
export SPARK_PKGS="org.apache.lucene:lucene-analyzers:3.6.2,${SPARK_PKGS}"
export SPARK_PKGS="org.scalanlp:breeze_2.10:0.11.2,${SPARK_PKGS}"
```
II. Configuration file    
(coming soon ...)

### Setup

```bash
$ git clone https://github.com/DivLoic/Rico
$ cd Rico
```

You will first need to edit the conf file rico.conf as show in the previous part: **Configuration**.
```bash
$ cp src/main/resources/rico.conf.template src/main/resources/rico.conf
$ vi src/main/resources/rico.conf
```

Now compile the project and run the prejob to fill the cassandra db.
```bash
$ sbt package
$ cqlsh -f src/main/resources/rico.cql
$ spark-submit --packages $SPARK_PKGS --class org.rico.etl.Restore --master <your-master>
$ spark-submit --packages $SPARK_PKGS --class org.rico.etl.Tfidf --master <your-master>
```
### Usage

(coming soon ...)

### Optionals

#### Logging
In order to have understandable logging system you can use the following
configuration. First, copy the template of *log4j file* in your spark home.
`cp ${SPARK_HOME}/conf/log4j.properties.template ${SPARK_HOME}/conf/log4j.properties`.
Then set all logger at **ERROR**. Finaly add the following line in the file:
```properties
log4j.rootCategory=ALL, rico
log4j.appender.rico=org.apache.log4j.ConsoleAppender
log4j.appender.rico.target=System.out
log4j.appender.rico.layout=org.apache.log4j.PatternLayout
log4j.appender.rico.layout.ConversionPattern=%d{yy/MM/dd HH:mm:ss} %5p (%F:%L): %m%n
```

#### Test
(coming soon ...)

#### Service with *SparkJobServer*
(coming soon ...)

### Project
Here is a tree of the project folder architecture. It present all files under the
*main* folder. The *resources* folder contains the config files example and the **cql**
script which initialise the cassandra keyspace. The package `org.rico.etl` refers to all
code dealing with data acquisition and `org.rico.app` refers to the recommmender itself.
```
.
├── resources
│   ├── log4j.properties
│   ├── rico.conf.template
│   ├── rico.cql
│   └── test.conf
└── scala
    ├── Functions.sc
    └── org
        └── rico
            ├── app
            │   ├── ItemView.scala
            │   ├── Rico.scala
            │   └── UserView.scala
            └── etl
                ├── Batch.scala
                ├── Extractor.scala
                ├── Loader.scala
                ├── Restore.scala
                ├── Tfidf.scala
                └── Transformer.scala
```

### Coding Style

```{scala}
val sparkConf = new SparkConf()
  .setAppName("[rico] - some task")
  .set("spark.cassandra.connection.host", conf.getString("cassandra.host"))
  .set("spark.cassandra.connection.port", conf.getString("cassandra.port"))
  // ...
  var obj = new Obj(/*some config*/)
  val someFunction = obj.ReturFunction
  // ...
  val rdd2 = rdd1.map { x => someFunction x }
```

### Our Team
- [@jordansportes](https://github.com/jordansportes8355)
- [@nrasolom](https://github.com/nrasolom)
- [@DivLoic](https://github.com/DivLoic)

See also, our engineering school : [isep.fr](http://www.isep.fr)
