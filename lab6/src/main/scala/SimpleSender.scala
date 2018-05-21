import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef}
import akka.io.Udp.SimpleSenderReady
import akka.io.{IO, Udp}
import akka.util.ByteString

class SimpleSender(remote: InetSocketAddress) extends Actor {
  import context.system
  IO(Udp) ! Udp.SimpleSender

  def receive = {
    case Udp.SimpleSenderReady =>
      println("context ready")
      context.become(ready(sender()))
  }

  def ready(send: ActorRef): Receive = {
    case msg: String => {
      println("Message sent")
      send ! Udp.Send(ByteString(msg), remote)
    }
    case msg: Udp.SimpleSenderReady =>
      println("Ye boi")
  }
}
