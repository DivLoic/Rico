# Rico
###### Final project 2016 from @ISEP

### Subject

### Technologies
- [X] Apache Spark
- [X] Apache Cassandra
- [ ] Apache Zeppelin
- [ ] Spark jobServer

### Prerequisites
- git
- sbt
- scala
- spark, cassandra, zeppelin, mysql or [The gowalla VM demo](https://github.com/natalinobusa/gowalla-spark-demo)


### Configuration
1. Environment
Add to your *.bashrc* the following lines:
````sh
export SPARK_PACKAGES="com.datastax.spark:spark-cassandra-connector_2.10:1.4.1"
export SPARK_PACKAGES="org.apache.lucene:lucene-analyzers:3.6.2,${SPARK_PACKAGES}"
export SPARK_PACKAGES="org.scalanlp:breeze_2.10:0.11.2,${SPARK_PACKAGES}"
````
2. Configuration file

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

```bash
$ sbt package
$ spark-submit --packages $SPARK_PACKAGES ...
```

### Our Team
- [@jordansportes](https://github.com/jordansportes8355)
- [@nrasolom](https://github.com/nrasolom)
- [@DivLoic](https://github.com/DivLoic)

See also, our engineering school : [isep.fr](http://www.isep.fr)
