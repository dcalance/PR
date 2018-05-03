package client
import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, ActorSystem, Kill, Props, UntypedActor}
import akka.event.Logging
import akka.event.LoggingAdapter
import akka.io.{IO, Tcp, TcpMessage}
import akka.io.Tcp._
import akka.japi.Procedure
import akka.util.ByteString

object ClientActor {
  def props(remote: InetSocketAddress, listener : ActorRef, doEncrypt : Boolean) : Props = Props.create(classOf[ClientActor], remote, listener, doEncrypt.asInstanceOf[AnyRef])

  class Failed {}

}

class ClientActor(remote: InetSocketAddress, listener: ActorRef, doEncrypt : Boolean) extends Actor {

  import Tcp._
  import context.system

  IO(Tcp) ! Connect(remote)

  def receive = {
    case CommandFailed(_: Connect) ⇒
      listener ! "connect failed"
      context stop self

    case c @ Connected(`remote`, local) ⇒
      println("Client Actor : Connected")
      listener ! c
      val connection = sender()
      connection ! Register(self)

      if (doEncrypt) {
        connection ! Write(ByteString("reqEncryption"))
      }
      context become {
        case data: ByteString ⇒
          connection ! Write(data)
        case data: String if data.startsWith("command:") =>
          listener ! data.substring(8)
        case CommandFailed(w: Write) ⇒
          // O/S buffer was full
          listener ! "write failed"
        case Received(data) ⇒
          listener ! data
        case "close" ⇒
          connection ! Close
        case _: ConnectionClosed ⇒
          listener ! "connection closed"
          context stop self
      }
  }

}
