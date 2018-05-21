import java.net._
import java.util.UUID.randomUUID
import scala.collection.mutable
import java.util.Base64
import java.nio.charset.StandardCharsets
import akka.actor.{Actor, ActorSystem, Props}
import akka.util.Timeout
import scala.concurrent.duration._


class Messenger(val userName : String) extends Actor {
  private val userUUID = randomUUID().toString
  private val usersID = new mutable.HashMap[String, String]
  private val IPAddress = InetAddress.getByName("230.185.192.108")
  private val socket = new MulticastSocket(42424)
  implicit val timeout = Timeout(20.seconds)
  import context.system

  override def receive: Receive = {
    case "start" => {
      val socketServerRef = system.actorOf(Props(new SocketServer(socket, IPAddress)), "socketServer")
      socketServerRef ! "start"
      createUser(userName)
    }
    case s : String if s.startsWith("response") => parseMessage(s.split(' ')(1))
    case s : String if s.startsWith("msg") => val splitedString = s.split('|')
      sendMessage(splitedString(1), splitedString(2))
  }

  def sendMessage(userId: String, message: String): Unit = {
    val messageString = "{:type :chat, :txt \"" + message + "\"}"
    val messageEncoded = Base64.getEncoder.encodeToString(messageString.getBytes(StandardCharsets.UTF_8))
    val finalPacket = s"${System.currentTimeMillis()}|$userUUID|$userId|$messageEncoded"
    val finalEncodedPacket = Base64.getEncoder.encodeToString(finalPacket.getBytes(StandardCharsets.UTF_8))

    sendPacket(finalEncodedPacket)
  }

  def createUser(username: String): Unit = {

    val userString = "{:type :online, :username \"" + username + "\"}"
    val encodedUsername = Base64.getEncoder.encodeToString(userString.getBytes(StandardCharsets.UTF_8))
    val finalPacket = s"${System.currentTimeMillis()}|$userUUID|:all|$encodedUsername"
    val finalEncodedPacket = Base64.getEncoder.encodeToString(finalPacket.getBytes(StandardCharsets.UTF_8))

    sendPacket(finalEncodedPacket)

  }

  def sendPacket(input : String): Unit = {
    val message : Array[Byte] = input.getBytes
    val clientSocket = new DatagramSocket
    val sendPacket = new DatagramPacket(message, message.length, IPAddress, 42424)
    clientSocket.send(sendPacket)
  }

  def parseMessage(message: String): Unit = {
    val decodedPacket = new String(Base64.getDecoder.decode(message), StandardCharsets.UTF_8)
    val splitPacket = decodedPacket.split('|')
    val decodedMessage = new String(Base64.getDecoder.decode(splitPacket(3)), StandardCharsets.UTF_8)
    if (splitPacket(1) != userUUID) {
      decodedMessage match {
        case s if s.startsWith("{:type :online") =>
          println(s"UserID : ${splitPacket(1)} | Name : ${s.substring(27, s.length - 2)}")
          usersID(splitPacket(1)) = s.substring(27, s.length - 2)
          if (splitPacket(2) == ":all") {
            respondToUser(splitPacket(1))
          }
        case s if s.startsWith("{:type :chat") && splitPacket(2) == userUUID => println(s"New message from ${usersID(splitPacket(1))} [${splitPacket(1)}] : ${decodedMessage.substring(20, decodedMessage.length - 2)}")
          confirmReceive(splitPacket(1))
        case _ => {}
      }
    }
  }

  def confirmReceive(userId : String): Unit = {
    val confirmString = "{:type :delivered}"
    val confirmEncoded = Base64.getEncoder.encodeToString(confirmString.getBytes(StandardCharsets.UTF_8))
    val finalPacket = s"${System.currentTimeMillis()}|$userUUID|$userId|$confirmEncoded"
    val finalEncodedPacket = Base64.getEncoder.encodeToString(finalPacket.getBytes(StandardCharsets.UTF_8))

    sendPacket(finalEncodedPacket)
  }

  def respondToUser(userId : String): Unit = {
    val userString = "{:type :online, :username \"" + userName + "\"}"
    val encodedUsername = Base64.getEncoder.encodeToString(userString.getBytes(StandardCharsets.UTF_8))
    val responseString = s"${System.currentTimeMillis()}|$userUUID|$userId|$encodedUsername"
    val finalEncodedPacket = Base64.getEncoder.encodeToString(responseString.getBytes(StandardCharsets.UTF_8))
    sendPacket(finalEncodedPacket)
  }
}
