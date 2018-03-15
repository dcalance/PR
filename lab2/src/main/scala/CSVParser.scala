class CSVParser extends Parser{
  override def parse(text: String) : Array[Map[String, String]] = {
    val rows = text.split("\n")
    val firstRowSplit = rows(0).split(",")
    val mapsArray : Array[Map[String, String]] = Array.fill(3){Map("" -> "")}

    for (counter <- 0 to 2) {
      mapsArray(counter) = (firstRowSplit zip rows(counter + 1).split(",")).toMap
    }
    mapsArray
  }
}
