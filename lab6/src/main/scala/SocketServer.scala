import java.net.{DatagramPacket, InetAddress, MulticastSocket}

import akka.actor.Actor

class SocketServer(socket: MulticastSocket, ipAddress : InetAddress) extends Actor{
  override def receive: Receive = {
    case "start" => {
      socket.joinGroup(ipAddress)
      while(true){
        val buffer = Array.ofDim[Byte](1024)
        val data = new DatagramPacket(buffer, buffer.length)
        socket.receive(data)
        sender ! ("response " + new String(data.getData).trim)
      }
    }
  }
}
