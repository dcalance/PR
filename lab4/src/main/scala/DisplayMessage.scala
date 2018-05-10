import com.sun.java.swing.action.AlignCenterAction
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.{BorderPane, VBox}
import javafx.scene.web.WebView
import javafx.stage.{Modality, Stage}
import javax.mail.Message
import javax.mail.internet.MimeMultipart

class DisplayMessage {
  def createWindow(message : Message): Unit = {
    val stage = new Stage()
    stage.setTitle(s"${message.getFrom()(0)}")

    val borderPane = new BorderPane
    val titleLabel = new Label(message.getSubject)

    val browser = new WebView
    val engine = browser.getEngine
    message.getContent match {
      case mime : MimeMultipart => {
        engine.loadContent(mime.getBodyPart(0).getContent.toString)
      }
      case message : String => {
        engine.loadContent(message)
      }
      }


    borderPane.setCenter(browser)

    val topPane = new VBox(10)
    topPane.setAlignment(Pos.CENTER)
    val messageFromLabel = new Label(s"From : ${message.getSubject}")
    val recipientsLabel = new Label(s"Recipients : ${message.getAllRecipients.map(x => x.toString).mkString(" ")}")
    val receivedDateLabel = new Label(s"Received : ${message.getReceivedDate}")
    topPane.getChildren.addAll(messageFromLabel, recipientsLabel, receivedDateLabel)

    borderPane.setTop(topPane)

    val scene = new Scene(borderPane, 800, 600)
    stage.setScene(scene)
    stage.initModality(Modality.APPLICATION_MODAL)
    stage.show()
  }
}
