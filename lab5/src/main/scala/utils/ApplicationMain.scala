package utils

import java.net.InetSocketAddress

import akka.actor.{ActorSystem, Props}
import client.{ClientActor, ClientHandler}
import server.ServerActor

object ApplicationMain {
  def main(args: Array[String]): Unit = {

    val serverActorSystem = ActorSystem.create("ServerActorSystem")
    val serverActor = serverActorSystem.actorOf(ServerActor.props(new InetSocketAddress("localhost", 9090)), "serverActor")

    val clientActorSystem = ActorSystem.create("ClientActorSystem")
    val clientHandlerActor =  serverActorSystem.actorOf(Props(new ClientHandler(doEncrypt = true)), "clientHandler")
    val clientActor = clientActorSystem.actorOf(ClientActor.props(new InetSocketAddress("localhost", 9090), clientHandlerActor, doEncrypt = true), "clientActor")

    while(true) {
      val msg = scala.io.StdIn.readLine()
      msg match{
        case "close" =>
          clientActor ! "close"
        case _ => clientActor ! s"command:$msg"
      }


    }


    //serverActorSystem.awaitTermination()
    //clientActorSystem.awaitTermination()

  }
}
