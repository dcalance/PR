package server

import java.net.InetSocketAddress
import java.security.{Key, KeyPair}
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.UntypedActor
import akka.event.Logging
import akka.event.LoggingAdapter
import akka.io.Tcp
import akka.io.Tcp.Bound
import akka.io.Tcp.CommandFailed
import akka.io.Tcp.Connected
import akka.io.Tcp.ConnectionClosed
import akka.io.Tcp.Received
import akka.io.TcpMessage
import akka.japi.Procedure
import akka.util.ByteString
import java.util.Base64
import utils.EncryptUtils


class ServerHandler(val keyPair : KeyPair) extends UntypedActor {
  private val log = Logging.getLogger(getContext.system, this)
  private val secretKey = keyPair.getPrivate
  private val publicKey = keyPair.getPublic
  private var isEncrypted = false
  private var symmetricKey : String = _
  private val serverCommands = new ServerCommands()

  @throws[Exception]
  override def onReceive(msg: Any): Unit =
    msg match {
      case p : Tcp.Received => {
        val message = p.data
        if (message.utf8String == "reqEncryption" && !isEncrypted) {
          sender ! TcpMessage.write(ByteString(publicKey.getEncoded))
          isEncrypted = true
        } else if (isEncrypted && symmetricKey == null) {
          val decodedKey = Base64.getDecoder.decode(message.utf8String)
          symmetricKey = message.utf8String
        } else if (isEncrypted){
          val decrypted = EncryptUtils.decrypt(message.toArray[Byte], secretKey)
          val symmetricEncrypted =  EncryptUtils.decryptAES(decrypted, symmetricKey)
          val decryptedString = new String(symmetricEncrypted)
          val resp = serverCommands.parse(decryptedString)

          val symmetricEncrypt = EncryptUtils.encryptAES(resp, symmetricKey)
          val finalEncrypt = EncryptUtils.encrypt(symmetricEncrypt, secretKey)
          getSender ! TcpMessage.write(ByteString(finalEncrypt))
        } else {
          val resp = serverCommands.parse(message.utf8String)
          getSender ! TcpMessage.write(ByteString(resp))
        }
        //log.info("In SimplisticHandlerActor - Received message: " + message.utf8String)

      }
      case p : ConnectionClosed => getContext.stop(getSelf)
    }

}