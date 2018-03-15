import akka.actor.{Actor, ActorSystem}
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContextExecutor

class ParseDataActor(val formatType : String, val text : String) extends Actor{
  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  override def receive: Receive = {
    case "parse" =>
      formatType match {
        case "csv" => sender() ! parse(new CSVParser(), text)
        case "xml" => sender() ! parse(new XMLParser(), text)
        case "json" => sender() ! parse(new JSONParser(), text)
      }
      context.parent ! "receiveSignal"
  }

  def parse(parser: Parser, text : String) : Array[Map[String, String]] = {
    parser.parse(text)
  }

}