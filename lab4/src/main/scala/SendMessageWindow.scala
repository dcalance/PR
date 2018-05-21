import javafx.scene.Scene
import javafx.scene.layout.{BorderPane, VBox}
import javafx.stage.{Modality, Stage}
import com.sun.mail.smtp.SMTPTransport
import javax.mail.{Address, Message, Session}
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import java.util.{Date, Properties}

import javafx.geometry.Pos
import javafx.scene.control.{Button, PasswordField, TextArea, TextField}

class SendMessageWindow {
  def createWindow(): Unit = {
    val stage = new Stage()
    stage.setTitle(s"Send Message")
    val borderPane = new BorderPane

    val vbox = new VBox(10)
    vbox.setAlignment(Pos.TOP_CENTER)

    val serverField = new TextField()
    serverField.setPromptText("Server")
    serverField.setMaxWidth(250)

    val emailField = new TextField()
    emailField.setPromptText("Email")
    emailField.setMaxWidth(250)

    val passwordField = new PasswordField()
    passwordField.setPromptText("Password")
    passwordField.setMaxWidth(250)

    vbox.getChildren.addAll(serverField, emailField, passwordField)
    borderPane.setLeft(vbox)

    val toField = new TextField()
    toField.setPromptText("To")
    toField.setMaxWidth(250)

    val subjectField = new TextField()
    subjectField.setPromptText("Subject")
    subjectField.setMaxWidth(250)

    val messageField = new TextArea()
    messageField.setPromptText("Message")
    messageField.setMaxWidth(250)
    messageField.setMinHeight(300)

    val sendButton = new Button("Send")

    val middleBox = new VBox(10)
    middleBox.setAlignment(Pos.CENTER)

    middleBox.getChildren.addAll(toField, subjectField, messageField, sendButton)
    borderPane.setCenter(middleBox)

    val scene = new Scene(borderPane, 800, 600)
    stage.setScene(scene)
    stage.initModality(Modality.APPLICATION_MODAL)
    stage.show()

    sendButton.setOnAction(_ => {
          val props = System.getProperties
          props.put("mail.smtps.host", emailField.getText)
          props.put("mail.smtps.auth", "true")
          val session = Session.getInstance(props, null)
          val msg = new MimeMessage(session)
          msg.setFrom(new InternetAddress(emailField.getText))
          msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toField.getText, false).toArray[Address])
          msg.setSubject(subjectField.getText)
          msg.setText(messageField.getText)
          msg.setHeader("Mail", "No idea what this does")
          msg.setSentDate(new Date())
          val t = session.getTransport("smtps").asInstanceOf[SMTPTransport]
          t.connect(serverField.getText, emailField.getText, passwordField.getText)
          t.sendMessage(msg, msg.getAllRecipients)
          System.out.println("Response: " + t.getLastServerResponse)
    })
  }
}
