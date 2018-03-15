
class JSONParser extends Parser {
  override def parse(text: String) : Array[Map[String, String]] = {
    val result = text.substring(1, text.length - 1)
      .split(",")
      .map(_.split(":"))
      .map { case Array(k, v) => (if (k.startsWith("\"")) {k.substring(1, k.length - 1)} else k, if (v.startsWith("\"")) {v.substring(1, v.length - 1)} else v)}
      .toMap
    Array(result)
  }
}
