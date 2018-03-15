import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props, Status, _}
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}

object Hello {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val actorRef = system.actorOf(Props(new GetKey("https://desolate-ravine-43301.herokuapp.com/")), "getKey")
    actorRef ! "start"
  }
}

