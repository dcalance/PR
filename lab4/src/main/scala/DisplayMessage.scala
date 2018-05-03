import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import javafx.scene.web.WebView
import javafx.stage.{Modality, Stage}
import javax.mail.Message

class DisplayMessage {
  def createWindow(message : Message): Unit = {
    val stage = new Stage()
    stage.setTitle(s"${message.getFrom()(0)}")
    println(message.getFrom)
    println(message.getAllRecipients)
    println(message.getFlags)
    println(message.getFolder)
    println(message.getReceivedDate)
    println(message.getSubject)
    println(message.getContent)

    val borderPane = new BorderPane
    val titleLabel = new Label(message.getSubject)

    val browser = new WebView
    val engine = browser.getEngine

    //stage.setScene(scene)
    stage.initModality(Modality.APPLICATION_MODAL)
    stage.show()
  }
}
