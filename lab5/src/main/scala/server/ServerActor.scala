package server

import java.net.InetSocketAddress
import akka.actor.{Actor, ActorRef, Props, UntypedActor}
import akka.event.Logging
import akka.event.LoggingAdapter
import akka.io.{IO, Tcp, TcpMessage}
import akka.io.Tcp.Bound
import akka.io.Tcp.CommandFailed
import akka.io.Tcp.Connected
import Tcp._
import utils.EncryptUtils


object ServerActor {
  def props(address: InetSocketAddress): Props = Props.create(classOf[ServerActor],address)
}

class ServerActor(val address : InetSocketAddress) extends Actor {
  import context.system
  IO(Tcp) ! Bind(self, address)

  def receive = {
    case b @ Bound(_) ⇒
      sender ! b

    case CommandFailed(_: Bind) ⇒ context stop self

    case c @ Connected(remote, local) ⇒
      val connection = sender()
      val keyPair = EncryptUtils.generateRSAKeyPair(2048)
      val handlerActor = system.actorOf(Props(new ServerHandler(keyPair)), s"client${connection.path.name}")
      connection ! Register(handlerActor)
  }
}