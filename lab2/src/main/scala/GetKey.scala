import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

class GetKey(val url : String) extends Actor {
  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  private var retriesLeft = 5
  private var childActorRef : ActorRef = _

  override def receive: Receive = {
    case "start" =>
      val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = url, method = HttpMethods.POST))

      responseFuture
        .onComplete {
          case Success(res) =>
            childActorRef = context.actorOf(Props(new RequestsSupervisor(url, res.headers(2), Unmarshal(res.entity).to[String])), "RequestsSupervisor")
            childActorRef ! "start"
          case Failure(_)   => sys.error("something wrong")
        }

    case "reload" =>
      if (retriesLeft > 0) {
        if (Option(childActorRef).isDefined) {
          retriesLeft -= 1
        }
        self ! "start"
      } else {
        println("Number of retries exceeded.")
      }

  }
}
