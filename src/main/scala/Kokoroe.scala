import com.typesafe.config.ConfigFactory

/**
  * Created by LoicMDIVAD on 02/05/2016.
  */
object Kokoroe {

  /**
    * Start of the job.
    *
    * @param args
    */
  def main(args: Array[String]): Unit = {

    val ricoConf = ConfigFactory.load("rico")

    def show(txt: String) = println(">>>>>>>>>>>>>>>>>>>>>>>>>>>> : " + txt)

    show(ricoConf.getString("sql.driver"))
    show(ricoConf.getString("sql.host"))
    show(ricoConf.getString("sql.port"))
    show(ricoConf.getString("sql.schema"))

  }
}
