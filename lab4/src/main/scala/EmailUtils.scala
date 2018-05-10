import java.io.UnsupportedEncodingException
import java.util.Date

import javax.activation.DataHandler
import javax.activation.DataSource
import javax.activation.FileDataSource
import javax.mail._
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart


object EmailUtils {
  def sendEmail(session: Session, toEmail: String, subject: String, body: String): Unit = {
    try {
      val msg = new MimeMessage(session)
      //set message headers
      msg.addHeader("Content-type", "text/HTML; charset=UTF-8")
      msg.addHeader("format", "flowed")
      msg.addHeader("Content-Transfer-Encoding", "8bit")
      msg.setFrom(new InternetAddress("no_reply@example.com", "NoReply-JD"))
      msg.setReplyTo(InternetAddress.parse("no_reply@example.com", false).toArray[Address])
      msg.setSubject(subject, "UTF-8")
      msg.setText(body, "UTF-8")
      msg.setSentDate(new Date)
      msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false).toArray[Address])
      System.out.println("Message is ready")
      Transport.send(msg)
      System.out.println("EMail Sent Successfully!!")
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
  }
}