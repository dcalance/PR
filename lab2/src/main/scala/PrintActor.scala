import akka.actor.{Actor, ActorSystem}
import akka.stream.ActorMaterializer

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContextExecutor

class PrintActor(val results : ListBuffer[Map[String, String]]) extends Actor {
  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  override def receive: Receive = {
    case "print" =>
      val sb : StringBuilder = new StringBuilder
      val maxTypeElement = max(results.toList)
      val maxTypeNr = maxTypeElement.get("sensor_type")
      for (counter <- 0 to maxTypeNr.toInt) {
        sb.append(s"Sensors type : $counter\n")
        val typeElements = results.filter(_("sensor_type") == counter.toString)
        for (el <- typeElements) {
          sb.append(s"${el("device_id")} - ${el("value")}\n")
        }
        sb.append("\n")
      }
      println(sb.toString)
      println()
  }

  def max(xs: List[Map[String, String]]): Option[Map[String, String]] = xs match {
    case Nil => None
    case List(x: Map[String, String]) => Some(x)
    case x :: y :: rest => max( (if (x("sensor_type") > y("sensor_type")) x else y) :: rest )
  }
}