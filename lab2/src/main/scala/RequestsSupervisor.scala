import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props, Status, _}
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.util.{ByteString, Timeout}

import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}
import scala.util.parsing.json._
import akka.pattern.{ask, pipe}

import scala.concurrent.duration._
import RegexUtils._

import scala.collection.mutable.ListBuffer

class RequestsSupervisor(val url : String, val key : HttpHeader, val paths : Future[String]) extends Actor {
  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val timeout = Timeout(30 seconds)

  private var signalsReceived = 0
  private var requestsNumber = 0
  val results : ListBuffer[Map[String, String]] = ListBuffer()

  override def receive: Receive = {
    case "start" =>
      paths.onComplete{
        case Success(res) =>
          val parsedBody = JSON.parseFull(res)
          val array = parsedBody.get
          val castedArray = array.asInstanceOf[List[Map[String, String]]]
          requestsNumber = castedArray.size - 1

          for(counter <- castedArray.indices) {
            val newUrl = url.dropRight(1) + castedArray(counter)("path")
            val actorRef = context.actorOf(Props(new RequestDataFromDevice(newUrl, key)), counter.toString)
            val future = actorRef ? "request"
            future.onComplete {
              case Success(response) =>
                //                println(response.asInstanceOf[HttpResponse].entity.toString)

                val csvPattern = ".+text/csv.+".r
                val xmlPattern = ".+application/xml.+".r
                val jsonPattern = ".+application/json.+".r

                val stringResponse = response.asInstanceOf[HttpResponse].entity.toString
                val futureBody = Unmarshal(response.asInstanceOf[HttpResponse].entity).to[String]

                futureBody.onComplete{
                  case Success(processedBody) =>
                    if(csvPattern matches stringResponse) {
                      val actorRef = context.actorOf(Props(new ParseDataActor("csv", processedBody)))
                      val futureResponse = actorRef ? "parse"
                      futureResponse.onComplete {
                        case Success(parsedResult) =>
                          results ++= parsedResult.asInstanceOf[Array[Map[String, String]]]
                      }

                    } else {
                      if(xmlPattern matches stringResponse) {
                        val actorRef = context.actorOf(Props(new ParseDataActor("xml", processedBody)))
                        val futureResponse = actorRef ? "parse"
                        futureResponse.onComplete {
                          case Success(parsedResult) =>
                            results ++= parsedResult.asInstanceOf[Array[Map[String, String]]]
                        }
                      } else {
                        if(jsonPattern matches stringResponse) {
                          val actorRef = context.actorOf(Props(new ParseDataActor("json", processedBody)))
                          val futureResponse = actorRef ? "parse"
                          futureResponse.onComplete {
                            case Success(parsedResult) =>
                              results ++= parsedResult.asInstanceOf[Array[Map[String, String]]]
                          }
                        }
                      }
                    }
                }
              case Failure(_) =>
                println("Failure to respond from child thread.")
            }
          }
        case Failure(_) => println("Oh boi, what have you done?")
      }
    case "receiveSignal" =>
      signalsReceived += 1
      if (signalsReceived == requestsNumber) {
        val actorRef = context.actorOf(Props(new PrintActor(results)), "printActor")
        actorRef ! "print"
      }
  }
}
