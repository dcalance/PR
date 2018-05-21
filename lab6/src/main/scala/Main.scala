import akka.actor.{ActorSystem, Props}

object Main {
  def main(args : Array[String]): Unit = {
    val system = ActorSystem("base")

    val userName = scala.io.StdIn.readLine("Input the username: ")
    val messengerRef = system.actorOf(Props(new Messenger(userName)), "messengerActor")
    messengerRef ! "start"
    println("Client started.")

    while(true) {
      val input = scala.io.StdIn.readLine
      input match{
        case s if s.startsWith("msg") => messengerRef ! s
        case _ => println("Unknown command.")
      }
    }
  }
}
