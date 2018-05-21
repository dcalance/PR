import java.net.{InetSocketAddress, SocketAddress}

import akka.actor.{ActorSystem, Props}
import akka.io.Udp
import akka.io.Udp.Send
import akka.io.UdpConnected.Connect
import akka.pattern.ask
import akka.stream.StreamRefMessages.ActorRef
import akka.util.{ByteString, Timeout}

import scala.concurrent.duration._
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global


object Main {
  def main(args : Array[String]): Unit = {
    val system = ActorSystem("base")
    implicit val timeout = Timeout(20.seconds)

    println("Hello World")
    val senderRef = system.actorOf(Props(new SimpleSender(new InetSocketAddress("127.0.0.1", 42424))), "senderActor")
    val listenerRef = system.actorOf(Props(new Listener(senderRef)), "listenerActor")
    senderRef ! Udp.Send(ByteString("hello"), new InetSocketAddress("127.0.0.1", 42424))
  }
}
