import javax.mail._

class Imap {



  def connect(server : String, mail : String, password : String): (String, Store) = {
    val props = System.getProperties
    props.setProperty("mail.store.protocol", "imaps")
    val session = Session.getDefaultInstance(props, null)
    val store = session.getStore("imaps")

    try {
      // use imap.gmail.com for gmail
      store.connect(server, mail, password)
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
      case e: NoSuchProviderException => return (e.getMessage, null)
      case me: MessagingException => return (me.getMessage, null)
    } finally {

    }
    ("success", store)
  }
}
