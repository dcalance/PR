import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef}
import akka.io.{IO, Udp}

class Listener(nextActor: ActorRef) extends Actor {
  import context.system
  IO(Udp) ! Udp.Bind(self, new InetSocketAddress("127.0.0.1", 42424))

  def receive = {
    case Udp.Bound(local) =>
      context.become(ready(sender()))
  }

  def ready(socket: ActorRef): Receive = {
    case Udp.Received(data, remote) =>
      val processed = data
        socket ! Udp.Send(data, remote)
      nextActor ! processed
    case Udp.Unbind => socket ! Udp.Unbind
    case Udp.Unbound => context.stop(self)
  }
}