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
        // use imap.gmail.com for gmail
        store.connect(server, port, mail, password)
        store.getDefaultFolder.list()
        val inbox = store.getFolder("daniel.calancea@faf.utm.md")
        inbox.open(Folder.READ_ONLY)

        val messages = inbox.getMessages()
        //      val limit = 20
        //      var count = 0
        //      for (message <- messages) {
        //        count = count + 1
        //        if (count > limit) return
        //        println(message.getSubject)
        //        println(message.getContent)
        //      }
        inbox.close(true)

      } catch {
        case e: NoSuchProviderException => sender ! (e.getMessage, null)
        case me: MessagingException => sender ! (me.getMessage, null)
      } finally {

      }
      sender ! ("success", store)
    }
  }
}
