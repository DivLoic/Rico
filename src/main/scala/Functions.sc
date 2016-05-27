import java.text.Normalizer

import scala.math.BigDecimal
import scala.util.matching.Regex
// TODO: Replace this

/**
  * take a description from an item and return a clean list of word <br/>
  * without ponctuation or space or
  * @param text:String text to parse
  *
  * @return :List[String] list of word without ponctuation
  **/
def formatWords(text: Any): String = {

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


formatWords(" foo bar etc ...")
formatWords("& @ # ' : , ?")
formatWords("Loïc ")
formatWords("Préférentiel!! !")
formatWords("Hi, you!")
formatWords(" No under_score!")
formatWords("it's 4 O'clock")
formatWords(" The Elephant's 4 cats. ")
formatWords(" éloïse")
formatWords("Loïc DIVAD")
formatWords("millénaire")
formatWords("Connaissez-vous")
formatWords(null)


val list = List(1,2,3)
list.size


val a = Array(1,2,3)
a.contains(0)

val i :BigDecimal =  3
i.toInt.asInstanceOf[java.lang.Integer]