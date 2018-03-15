import akka.http.scaladsl.model.{HttpHeader, HttpRequest, HttpResponse}
import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props, Status, _}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.concurrent.duration._


class RequestDataFromDevice(val url : String, val key : HttpHeader) extends Actor {
  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  override def receive: Receive = {
    case "request" =>
      val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = url, headers = List(key)))
      val result = Await.result(responseFuture, 30 seconds)
      sender() ! result
  }
}