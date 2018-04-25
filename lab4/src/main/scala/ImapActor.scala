import javax.mail._

import akka.actor.Actor

class ImapActor(server : String, mail : String, password : String, port : Int) extends Actor{

  def receive = {
    case "connect" => {
      val props = System.getProperties
      props.setProperty("mail.store.protocol", "imaps")
      val session = Session.getDefaultInstance(props, null)
      val store = session.getStore("imaps")

      try {
        store.connect(server, port, mail, password)
      } catch {
        case e: NoSuchProviderException => sender ! (e.getMessage, null)
        case me: MessagingException => sender ! (me.getMessage, null)
        case _ => sender ! ("error", null)
      } finally {

      }
      sender ! ("success", store)
    }
  }
}
