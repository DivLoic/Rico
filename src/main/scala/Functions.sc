import java.text.Normalizer
import scala.math.BigDecimal
import scala.util.matching.Regex

cleanWords(" foo bar etc ...")
cleanWords("& @ # ' : , ?")
cleanWords("Préférentiel!! !")
cleanWords("Hi, you!")
cleanWords(" No under_score!")
cleanWords("it's 4 O'clock")
cleanWords(" The Elephant's 4 cats. ")
cleanWords(" éloïse")
cleanWords("Loïc DIVAD")
cleanWords("millénaire")
cleanWords("Connaissez-vous")
cleanWords(null)


insureParms(Array("A"))
insureParms(Array("12563"))
insureParms(Array("1", "2"))

val a = Array(1,2,3)
a.contains(0)

val i :BigDecimal =  3
i.toInt.asInstanceOf[java.lang.Integer]




/**
  * take a description from an item and return a clean list of word <br/>
  * without ponctuation or space or
  *
  * @param text:String text to parse
  * @return :List[String] list of word without ponctuation
  **/
def cleanWords(text: Any): String = {

  def format(t: String, reg :String = "[^a-zA-Z\\p{M} ]"): String = {
    val ascii = Normalizer.normalize(t, Normalizer.Form.NFD)
    val keepPattern = new Regex(reg)
    keepPattern.replaceAllIn(ascii, Regex.quoteReplacement(""))
      .toLowerCase.trim
  }

  text match {
    case null => new String()
    case _ => format(text.toString)
  }
}

def insureParms(args: Array[String]):Unit = try {
  assert(args.size equals 1); args(0).toInt
} catch {
  case _ : java.lang.AssertionError => println("Le nombre de param est incorrect")
  case _ : java.lang.NumberFormatException => println("Le param n'est pas un nombre")
  case _ : java.lang.Exception => println("Une Exception")
  case _ => println("Une erreur ...")
}

