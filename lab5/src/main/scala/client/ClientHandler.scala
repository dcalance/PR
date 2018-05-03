package client

import akka.actor.Actor
import akka.io.Tcp.{Received, Write}
import akka.util.ByteString
import java.security.{KeyFactory, PublicKey}
import java.security.spec.X509EncodedKeySpec

import utils.EncryptUtils

class ClientHandler(val doEncrypt : Boolean) extends Actor{
  var publicKey : PublicKey = _
  var symmetricKey : String = _

  override def receive: Receive = {
    case data : ByteString => {
      if(doEncrypt && publicKey == null) {
        publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(data.toArray[Byte]))
        symmetricKey = EncryptUtils.generateAESKey()
        sender() ! ByteString(symmetricKey)
      } else if (doEncrypt){
        val decrypted = EncryptUtils.decrypt(data.toArray[Byte], publicKey)
        val symmetricEncrypted =  EncryptUtils.decryptAES(decrypted, symmetricKey)
        val decryptedString = new String(symmetricEncrypted)

        println(s"$decryptedString")
      }
    }
    case data : String =>
      val symmetricEncrypt = EncryptUtils.encryptAES(data, symmetricKey)
      val finalEncrypt = EncryptUtils.encrypt(symmetricEncrypt, publicKey)
      sender() ! ByteString(finalEncrypt)
  }
}
