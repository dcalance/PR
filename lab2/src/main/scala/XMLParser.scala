
class XMLParser extends Parser {
  override def parse(text: String): Array[Map[String, String]] = {
    val xml = scala.xml.XML.loadString(text)
    val id = xml \@ "id"
    val tp = (xml \ "type").text
    val value = (xml \ "value").text
    Array(Map("device_id" -> id, "sensor_type" -> tp, "value" -> value))
  }
}
