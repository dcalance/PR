import akka.actor.{Actor, ActorSystem, PoisonPill, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object Hello {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val actorRef = system.actorOf(Props(new GetKey("http://akka.io")), "getKey")
    actorRef ! "reload"
  }

}

class GetKey(val url : String) extends Actor {
  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  private var childActorRef = null
  override def receive: Receive = {
    case "start" =>
      println("start")
//      val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = url))
//
//      responseFuture
//        .onComplete {
//          case Success(res) => println(res)
//          case Failure(_)   => sys.error("something wrong")
//        }
    case "reload" =>
      if(childActorRef ) {
        context.stop(childActorRef)
      }
      println("reload")
      self ! "start"
  }
}
