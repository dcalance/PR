package utils

import java.security.{Key, KeyPair, KeyPairGenerator, MessageDigest}
import javax.crypto.Cipher
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}

import scala.util.Random

object EncryptUtils {
  private val initVector = "RandomInitVector"
  private var iv : Array[Byte] = _

  def decrypt(data: Array[Byte], key: Key): Array[Byte] = {
    val cipher = Cipher.getInstance("RSA")
    cipher.init(Cipher.DECRYPT_MODE, key)
    cipher.doFinal(data)
  }
  def encrypt(data: Array[Byte], key: Key): Array[Byte] = {
    val cipher = Cipher.getInstance("RSA")
    cipher.init(Cipher.ENCRYPT_MODE, key)
    cipher.doFinal(data)
  }
  def generateRSAKeyPair(size: Int): KeyPair = {
    val keyGen = KeyPairGenerator.getInstance("RSA")
    keyGen.initialize(4096)
    keyGen.generateKeyPair
  }

  @throws[Exception]
  def encryptAES(plainText: String, key: String): Array[Byte] = {
    val clean = plainText.getBytes
    // Generating IV.
    val ivSize = 16
    val iv = new Array[Byte](ivSize)
    val random = new Random()
    random.nextBytes(iv)
    val ivParameterSpec = new IvParameterSpec(iv)
    // Hashing key.
    val digest = MessageDigest.getInstance("SHA-256")
    digest.update(key.getBytes("UTF-8"))
    val keyBytes = new Array[Byte](16)
    System.arraycopy(digest.digest, 0, keyBytes, 0, keyBytes.length)
    val secretKeySpec = new SecretKeySpec(keyBytes, "AES")
    // Encrypt.
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
    val encrypted = cipher.doFinal(clean)
    // Combine IV and encrypted part.
    val encryptedIVAndText = new Array[Byte](ivSize + encrypted.length)
    System.arraycopy(iv, 0, encryptedIVAndText, 0, ivSize)
    System.arraycopy(encrypted, 0, encryptedIVAndText, ivSize, encrypted.length)
    encryptedIVAndText
  }

  @throws[Exception]
  def decryptAES(encryptedIvTextBytes: Array[Byte], key: String): Array[Byte] = {
    val ivSize = 16
    val keySize = 16
    // Extract IV.
    val iv = new Array[Byte](ivSize)
    System.arraycopy(encryptedIvTextBytes, 0, iv, 0, iv.length)
    val ivParameterSpec = new IvParameterSpec(iv)
    // Extract encrypted part.
    val encryptedSize = encryptedIvTextBytes.length - ivSize
    val encryptedBytes = new Array[Byte](encryptedSize)
    System.arraycopy(encryptedIvTextBytes, ivSize, encryptedBytes, 0, encryptedSize)
    // Hash key.
    val keyBytes = new Array[Byte](keySize)
    val md = MessageDigest.getInstance("SHA-256")
    md.update(key.getBytes)
    System.arraycopy(md.digest, 0, keyBytes, 0, keyBytes.length)
    val secretKeySpec = new SecretKeySpec(keyBytes, "AES")
    // Decrypt.
    val cipherDecrypt = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cipherDecrypt.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
    cipherDecrypt.doFinal(encryptedBytes)
  }

  def generateAESKey() : String = {
    val SALTCHARS: String = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
    val salt: StringBuilder = new StringBuilder
    val rnd = new Random()
    while ( {
      salt.length < 18
    }) { // length of the random string.
      val index: Int = (rnd.nextFloat * SALTCHARS.length).asInstanceOf[Int]
      salt.append(SALTCHARS.charAt(index))
    }
    salt.toString
  }

}
