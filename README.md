# Rico
###### Final project 2016 from @ISEP

### Subject
> a really
> a really
> a really cool description

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
- spark (1.4.1), cassandra, zeppelin, mysql or [The gowalla VM demo](https://github.com/natalinobusa/gowalla-spark-demo)


### Configuration
1. Environment
Add to your *.bashrc* the following lines:
````sh
export SPARK_PKGS="com.datastax.spark:spark-cassandra-connector_2.10:1.4.1"
export SPARK_PKGS="org.apache.lucene:lucene-analyzers:3.6.2,${SPARK_PKGS}"
export SPARK_PKGS="org.scalanlp:breeze_2.10:0.11.2,${SPARK_PKGS}"
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

Now compile the project and run
```bash
$ sbt package
$ cqlsh -f src/main/resources/rico.cql
$ spark-submit --packages $SPARK_PKGS --class org.rico.etl.Tfidf --master <your-master>
```

### Our Team
- [@jordansportes](https://github.com/jordansportes8355)
- [@nrasolom](https://github.com/nrasolom)
- [@DivLoic](https://github.com/DivLoic)

See also, our engineering school : [isep.fr](http://www.isep.fr)
