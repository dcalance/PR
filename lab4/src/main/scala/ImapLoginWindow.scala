import java.util.concurrent.TimeUnit
import javafx.application.{Application, Platform}
import javafx.beans.value.ChangeListener
import javafx.geometry.{Insets, Pos}
import javafx.scene.control.{Button, Label, PasswordField, TextField}
import javafx.stage.Stage
import javafx.scene.Scene
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.{HBox, StackPane, VBox}
import javafx.scene.paint.Color
import javafx.stage.Modality
import javax.mail.Store

import scala.language.postfixOps
import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.pattern.ask
import akka.util.Timeout
import javafx.scene.control.TextField
import javax.swing.event.ChangeListener

import com.sun.javafx.scene.control.skin.IntegerField

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global


class ImapLoginWindow{
  var store : Store = _
  var isConnected = false
  implicit val timeout = Timeout(5, TimeUnit.SECONDS)

  def display(): Unit = {

    val system = ActorSystem("ImapLoginWindow")

    val stage = new Stage()
    stage.setResizable(false)

    val vbox = new VBox(10)
    vbox.setAlignment(Pos.CENTER)

    val lockedImage = new ImageView(new Image("/locked-icon.png"))
    lockedImage.setFitHeight(200)
    lockedImage.setFitWidth(200)

    val errorText = new Label()
    errorText.setTextFill(Color.RED)

    val serverField = new TextField()
    serverField.setPromptText("Server")
    serverField.setMaxWidth(250)

    val portField = new TextField() {
      override def replaceText(start: Int, end: Int, text: String): Unit = {
        if (text.matches("[0-9]*")) super.replaceText(start, end, text)
      }

      override def replaceSelection(text: String): Unit = {
        if (text.matches("[0-9]*")) super.replaceSelection(text)
      }
    }
    portField.setText("993")
    portField.setMaxWidth(60)
    portField.setPromptText("Port")

    val serverPortBox = new HBox(40)
    serverPortBox.getChildren.addAll(serverField, portField)
    serverPortBox.setAlignment(Pos.CENTER)

    val emailField = new TextField()
    emailField.setPromptText("Email")
    emailField.setMaxWidth(250)

    val passwordField = new PasswordField()
    passwordField.setPromptText("Password")
    passwordField.setMaxWidth(250)

    val hbox = new HBox(10)
    hbox.setAlignment(Pos.CENTER)



    val connectButton = new Button("Connect")
    connectButton.setOnAction(_ => {
      val imapActor = system.actorOf(Props(new ImapActor(serverField.getText, emailField.getText, passwordField.getText, portField.getText.toInt)), name = "imapActor")
      val future = imapActor ? "connect"
      vbox.setDisable(true)
      future.onComplete {
        case Success(resp) => {
          val (msg, respStore) = resp.asInstanceOf[(String, Store)]
          if (msg == "success") {
            store = respStore
            isConnected = true
            Platform.runLater(() => stage.close())
          } else {
            Platform.runLater(() => errorText.setText(msg))
            vbox.setDisable(false)
          }
          imapActor ! PoisonPill
        }
        case Failure(_) => {
          vbox.setDisable(false)
          imapActor ! PoisonPill
          Platform.runLater(() => errorText.setText("Request timeout"))
        }
      }
    })
    val cancelButton = new Button("Cancel")
    cancelButton.setOnAction(_ =>{
      stage.close()
    })

    hbox.getChildren.addAll(connectButton, cancelButton)
    vbox.getChildren.addAll(lockedImage, errorText, serverPortBox, emailField, passwordField, hbox)

    val scene: Scene = new Scene(vbox, 350, 400)

    stage.setTitle("Connect to Server IMAP")
    stage.setScene(scene)
    stage.initModality(Modality.APPLICATION_MODAL)
    stage.showAndWait()
  }
}
